//Copyright (C) 2008-2013 Paul Done . All rights reserved.
//This file is part of the DomainHealth software distribution. Refer to the  
//file LICENSE in the root of the DomainHealth distribution.
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE 
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//POSSIBILITY OF SUCH DAMAGE.
package domainhealth.backend.wldfcapture;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;
import static domainhealth.core.statistics.StatisticsStorage.*;
import static domainhealth.core.statistics.MonitorProperties.*;
import static domainhealth.core.util.DateUtil.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.ObjectName;

import domainhealth.backend.retriever.DataRetrievalException;
import domainhealth.backend.retriever.StatisticCapturer;
import domainhealth.backend.wldfcapture.data.DataRecordsCollection;
import domainhealth.backend.wldfcapture.data.InstanceDataRecord;
import domainhealth.backend.wldfcapture.data.TypeDataRecord;
import domainhealth.core.jmx.WebLogicMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.statistics.ResourceNameNormaliser;

/**
 * Enables a specific WebLogic server's Core, JDBC and JMS related statistics 
 * to be retrieved from a server's 'HarvestedDataArchive' store using WLDF 
 * queries and then stored in a local statistics CSV file
 */
public class StatisticCapturerWLDFQuery extends StatisticCapturer {
	/**
	 * Creates a new statistics capute instance based on the given Weblogic 
	 * server connection details
	 * 
	 * @param csvStats Meta-data about the server statistics CSV file being generated
	 * @param conn Connection to the server's MBean tree
	 * @param serverRuntime Handle on the server's main runtime MBean
	 * @param serverName Name of the server to retrieve statistics for
	 * @param componentBlacklist Names of web-apps/ejbs than should not haves results collected/shown
	 * @param wlsVersionNumber The version of the host WebLogic Domain
	 */
	public StatisticCapturerWLDFQuery(StatisticsStorage csvStats, WebLogicMBeanConnection conn, ObjectName serverRuntime, String serverName, int queryIntervalMillis, List<String> componentBlacklist, String wlsVersionNumber) {
		super(csvStats, conn, serverRuntime, serverName, queryIntervalMillis, componentBlacklist, wlsVersionNumber);
	}

	/**
	 * Implements the abstract method to log core server stats to a CSV file.
	 */
	protected void logCoreStats() throws DataRetrievalException {
		try {
			String headerLine = getCoreStatsHeaderLine();
			HarvesterWLDFQueryRunner queryRunner = new HarvesterWLDFQueryRunner(getConn(), getServerName(), coreServerStatsQuery, getQueryIntervalMillis());
			String contentLine = getCoreStatsLine(queryRunner.retrieveDataRecords()); 
			getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), CORE_RESOURCE_TYPE, CORE_RSC_DEFAULT_NAME, headerLine, contentLine);
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + CORE_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}		
	}	

	/**
	 * Use WLDF queried stats from harvested archive to obtain the core 
	 * server stats and creates a row of text data
	 *   
	 * @param dataRecords The WLDF query results containing the stats
	 * @return The text data row (ready to be appended to a CSV)
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the statistics
	 */
	private String getCoreStatsLine(DataRecordsCollection dataRecords) throws IOException, WebLogicMBeanException {
		StringBuilder line = new StringBuilder(DEFAULT_CONTENT_LINE_LEN);
		TypeDataRecord serverTypeRecord = dataRecords.getTypeDataRecord(String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, SERVER_RUNTIME));
		
		if ((serverTypeRecord != null) && (serverTypeRecord.getInstanceNames().hasNext())){
			// Datetime + Server attributes (use first MBean's date-time for all MBeans for this CSV line)
			String serverObjectName = serverTypeRecord.getInstanceNames().next();
			InstanceDataRecord serverObjectRecord = serverTypeRecord.getInstanceDataRecord(serverObjectName);
			line.append(formatSecondsDateTime(serverObjectRecord.getTimestamp()) + SEPARATOR);
						
			for (String attr : SERVER_MBEAN_MONITOR_ATTR_LIST) {
				line.append(serverObjectRecord.getAttrValue(attr) + SEPARATOR);
			}
		
			// JVM attributes (got to do these separately because changing some figures to MegaBytes and calculate heap size current)
			TypeDataRecord jvmTypeRecord = dataRecords.getTypeDataRecord(String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JVM_RUNTIME));
			
			// If JRockit rather than Sun Hotspot VM, the WLDF retrieved MBean type is different
			if (jvmTypeRecord == null) {
				jvmTypeRecord = dataRecords.getTypeDataRecord(String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JROCKIT_RUNTIME));
			}
			
			String jvmObjectName = jvmTypeRecord.getInstanceNames().next();
			InstanceDataRecord jvmObjectRecord = jvmTypeRecord.getInstanceDataRecord(jvmObjectName);
			line.append((long)(Long.parseLong(jvmObjectRecord.getAttrValue(HEAP_SIZE_CURRENT)) / BYTES_IN_MEGABYTE) + SEPARATOR);
			line.append((long)(Long.parseLong(jvmObjectRecord.getAttrValue(HEAP_FREE_CURRENT)) / BYTES_IN_MEGABYTE) + SEPARATOR);
			line.append((long)((Long.parseLong(jvmObjectRecord.getAttrValue(HEAP_SIZE_CURRENT)) - Long.parseLong(jvmObjectRecord.getAttrValue(HEAP_FREE_CURRENT))) / BYTES_IN_MEGABYTE) + SEPARATOR);
			line.append(jvmObjectRecord.getAttrValue(HEAP_FREE_PERCENT) + SEPARATOR);
	
			// Thread Pool attributes - thread pool may not exist if Use81StyleExecuteQueues is enabled
			InstanceDataRecord threadPoolObjectRecord = null;
			TypeDataRecord threadPoolTypeRecord = dataRecords.getTypeDataRecord(String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, THREAD_POOL_RUNTIME));
			
			if (threadPoolTypeRecord != null) {
				String threadPoolObjectName = threadPoolTypeRecord.getInstanceNames().next();
				threadPoolObjectRecord = threadPoolTypeRecord.getInstanceDataRecord(threadPoolObjectName);
			}
			
			for (String attr : THREADPOOL_MBEAN_MONITOR_ATTR_LIST) {
				// If thread pool does not exist can only put Zero into CSV as result
				if (threadPoolObjectRecord == null) {
					line.append(0 + SEPARATOR);
				} else {
					line.append(threadPoolObjectRecord.getAttrValue(attr) + SEPARATOR);
				}
			}
			
			// JTA Transaction Manager attributes
			TypeDataRecord jtaTypeRecord = dataRecords.getTypeDataRecord(String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JTA_RUNTIME));
			String jtaObjectName = jtaTypeRecord.getInstanceNames().next();
			InstanceDataRecord jtaObjectRecord = jtaTypeRecord.getInstanceDataRecord(jtaObjectName);
			
			for (String attr : JTA_MBEAN_MONITOR_ATTR_LIST) {
				line.append(jtaObjectRecord.getAttrValue(attr) + SEPARATOR);
			}			
		}
		
		return line.toString();
	}

	/**
	 * Implements the abstract method to log JDBC data source stats to a CSV 
	 * file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logDataSourcesStats() throws DataRetrievalException {
		logResourceStats(DATASOURCE_RESOURCE_TYPE, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JDBC_DATASOURCE_RUNTIME), JDBC_MBEAN_MONITOR_ATTR_LIST, jdbcStatsQuery);
	}

	/**
	 * Implements the abstract method to log JMS destination stats to a CSV 
	 * file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logDestinationsStats() throws DataRetrievalException {
		logResourceStats(DESTINATION_RESOURCE_TYPE, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JMS_DESTINATION_RUNTIME), JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST, jmsDestinationStatsQuery);
	}

	/**
	 * Implements the abstract method to log Web Application stats to a CSV 
	 * file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logWebAppStats() throws DataRetrievalException {
		logResourceStats(WEBAPP_RESOURCE_TYPE, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, WEBAPP_COMPONENT_RUNTIME), WEBAPP_MBEAN_MONITOR_ATTR_LIST, webAppStatsQuery);
	}
	
	/**
	 * Implements the abstract method to log EJB stats to a CSV file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logEJBStats() throws DataRetrievalException {
		logResourceEJBStats();
	}

	/**
	 * Implements the abstract method to log WLHostMachine optional mbean stats to a CSV file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logHostMachineStats() throws DataRetrievalException {
		logResourceStats(HOSTMACHINE_RESOURCE_TYPE, HOST_MACHINE_MBEAN, HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST, hostMachineStatsQuery);		
	}

	/**
	 * Implements the abstract method to log extended stats to a CSV file.
	 * Specifically, logs work manager and server channel stats
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 * @throws IOException Indicates a problem in retrieving and persisting statistics
	 */
	protected void logExtendedStats() throws DataRetrievalException, IOException {
		logResourceStats(WORKMGR_RESOURCE_TYPE, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, WORK_MANAGER_RUNTIME), WKMGR_MBEAN_MONITOR_ATTR_LIST, wkMgrStatsQuery);
		logResourceStats(SVRCHNL_RESOURCE_TYPE, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, SERVER_CHANNEL_RUNTIME), SVR_CHANNEL_MBEAN_MONITOR_ATTR_LIST, svrChnlStatsQuery);
	}

	/**
	 * For a specific MBean type on a specific WebLogic server, retrieve all 
	 * the WLDF data records from the serrver's harvester and add the content 
	 * of these records as new entries in the relevant CSV files for this M
	 * Bean type.
	 * 
	 * @param resourceType The type of resource to retrvreive and store data for
	 * @param mbeanPropertyName The runtime mbean property name for this resource type
	 * @param monitorAttrList The list of properties/attributes which should be extracted into a CSV for these MBean types
	 * @param wldfQuery The WLDF query to run against the remote server's harvester log
	 * @throws DataRetrievalException Indicates a problem occurred in retrieving the WLDF data
	 */
	private void logResourceStats(String resourceType, String mbeanPropertyName, String[] monitorAttrList, String wldfQuery) throws DataRetrievalException {
		try {
			String headerLine = constructHeaderLine(monitorAttrList);
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			HarvesterWLDFQueryRunner queryRunner = new HarvesterWLDFQueryRunner(getConn(), getServerName(), wldfQuery, getQueryIntervalMillis());
			DataRecordsCollection dataRecords = queryRunner.retrieveDataRecords();			
			Map<String, InstanceDataRecord> objectRecords = getUniqueObjectRecords(resourceType, mbeanPropertyName, dataRecords);
			Iterator<String> names = objectRecords.keySet().iterator(); 

			while (names.hasNext()) {
				String name = (String) names.next();
			
				// Skip resources which are on blacklist (unless this is for 
				// the WLHostMachine resource type in which case allow anyway)
				if ((resourceType.equals(HOSTMACHINE_RESOURCE_TYPE)) || (!getComponentBlacklist().contains(name))) {										
					String contentLine = constructStatsLine(objectRecords.get(name), monitorAttrList);
					getCSVStats().appendToResourceStatisticsCSV(nowDate, getServerName(), resourceType, name, headerLine, contentLine);
					artifactList.put(name, now);
				}
			}
			
			getCSVStats().appendSavedOneDayResourceNameList(nowDate, resourceType, artifactList);			
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + resourceType + " resources for server " + getServerName() + ". " + e.getMessage(), e);
		} 
	}

	/**
	 * From a list of data records for a specifc MBean type, get list of 
	 * uniquely named mbeans and their records only.
	 *  
	 * @param resourceType The type of resource the data records relate to (eg. datasource)
	 * @param mbeanPropertyName The runtime mbean property name for this resource type
	 * @param dataRecords The list of data records
	 * @return The unique named - data-record pairs
	 */
	private Map<String,InstanceDataRecord> getUniqueObjectRecords(String resourceType, String mbeanPropertyName, DataRecordsCollection dataRecords) {
		Map<String, InstanceDataRecord> uniquelyNamedRecords = new HashMap<String, InstanceDataRecord>();
		TypeDataRecord mbeanTypeRecord = dataRecords.getTypeDataRecord(mbeanPropertyName);
		
		if ((mbeanTypeRecord != null) && (mbeanTypeRecord.getInstanceNames().hasNext())) {
			Iterator<String> objectNames = mbeanTypeRecord.getInstanceNames();
			
			while (objectNames.hasNext()) {
				String objectName = objectNames.next();
				String normalisedName = ResourceNameNormaliser.normalise(resourceType, objectName);
				
				if (!uniquelyNamedRecords.containsKey(normalisedName)) {
					uniquelyNamedRecords.put(normalisedName, mbeanTypeRecord.getInstanceDataRecord(objectName));
				}
			}
		}
		
		return uniquelyNamedRecords;
	}

	/**
	 * For an EJB MBean type specifically on a specific WebLogic server, 
	 * retrieve all the WLDF data records from the server's harvester and add 
	 * the content of these records as new entries in the EJB CSV files. This 
	 * method is required specifically for EJBs, because the EJB attributes 
	 * actually belong to two different sub-mbean objects (eg. Pool & 
	 * Transactions)
	 * 
	 * @throws DataRetrievalException Indicates a problem occurred in retrieving the WLDF data
	 */
	private void logResourceEJBStats() throws DataRetrievalException {
		try {
			String headerLine = constructHeaderLine(EJB_MBEAN_MONITOR_ATTR_LIST);
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			HarvesterWLDFQueryRunner queryRunner = new HarvesterWLDFQueryRunner(getConn(), getServerName(), ejbStatsQuery, getQueryIntervalMillis());
			DataRecordsCollection dataRecords = queryRunner.retrieveDataRecords();					
			Map<String, InstanceDataRecord> poolObjectRecords = getUniqueObjectRecords(EJB_RESOURCE_TYPE, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, EJB_POOL_RUNTIME), dataRecords);
			Map<String, InstanceDataRecord> txObjectRecords = getUniqueObjectRecords(EJB_RESOURCE_TYPE, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, EJB_TRANSACTION_RUNTIME), dataRecords);
			Iterator<String> poolObjectNames = poolObjectRecords.keySet().iterator(); 
			
			while (poolObjectNames.hasNext()) {
				String name = (String) poolObjectNames.next();
				
				if (!getComponentBlacklist().contains(name)) {										
					InstanceDataRecord poolObjRecord = poolObjectRecords.get(name);
					InstanceDataRecord txObjRecord = txObjectRecords.get(name);
					StringBuilder contentLine = new StringBuilder(constructStatsLine(poolObjRecord, EJB_POOL_MBEAN_MONITOR_ATTR_LIST));
					appendToStatsLine(contentLine, txObjRecord, EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST);
					getCSVStats().appendToResourceStatisticsCSV(nowDate, getServerName(), EJB_RESOURCE_TYPE, name, headerLine, contentLine.toString());
					artifactList.put(name, now);
				}
			}
			
			getCSVStats().appendSavedOneDayResourceNameList(nowDate, EJB_RESOURCE_TYPE, artifactList);			
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + EJB_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		} 
	}

	/**
	 * Appends a new section of a WLDF query being built up by adding a new 
	 * section of query text, containing the MBean type to be queried and its 
	 * attribute names.
	 * 
	 * @param wldfQueryBuilder The current query text being built-up, to be added to by this method
	 * @param mbeanType The mbean type to include in the query
	 * @param attributes The attribute names to include in the query
	 */
	private static void appendWLDFQueryPart(StringBuilder wldfQueryBuilder, String mbeanType, String[] attributes) {
		appendWLDFQueryPartWithQueryTemplate(WLDF_QUERY_PART_TEMPLATE, wldfQueryBuilder, mbeanType, attributes);
	}

	/**
	 * Appends a new section of a WLDF query being built up by adding a new 
	 * section of query text, containing the MBean type to be queried and its 
	 * attribute names.
	 * 
	 * @param queryTemplate The template to use for the section of query to build
	 * @param wldfQueryBuilder The current query text being built-up, to be added to by this method
	 * @param mbeanType The mbean type to include in the query
	 * @param attributes The attribute names to include in the query
	 */
	private static void appendWLDFQueryPartWithQueryTemplate(String queryTemplate, StringBuilder wldfQueryBuilder, String mbeanType, String[] attributes) {
		for (String attribute : attributes) {		
			if (wldfQueryBuilder.length() > 0) {
				wldfQueryBuilder.append(WLDF_QUERY_OR);
			}
			
			wldfQueryBuilder.append(String.format(queryTemplate, mbeanType, attribute));
		}
	}	
	
	/**
	 * Construct a single line of statistics to go in a CSV file, by querying 
	 * an MBean object's data record's fields matching a list of given 
	 * attribute names.
	 * 
	 * @param objectRecord MBean object data record to query the statistics from
	 * @param attrList List of attributes
	 * @return The new statistics text line
	 */
	private String constructStatsLine(InstanceDataRecord objectRecord, String[] attrList) {
		StringBuilder line = new StringBuilder(DEFAULT_CONTENT_LINE_LEN);
		line.append(formatSecondsDateTime(objectRecord.getTimestamp()) + SEPARATOR);
		appendToStatsLine(line, objectRecord, attrList);
		return line.toString();
	}

	/**
	 * Adds to a line of statistics based on querying an MBean object's data 
	 * record's matching a list of given attribute names.
	 * 
	 * @param line The string buffer to add content to
	 * @param objectRecord MBean object data record to query the statistics from
	 * @param attrList List of attributes
	 */
	private void appendToStatsLine(StringBuilder line, InstanceDataRecord objectRecord, String[] attrList) {
		for (String attr : attrList) {
			line.append(objectRecord.getAttrValue(attr) + SEPARATOR);
		}
	}

	// Constants
	private static final int DEFAULT_CONTENT_LINE_LEN = 100;	
	private final static String WLDF_QUERY_OR = " OR ";
	private final static String WLDF_QUERY_PART_TEMPLATE = "(TYPE='%s' AND ATTRNAME='%s')";
	// Example of query restricting on specific mbean instances
	//private final static String DEF_WKMGR_WLDF_QUERY_PART_TEMPLATE = "(NAME LIKE 'com.bea:Name=weblogic.kernel.Default%%' AND TYPE='" + RUNTIME_MBEAN_TYPE_TEMPLATE + "' AND ATTRNAME='%s')";
	
	// Members
	private final static String coreServerStatsQuery;   
	private final static String jdbcStatsQuery;   
	private final static String jmsDestinationStatsQuery;   
	private final static String webAppStatsQuery;   
	private final static String ejbStatsQuery;   
	private final static String wkMgrStatsQuery;   
	private final static String svrChnlStatsQuery;   
	private final static String hostMachineStatsQuery;   
	
	// Static initialiser
	static {
		StringBuilder coreStatsQueryBuilder = new StringBuilder(100);
		appendWLDFQueryPart(coreStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, SERVER_RUNTIME), SERVER_MBEAN_MONITOR_ATTR_LIST);
		appendWLDFQueryPart(coreStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JVM_RUNTIME), JVM_MBEAN_MONITOR_ATTR_LIST);
		appendWLDFQueryPart(coreStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JROCKIT_RUNTIME), JVM_MBEAN_MONITOR_ATTR_LIST);
		appendWLDFQueryPart(coreStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, THREAD_POOL_RUNTIME), THREADPOOL_MBEAN_MONITOR_ATTR_LIST);
		// Example of a query for a restricted set of MBean instances
		//appendWLDFQueryPartWithQueryTemplate(DEF_WKMGR_WLDF_QUERY_PART_TEMPLATE, coreStatsQueryBuilder, WORK_MANAGER_RUNTIME, WKMGR_MBEAN_MONITOR_ATTR_LIST);
		appendWLDFQueryPart(coreStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JTA_RUNTIME), JTA_MBEAN_MONITOR_ATTR_LIST);
		coreServerStatsQuery = coreStatsQueryBuilder.toString();
		StringBuilder jdbcStatsQueryBuilder = new StringBuilder(100);
		appendWLDFQueryPart(jdbcStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JDBC_DATASOURCE_RUNTIME), JDBC_MBEAN_MONITOR_ATTR_LIST);
		jdbcStatsQuery = jdbcStatsQueryBuilder.toString();
		StringBuilder jmsDestinationStatsQueryBuilder = new StringBuilder(100);
		appendWLDFQueryPart(jmsDestinationStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JMS_DESTINATION_RUNTIME), JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST);
		jmsDestinationStatsQuery = jmsDestinationStatsQueryBuilder.toString();
		StringBuilder webAppStatsQueryBuilder = new StringBuilder(100);
		appendWLDFQueryPart(webAppStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, WEBAPP_COMPONENT_RUNTIME), WEBAPP_MBEAN_MONITOR_ATTR_LIST);
		webAppStatsQuery = webAppStatsQueryBuilder.toString();
		StringBuilder ejbStatsQueryBuilder = new StringBuilder(100);
		appendWLDFQueryPart(ejbStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, EJB_POOL_RUNTIME), EJB_POOL_MBEAN_MONITOR_ATTR_LIST);
		appendWLDFQueryPart(ejbStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, EJB_TRANSACTION_RUNTIME), EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST);
		ejbStatsQuery = ejbStatsQueryBuilder.toString();
		StringBuilder wkMgrStatsQueryBuilder = new StringBuilder(100);
		appendWLDFQueryPart(wkMgrStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, WORK_MANAGER_RUNTIME), WKMGR_MBEAN_MONITOR_ATTR_LIST);
		wkMgrStatsQuery = wkMgrStatsQueryBuilder.toString();
		StringBuilder svrChnlStatsQueryBuilder = new StringBuilder(100);
		appendWLDFQueryPart(svrChnlStatsQueryBuilder, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, SERVER_CHANNEL_RUNTIME), SVR_CHANNEL_MBEAN_MONITOR_ATTR_LIST);
		svrChnlStatsQuery = svrChnlStatsQueryBuilder.toString();
		StringBuilder hostMachineStatsQueryBuilder = new StringBuilder(100);
		appendWLDFQueryPart(hostMachineStatsQueryBuilder, HOST_MACHINE_MBEAN, HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST);
		hostMachineStatsQuery = hostMachineStatsQueryBuilder.toString();				
	}
}
