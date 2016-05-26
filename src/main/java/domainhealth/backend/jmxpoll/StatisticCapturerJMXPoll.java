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

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.AGENTS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.APPLICATION_RUNTIMES;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.COMPONENT_RUNTIMES;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DESTINATIONS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.EJB_COMPONENT_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.EJB_RUNTIMES;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.HEAP_FREE_CURRENT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.HEAP_FREE_PERCENT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.HEAP_SIZE_CURRENT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JDBC_DATA_SOURCE_RUNTIMES;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JDBC_SERVICE_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_SERVERS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JTA_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JVM_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.OPEN_SOCKETS_CURRENT_COUNT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.POOL_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.SAF_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.SERVER_STATE;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.THREAD_POOL_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.TRANSACTION_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.TYPE;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.WEBAPP_COMPONENT_RUNTIME;
import static domainhealth.core.statistics.MonitorProperties.CORE_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.CORE_RSC_DEFAULT_NAME;
import static domainhealth.core.statistics.MonitorProperties.DATASOURCE_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.DESTINATION_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.EJB_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.EJB_POOL_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.EJB_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.HOSTMACHINE_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.HOST_MACHINE_MBEAN_FULLNAME_TEMPLATE;
import static domainhealth.core.statistics.MonitorProperties.HOST_MACHINE_MBEAN_NAME;
import static domainhealth.core.statistics.MonitorProperties.HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.JAVA_JVM_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.JDBC_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.JTA_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.JVM_MBEAN_FULLNAME_TEMPLATE;
import static domainhealth.core.statistics.MonitorProperties.JVM_MBEAN_NAME;
import static domainhealth.core.statistics.MonitorProperties.JVM_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.OSB_MBEAN_FULLNAME_TEMPLATE;
import static domainhealth.core.statistics.MonitorProperties.SAF_AGENT_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.SAF_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.THREADPOOL_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.WEBAPP_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.WEBAPP_RESOURCE_TYPE;
import static domainhealth.core.statistics.StatisticsStorage.SEPARATOR;
import static domainhealth.core.util.DateUtil.DATETIME_PARAM_FORMAT;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.management.ObjectName;

import domainhealth.backend.retriever.DataRetrievalException;
import domainhealth.backend.retriever.StatisticCapturer;
import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.WebLogicMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.jmx.WebLogicMBeanPropConstants;
import domainhealth.core.statistics.MonitorProperties;
import domainhealth.core.statistics.ResourceNameNormaliser;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.frontend.data.ServerState;

/**
 * Implementation of the statistics capturer for capturing a specific WebLogic 
 * server's Core, JDBC and JMS related statistics, using JMX Polling, before 
 * storing the result row in a local statistics CSV file.
 */
public class StatisticCapturerJMXPoll extends StatisticCapturer {
	
	// Constants
	private static final int DEFAULT_CONTENT_LINE_LEN = 100;
		
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

		line.append(ServerState.getValueForState(getConn().getTextAttr(serverRuntime, SERVER_STATE)) + SEPARATOR);
		line.append(getConn().getNumberAttr(serverRuntime, OPEN_SOCKETS_CURRENT_COUNT) + SEPARATOR);
		
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
					Iterator<String> iteratorBlacklist = getComponentBlacklist().iterator();
	            	boolean blacklist = false;
	            	
	            	while(iteratorBlacklist.hasNext()){
	            		String element = iteratorBlacklist.next();
						if(name.contains(element)){
							blacklist = true;
							break;
						}
					}
						
					if (!blacklist) {
						String contentLine = constructStatsLine(ds, JDBC_MBEAN_MONITOR_ATTR_LIST);
						getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), DATASOURCE_RESOURCE_TYPE, name, headerLine, contentLine);
						artifactList.put(name, now);
					}
					
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
						Iterator<String> iteratorBlacklist = getComponentBlacklist().iterator();
		            	boolean blacklist = false;
		            	
		            	while(iteratorBlacklist.hasNext()){
		            		String element = iteratorBlacklist.next();
							if(name.contains(element)){
								blacklist = true;
								break;
							}
						}
							
						if (!blacklist) {
							String contentLine = constructStatsLine(destination, JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST);
							getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), DESTINATION_RESOURCE_TYPE, name, headerLine, contentLine);
							artifactList.put(name, now);
						}
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
				
			for (ObjectName safAgent : getConn().getChildren(safRuntime, AGENTS)) { 
					try {
						
						String name = ResourceNameNormaliser.normalise(SAF_RESOURCE_TYPE, getConn().getTextAttr(safAgent, NAME));		
						Iterator<String> iteratorBlacklist = getComponentBlacklist().iterator();
		            	boolean blacklist = false;
		            	
		            	while(iteratorBlacklist.hasNext()){
		            		String element = iteratorBlacklist.next();
							if(name.contains(element)){
								blacklist = true;
								break;
							}
						}
							
						if (!blacklist) {
							String contentLine = constructStatsLine(safAgent, SAF_AGENT_MBEAN_MONITOR_ATTR_LIST);
							getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), SAF_RESOURCE_TYPE, name, headerLine, contentLine);
							artifactList.put(name, now);
						}
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
			
//AppLog.getLogger().notice("Processing WebApp for server [" + getServerName() + "]");

			for (ObjectName appRuntime : appRuntimes) {
				try {
					ObjectName[] componentRuntimes = getConn().getChildren(appRuntime, COMPONENT_RUNTIMES);
									
					for (ObjectName componentRuntime : componentRuntimes) {
						try {
							String componentType = getConn().getTextAttr(componentRuntime, TYPE);
						
							if (componentType.equals(WEBAPP_COMPONENT_RUNTIME)) {
								String name = ResourceNameNormaliser.normalise(WEBAPP_RESOURCE_TYPE, getConn().getTextAttr(componentRuntime, NAME));
								
								Iterator<String> iteratorBlacklist = getComponentBlacklist().iterator();
				            	boolean blacklist = false;
				            	
				            	while(iteratorBlacklist.hasNext()){
				            		String element = iteratorBlacklist.next();
									if(name.contains(element)){
										blacklist = true;
										break;
									}
								}
									
								if (!blacklist) {
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
										
										Iterator<String> iteratorBlacklist = getComponentBlacklist().iterator();
						            	boolean blacklist = false;
						            	
						            	while(iteratorBlacklist.hasNext()){
						            		String element = iteratorBlacklist.next();
											if(name.contains(element)){
												blacklist = true;
												break;
											}
										}
											
										if (!blacklist) {
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
				
//AppLog.getLogger().notice("Processing MachineHost for server [" + getServerName() + "]");
				
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
	 * Implements the abstract method to log WLJvm optional mbean stats to a CSV file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logJvmStats() throws DataRetrievalException {
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String headerLine = constructHeaderLine(JAVA_JVM_MBEAN_MONITOR_ATTR_LIST);			
			String jvmMBeanName = String.format(JVM_MBEAN_FULLNAME_TEMPLATE, getServerName());
			ObjectName remoteWLJvmStatsMBean = getConn().getCustomMBean(jvmMBeanName);
			
			if (remoteWLJvmStatsMBean != null) {
				String name = ResourceNameNormaliser.normalise(JVM_RESOURCE_TYPE, JVM_MBEAN_NAME);
				String contentLine = constructStatsLine(remoteWLJvmStatsMBean, JAVA_JVM_MBEAN_MONITOR_ATTR_LIST);
				getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), JVM_RESOURCE_TYPE, name, headerLine, contentLine);
				artifactList.put(name, now);
			}

			getCSVStats().appendSavedOneDayResourceNameList(nowDate, JVM_RESOURCE_TYPE, artifactList);			
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + JVM_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}		
	}
	
	/**
	 * Implements the abstract method to log WLOsb optional mbean stats to a CSV file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logOsbStats() throws DataRetrievalException {
		
		// PROXY elements
		logOsbProxyStats();
		
		// BUSINESS elements
		logOsbBusinessStats();
	}
	
	/**
	 * 
	 * @throws DataRetrievalException
	 */
	private void logOsbProxyStats() throws DataRetrievalException {
		
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			//Properties artifactServiceList = new Properties();
			//Properties artifactResourceStatisticList = new Properties();
			//Properties artifactStatisticList = new Properties();
			Properties headerLineList = new Properties();
			Properties artifactList = new Properties();

			//String headerLine = constructHeaderLine(OSB_MBEAN_MONITOR_ATTR_LIST);			
			String osbMBeanName = String.format(OSB_MBEAN_FULLNAME_TEMPLATE, getServerName());
			
			// OSB metric are retrieved from the ADMIN server
			// The custom MBean should be deployed only on it anyway
			
// Depending of architecture, the OSB is running on ADMIN or on CLUSTER
// If we don't set a target, then the cluster will be used
// -> For standalone, as the OSB is deployed in CLUSTER, the ADMIN server's name shouldn't be used but getServerName() returns all the times the ADMIN server's name ... (we should check why)
// -> We should check if the target "null" is working fine in case of standalone OSB environment
						
			if(WebLogicMBeanConnection.isThisTheAdminServer()) {
								
				ObjectName remoteWLOsbStatsMBean = getConn().getCustomMBean(osbMBeanName);
				if (remoteWLOsbStatsMBean != null) {
										
					for(int index = 0; index < MonitorProperties.OSB_PS_RESSOURCE_TYPE.length; index ++) {
						
						// -----------------------------------------------------------------------------
						try {
							
							// Select the ResourceType
							String resourceType = MonitorProperties.OSB_PS_RESSOURCE_TYPE[index];
							//String serviceFilename = MonitorProperties.OSB_PS_TYPE + "_" + resourceType + "_" + MonitorProperties.OSB_SERVICE_TYPE;
							String serviceFilename = MonitorProperties.OSB_PS_TYPE + "_" + resourceType;
														
							// -----------------------------------------------------------------------------
							// Collect the statistics
							//getConn().invoke(remoteWLOsbStatsMBean, "collectServiceStatistics", new Object [] {getServerName(), MonitorProperties.OSB_PS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});						
							//getConn().invoke(remoteWLOsbStatsMBean, "collectServiceStatistics", new Object [] {null, MonitorProperties.OSB_PS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
							getConn().invoke(remoteWLOsbStatsMBean, "collectServiceStatistics", new Object [] {MonitorProperties.OSB_PS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName()});
							// -----------------------------------------------------------------------------
							
							// -----------------------------------------------------------------------------
							try {
								// Get the list of Service
//AppLog.getLogger().notice("Collecting information for server [" + getServerName() + "]");
								String[] osbServiceList = (String[])getConn().getObjectAttr(remoteWLOsbStatsMBean, "OsbServiceList");
								if(osbServiceList != null) {
									
									//AppLog.getLogger().notice("---------------------------------------------------------");
									//AppLog.getLogger().notice("List of Service");
									//AppLog.getLogger().notice("---------------");
									
									for(int indexService = 0; indexService < osbServiceList.length; indexService ++) {
										String serviceName = osbServiceList[indexService];									
//artifactServiceList.put(serviceName, now);
										
										// -----------------------------------------------------------------------------
										try {
											// Get the list of ResourceStatistic
											String[] resourceStatisticList = (String[])getConn().invoke(remoteWLOsbStatsMBean, "getOsbResourceStatisticList", new Object [] {serviceName}, new String[]{String.class.getName()});
											if(resourceStatisticList != null) {
												
												//AppLog.getLogger().notice("");
												//AppLog.getLogger().notice("\t---------------------------------------------------------");
												//AppLog.getLogger().notice("\tResourceStatistic for [" + serviceName + "]");
												//AppLog.getLogger().notice("\t---------------------------------------------------------");
												
												for(int indexResourceStatistic = 0; indexResourceStatistic < resourceStatisticList.length; indexResourceStatistic ++) {
													
													String resourceStatisticName = resourceStatisticList[indexResourceStatistic];
//artifactResourceStatisticList.put(resourceStatisticName, now);
													
													// -----------------------------------------------------------------------------
													try {
														// Get the list of Statistic
														String[] statisticList = (String[])getConn().invoke(remoteWLOsbStatsMBean, "getOsbStatisticList", new Object [] {serviceName, resourceStatisticName}, new String[]{String.class.getName(), String.class.getName()});
														if(statisticList != null) {
															
															//AppLog.getLogger().notice("");
															//AppLog.getLogger().notice("\t\t---------------------------------------------------------");
															//AppLog.getLogger().notice("\t\tStatistic for [" + serviceName + "/" + resourceStatisticName + "]");
															//AppLog.getLogger().notice("\t\t---------------------------------------------------------");
															
															// Collect the list of elements
															for(int indexStatistic = 0; indexStatistic < statisticList.length; indexStatistic ++) {
																String statisticName = statisticList[indexStatistic];
																//artifactStatisticList.put(statisticName, now);
																headerLineList.put(statisticName, now);
															}
															
															// Generate the properties file containing the list of element
															String name = serviceName + "_" + resourceStatisticName;
															artifactList.put(name, now);
															
//String statisticFilename = MonitorProperties.OSB_PS_TYPE + "_" + resourceType + "_" + MonitorProperties.OSB_SERVICE_TYPE + "_" + serviceName + "_" + MonitorProperties.OSB_RESOURCE_STATISTIC_TYPE + "_" + resourceStatisticName + "_" + MonitorProperties.OSB_STATISTIC_TYPE;
//getCSVStats().appendSavedOneDayResourceNameList(nowDate, statisticFilename, artifactStatisticList);
															
															// Generate the header line from the properties file containing all the properties's name
/*
Set<Object> objects = artifactStatisticList.keySet();
String[] osbAttributeList = new String[objects.size()];
osbAttributeList = (String[])artifactStatisticList.keySet().toArray(osbAttributeList);
String headerLine = constructHeaderLine(osbAttributeList);

// Reset the list
artifactStatisticList = new Properties();
*/
															
															Set<Object> objects = headerLineList.keySet();
															String[] osbAttributeList = new String[objects.size()];
															osbAttributeList = (String[])headerLineList.keySet().toArray(osbAttributeList);
															String headerLine = constructHeaderLine(osbAttributeList);
															
															// Reset the list
															headerLineList = new Properties();
															
															// Get the statistics
															String contentLine = constructOsbStatsLine(remoteWLOsbStatsMBean, serviceName, resourceStatisticName, statisticList);
															
															// Save the statistics into the specific file
															getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), serviceFilename, serviceName, headerLine, contentLine);
															
															//AppLog.getLogger().notice("Adding the statistic line to the file [" + serviceFilename + "] for the service [" + serviceName + "]");

															//AppLog.getLogger().notice("\t\t---------------------------------------------------------");
															//AppLog.getLogger().notice("");
														}
													} catch(Exception ex) {
														AppLog.getLogger().error("Couldn't find the method [getOsbStatisticList] in OSB extention for DomainHealth application");
														AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
													}
													// -----------------------------------------------------------------------------
												}
												
// Generate the properties file for the ResourceStatistic
//String resourceStatisticFilename = MonitorProperties.OSB_PS_TYPE + "_" + resourceType + "_" + MonitorProperties.OSB_SERVICE_TYPE + "_" + serviceName + "_" + MonitorProperties.OSB_RESOURCE_STATISTIC_TYPE;
//getCSVStats().appendSavedOneDayResourceNameList(nowDate, resourceStatisticFilename, artifactResourceStatisticList);
//artifactResourceStatisticList = new Properties();
												
												//AppLog.getLogger().notice("\t---------------------------------------------------------");
												//AppLog.getLogger().notice("");
											}
										} catch(Exception ex) {
											AppLog.getLogger().error("Couldn't find the method [getOsbResourceStatisticList] in OSB extention for DomainHealth application");
											AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
										}
										// -----------------------------------------------------------------------------
									}
									
									// Persist the list of element into properties (for each king of OSB resource)
									getCSVStats().appendSavedOneDayResourceNameList(nowDate, serviceFilename, artifactList);
									
									// Reset the list
									artifactList = new Properties();
									
									//AppLog.getLogger().notice("---------------------------------------------------------");
									//AppLog.getLogger().notice("");
								}
							} catch(Exception ex) {
								AppLog.getLogger().error("Couldn't find the method [getOsbServiceList] in OSB extention for DomainHealth application");
								AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
							}
							// -----------------------------------------------------------------------------
	
						} catch(Exception ex) {
							AppLog.getLogger().error("Couldn't find the method [collectServiceStatistics] in OSB extention for DomainHealth application");
							AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
						}
						// -----------------------------------------------------------------------------
					}
					
					
					
/*
// Is not working ...
// Need to find how to read the Map sent by MBean			
try {
	
	Map<String, Map<String, Map<String, String>>> globalStatistics = (Map<String, Map<String, Map<String, String>>>)getConn().invoke(remoteWLOsbStatsMBean, "getServiceStatistics", new Object [] {MonitorProperties.OSB_PS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE}, new String[]{String.class.getName(), String.class.getName()});
	if(globalStatistics != null) {
		
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
	AppLog.getLogger().error("Couldn't find the method [getServiceStatistics] in OSB extention for DomainHealth application");
	AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
}
*/

/*
try {
	
	TabularDataSupport statistics = (TabularDataSupport)getConn().invoke(remoteWLOsbStatsMBean, "getServiceStatistics", new Object [] {MonitorProperties.OSB_PS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE}, new String[]{String.class.getName(), String.class.getName()});
						if(statistics != null) {
							
							Set<Object> serviceKeys = statistics.keySet();
							
AppLog.getLogger().notice("Found [" + serviceKeys.size() + "] Services");
		
		Iterator serviceIterator = serviceKeys.iterator();
		while(serviceIterator.hasNext()) {
				
			Object serviceName = serviceIterator.next();
			AppLog.getLogger().notice("--- Service [" + serviceName + "]");
								
								TabularDataSupport resourceStatistic = (TabularDataSupport)statistics.get(serviceName);
								Set<Object> resourceStatisticKeys = resourceStatistic.keySet();
								
AppLog.getLogger().notice("Found [" + resourceStatisticKeys.size() + "] ResourceStatistic");
			
			Iterator resourceStatisticIterator = resourceStatisticKeys.iterator();
			while(resourceStatisticIterator.hasNext()) {
						
				Object resourceStatisticName = resourceStatisticIterator.next();
				AppLog.getLogger().notice("------ ResourceStatistic [" + resourceStatisticName + "]");
									
									TabularDataSupport statistic = (TabularDataSupport)resourceStatistic.get(resourceStatisticName);
									Set<Object> statisticKeys = statistic.keySet();
									
AppLog.getLogger().notice("Found [" + statisticKeys.size() + "] Statistic");
				
				Iterator statisticIterator = statisticKeys.iterator();
				while(statisticIterator.hasNext()) {
					
					Object statisticName = statisticIterator.next();
					AppLog.getLogger().notice("--------- StatisticName [" + statisticName + "]");
				}
			}
		}
		
//							String SEPARATOR = ";";
//							List<String> list = new ArrayList<String>();
//							for (Object v: statistics.values()) {
//							   CompositeData row = (CompositeData)v;
//							   StringBuilder rowString = new StringBuilder();
//							   for (Object rv: row.values()) {
//							       if (rowString.length()!=0)
//							            rowString.append(SEPARATOR);
//							       rowString.append(rv);
//							   }
//							   list.add(rowString.toString());
//							   AppLog.getLogger().notice("--- [" + rowString + "]");
//							}
		
//							for(Object o : statistics.values())
//				            {
//				                CompositeData compositeData = (CompositeData) o;
//				                Collection collection = compositeData.values();
//				                Iterator iterator = collection.iterator();
//				                while(iterator.hasNext()) {
//				                	AppLog.getLogger().notice("Element is [" + iterator.next() + "]");
//				                }
//							}
	}
} catch (Exception ex) {
	AppLog.getLogger().error("Couldn't find the method [getServiceStatistics TabularDataSupport] in OSB extention for DomainHealth application");
	AppLog.getLogger().error("Message is [" +ex.getMessage() + "]");
}
*/
					
					/*
					try {
						
						TabularDataSupport statistics = (TabularDataSupport)getConn().invoke(remoteWLOsbStatsMBean, "getServiceStatistics", new Object [] {MonitorProperties.OSB_PS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE}, new String[]{String.class.getName(), String.class.getName()});
						if(statistics != null) {
														
							Set<Object> serviceKeys = statistics.keySet();
							Iterator serviceIterator = serviceKeys.iterator();
							while(serviceIterator.hasNext()) {
							
								Object serviceName = serviceIterator.next();
								AppLog.getLogger().notice("Service   [" + serviceName + "]");
								
//								//String serviceName = (String)serviceIterator.next();
//								CompositeData serviceName = (CompositeData)serviceIterator.next();
//								AppLog.getLogger().notice("Service [" + serviceName + "]");
//								
//								TabularDataSupport resourceStatistic = (TabularDataSupport)statistics.get(serviceName);
//								Set<Object> resourceStatisticKeys = resourceStatistic.keySet();
//								Iterator resourceStatisticIterator = resourceStatisticKeys.iterator();
//								while(resourceStatisticIterator.hasNext()) {
//									
//									//String resourceStatisticName = resourceStatisticIterator.next().toString();
//									CompositeData resourceStatisticName = (CompositeData)resourceStatisticIterator.next();
//									
//									AppLog.getLogger().notice("ResourceStatisticName [" + resourceStatisticName + "]");
//									
//									TabularDataSupport statistic = (TabularDataSupport)resourceStatistic.get(resourceStatisticName);
//									Set<Object> statisticKeys = statistic.keySet();
//									Iterator statisticIterator = statisticKeys.iterator();
//									while(statisticIterator.hasNext()) {
//										
//										//String statisticName = statisticIterator.next().toString();
//										CompositeData statisticName = (CompositeData)statisticIterator.next();
//										AppLog.getLogger().notice("StatisticName [" + statisticName + "]");
//										AppLog.getLogger().notice("StatisticValue [" + statistic.get(statisticName) + "]");
//										
//									}
//								}
							}
						}
					} catch (Exception ex) {
						AppLog.getLogger().error("Couldn't find the method [getServiceStatistics TabularDataSupport] in OSB extention for DomainHealth application");
						AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
					}
					*/
					
					 



					/*
					try {
						
						Map<String, Map<String, Map<String, String>>> statistics = (Map<String, Map<String, Map<String, String>>>)getConn().invoke(remoteWLOsbStatsMBean, "getServiceStatistics", new Object [] {MonitorProperties.OSB_PS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE}, new String[]{String.class.getName(), String.class.getName()});						
						if(statistics != null) { 
						
							// Only for testing purpose
							try {
								Set<String> serviceList = (Set<String>)getConn().invoke(remoteWLOsbStatsMBean, "getOsbServiceList", new Object [] {statistics}, new String[]{Map.class.getName()});
								if(serviceList != null) AppLog.getLogger().notice("Got [" + serviceList.size() + "] services");
								else AppLog.getLogger().notice("Didn't find any service");
								
							} catch(Exception ex) {
								AppLog.getLogger().error("Couldn't find the method [getOsbServiceList] in OSB extension for DomainHealth application");
								AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
							}
						}
					} catch (Exception ex) {
						AppLog.getLogger().error("Couldn't find the method [getServiceStatistics] in OSB extension for DomainHealth application");
					}
					*/
					
					
					
					
					
					
/*
					try {
						// Problem to read datas coming from MBean (TabularData)
						Map<String, Map<String, Map<String, String>>> statistics = (Map<String, Map<String, Map<String, String>>>)getConn().invoke(remoteWLOsbStatsMBean, "getServiceStatistics", new Object [] {MonitorProperties.OSB_PS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE}, new String[]{String.class.getName(), String.class.getName()});						
						if(statistics != null) { 

AppLog.getLogger().notice("(getStatsForServices (1)) Found [" + statistics.size() + "] statistics for the ProxyServices/SERVICE");

//Problem to read datas coming from MBean (TabularData)
//printOsbStatistic(statistics);


// From the statistics we are able to split the statistics by MonitorProperties.OSB_RESOURCE_STATISTIC_TYPE and MonitorProperties.OSB_STATISTIC_TYPE
// Properties file should be generated for each case
//
// See StatisticCapturer and those methods:
//   - protected Set<String> getOsbServiceList(Map<String, Map<String, Map<String, String>>> datas)
//   - protected Set<String> getOsbResourceStatisticList(Map<String, Map<String, String>> datas)
//   - protected Set<String> getOsbStatisticList(Map<String, String> datas)

							
//							// Only for testing purpose
//							try {
//								
//								Object serviceName = "AttachmentPS";
//								Object resourceStatisticName = "Transport";
//								Object statisticName = "success-rate_count";
//							
//								String result = getValueForOsbStatistic(statistics, serviceName, resourceStatisticName, statisticName);
//								AppLog.getLogger().notice("Got [" + result + "] as value for [" + serviceName + "/" + resourceStatisticName + "/" + statisticName + "] for ProxyServices/SERVICE/COUNT");
//								AppLog.getLogger().notice("");
//								
//							} catch(Exception ex) {}
							
							// Only for testing purpose
							try {
								Set<String> results = getOsbServiceList(statistics);
								if(results != null) AppLog.getLogger().notice("Got [" + results.size() + "] services");
								else AppLog.getLogger().error("Issue during execution of getOsbServiceList method");
								
							} catch(Exception ex) {}
							
						} else AppLog.getLogger().notice("(getStatsForServices (1)) Found nothing ...");

					} catch(Exception ex) {
						AppLog.getLogger().error("Couldn't find the method [getStatsForServices (1)] in OSB extension for DomainHealth application - Message is [" + ex.getMessage() + "]");
					}
*/
					
					/*
					try {
						Map<String, Map<String, Map<String, String>>> statistics = (Map<String, Map<String, Map<String, String>>>)getConn().invoke(remoteWLOsbStatsMBean, "getServiceStatistics", new Object [] {MonitorProperties.OSB_PS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE, MonitorProperties.OSB_STATISTIC_TYPE_COUNT}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
						if(statistics != null) { 

AppLog.getLogger().notice("(getStatsForServices (2)) Found [" + statistics.size() + "] statistics for the ProxyServices/SERVICE/COUNT");

							try {
								
								String serviceName = "AttachmentPS";
								String resourceStatisticName = "Transport";
								String statisticName = "success-rate_count";
							
								String result= getValueForOsbStatistic(statistics, serviceName, resourceStatisticName, statisticName);
								AppLog.getLogger().notice("Got [" + result + "] as value for [" + serviceName + "/" + resourceStatisticName + "/" + statisticName + "] for ProxyServices/SERVICE/COUNT");
								AppLog.getLogger().notice("");
								
							} catch(Exception ex) {}
						} else AppLog.getLogger().notice("(getStatsForServices (2)) Found nothing ...");
						
					} catch(Exception ex) {
						AppLog.getLogger().error("Couldn't find the method [getStatsForServices (2)] in OSB extension for DomainHealth application");
					}
					*/

/*
// Others ProxyServices elements
statistics = (Map<String, Map<String, Map<String, String>>>)getConn().invoke(remoteWLOsbStatsMBean, "getStatsForServices", new Object [] {MonitorProperties.OSB_PS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION}, new String[]{String.class.getName(), String.class.getName()});
//AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the ProxyServices/WEBSERVICE_OPERATION - Server is [" + getServerName() + "]");
AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the ProxyServices/WEBSERVICE_OPERATION");

statistics = (Map<String, Map<String, Map<String, String>>>)getConn().invoke(remoteWLOsbStatsMBean, "getStatsForServices", new Object [] {MonitorProperties.OSB_PS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT}, new String[]{String.class.getName(), String.class.getName()});
//AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the ProxyServices/FLOW_COMPONENT - Server is [" + getServerName() + "]");
AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the ProxyServices/FLOW_COMPONENT");
*/

/*
// BusinessServices elements
statistics = (Map<String, Map<String, String>>)getConn().invoke(remoteWLOsbStatsMBean, "getStatsForServices", new Object [] {MonitorProperties.OSB_BS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE}, new String[]{String.class.getName(), String.class.getName()});
//AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/SERVICE - Server is [" + getServerName() + "]");
AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/SERVICE");

statistics = (Map<String, Map<String, String>>)getConn().invoke(remoteWLOsbStatsMBean, "getStatsForServices", new Object [] {MonitorProperties.OSB_BS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION}, new String[]{String.class.getName(), String.class.getName()});
//AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/WEBSERVICE_OPERATION - Server is [" + getServerName() + "]");
AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/WEBSERVICE_OPERATION");

statistics = (Map<String, Map<String, String>>)getConn().invoke(remoteWLOsbStatsMBean, "getStatsForServices", new Object [] {MonitorProperties.OSB_BS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_URI}, new String[]{String.class.getName(), String.class.getName()});
//AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/URI - Server is [" + getServerName() + "]");
AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/URI");
*/
/*
					} catch (Exception ex) {
						AppLog.getLogger().error("Couldn't find the method [getServiceList] in OSB extension for DomainHealth application");
					}
*/


/*
// ---------------------------------------------
// Is working and it calls the getXXX methods (with input parameters)
// -> Is not a simple getXXX method so introspection must be used
try {
	Object[][] argList = new Object [][] {{""},{""},{""},{""},{""}};
	String[][] argTypeList = new String[][] {{String.class.getName()}, {String.class.getName()}, {String.class.getName()}, {String.class.getName()}, {String.class.getName()}};
	String contentLine = constructStatsLine(remoteWLOsbStatsMBean, OSB_MBEAN_MONITOR_ATTR_INVOKE_LIST, argList, argTypeList);

	AppLog.getLogger().notice("(1) ContentLine is [" + contentLine + "]");
} catch (Exception ex) {
	AppLog.getLogger().notice("(1) ContentLine couldn't be set ...");
	ex.printStackTrace();
}
AppLog.getLogger().notice("");
// ---------------------------------------------
*/

/*
//-------------------------------------------------------------------------
// Is working and it calls the real getXXX methods (without input parameters)
// -> getAttribute... method could be used
try {	
	String contentLine = constructStatsLine(remoteWLOsbStatsMBean, OSB_MBEAN_MONITOR_ATTR_LIST);
	AppLog.getLogger().notice("(2) ContentLine is [" + contentLine + "]");
} catch (Exception ex) {
	AppLog.getLogger().notice("(2) ContentLine couldn't be set ...");
}
AppLog.getLogger().notice("");
//---------------------------------------------
*/
				}
			}
		} catch (Exception ex) {
			throw new DataRetrievalException("Problem logging " + MonitorProperties.OSB_PS_TYPE + " resources for server [" + getServerName() + "]", ex);
		}
	}
	
	/**
	 * 
	 * @throws DataRetrievalException
	 */
	private void logOsbBusinessStats() throws DataRetrievalException {
		
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			//Properties artifactServiceList = new Properties();
			//Properties artifactResourceStatisticList = new Properties();
			//Properties artifactStatisticList = new Properties();
			Properties headerLineList = new Properties();
			Properties artifactList = new Properties();

			//String headerLine = constructHeaderLine(OSB_MBEAN_MONITOR_ATTR_LIST);			
			String osbMBeanName = String.format(OSB_MBEAN_FULLNAME_TEMPLATE, getServerName());
			
			// OSB metric are retrieved from the ADMIN server
			// The custom MBean should be deployed only on it anyway
			
// Depending of architecture, the OSB is running on ADMIN or on CLUSTER
// If we don't set a target, then the cluster will be used
// -> For standalone, as the OSB is deployed in CLUSTER, the ADMIN server's name shouldn't be used but getServerName() returns all the times the ADMIN server's name ... (we should check why)
// -> We should check if the target "null" is working fine in case of standalone OSB environment
			
			if(WebLogicMBeanConnection.isThisTheAdminServer()) {
								
				ObjectName remoteWLOsbStatsMBean = getConn().getCustomMBean(osbMBeanName);
				if (remoteWLOsbStatsMBean != null) {
										
					for(int index = 0; index < MonitorProperties.OSB_BS_RESSOURCE_TYPE.length; index ++) {
						
						// -----------------------------------------------------------------------------
						try {
							
							// Select the ResourceType
							String resourceType = MonitorProperties.OSB_BS_RESSOURCE_TYPE[index];
							//String serviceFilename = MonitorProperties.OSB_BS_TYPE + "_" + resourceType + "_" + MonitorProperties.OSB_SERVICE_TYPE;
							String serviceFilename = MonitorProperties.OSB_BS_TYPE + "_" + resourceType;
														
							// -----------------------------------------------------------------------------
							// Collect the statistics
							//getConn().invoke(remoteWLOsbStatsMBean, "collectServiceStatistics", new Object [] {getServerName(), MonitorProperties.OSB_BS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});						
							//getConn().invoke(remoteWLOsbStatsMBean, "collectServiceStatistics", new Object [] {null, MonitorProperties.OSB_BS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
							getConn().invoke(remoteWLOsbStatsMBean, "collectServiceStatistics", new Object [] {MonitorProperties.OSB_BS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName()});
							// -----------------------------------------------------------------------------
							
							// -----------------------------------------------------------------------------
							try {
								// Get the list of Service
//AppLog.getLogger().notice("Collecting information for server [" + getServerName() + "]");
								String[] osbServiceList = (String[])getConn().getObjectAttr(remoteWLOsbStatsMBean, "OsbServiceList");
								if(osbServiceList != null) {
									
									//AppLog.getLogger().notice("---------------------------------------------------------");
									//AppLog.getLogger().notice("List of Service");
									//AppLog.getLogger().notice("---------------");
									
									for(int indexService = 0; indexService < osbServiceList.length; indexService ++) {
										String serviceName = osbServiceList[indexService];									
//artifactServiceList.put(serviceName, now);
										
										// -----------------------------------------------------------------------------
										try {
											// Get the list of ResourceStatistic
											String[] resourceStatisticList = (String[])getConn().invoke(remoteWLOsbStatsMBean, "getOsbResourceStatisticList", new Object [] {serviceName}, new String[]{String.class.getName()});
											if(resourceStatisticList != null) {
												
												//AppLog.getLogger().notice("");
												//AppLog.getLogger().notice("\t---------------------------------------------------------");
												//AppLog.getLogger().notice("\tResourceStatistic for [" + serviceName + "]");
												//AppLog.getLogger().notice("\t---------------------------------------------------------");
												
												for(int indexResourceStatistic = 0; indexResourceStatistic < resourceStatisticList.length; indexResourceStatistic ++) {
													
													String resourceStatisticName = resourceStatisticList[indexResourceStatistic];
//artifactResourceStatisticList.put(resourceStatisticName, now);
													
													// -----------------------------------------------------------------------------
													try {
														// Get the list of Statistic
														String[] statisticList = (String[])getConn().invoke(remoteWLOsbStatsMBean, "getOsbStatisticList", new Object [] {serviceName, resourceStatisticName}, new String[]{String.class.getName(), String.class.getName()});
														if(statisticList != null) {
															
															//AppLog.getLogger().notice("");
															//AppLog.getLogger().notice("\t\t---------------------------------------------------------");
															//AppLog.getLogger().notice("\t\tStatistic for [" + serviceName + "/" + resourceStatisticName + "]");
															//AppLog.getLogger().notice("\t\t---------------------------------------------------------");
															
															// Collect the list of elements
															for(int indexStatistic = 0; indexStatistic < statisticList.length; indexStatistic ++) {
																String statisticName = statisticList[indexStatistic];
																//artifactStatisticList.put(statisticName, now);
																headerLineList.put(statisticName, now);
															}
															
															// Generate the properties file containing the list of element
															String name = serviceName + "_" + resourceStatisticName;
															artifactList.put(name, now);
															
//String statisticFilename = MonitorProperties.OSB_BS_TYPE + "_" + resourceType + "_" + MonitorProperties.OSB_SERVICE_TYPE + "_" + serviceName + "_" + MonitorProperties.OSB_RESOURCE_STATISTIC_TYPE + "_" + resourceStatisticName + "_" + MonitorProperties.OSB_STATISTIC_TYPE;
//getCSVStats().appendSavedOneDayResourceNameList(nowDate, statisticFilename, artifactStatisticList);
															
															// Generate the header line from the properties file containing all the properties's name
/*
Set<Object> objects = artifactStatisticList.keySet();
String[] osbAttributeList = new String[objects.size()];
osbAttributeList = (String[])artifactStatisticList.keySet().toArray(osbAttributeList);
String headerLine = constructHeaderLine(osbAttributeList);

// Reset the list
artifactStatisticList = new Properties();
*/
															
															Set<Object> objects = headerLineList.keySet();
															String[] osbAttributeList = new String[objects.size()];
															osbAttributeList = (String[])headerLineList.keySet().toArray(osbAttributeList);
															String headerLine = constructHeaderLine(osbAttributeList);
															
															// Reset the list
															headerLineList = new Properties();
															
															// Get the statistics
															String contentLine = constructOsbStatsLine(remoteWLOsbStatsMBean, serviceName, resourceStatisticName, statisticList);
															
															// Save the statistics into the specific file
															getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), serviceFilename, serviceName, headerLine, contentLine);
															
															//AppLog.getLogger().notice("Adding the statistic line to the file [" + serviceFilename + "] for the service [" + serviceName + "]");

															//AppLog.getLogger().notice("\t\t---------------------------------------------------------");
															//AppLog.getLogger().notice("");
														}
													} catch(Exception ex) {
														AppLog.getLogger().error("Couldn't find the method [getOsbStatisticList] in OSB extention for DomainHealth application");
														AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
													}
													// -----------------------------------------------------------------------------
												}
												
// Generate the properties file for the ResourceStatistic
//String resourceStatisticFilename = MonitorProperties.OSB_BS_TYPE + "_" + resourceType + "_" + MonitorProperties.OSB_SERVICE_TYPE + "_" + serviceName + "_" + MonitorProperties.OSB_RESOURCE_STATISTIC_TYPE;
//getCSVStats().appendSavedOneDayResourceNameList(nowDate, resourceStatisticFilename, artifactResourceStatisticList);
//artifactResourceStatisticList = new Properties();
												
												//AppLog.getLogger().notice("\t---------------------------------------------------------");
												//AppLog.getLogger().notice("");
											}
										} catch(Exception ex) {
											AppLog.getLogger().error("Couldn't find the method [getOsbResourceStatisticList] in OSB extention for DomainHealth application");
											AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
										}
										// -----------------------------------------------------------------------------
									}
									
									// Persist the list of element into properties (for each king of OSB resource)
									getCSVStats().appendSavedOneDayResourceNameList(nowDate, serviceFilename, artifactList);
									
									// Reset the list
									artifactList = new Properties();
									
									//AppLog.getLogger().notice("---------------------------------------------------------");
									//AppLog.getLogger().notice("");
								}
							} catch(Exception ex) {
								AppLog.getLogger().error("Couldn't find the method [getOsbServiceList] in OSB extention for DomainHealth application");
								AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
							}
							// -----------------------------------------------------------------------------
	
						} catch(Exception ex) {
							AppLog.getLogger().error("Couldn't find the method [collectServiceStatistics] in OSB extention for DomainHealth application");
							AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
						}
						// -----------------------------------------------------------------------------
					}

/*
// BusinessServices elements
statistics = (Map<String, Map<String, String>>)getConn().invoke(remoteWLOsbStatsMBean, "getStatsForServices", new Object [] {MonitorProperties.OSB_BS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE}, new String[]{String.class.getName(), String.class.getName()});
//AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/SERVICE - Server is [" + getServerName() + "]");
AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/SERVICE");

statistics = (Map<String, Map<String, String>>)getConn().invoke(remoteWLOsbStatsMBean, "getStatsForServices", new Object [] {MonitorProperties.OSB_BS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION}, new String[]{String.class.getName(), String.class.getName()});
//AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/WEBSERVICE_OPERATION - Server is [" + getServerName() + "]");
AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/WEBSERVICE_OPERATION");

statistics = (Map<String, Map<String, String>>)getConn().invoke(remoteWLOsbStatsMBean, "getStatsForServices", new Object [] {MonitorProperties.OSB_BS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_URI}, new String[]{String.class.getName(), String.class.getName()});
//AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/URI - Server is [" + getServerName() + "]");
AppLog.getLogger().notice("Found [" + statistics.size() + "] statistics for the BusinessServices/URI");
*/

				}
			}
		} catch (Exception ex) {
			throw new DataRetrievalException("Problem logging " + MonitorProperties.OSB_BS_TYPE + " resources for server [" + getServerName() + "]", ex);
		}
	}
	
	/**
	 * Implements the abstract method to log WLSoaBpm optional mbean stats to a CSV file.
	 * 
	 * @throws DataRetrievalException Indicates problem occurred in trying to obtain and persist the server's statistics
	 */
	protected void logSoaBpmStats() throws DataRetrievalException {
				
//@TODO
		
		/*
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String headerLine = constructHeaderLine(SOA_BPM_MBEAN_MONITOR_ATTR_LIST);			
			String soaBpmMBeanName = String.format(SOA_BPM_MBEAN_FULLNAME_TEMPLATE, getServerName());
			ObjectName remoteWLSoaBpmStatsMBean = getConn().getCustomMBean(soaBpmMBeanName);
			
			if (remoteWLSoaBpmStatsMBean != null) {
				String name = ResourceNameNormaliser.normalise(SOA_BPM_RESOURCE_TYPE, SOA_BPM_MBEAN_NAME);
				String contentLine = constructStatsLine(remoteWLSoaBpmStatsMBean, SOA_BPM_MBEAN_MONITOR_ATTR_LIST);
				getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), SOA_BPM_RESOURCE_TYPE, name, headerLine, contentLine);
				artifactList.put(name, now);
			} 

			getCSVStats().appendSavedOneDayResourceNameList(nowDate, SOA_BPM_RESOURCE_TYPE, artifactList);			
		} catch (Exception e) {
			throw new DataRetrievalException("Problem logging " + SOA_BPM_RESOURCE_TYPE + " resources for server " + getServerName(), e);
		}
		*/
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
	 * @param attrList List of attributes*
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
	 * Add to a partial line of statistics to go in a CSV file, by querying an MBean object's specific attributes from a list of given attribute names.
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
	
	// ---------------------------------------------------------------------
	// Added by gregoan
	/**
	 * Construct a single line of statistics to go in a CSV file, by querying 
	 * an MBean object's specific attributes from a list of given attribute 
	 * names.
	 * 
	 * @param objectName MBean object name to query the statistics from
	 * @param argList List of arguments
	 * @param argTypeList List of argument's type
	 * @return The new statistics text line
	 * @throws WebLogicMBeanException Indicates problem occurred retrieving MBean properties
	 */
	private String constructOsbStatsLine(ObjectName objectName, Object serviceName, Object resourceStatisticName, Object[] statisticNameList) throws WebLogicMBeanException {
		
		StringBuilder line = new StringBuilder(DEFAULT_CONTENT_LINE_LEN);
		line.append(formatSeconsdDateTime(new Date()) + SEPARATOR);
		appendToOsbStatsLine(line, objectName, serviceName, resourceStatisticName, statisticNameList);
		return line.toString();
	}
	// ---------------------------------------------------------------------	
	
	// ---------------------------------------------------------------------
	// Added by gregoan
	/**
	 * Add to a partial line of statistics to go in a CSV file, by querying an MBean object's specific attributes from a list of given attribute names.
	 * 
	 * @param line The line of text to add statistics info to
	 * @param objectName MBean object name to query the statistics from
	 * @param serviceName Service's name
	 * @param resourceStatisticName ResourceStatistic's name
	 * @param statisticNameList List of statistic to retrieve
	 * @throws WebLogicMBeanException Indicates problem occurred retrieving MBean properties
	 */
	private void appendToOsbStatsLine(StringBuilder line, ObjectName objectName, Object serviceName, Object resourceStatisticName, Object[] statisticNameList) throws WebLogicMBeanException {
		
		// Define the the type of arguments for "invoke"
		String[] argType = new String[]{String.class.getName(), String.class.getName(), String.class.getName()};
		
		// Check the input parameters
		if(objectName != null && serviceName != null && resourceStatisticName != null && statisticNameList != null && statisticNameList.length > 0) {
			
			for(int index = 0; index < statisticNameList.length; index ++) {
				
				Object statisticName = statisticNameList[index];
				Object[] arg = new Object [] {serviceName, resourceStatisticName, statisticName};
				
				String result = getConn().invoke(objectName, WebLogicMBeanPropConstants.OSB_STATISTIC_ATTRIBUTE_NAME, arg, argType).toString();					
				line.append(result).append(SEPARATOR);
					
//AppLog.getLogger().debug("appendToStatsLine method - Calling the method [" + WebLogicMBeanPropConstants.OSB_STATISTIC_ATTRIBUTE_NAME + "] with arguments and got the result [" + result + "]");

			}
		} else {
			
			// Simple method definition without any arguments
			String result = getConn().invoke(objectName, WebLogicMBeanPropConstants.OSB_STATISTIC_ATTRIBUTE_NAME, null, null).toString();
			line.append(result).append(SEPARATOR);
		}
	}
	// ---------------------------------------------------------------------
}