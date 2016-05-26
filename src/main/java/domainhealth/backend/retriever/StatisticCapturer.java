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
package domainhealth.backend.retriever;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DATE_TIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.HEAP_FREE_CURRENT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.HEAP_FREE_PERCENT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.HEAP_SIZE_CURRENT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.HEAP_USED_CURRENT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.WORK_MANAGER_RUNTIMES;
import static domainhealth.core.statistics.MonitorProperties.DEFAULT_WKMGR_NAME;
import static domainhealth.core.statistics.MonitorProperties.JTA_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.SERVER_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.THREADPOOL_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.StatisticsStorage.SEPARATOR;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.WebLogicMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.DateUtil;


/**
 * Base class implementation for a concrete implementation class which will 
 * capture a specific WebLogic server's Core, JDBC and JMS and other 
 * statistics. An implementation may use JMX Polling of WLDF Harvesting to 
 * obtain statistics, for example. The implementation stores the captured 
 * statistics in a set of CSV files.
 */
public abstract class StatisticCapturer {
	
	// Constants
	private static final int DEFAULT_HEADER_LINE_LEN = 100;
	protected static final long BYTES_IN_MEGABYTE = 1024 * 1024;
	
	// Members
	private final StatisticsStorage csvStats;
	private final WebLogicMBeanConnection conn;
	private final ObjectName serverRuntime;
	private final String serverName;
	private final int queryIntervalMillis;
	private final List<String> componentBlacklist;
	private final String wlsVersionNumber;
	private final DateFormat secondDateFormat = new SimpleDateFormat(DateUtil.DISPLAY_DATETIME_FORMAT);
	
	/**
	 * Base class constructor for statistic retriever logger implementation
	 * which stores the WebLogic server connection details.
	 * 
	 * @param appProps The system/application key/value pairs
	 * @param csvStats Meta-data about the server statistics CSV file being generated
	 * @param conn Connection to the server's MBean tree
	 * @param serverRuntime Handle on the server's main runtime MBean
	 * @param serverName Name of the server to retrieve statistics for
	 * @param componentBlacklist Names of web-apps/ejbs than should not haves results collected/shown
	 * @param wlsVersionNumber The version of the host WebLogic Domain
	 */
	public StatisticCapturer(StatisticsStorage csvStats, WebLogicMBeanConnection conn, ObjectName serverRuntime, String serverName, int queryIntervalMillis, List<String> componentBlacklist, String wlsVersionNumber) {
		this.csvStats = csvStats;
		this.conn = conn;
		this.serverRuntime = serverRuntime;
		this.serverName = serverName;
		this.queryIntervalMillis = queryIntervalMillis;
		this.componentBlacklist = componentBlacklist;
		this.wlsVersionNumber = wlsVersionNumber;
	}

	/**
	 * "Template Method" based pattern. Main controlling method which calls 
	 * implementation class methods for obtaining each category of statistic (
	 * eg. JDBC, JMS).
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	public final void captureAndLogServerStats() throws DataRetrievalException, IOException {
		
		AppLog.getLogger().debug(getClass() + " initiated to collect stats for server: " + serverName);
		
		logCoreStats();
		logDataSourcesStats();
		logDestinationsStats();
		logSafAgentStats();
		logWebAppStats();
		logEJBStats();
		
		// Added by gregoan
		logHostMachineStats();
		logJvmStats();
		logExtendedStats();
		logOsbStats();
		logSoaBpmStats();
	}

	/**
	 * Abstract method for capturing and persisting core server statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logCoreStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting JDBC data source 
	 * statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logDataSourcesStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting JMS destination
	 * statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logDestinationsStats() throws DataRetrievalException;
	
	/**
	 * Abstract method for capturing and persisting SAF Agent
	 * statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logSafAgentStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting Web Application 
	 * statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logWebAppStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting EJB statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logEJBStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting WLHostMachine optional mbean statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logHostMachineStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting WLJvm optional mbean statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logJvmStats() throws DataRetrievalException;
	
	/**
	 * Abstract method for capturing and persisting WLOsb optional mbean statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logOsbStats() throws DataRetrievalException;
	
	/**
	 * Abstract method for capturing and persisting WLSoaBpm optional mbean statistics.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logSoaBpmStats() throws DataRetrievalException;

	/**
	 * Abstract method for capturing and persisting other types of server 
	 * statistics which are specific to the particular implementation of the 
	 * statistics capturer.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected abstract void logExtendedStats() throws DataRetrievalException, IOException;

	/**
	 * Returns a text line containing the comma separated statistic field headers for core server statistics.
	 * 
	 * @return The CVS field name header
	 */
	protected String getCoreStatsHeaderLine() {
		
		StringBuilder headerLine = new StringBuilder(DEFAULT_HEADER_LINE_LEN);
		headerLine.append(DATE_TIME + SEPARATOR);	

		for (String attr : SERVER_MBEAN_MONITOR_ATTR_LIST) {
			headerLine.append(attr + SEPARATOR);
		}			

		// Got to do these separately because adding calculated filed for heap size current
		headerLine.append(HEAP_SIZE_CURRENT + SEPARATOR); 
		headerLine.append(HEAP_FREE_CURRENT + SEPARATOR); 
		headerLine.append(HEAP_USED_CURRENT + SEPARATOR); 
		headerLine.append(HEAP_FREE_PERCENT + SEPARATOR);
		
		for (String attr : THREADPOOL_MBEAN_MONITOR_ATTR_LIST) {
			headerLine.append(attr + SEPARATOR);
		}			

		for (String attr : JTA_MBEAN_MONITOR_ATTR_LIST) {
			headerLine.append(attr + SEPARATOR);
		}			

		return headerLine.toString();
	}

	/**
	 * Construct the single header line to go in a CSV file, from a list of 
	 * attribute names.
	 * 
	 * @param attrList List of attributes
	 * @param estLength Approximate lenght of line
	 * @return The new header text line
	 */
	protected String constructHeaderLine(String[] attrList) {
		StringBuilder headerLine = new StringBuilder(DEFAULT_HEADER_LINE_LEN);
		headerLine.append(DATE_TIME + SEPARATOR);		

		for (String attr : attrList) {
			headerLine.append(attr + SEPARATOR);
		}			

		return headerLine.toString();
	}

	/**
	 * Returns the Meta-data about the server statistics CSV file being generated
	 * 
	 * @return Meta-data about the server statistics CSV file being generated
	 */
	protected StatisticsStorage getCSVStats() {
		return csvStats;
	}

	/**
	 * Returns the connection to the server's MBean tree
	 * 
	 * @return Connection to the server's MBean tree
	 */
	protected WebLogicMBeanConnection getConn() {
		return conn;
	}

	/**
	 * Returns the handle on the server's main runtime MBean
	 * 
	 * @return Handle on the server's main runtime MBean
	 */
	protected ObjectName getServerRuntime() {
		return serverRuntime;
	}

	/**
	 * Returns the name of the server to retrieve statistics for
	 * 
	 * @return Name of the server to retrieve statistics for
	 */
	protected String getServerName() {
		return serverName;
	}

	/**
	 * Returns the statistic poll/query interval in milliseconds 
	 * 
	 * @return The query interval in milliseconds
	 */
	protected int getQueryIntervalMillis() {
		return queryIntervalMillis;
	}

	/**
	 * Returns the list of component names to be ignored (the blacklist) 
	 * 
	 * @return The blacklist of component names
	 */
	protected List<String> getComponentBlacklist() {
		return componentBlacklist;
	}

	/**
	 * Return the current host WebLogic Domain's version 
	 * 
	 * @return The version text (e.g. 10.3.5)
	 */
	protected String getWlsVersionNumber() {
		return wlsVersionNumber;
	}
	
	/**
	 * Single-threaded utility method to generate a data-time string from a 
	 * given date (including seconds in format)
	 * 
	 * @return Text representation of given date-time including seconds
	 */
	protected String formatSeconsdDateTime(Date dateTime) {
		return secondDateFormat.format(dateTime);
	}

	/**
	 * Single-threaded utility method to generate a data-time string from a 
	 * given milli-seconds version of a date (including seconds in format)
	 * 
	 * @return Text representation of given date-time including seconds
	 */
	protected String formatSecondsDateTime(long dateTimeMillis) {
		return secondDateFormat.format(new Date(dateTimeMillis));
	}

	/**
	 * Returns an MBean handle onto the default work manager for the server 
	 * 
	 * @return The default Work Manager MBean
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the statistics
	 */
	// DOESN'T SEEM TO BE USED ...
	protected ObjectName getDefaultWorkManager() throws WebLogicMBeanException {
		
		for (ObjectName wkMgr : getConn().getChildren(getServerRuntime(), WORK_MANAGER_RUNTIMES)) {
			if (getConn().getTextAttr(wkMgr, NAME).equals(DEFAULT_WKMGR_NAME)) {
				return wkMgr;
			}
		}
		return null;
	}
	
	
	
	
	// The next methods are not working with statistics coming from MBEAN
	// IT SHOULD BE ANALYZED
	// Maybe useful : http://docs.oracle.com/javase/6/docs/api/javax/management/MXBean.html
	
	
	
	/**
	 * 
	 * @param statistics
	 * @param serviceName
	 * @param resourceStatisticName
	 * @param statisticName
	 * @return
	 */
/*
	protected String getValueForOsbStatistic(Map<String, Map<String, Map<String, String>>> statistics, String serviceName, String resourceStatisticName, String statisticName) {
		
		if(statistics != null) {
			
			try {
				
				AppLog.getLogger().notice("statistics contains [" + statistics.size() + "] elements");
				
				Map<String, Map<String, String>> resourceStatistics = statistics.get(serviceName);
				AppLog.getLogger().notice("resourceStatistics contains [" + resourceStatistics.size() + "] elements");
				
				Map<String, String> statisticValues = resourceStatistics.get(resourceStatisticName);
				AppLog.getLogger().notice("statisticValues contains [" + statisticValues.size() + "] elements");
				
				String statisticValue = statisticValues.get(statisticName);
				AppLog.getLogger().notice("statisticValue is [" + statisticValue + "]");
				
				//return statistics.get(serviceName).get(resourceStatisticName).get(statisticName);
				return statisticValue;
				
			} catch (Exception ex) {
				AppLog.getLogger().notice("Not possible to get the value of the statistic [" + serviceName + "/" + resourceStatisticName + "/" + statisticName + "] - Message is [" + ex.getMessage() + "]");
			}
		} else {
			AppLog.getLogger().error("Not possible to extract statictic value - The statistic object is null");
		}
		return null;
	}
*/
	
	/**
	 * 
	 * @param globalStatistics
	 * 
	 * MAP object containing all the informations
	 * 
	 * The KEY is the NAME of the SERVICE (name of PS or BS for example)
	 * The CONTENT is a MAP object having :
	 *    The KEY is the NAME of the ResourceStatistic object
	 *    The CONTENT is a MAP object having :
	 *        The KEY is the NAME of the StatisticValue object
	 *        The CONTENT is the VALUE of the StatisticValue object
	 */
	/*
	protected void printOsbStatistic(Map<String, Map<String, Map<String, String>>> globalStatistics) {

		try {
			if(globalStatistics != null && globalStatistics.size() > 0) {
				
				Iterator<String> serviceKeys = globalStatistics.keySet().iterator();
				while(serviceKeys.hasNext()) {
					
					String serviceName = serviceKeys.next();
					
					AppLog.getLogger().notice("------------------------------------------------------------------");
					AppLog.getLogger().notice("Service [" + serviceName + "]");
					AppLog.getLogger().notice("--------------------------------------------");
					
					Map<String, Map<String, String>> services = globalStatistics.get(serviceName);
					Iterator<String> resourceStatisticKeys = services.keySet().iterator();
					while(resourceStatisticKeys.hasNext()) {
						
						String resourceStatisticName = resourceStatisticKeys.next();
						Map<String, String> statistics = services.get(resourceStatisticName);
						
						AppLog.getLogger().notice("-- ResourceStatisticName  [" + resourceStatisticName + "]");
						
						Iterator<String> statisticsKeys = statistics.keySet().iterator();
						while(statisticsKeys.hasNext()) {
							
							String statisticName = statisticsKeys.next();
							String statisticValue = statistics.get(statisticName);
							
							AppLog.getLogger().notice("---- Statistic Name [" + statisticName + "] - Value [" + statisticValue + "]");
						}
					}
					AppLog.getLogger().notice("------------------------------------------------------------------");
				}
			}
		} catch (Exception ex) {
			AppLog.getLogger().notice("Error during printOsbStatistic method .. - Message is [" + ex.getMessage() + "]");
		}
	}
	*/
	
	/**
	 * 
	 * @param statistics
	 * @return
	 */
	/*
	protected Set<String> getOsbServiceList(Map<String, Map<String, Map<String, String>>> datas) {

		if(datas != null && datas.size() > 0) {			
			return datas.keySet();
		}
		return null;
	}
	*/
	
	/**
	 * 
	 * @param datas
	 * return
	 */
	/*
	protected Set<String> getOsbResourceStatisticList(Map<String, Map<String, String>> datas) {
		
		if(datas != null && datas.size() > 0) {			
			return datas.keySet();
		}
		return null;
	}
	*/
	
	/**
	 * 
	 * @param datas
	 * return
	 */
	/*
	protected Set<String> getOsbStatisticList(Map<String, String> datas) {
		
		if(datas != null && datas.size() > 0) {			
			return datas.keySet();
		}
		return null;
	}
	*/
}