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
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.OSB_COLLECT_SERVICE_STATISTIC_METHOD;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.OSB_GET_OSB_RESOURCE_STATISTIC_LIST_METHOD;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.OSB_GET_SERVICE_STATISTIC_METHOD;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.OSB_GET_STATISTIC_LIST_METHOD;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.OSB_SERVICE_LIST_ATTRIBUTE;
import static domainhealth.core.statistics.MonitorProperties.JTA_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.OSB_MBEAN_FULLNAME_TEMPLATE;
import static domainhealth.core.statistics.MonitorProperties.SERVER_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.MonitorProperties.THREADPOOL_MBEAN_MONITOR_ATTR_LIST;
import static domainhealth.core.statistics.StatisticsStorage.SEPARATOR;
import static domainhealth.core.util.DateUtil.DATETIME_PARAM_FORMAT;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.WebLogicMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.jmx.WebLogicMBeanPropConstants;
import domainhealth.core.statistics.MonitorProperties;
import domainhealth.core.statistics.ResourceNameNormaliser;
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
	/*
	protected ObjectName getDefaultWorkManager() throws WebLogicMBeanException {
		
		for (ObjectName wkMgr : getConn().getChildren(getServerRuntime(), WORK_MANAGER_RUNTIMES)) {
			if (getConn().getTextAttr(wkMgr, NAME).equals(DEFAULT_WKMGR_NAME)) {
				return wkMgr;
			}
		}
		return null;
	}
	*/
	
	/**
	 * @param statistics
	 * @return
	 */
	//private Set<String> getOsbServiceList(Map<String, Map<String, Map<String, Double>>> statistics) {
	private String[] getOsbServiceList(Map<String, Map<String, Map<String, Double>>> statistics) {

		if(statistics != null && statistics.size() > 0) {
			String[] toReturn = new String[statistics.size()];
			statistics.keySet().toArray(toReturn);
			return toReturn;
			
			//return statistics.keySet().toArray(toReturn);
		}
		return null;
	}
	
	/**
	 * 
	 * @param statistics
	 * @param serviceName
	 * @return
	 */
	//private Set<String> getOsbResourceStatisticList(Map<String, Map<String, Map<String, Double>>> statistics, String serviceName) {
	private String[] getOsbResourceStatisticList(Map<String, Map<String, Map<String, Double>>> statistics, String serviceName) {
		
		if(statistics != null && statistics.size() > 0) {
			
			String[] toReturn = new String[statistics.get(serviceName).size()];
			statistics.get(serviceName).keySet().toArray(toReturn);
			return toReturn;
			
			//return statistics.get(serviceName).keySet();
		}
		return null;
	}
	
	/**
	 * 
	 * @param statistics
	 * @param serviceName
	 * @param resourceStatisticName
	 * @return
	 */
	//private Set<String> getOsbStatisticList(Map<String, Map<String, Map<String, Double>>> statistics, String serviceName, String resourceStatisticName) {
	private String[] getOsbStatisticList(Map<String, Map<String, Map<String, Double>>> statistics, String serviceName, String resourceStatisticName) {
		
		if(statistics != null && statistics.size() > 0) {	
			
			String[] toReturn = new String[statistics.get(serviceName).get(resourceStatisticName).size()];
			statistics.get(serviceName).get(resourceStatisticName).keySet().toArray(toReturn);
			return toReturn;
			
			//return statistics.get(serviceName).get(resourceStatisticName).keySet();
		}
		return null;
	}
	
	/**
	 * 
	 * @param statistics
	 * @param serviceName
	 * @param resourceStatisticName
	 * @param statisticName
	 * @return
	 */
	private double getValueForOsbStatistic(Map<String, Map<String, Map<String, Double>>> statistics, String serviceName, String resourceStatisticName, String statisticName) {
		
		if(statistics != null) {
			
			try {
				return statistics.get(serviceName).get(resourceStatisticName).get(statisticName);
			} catch (Exception ex) {
				//AppLog.getLogger().error("Not possible to get the value of the statistic [" + serviceName + "/" + resourceStatisticName + "/" + statisticName + "]");
			}
		} else {
			AppLog.getLogger().error("Not possible to extract statictic value - The statistic object is null");
		}
		return 0;
	}
	
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
	private void printOsbStatistic(Map<String, Map<String, Map<String, Double>>> globalStatistics) {

		try {
			if(globalStatistics != null && globalStatistics.size() > 0) {
				
				Iterator<String> serviceKeys = globalStatistics.keySet().iterator();
				while(serviceKeys.hasNext()) {
					
					String serviceName = serviceKeys.next();
					
					AppLog.getLogger().notice("------------------------------------------------------------------");
					AppLog.getLogger().notice("ServiceName [" + serviceName + "]");
					AppLog.getLogger().notice("--------------------------------------------");
					
					Map<String, Map<String, Double>> services = globalStatistics.get(serviceName);
					Iterator<String> resourceStatisticKeys = services.keySet().iterator();
					while(resourceStatisticKeys.hasNext()) {
						
						String resourceStatisticName = resourceStatisticKeys.next();
						Map<String, Double> statistics = services.get(resourceStatisticName);
						
						AppLog.getLogger().notice("-- ResourceStatisticName  [" + resourceStatisticName + "]");
						
						Iterator<String> statisticsKeys = statistics.keySet().iterator();
						while(statisticsKeys.hasNext()) {
							
							String statisticName = statisticsKeys.next();
							Double statisticValue = statistics.get(statisticName);
							
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
	 * @param tabularStatistics
	 * @return
	 */
	private Map<String, Map<String, Map<String, Double>>> convertTabularData2Map(TabularDataSupport tabularStatistics) {
		
		Map<String, Map<String, Map<String, Double>>> globalStatistics = new LinkedHashMap<>();
		
		if(tabularStatistics != null) {
		
			for (Object tabularDataRow : tabularStatistics.values()) {
	
				final CompositeData compositeData = (CompositeData) tabularDataRow;
				
				Object[] valueServices = compositeData.values().toArray();
				
				// First element is the Service name
				String serviceName = (String)valueServices[0];
				
				// Second element is the Service value
				TabularDataSupport tabDataService = (TabularDataSupport)valueServices[1];
				
				Map<String, Map<String, Double>> services = new LinkedHashMap<>();
				for (Object serviceData : tabDataService.values()) {
					final CompositeData compositeDataService = (CompositeData) serviceData;
					
					Object[] valuesStatistic = compositeDataService.values().toArray();
					
					// First element is the ResourceStatistic name
					String resourceStatisticName = (String)valuesStatistic[0];
					
					// Second element is the ResourceStatistic value
					TabularDataSupport tabStatisticsData = (TabularDataSupport)valuesStatistic[1];
					Map<String, Double> statistics = new LinkedHashMap<>();
					
					for (Object statisticData : tabStatisticsData.values()) {
						final CompositeData compositeDataStatistic = (CompositeData) statisticData;
						
						Object[] values = compositeDataStatistic.values().toArray();
						
						// First element is the Statistic name
						String statisticName = (String)values[0];
						
						// Second element is the Statistic value
						Double statisticValue = (Double)values[1];
																
						statistics.put(statisticName, statisticValue);										
					}
					services.put(resourceStatisticName, statistics);
				}				
				globalStatistics.put(serviceName, services);
			}
		}		
		return globalStatistics;
		
		/*
		Map<String, Map<String, Map<String, Double>>> statistics = new LinkedHashMap<>();
		Map<String, Map<String, Double>> services = new LinkedHashMap<>();
		Map<String, Double> statisticValues = new LinkedHashMap<>();
		
		if(tabularStatistics != null) {
		
			for (Object tabularDataRow : tabularStatistics.values()) {
	
				final CompositeData compositeData = (CompositeData) tabularDataRow;
				String serviceName = "";
				for (Object valueService : compositeData.values())  {
					
					if(valueService instanceof String ) {
						
						serviceName = (String)valueService;
						//AppLog.getLogger().notice("ServiceName is [" + valueService + "]");
						
					} else if(valueService instanceof TabularDataSupport) {
						
						TabularDataSupport tabDataService = (TabularDataSupport)valueService;
						for (Object serviceData : tabDataService.values()) {
							final CompositeData compositeDataService = (CompositeData) serviceData;
							
							String resourceStatisticName = "";
							for (Object valueStatistic : compositeDataService.values()) {
								
								if(valueStatistic instanceof String ) {
									
									resourceStatisticName = (String) valueStatistic;
									//AppLog.getLogger().notice("  ResourceStatisticName is [" + resourceStatisticName + "]");
									
							    } else if(valueStatistic instanceof TabularDataSupport) {
							    	
							    	TabularDataSupport tabDataStatistic = (TabularDataSupport)valueStatistic;
									for (Object statisticData : tabDataStatistic.values()) {
										final CompositeData compositeDataStatistic = (CompositeData) statisticData;
																				
										String statisticName = "";
										Double statisticValue = new Double(0);
										
										for (Object valueInIn : compositeDataStatistic.values()) {
											
											if(valueInIn instanceof String ) {
												statisticName = (String)valueInIn;
											} else if(valueInIn instanceof Double){
												statisticValue = (Double)valueInIn;
											}
										}
										
										//AppLog.getLogger().notice("    Statistic name/value is [" + statisticName + "/" + statisticValue + "]");
										statisticValues.put(statisticName, statisticValue);										
									}
							    }
							}
							
							services.put(resourceStatisticName, statisticValues);
							statisticValues = new LinkedHashMap<>();
						}
					}
				}
				
				statistics.put(serviceName, services);
				services = new LinkedHashMap<>();
			}
		}
		return statistics;
		*/
	}

	/**
	 * 
	 * @throws DataRetrievalException
	 */
	protected void logOsbProxyStats() throws DataRetrievalException {
		
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String osbMBeanName = String.format(OSB_MBEAN_FULLNAME_TEMPLATE, getServerName());
			
			/*
			If the serverName is set, the statistic will be retrieved for it but OSB MBean should be deployed on it
			If the serverName is null, then the statistic will be retrieved for the cluster
			*/
			
			// If this check is made the serverNamer shouldn't be specified (-> null for cluster processing)
			//if(WebLogicMBeanConnection.isThisTheAdminServer()) {
				
				ObjectName remoteWLOsbStatsMBean = getConn().getCustomMBean(osbMBeanName);
				if (remoteWLOsbStatsMBean != null) {
										
					for(int index = 0; index < MonitorProperties.OSB_PS_RESSOURCE_TYPE.length; index ++) {
						
						// -----------------------------------------------------------------------------
						try {
							
							// Select the ResourceType
							String resourceType = MonitorProperties.OSB_PS_RESSOURCE_TYPE[index];
							String serviceFilename = MonitorProperties.OSB_PS_TYPE + "_" + resourceType;
					
							// -----------------------------------------------------------------------------
							// Get the statistics
							TabularDataSupport tabularStatistics = (TabularDataSupport)getConn().invoke(remoteWLOsbStatsMBean, OSB_GET_SERVICE_STATISTIC_METHOD, new Object [] {getServerName(), MonitorProperties.OSB_PS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
							
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {getServerName(), MonitorProperties.OSB_PS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});						
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {null, MonitorProperties.OSB_PS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {MonitorProperties.OSB_PS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName()});
							// -----------------------------------------------------------------------------
							
							if(tabularStatistics != null) {
																
								Map<String, Map<String, Map<String, Double>>> statisticsMap = convertTabularData2Map(tabularStatistics);
								
								// -----------------------------------------------------------------------------
								try {
									// Get the list of Service
									String[] osbServiceList = getOsbServiceList(statisticsMap);
									if(osbServiceList != null) {
																			
										for(int indexService = 0; indexService < osbServiceList.length; indexService ++) {
											String serviceName = osbServiceList[indexService];									
											
											// -----------------------------------------------------------------------------
											try {
												// Get the list of ResourceStatistic
												String[] resourceStatisticList = getOsbResourceStatisticList(statisticsMap, serviceName);
												if(resourceStatisticList != null) {
													
													for(int indexResourceStatistic = 0; indexResourceStatistic < resourceStatisticList.length; indexResourceStatistic ++) {
														
														String resourceStatisticName = resourceStatisticList[indexResourceStatistic];
														
														// -----------------------------------------------------------------------------
														try {
															// Get the list of Statistic
															String[] statisticList = getOsbStatisticList(statisticsMap, serviceName, resourceStatisticName);
															if(statisticList != null) {
																														
																// Generate the properties file containing the list of element
																String name = ResourceNameNormaliser.normalise(resourceType, serviceName) + "_" + ResourceNameNormaliser.normalise(resourceType, resourceStatisticName);
																
																// Add the element to the list
																artifactList.put(name, now);
																
																String[] headerElements = new String[0];
																if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_SERVICE)) {
																	
																	headerElements = MonitorProperties.OSB_RESOURCE_TYPE_SERVICE_PS_ATTR_LIST;
																	
																} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION)) {
																	
																	headerElements = MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION_ATTR_LIST;
																	
																} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_URI)) {
																	
																	headerElements = MonitorProperties.OSB_RESOURCE_TYPE_URI_ATTR_LIST;
																	
																} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT)) {
																	
																	headerElements = MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT_ATTR_LIST;
																}
																
																String headerLine = constructHeaderLine(headerElements);
																String contentLine = constructOsbStatsLine(remoteWLOsbStatsMBean, serviceName, resourceStatisticName, headerElements);
																getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), serviceFilename, name, headerLine, contentLine);														
															}
														} catch(Exception ex) {
															AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_STATISTIC_LIST_METHOD + "] in OSB extension for DomainHealth application");
															AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
														}
														// -----------------------------------------------------------------------------
													}
												}
											} catch(Exception ex) {
												AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_OSB_RESOURCE_STATISTIC_LIST_METHOD + "] in OSB extension for DomainHealth application");
												AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
											}
											// -----------------------------------------------------------------------------
										}
										
										// Persist the list of element into properties (for each king of OSB resource)
										getCSVStats().appendSavedOneDayResourceNameList(nowDate, serviceFilename, artifactList);
										
										// Reset the list
										artifactList = new Properties();
									}
								} catch(Exception ex) {
									AppLog.getLogger().error("Couldn't execute the method [" + OSB_SERVICE_LIST_ATTRIBUTE + "] in OSB extension for DomainHealth application");
									AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
								}
								// -----------------------------------------------------------------------------
							}
						} catch(Exception ex) {
							AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_SERVICE_STATISTIC_METHOD + "] in OSB extension for DomainHealth application");
							AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
						}
					}
				}
			// IF ADMIN SERVER
			//}
		} catch (Exception ex) {
			throw new DataRetrievalException("Problem logging " + MonitorProperties.OSB_PS_TYPE + " resources", ex);
		}
	}
	
	/**
	 * 
	 * @throws DataRetrievalException
	 */
	protected void logOsbProxyStatsOld() throws DataRetrievalException {
		
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String osbMBeanName = String.format(OSB_MBEAN_FULLNAME_TEMPLATE, getServerName());
			
			/*
			If the serverName is set, the statistic will be retrieved for it but OSB MBean should be deployed on it
			If the serverName is null, then the statistic will be retrieved for the cluster
			*/
			
			// If this check is made the serverNamer shouldn't be specified (-> null for cluster processing)
			//if(WebLogicMBeanConnection.isThisTheAdminServer()) {
				
				ObjectName remoteWLOsbStatsMBean = getConn().getCustomMBean(osbMBeanName);
				if (remoteWLOsbStatsMBean != null) {
										
					for(int index = 0; index < MonitorProperties.OSB_PS_RESSOURCE_TYPE.length; index ++) {
						
						// -----------------------------------------------------------------------------
						try {
							
							// Select the ResourceType
							String resourceType = MonitorProperties.OSB_PS_RESSOURCE_TYPE[index];
							String serviceFilename = MonitorProperties.OSB_PS_TYPE + "_" + resourceType;
														
							// -----------------------------------------------------------------------------
							// Collect the statistics
							getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {getServerName(), MonitorProperties.OSB_PS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});						
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {null, MonitorProperties.OSB_PS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {MonitorProperties.OSB_PS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName()});
							// -----------------------------------------------------------------------------
							
							// -----------------------------------------------------------------------------
							try {
								// Get the list of Service
								String[] osbServiceList = (String[])getConn().getObjectAttr(remoteWLOsbStatsMBean, OSB_SERVICE_LIST_ATTRIBUTE);
								if(osbServiceList != null) {
																		
									for(int indexService = 0; indexService < osbServiceList.length; indexService ++) {
										String serviceName = osbServiceList[indexService];									
										
										// -----------------------------------------------------------------------------
										try {
											// Get the list of ResourceStatistic
											String[] resourceStatisticList = (String[])getConn().invoke(remoteWLOsbStatsMBean, OSB_GET_OSB_RESOURCE_STATISTIC_LIST_METHOD, new Object [] {serviceName}, new String[]{String.class.getName()});
											if(resourceStatisticList != null) {
												
												for(int indexResourceStatistic = 0; indexResourceStatistic < resourceStatisticList.length; indexResourceStatistic ++) {
													
													String resourceStatisticName = resourceStatisticList[indexResourceStatistic];
													
													// -----------------------------------------------------------------------------
													try {
														// Get the list of Statistic
														String[] statisticList = (String[])getConn().invoke(remoteWLOsbStatsMBean, OSB_GET_STATISTIC_LIST_METHOD, new Object [] {serviceName, resourceStatisticName}, new String[]{String.class.getName(), String.class.getName()});
														if(statisticList != null) {
															
															/*
															// Collect the list of elements
															for(int indexStatistic = 0; indexStatistic < statisticList.length; indexStatistic ++) {
																String statisticName = statisticList[indexStatistic];																
																headerLineList.put(statisticName, now);
															}
															*/
															
															// Generate the properties file containing the list of element
															String name = ResourceNameNormaliser.normalise(resourceType, serviceName) + "_" + ResourceNameNormaliser.normalise(resourceType, resourceStatisticName);
															
															// Add the element to the list
															artifactList.put(name, now);
															
															/*
															Set<Object> objects = headerLineList.keySet();
															String[] osbAttributeList = new String[objects.size()];
															osbAttributeList = (String[])headerLineList.keySet().toArray(osbAttributeList);
															String headerLine = constructHeaderLine(osbAttributeList);
															*/
															
															String[] headerElements = new String[0];
															if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_SERVICE)) {
																
																headerElements = MonitorProperties.OSB_RESOURCE_TYPE_SERVICE_PS_ATTR_LIST;
																
															} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION)) {
																
																headerElements = MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION_ATTR_LIST;
																
															} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_URI)) {
																
																headerElements = MonitorProperties.OSB_RESOURCE_TYPE_URI_ATTR_LIST;
																
															} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT)) {
																
																headerElements = MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT_ATTR_LIST;
															}
															
															String headerLine = constructHeaderLine(headerElements);
															String contentLine = constructOsbStatsLine(remoteWLOsbStatsMBean, serviceName, resourceStatisticName, headerElements);
															getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), serviceFilename, name, headerLine, contentLine);
															
//															// Reset the list, get the statistics and save them into a file
//															headerLineList = new Properties();
//															String contentLine = constructOsbStatsLine(remoteWLOsbStatsMBean, serviceName, resourceStatisticName, statisticList);
//															getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), serviceFilename, name, headerLine, contentLine);
														}
													} catch(Exception ex) {
														AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_STATISTIC_LIST_METHOD + "] in OSB extension for DomainHealth application");
														AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
													}
													// -----------------------------------------------------------------------------
												}
											}
										} catch(Exception ex) {
											AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_OSB_RESOURCE_STATISTIC_LIST_METHOD + "] in OSB extension for DomainHealth application");
											AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
										}
										// -----------------------------------------------------------------------------
									}
									
									// Persist the list of element into properties (for each king of OSB resource)
									getCSVStats().appendSavedOneDayResourceNameList(nowDate, serviceFilename, artifactList);
									
									// Reset the list
									artifactList = new Properties();
								}
							} catch(Exception ex) {
								AppLog.getLogger().error("Couldn't execute the method [" + OSB_SERVICE_LIST_ATTRIBUTE + "] in OSB extension for DomainHealth application");
								AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
							}
							// -----------------------------------------------------------------------------
	
						} catch(Exception ex) {
							AppLog.getLogger().error("Couldn't execute the method [" + OSB_COLLECT_SERVICE_STATISTIC_METHOD + "] in OSB extension for DomainHealth application");
							AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
						}
						// -----------------------------------------------------------------------------
					}
				}
			//}
		} catch (Exception ex) {
			throw new DataRetrievalException("Problem logging " + MonitorProperties.OSB_PS_TYPE + " resources for server [" + getServerName() + "]", ex);
		}
	}
	
	/**
	 * 
	 * @throws DataRetrievalException
	 */
	protected void logOsbBusinessStats() throws DataRetrievalException {
		
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String osbMBeanName = String.format(OSB_MBEAN_FULLNAME_TEMPLATE, getServerName());
			
			/*
			If the serverName is set, the statistic will be retrieved for it but OSB MBean should be deployed on it
			If the serverName is null, then the statistic will be retrieved for the cluster
			*/
			
			// If this check is made the serverNamer shouldn't be specified (-> null for cluster processing)
			//if(WebLogicMBeanConnection.isThisTheAdminServer()) {
				
				ObjectName remoteWLOsbStatsMBean = getConn().getCustomMBean(osbMBeanName);
				if (remoteWLOsbStatsMBean != null) {
										
					for(int index = 0; index < MonitorProperties.OSB_BS_RESSOURCE_TYPE.length; index ++) {
						
						// -----------------------------------------------------------------------------
						try {
							
							// Select the ResourceType
							String resourceType = MonitorProperties.OSB_BS_RESSOURCE_TYPE[index];
							String serviceFilename = MonitorProperties.OSB_BS_TYPE + "_" + resourceType;
					
							// -----------------------------------------------------------------------------
							// Get the statistics
							TabularDataSupport tabularStatistics = (TabularDataSupport)getConn().invoke(remoteWLOsbStatsMBean, OSB_GET_SERVICE_STATISTIC_METHOD, new Object [] {getServerName(), MonitorProperties.OSB_BS_TYPE, MonitorProperties.OSB_RESOURCE_TYPE_SERVICE}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
							
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {getServerName(), MonitorProperties.OSB_BS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});						
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {null, MonitorProperties.OSB_BS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {MonitorProperties.OSB_BS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName()});
							// -----------------------------------------------------------------------------
							
							if(tabularStatistics != null) {
																
								Map<String, Map<String, Map<String, Double>>> statisticsMap = convertTabularData2Map(tabularStatistics);
								
								// -----------------------------------------------------------------------------
								try {
									// Get the list of Service
									String[] osbServiceList = getOsbServiceList(statisticsMap);
									if(osbServiceList != null) {
																			
										for(int indexService = 0; indexService < osbServiceList.length; indexService ++) {
											String serviceName = osbServiceList[indexService];									
											
											// -----------------------------------------------------------------------------
											try {
												// Get the list of ResourceStatistic
												String[] resourceStatisticList = getOsbResourceStatisticList(statisticsMap, serviceName);
												if(resourceStatisticList != null) {
													
													for(int indexResourceStatistic = 0; indexResourceStatistic < resourceStatisticList.length; indexResourceStatistic ++) {
														
														String resourceStatisticName = resourceStatisticList[indexResourceStatistic];
														
														// -----------------------------------------------------------------------------
														try {
															// Get the list of Statistic
															String[] statisticList = getOsbStatisticList(statisticsMap, serviceName, resourceStatisticName);
															if(statisticList != null) {
																														
																// Generate the properties file containing the list of element
																String name = ResourceNameNormaliser.normalise(resourceType, serviceName) + "_" + ResourceNameNormaliser.normalise(resourceType, resourceStatisticName);
																
																// Add the element to the list
																artifactList.put(name, now);
																
																String[] headerElements = new String[0];
																if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_SERVICE)) {
																	
																	headerElements = MonitorProperties.OSB_RESOURCE_TYPE_SERVICE_BS_ATTR_LIST;
																	
																} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION)) {
																	
																	headerElements = MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION_ATTR_LIST;
																	
																} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_URI)) {
																	
																	headerElements = MonitorProperties.OSB_RESOURCE_TYPE_URI_ATTR_LIST;
																	
																} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT)) {
																	
																	headerElements = MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT_ATTR_LIST;
																}
																
																String headerLine = constructHeaderLine(headerElements);
																String contentLine = constructOsbStatsLine(remoteWLOsbStatsMBean, serviceName, resourceStatisticName, headerElements);
																getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), serviceFilename, name, headerLine, contentLine);														
															}
														} catch(Exception ex) {
															AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_STATISTIC_LIST_METHOD + "] in OSB extension for DomainHealth application");
															AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
														}
														// -----------------------------------------------------------------------------
													}
												}
											} catch(Exception ex) {
												AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_OSB_RESOURCE_STATISTIC_LIST_METHOD + "] in OSB extension for DomainHealth application");
												AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
											}
											// -----------------------------------------------------------------------------
										}
										
										// Persist the list of element into properties (for each king of OSB resource)
										getCSVStats().appendSavedOneDayResourceNameList(nowDate, serviceFilename, artifactList);
										
										// Reset the list
										artifactList = new Properties();
									}
								} catch(Exception ex) {
									AppLog.getLogger().error("Couldn't execute the method [" + OSB_SERVICE_LIST_ATTRIBUTE + "] in OSB extension for DomainHealth application");
									AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
								}
								// -----------------------------------------------------------------------------
							}
						} catch(Exception ex) {
							AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_SERVICE_STATISTIC_METHOD + "] in OSB extension for DomainHealth application");
							AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
						}
					}
				}
			// IF ADMIN SERVER
			//}
		} catch (Exception ex) {
			throw new DataRetrievalException("Problem logging " + MonitorProperties.OSB_BS_TYPE + " resources", ex);
		}
	}
	
	/**
	 * 
	 * @throws DataRetrievalException
	 */
	protected void logOsbBusinessStatsOld() throws DataRetrievalException {
		
		try {
			Date nowDate = new Date();
			String now = (new SimpleDateFormat(DATETIME_PARAM_FORMAT)).format(nowDate);
			Properties artifactList = new Properties();
			String osbMBeanName = String.format(OSB_MBEAN_FULLNAME_TEMPLATE, getServerName());
			
			/*
			If the serverName is set, the statistic will be retrieved for it but OSB MBean should be deployed on it
			If the serverName is null, then the statistic will be retrieved for the cluster
			*/
			
			// If this check is made the serverNamer shouldn't be specified (-> null for cluster processing)
			//if(WebLogicMBeanConnection.isThisTheAdminServer()) {
				
				ObjectName remoteWLOsbStatsMBean = getConn().getCustomMBean(osbMBeanName);
				if (remoteWLOsbStatsMBean != null) {
										
					for(int index = 0; index < MonitorProperties.OSB_BS_RESSOURCE_TYPE.length; index ++) {
						
						// -----------------------------------------------------------------------------
						try {
							
							// Select the ResourceType
							String resourceType = MonitorProperties.OSB_BS_RESSOURCE_TYPE[index];
							String serviceFilename = MonitorProperties.OSB_BS_TYPE + "_" + resourceType;
														
							// -----------------------------------------------------------------------------
							// Collect the statistics
							getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {getServerName(), MonitorProperties.OSB_BS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});						
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {null, MonitorProperties.OSB_BS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});
							//getConn().invoke(remoteWLOsbStatsMBean, OSB_COLLECT_SERVICE_STATISTIC_METHOD, new Object [] {MonitorProperties.OSB_BS_TYPE, resourceType}, new String[]{String.class.getName(), String.class.getName()});
							// -----------------------------------------------------------------------------
							
							// -----------------------------------------------------------------------------
							try {
								// Get the list of Service
								String[] osbServiceList = (String[])getConn().getObjectAttr(remoteWLOsbStatsMBean, OSB_SERVICE_LIST_ATTRIBUTE);
								if(osbServiceList != null) {
																		
									for(int indexService = 0; indexService < osbServiceList.length; indexService ++) {
										String serviceName = osbServiceList[indexService];									
										
										// -----------------------------------------------------------------------------
										try {
											// Get the list of ResourceStatistic
											String[] resourceStatisticList = (String[])getConn().invoke(remoteWLOsbStatsMBean, OSB_GET_OSB_RESOURCE_STATISTIC_LIST_METHOD, new Object [] {serviceName}, new String[]{String.class.getName()});
											if(resourceStatisticList != null) {
												
												for(int indexResourceStatistic = 0; indexResourceStatistic < resourceStatisticList.length; indexResourceStatistic ++) {
													
													String resourceStatisticName = resourceStatisticList[indexResourceStatistic];
													
													// -----------------------------------------------------------------------------
													try {
														// Get the list of Statistic
														String[] statisticList = (String[])getConn().invoke(remoteWLOsbStatsMBean, OSB_GET_STATISTIC_LIST_METHOD, new Object [] {serviceName, resourceStatisticName}, new String[]{String.class.getName(), String.class.getName()});
														if(statisticList != null) {
															
															/*
															// Collect the list of elements
															for(int indexStatistic = 0; indexStatistic < statisticList.length; indexStatistic ++) {
																String statisticName = statisticList[indexStatistic];
																headerLineList.put(statisticName, now);
															}
															*/
															
															// Generate the properties file containing the list of element
															String name = ResourceNameNormaliser.normalise(resourceType, serviceName) + "_" + ResourceNameNormaliser.normalise(resourceType, resourceStatisticName);
															
															// Add the element to the list
															artifactList.put(name, now);
															
															/*
															// Generate the header line from the properties file containing all the properties's name
															Set<Object> objects = headerLineList.keySet();
															String[] osbAttributeList = new String[objects.size()];
															osbAttributeList = (String[])headerLineList.keySet().toArray(osbAttributeList);
															String headerLine = constructHeaderLine(osbAttributeList);
															*/
															
															String[] headerElements = new String[0];
															if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_SERVICE)) {
																
																headerElements = MonitorProperties.OSB_RESOURCE_TYPE_SERVICE_BS_ATTR_LIST;
																
															} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION)) {
																
																headerElements = MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION_ATTR_LIST;
																
															} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_URI)) {
																
																headerElements = MonitorProperties.OSB_RESOURCE_TYPE_URI_ATTR_LIST;
																
															} else if(resourceType.equals(MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT)) {
																
																headerElements = MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT_ATTR_LIST;
															}

															String headerLine = constructHeaderLine(headerElements);
															String contentLine = constructOsbStatsLine(remoteWLOsbStatsMBean, serviceName, resourceStatisticName, headerElements);
															getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), serviceFilename, name, headerLine, contentLine);
																														
															// Reset the list, get the statistics and save them into a file
//															headerLineList = new Properties();
//															String contentLine = constructOsbStatsLine(remoteWLOsbStatsMBean, serviceName, resourceStatisticName, statisticList);
//															getCSVStats().appendToResourceStatisticsCSV(new Date(), getServerName(), serviceFilename, name, headerLine, contentLine);
														}
													} catch(Exception ex) {
														AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_STATISTIC_LIST_METHOD + "] in OSB extension for DomainHealth application");
														AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
													}
													// -----------------------------------------------------------------------------
												}
											}
										} catch(Exception ex) {
											AppLog.getLogger().error("Couldn't execute the method [" + OSB_GET_OSB_RESOURCE_STATISTIC_LIST_METHOD + "] in OSB extension for DomainHealth application");
											AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
										}
										// -----------------------------------------------------------------------------
									}
									
									// Persist the list of element into properties (for each king of OSB resource)
									getCSVStats().appendSavedOneDayResourceNameList(nowDate, serviceFilename, artifactList);
									
									// Reset the list
									artifactList = new Properties();
								}
							} catch(Exception ex) {
								AppLog.getLogger().error("Couldn't execute the method [" + OSB_SERVICE_LIST_ATTRIBUTE + "] in OSB extension for DomainHealth application");
								AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
							}
							// -----------------------------------------------------------------------------
	
						} catch(Exception ex) {
							AppLog.getLogger().error("Couldn't execute the method [" + OSB_COLLECT_SERVICE_STATISTIC_METHOD + "] in OSB extension for DomainHealth application");
							AppLog.getLogger().error("Message is [" + ex.getMessage() + "]");
						}
						// -----------------------------------------------------------------------------
					}
				}
			//}
		} catch (Exception ex) {
			throw new DataRetrievalException("Problem logging " + MonitorProperties.OSB_BS_TYPE + " resources for server [" + getServerName() + "]", ex);
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
	protected String constructOsbStatsLine(ObjectName objectName, Object serviceName, Object resourceStatisticName, Object[] statisticNameList) throws WebLogicMBeanException {
		
		StringBuilder line = new StringBuilder(DEFAULT_HEADER_LINE_LEN);
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
	protected void appendToOsbStatsLine(StringBuilder line, ObjectName objectName, Object serviceName, Object resourceStatisticName, Object[] statisticNameList) throws WebLogicMBeanException {
		
		// Define the the type of arguments for "invoke"
		String[] argType = new String[]{String.class.getName(), String.class.getName(), String.class.getName()};
		
		// Check the input parameters
		if(objectName != null && serviceName != null && resourceStatisticName != null && statisticNameList != null && statisticNameList.length > 0) {
			
			for(int index = 0; index < statisticNameList.length; index ++) {
				
				Object statisticName = statisticNameList[index];
				Object[] arg = new Object [] {serviceName, resourceStatisticName, statisticName};
				
				String result = getConn().invoke(objectName, WebLogicMBeanPropConstants.OSB_GET_VALUE_FOR_STATISTIC_METHOD, arg, argType).toString();					
				line.append(result).append(SEPARATOR);
			}
		} else {
			
			// Simple method definition without any arguments
			String result = getConn().invoke(objectName, WebLogicMBeanPropConstants.OSB_GET_VALUE_FOR_STATISTIC_METHOD, null, null).toString();
			line.append(result).append(SEPARATOR);
		}
	}
	// ---------------------------------------------------------------------
}