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
package domainhealth.backend.jmxpoll;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.management.ObjectName;

import domainhealth.backend.retriever.DataRetrievalException;
import domainhealth.backend.retriever.StatisticCapturer;
import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.WebLogicMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.statistics.ResourceNameNormaliser;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;
import static domainhealth.core.statistics.StatisticsStorage.*;
import static domainhealth.core.statistics.MonitorProperties.*;
import static domainhealth.core.util.DateUtil.DATETIME_PARAM_FORMAT;

/**
 * Implementation of the statistics capturer for capturing a specific WebLogic 
 * server's Core, JDBC and JMS related statistics, using JMX Polling, before 
 * storing the result row in a local statistics CSV file.
 */
public class StatisticCapturerJMXPoll extends StatisticCapturer {
	/**
	 * Creates a new JMX Poll capturer instance based on the given Weblogic 
	 * server connection details
	 * 
	 * @param csvStats Meta-data about the server statistics CSV file being generated
	 * @param conn Connection to the server's MBean tree
	 * @param serverRuntime Handle on the server's main runtime MBean
	 * @param serverName Name of the server to retrieve statistics for
	 * @param componentBlacklist Names of web-apps/ejbs than should not haves results collected/shown
	 * @param wlsVersionNumber The version of the host WebLogic Domain
	 */
	public StatisticCapturerJMXPoll(StatisticsStorage csvStats, WebLogicMBeanConnection conn, ObjectName serverRuntime, String serverName, int queryIntervalMillis, List<String> componentBlacklist, String wlsVersionNumber) {
		super(csvStats, conn, serverRuntime, serverName, queryIntervalMillis, componentBlacklist, wlsVersionNumber);
	}

	/**
	 * Implements the abstract method to log core server stats to a CSV file.
	 */
	protected void logCoreStats() throws DataRetrievalException {
		try {
			String headerLine = getCoreStatsHeaderLine();
			String contentLine = getCoreStatsLine();
			getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), CORE_RESOURCE_TYPE, CORE_RSC_DEFAULT_NAME, headerLine, contentLine);
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + CORE_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}		
	}

	/**
	 * Use JMX to retrieve the core server stats and creates a row of text data
	 *   
	 * @return The text data row (ready to be appended to a CSV)
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the statistics
	 */
	private String getCoreStatsLine() throws WebLogicMBeanException {
		StringBuilder line = new StringBuilder(DEFAULT_CONTENT_LINE_LEN);

		// Date-time
		line.append(formatSeconsdDateTime(new Date()) + SEPARATOR);

		// Server attributes (not looping because state attr is not a num unlike all other attrs)
		ObjectName serverRuntime = getServerRuntime();
		line.append(getConn().getTextAttr(serverRuntime, SERVER_STATE) + SEPARATOR);
		line.append(getConn().getNumberAttr(serverRuntime, OPEN_SOCKETS) + SEPARATOR);
		
		// JVM attributes (got to do these separately because changing some figures to MegaBytes and calculate heap size current)
		ObjectName jvm = getConn().getChild(getServerRuntime(), JVM_RUNTIME);
		line.append((long)(getConn().getNumberAttr(jvm, HEAP_SIZE_CURRENT) / BYTES_IN_MEGABYTE) + SEPARATOR);
		line.append((long)(getConn().getNumberAttr(jvm, HEAP_FREE_CURRENT) / BYTES_IN_MEGABYTE) + SEPARATOR);
		line.append((long)((getConn().getNumberAttr(jvm, HEAP_SIZE_CURRENT) - getConn().getNumberAttr(jvm, HEAP_FREE_CURRENT)) / BYTES_IN_MEGABYTE) + SEPARATOR);
		line.append(getConn().getNumberAttr(jvm, HEAP_FREE_PERCENT) + SEPARATOR);

		// Thread Pool Attributes
		ObjectName threadPool = getConn().getChild(getServerRuntime(), THREAD_POOL_RUNTIME);
		
		for (String attr : THREADPOOL_MBEAN_MONITOR_ATTR_LIST) {
			// If thread pool does not exist (Use81StyleExecuteQueues) can only put Zero into CSV as result
			if (threadPool == null) {
				line.append(0 + SEPARATOR);
			} else {			
				line.append(getConn().getNumberAttr(threadPool, attr) + SEPARATOR);
			}
		}
		
		// Transaction attributes
		ObjectName txMgr = getConn().getChild(getServerRuntime(), JTA_RUNTIME);
		
		for (String attr : JTA_MBEAN_MONITOR_ATTR_LIST) {
			line.append(getConn().getNumberAttr(txMgr, attr) + SEPARATOR);
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
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String headerLine = constructHeaderLine(JDBC_MBEAN_MONITOR_ATTR_LIST);
			ObjectName jdbcRuntime = getConn().getChild(getServerRuntime(), JDBC_SERVICE_RUNTIME);
	
			for (ObjectName ds : getConn().getChildren(jdbcRuntime, JDBC_DATA_SOURCE_RUNTIMES)) {
				try {
					String name = ResourceNameNormaliser.normalise(DATASOURCE_RESOURCE_TYPE, getConn().getTextAttr(ds, NAME));
					String contentLine = constructStatsLine(ds, JDBC_MBEAN_MONITOR_ATTR_LIST);
					getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), DATASOURCE_RESOURCE_TYPE, name, headerLine, contentLine);
					artifactList.put(name, now);
				} catch (Exception e) {
					AppLog.getLogger().warning("Issue logging " + DATASOURCE_RESOURCE_TYPE + ":" + ds.getCanonicalName() + " for server " + getServerName() + ", reason=" + e.getLocalizedMessage());
				}
			}

			getCSVStats().appendSavedOneDayResourceNameList(nowDate, DATASOURCE_RESOURCE_TYPE, artifactList);			
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + DATASOURCE_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}		
	}

	/**
	 * Implements the abstract method to log JMS destination stats to a CSV 
	 * file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logDestinationsStats() throws DataRetrievalException {
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String headerLine = constructHeaderLine(JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST);
			ObjectName jmsRuntime = getConn().getChild(getServerRuntime(), JMS_RUNTIME);
			
			for (ObjectName jmsServer : getConn().getChildren(jmsRuntime, JMS_SERVERS)) { 
				for (ObjectName destination : getConn().getChildren(jmsServer, DESTINATIONS)) {
					try {
						String name = ResourceNameNormaliser.normalise(DESTINATION_RESOURCE_TYPE, getConn().getTextAttr(destination, NAME));
						String contentLine = constructStatsLine(destination, JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST);
						getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), DESTINATION_RESOURCE_TYPE, name, headerLine, contentLine);
						artifactList.put(name, now);
					} catch (Exception e) {
						AppLog.getLogger().warning("Issue logging " + DESTINATION_RESOURCE_TYPE + ":" + destination.getCanonicalName() + " for server " + getServerName() + ", reason=" + e.getLocalizedMessage());
					}						
				}
			}

			getCSVStats().appendSavedOneDayResourceNameList(nowDate, DESTINATION_RESOURCE_TYPE, artifactList);			
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + DESTINATION_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}		
	}	
	
	
	/**
	 * Implements the abstract method to log SAF destination stats to a CSV 
	 * file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logSafAgentStats() throws DataRetrievalException {
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String headerLine = constructHeaderLine(SAF_AGENT_MBEAN_MONITOR_ATTR_LIST);
			ObjectName safRuntime = getConn().getChild(getServerRuntime(), SAF_RUNTIME);
			
		
			
			for (ObjectName safAgent : getConn().getChildren(safRuntime, "Agents")) { 
					try {
						System.out.println("!!!!!!!!!!!!"+getConn().getTextAttr(safAgent, NAME));
						
						String name = ResourceNameNormaliser.normalise(SAF_RESOURCE_TYPE, getConn().getTextAttr(safAgent, NAME));
						String contentLine = constructStatsLine(safAgent, SAF_AGENT_MBEAN_MONITOR_ATTR_LIST);
						getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), SAF_RESOURCE_TYPE, name, headerLine, contentLine);
						artifactList.put(name, now);
					} catch (Exception e) {
						AppLog.getLogger().warning("Issue logging " + SAF_RESOURCE_TYPE + ":" + safAgent.getCanonicalName() + " for server " + getServerName() + ", reason=" + e.getLocalizedMessage());
					}						
				
			}

			getCSVStats().appendSavedOneDayResourceNameList(nowDate, SAF_RESOURCE_TYPE, artifactList);			
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + SAF_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}
		
	}

	/**
	 * Implements the abstract method to log Web Application stats to a CSV 
	 * file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logWebAppStats() throws DataRetrievalException {
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String headerLine = constructHeaderLine(WEBAPP_MBEAN_MONITOR_ATTR_LIST);
			ObjectName[] appRuntimes = getConn().getChildren(getServerRuntime(), APPLICATION_RUNTIMES);

			for (ObjectName appRuntime : appRuntimes) {
				try {
					ObjectName[] componentRuntimes = getConn().getChildren(appRuntime, COMPONENT_RUNTIMES);
									
					for (ObjectName componentRuntime : componentRuntimes) {
						try {
							String componentType = getConn().getTextAttr(componentRuntime, TYPE);
						
							if (componentType.equals(WEBAPP_COMPONENT_RUNTIME)) {
								String name = ResourceNameNormaliser.normalise(WEBAPP_RESOURCE_TYPE, getConn().getTextAttr(componentRuntime, NAME));
								
								if (!getComponentBlacklist().contains(name)) {						
									String contentLine = constructStatsLine(componentRuntime, WEBAPP_MBEAN_MONITOR_ATTR_LIST);
									getCSVStats().appendToResourceStatisticsCSV(nowDate, getServerName(), WEBAPP_RESOURCE_TYPE, name, headerLine, contentLine);
									artifactList.put(name, now);
								}
							}
						} catch (Exception e) {
							AppLog.getLogger().warning("Issue logging " + WEBAPP_RESOURCE_TYPE + ":" + componentRuntime.getCanonicalName() + " for server " + getServerName() + ", reason=" + e.getLocalizedMessage());
						}
					}
				} catch (Exception e) {
					AppLog.getLogger().warning("Issue logging " + WEBAPP_RESOURCE_TYPE + ":" + appRuntime.getCanonicalName() + " for server " + getServerName() + ", reason=" + e.getLocalizedMessage());
				}					
			}
			
			getCSVStats().appendSavedOneDayResourceNameList(nowDate, WEBAPP_RESOURCE_TYPE, artifactList);			
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + WEBAPP_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}		
	}	

	/**
	 * Implements the abstract method to log EJB stats to a CSV file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logEJBStats() throws DataRetrievalException {
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String headerLine = constructHeaderLine(EJB_MBEAN_MONITOR_ATTR_LIST);
			ObjectName[] appRuntimes = getConn().getChildren(getServerRuntime(), APPLICATION_RUNTIMES);

			for (ObjectName appRuntime : appRuntimes) {
				try {
					ObjectName[] componentRuntimes = getConn().getChildren(appRuntime, COMPONENT_RUNTIMES);
					
					for (ObjectName componentRuntime : componentRuntimes) {
						try {
							String componentType = getConn().getTextAttr(componentRuntime, TYPE);
						
							if (componentType.equals(EJB_COMPONENT_RUNTIME)) {  
								ObjectName[] ejbRuntimes = getConn().getChildren(componentRuntime, EJB_RUNTIMES);
		
								for (ObjectName ejbRuntime : ejbRuntimes) {
									try { 
										String name = ResourceNameNormaliser.normalise(EJB_RESOURCE_TYPE, getConn().getTextAttr(ejbRuntime, NAME));
			
										if (!getComponentBlacklist().contains(name)) {													
											ObjectName poolRuntime = getConn().getChild(ejbRuntime, POOL_RUNTIME);
											ObjectName txRuntime = getConn().getChild(ejbRuntime, TRANSACTION_RUNTIME);
											StringBuilder contentLine = new StringBuilder(constructStatsLine(poolRuntime, EJB_POOL_MBEAN_MONITOR_ATTR_LIST));
											appendToStatsLine(contentLine, txRuntime, EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST);							
											getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), EJB_RESOURCE_TYPE, name, headerLine, contentLine.toString());
											artifactList.put(name, now);
										}
									} catch (Exception e) {
										AppLog.getLogger().warning("Issue logging " + EJB_RESOURCE_TYPE + ":" + ejbRuntime.getCanonicalName() + " resources for server " + getServerName() + ", reason=" + e.getLocalizedMessage());
									}										
								}
							}
						} catch (Exception e) {
							AppLog.getLogger().warning("Issue logging " + EJB_RESOURCE_TYPE + ":" + componentRuntime.getCanonicalName() + " resources for server " + getServerName() + ", reason=" + e.getLocalizedMessage());
						}
					}
				} catch (Exception e) {
					AppLog.getLogger().warning("Issue logging " + EJB_RESOURCE_TYPE + ":" + appRuntime.getCanonicalName() + " resources for server " + getServerName() + ", reason=" + e.getLocalizedMessage());
				}
			}
			
			getCSVStats().appendSavedOneDayResourceNameList(nowDate, EJB_RESOURCE_TYPE, artifactList);
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + EJB_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}		
	}

	/**
	 * Implements the abstract method to log WLHostMachine optional mbean stats to a CSV file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logHostMachineStats() throws DataRetrievalException {
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String headerLine = constructHeaderLine(HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST);			
			String hostMBeanName = String.format(HOST_MACHINE_MBEAN_FULLNAME_TEMPLATE, getServerName());
			ObjectName remoteWLHostMachineStatsMBean = getConn().getCustomMBean(hostMBeanName);
			
			if (remoteWLHostMachineStatsMBean != null) {
				String name = ResourceNameNormaliser.normalise(HOSTMACHINE_RESOURCE_TYPE, HOST_MACHINE_MBEAN_NAME);
				String contentLine = constructStatsLine(remoteWLHostMachineStatsMBean, HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST);
				getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), HOSTMACHINE_RESOURCE_TYPE, name, headerLine, contentLine);
				artifactList.put(name, now);
			} 

			getCSVStats().appendSavedOneDayResourceNameList(nowDate, HOSTMACHINE_RESOURCE_TYPE, artifactList);			
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + HOSTMACHINE_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}				
	}	

	/**
	 * Does nothing - for JMX Polling statistic capture, we don't go deep and 
	 * try to get extensive stats due to the likely performance impact this 
	 * would have on the running servers.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logExtendedStats() throws DataRetrievalException {
		// Do nothing - no extended stats for JMX Poll
	}

	/**
	 * Construct a single line of statistics to go in a CSV file, by querying 
	 * an MBean object's specific attributes from a list of given attribute 
	 * names.
	 * 
	 * @param objectName MBean object name to query the statistics from
	 * @param attrList List of attributes
	 * @param estLength Approximate length of line
	 * @return The new statistics text line
	 * @throws WebLogicMBeanException Indicates problem occurred retrieving MBean properties
	 */
	private String constructStatsLine(ObjectName objectName, String[] attrList) throws WebLogicMBeanException {
		StringBuilder line = new StringBuilder(DEFAULT_CONTENT_LINE_LEN);
		line.append(formatSeconsdDateTime(new Date()) + SEPARATOR);
		appendToStatsLine(line, objectName, attrList);
		return line.toString();
	}

	/**
	 * Add to a partial line of statistics to go in a CSV file, by querying 
	 * an MBean object's specific attributes from a list of given attribute 
	 * names.
	 * 
	 * @param line The line of text to add statistics info to
	 * @param objectName MBean object name to query the statistics from
	 * @param attrList List of attributes
	 * @throws WebLogicMBeanException Indicates problem occurred retrieving MBean properties
	 */
	private void appendToStatsLine(StringBuilder line, ObjectName objectName, String[] attrList) throws WebLogicMBeanException {		
		for (String attr : attrList) {
			line.append(getConn().getNumberAttr(objectName, attr) + SEPARATOR);
		}
	}

	// Constants
	private static final int DEFAULT_CONTENT_LINE_LEN = 100;

	
}
