package domainhealth.rest;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.ObjectName;

import com.bea.wli.config.Ref;
import com.bea.wli.monitoring.DomainMonitoringDisabledException;
import com.bea.wli.monitoring.InvalidServiceRefException;
import com.bea.wli.monitoring.MonitoringException;
import com.bea.wli.monitoring.MonitoringNotEnabledException;
import com.bea.wli.monitoring.ResourceStatistic;
import com.bea.wli.monitoring.ResourceType;
import com.bea.wli.monitoring.ServiceDomainMBean;
import com.bea.wli.monitoring.ServiceResourceStatistic;
import com.bea.wli.monitoring.StatisticType;
import com.bea.wli.monitoring.StatisticValue;

import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.MonitorProperties;
import domainhealth.core.statistics.ResourceNameNormaliser;

public class OSBServiceTool {
	
	private ServiceDomainMBean serviceDomainMBean = null;

	/**
	 * 
	 * @param type
	 * @return
	 */
    public Set<String> osbDetail(String type) {
    	    	
    	try {
        	
    		DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
        	switch (type) {
            
		    	case MonitorProperties.PROXY_SERVICE_RESOURCE_TYPE:
		    		return getProxyServices(conn);
		    		
		    	case MonitorProperties.BUSINESS_SERVICE_RESOURCE_TYPE:
		    		return getBusinessServices(conn);
		    		
				default:
					return null;
        	}	
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the osbDetail method", ex);
        }
        return null;
    }
    
    /**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
    private Set<ObjectName> getOsbResource(DomainRuntimeServiceMBeanConnection conn) throws Exception {
    	
        Set<ObjectName> osbConfigs = conn.getElementByQueryNames("com.oracle.osb:Type=ResourceConfigurationMBean,*");
        return osbConfigs;
    }
    
    /**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
    private Set<String> getProxyServices(DomainRuntimeServiceMBeanConnection conn) throws Exception {

// -------------------------------------------
AppLog.getLogger().notice("");

initServiceDomainMBean(conn);
Ref[] serviceRefs = serviceDomainMBean.getMonitoredProxyServiceRefs();
if(serviceRefs != null && serviceRefs.length > 0) {
	AppLog.getLogger().notice("Found [" + serviceRefs.length + "] Proxy services");
	
	// Create a bitwise map for desired resource types.
    int typeFlag = 0;
    typeFlag = typeFlag | ResourceType.SERVICE.value();
    typeFlag = typeFlag | ResourceType.FLOW_COMPONENT.value();
    typeFlag = typeFlag | ResourceType.WEBSERVICE_OPERATION.value();
    
    HashMap<Ref, ServiceResourceStatistic> resourcesMap = null;
    
    // Get cluster-level statistics.
    try {
         // Get statistics.
         AppLog.getLogger().notice("Now trying to get statistics for [" + serviceRefs.length + "] proxy services...");
         //resourcesMap = serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, serverName);
         resourcesMap = serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, null);
         
         // Print Statistic
         saveStatisticsToFile(resourcesMap);
    }
    catch (IllegalArgumentException iae) {
         AppLog.getLogger().notice("===============================");
         AppLog.getLogger().notice("Encountered IllegalArgumentException... Details:");
         AppLog.getLogger().notice(iae.getMessage());
         AppLog.getLogger().notice("Check if proxy reference OR bitmap are valid... !!!");
         AppLog.getLogger().notice("==================================");
         throw iae;
    }
    catch (DomainMonitoringDisabledException dmde) {
         // Statistics not available as monitoring is turned off at domain level.
         AppLog.getLogger().notice("==================================");
         AppLog.getLogger().notice("Statistics not available as monitoring is turned off at domain level.");
         AppLog.getLogger().notice("==============================");
         throw dmde;
    }
    catch (MonitoringException me) {
         // Internal problem... May be aggregation server is crashed...
         AppLog.getLogger().notice("================================");
         AppLog.getLogger().notice("ERROR: Statistics is not available... Check if aggregation server is crashed...");
         AppLog.getLogger().notice("=================================");
         throw me;
    }
} else {
	AppLog.getLogger().notice("Didn't find any Proxy services with monitoring enabled - Not possible to collect anything");
}
AppLog.getLogger().notice("");
//-------------------------------------------

		// -------------------------------------------
		AppLog.getLogger().notice("");
    	Set<String> result = new TreeSet<String>();
    	Set<ObjectName> osbConfigs = getOsbResource(conn);
    	    	
        for (ObjectName config : osbConfigs) {
        	
        	//String canonicalName = config.getCanonicalName();        	
        	//AppLog.getLogger().notice("canonicalName is [" + canonicalName + "]");

/*
// -------------------------------------------
try {
	
	ObjectName objectName = new ObjectName(canonicalName);
	String proxyName = conn.getTextAttr(objectName, NAME);
	AppLog.getLogger().notice("proxyName is [" + proxyName + "]");
	
	String resourceType = conn.getTextAttr(objectName, "ResourceType");
	AppLog.getLogger().notice("resourceType is [" + resourceType + "]");
	
	MBeanInfo tempMBean = conn.getMBeanInfo(canonicalName);
	
	if(tempMBean != null) {
		String[] fieldNames = tempMBean.getDescriptor().getFieldNames();
		if(fieldNames != null) {
			for(int index = 0 ; index < fieldNames.length; index ++) {
				AppLog.getLogger().notice("fieldNames[index] is [" + fieldNames[index] + "]");
			}
		}
		
		MBeanOperationInfo[] mbeanOperationInfos = tempMBean.getOperations();
		if(mbeanOperationInfos != null) {
			for(int index = 0; index < mbeanOperationInfos.length; index ++) {
				MBeanOperationInfo mbeanOperationInfo = mbeanOperationInfos[index];
				AppLog.getLogger().notice("mbeanOperationInfo.getName() is [" + mbeanOperationInfo.getName() + "]");
			}
		}
	}
	
} catch (Exception ex) {
}
AppLog.getLogger().notice("");
//-------------------------------------------
*/

/*
			//-------------------------------------------
        	Hashtable<String, String> keyList = config.getKeyPropertyList();
        	Enumeration<String> enumKey = keyList.keys();
        	while(enumKey.hasMoreElements()) {
        		
        		String key = enumKey.nextElement();
        		String value = config.getKeyProperty(key);
        		
        		AppLog.getLogger().notice("   Key [" + key + "] - Value [" + value + "]");        		
        	}
        	AppLog.getLogger().notice("");
        	// -------------------------------------------
*/

			//-------------------------------------------
        	String element = config.getKeyProperty(NAME);
        	AppLog.getLogger().notice("Checking if the element [" + element + "] is a ProxyService");
        	
			// Check if ProxyService
	        if (element.startsWith("ProxyService$")) {
	        	
	        	element = ResourceNameNormaliser.normalise(MonitorProperties.PROXY_SERVICE_RESOURCE_TYPE, element);
	        	AppLog.getLogger().notice("Adding the element [" + element + "]");
	        	result.add(element);
	        }
	        // -------------------------------------------
        }
        return result;
    }
    
    /**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
    private Set<String> getBusinessServices(DomainRuntimeServiceMBeanConnection conn) throws Exception {

// -------------------------------------------
AppLog.getLogger().notice("");

initServiceDomainMBean(conn);
Ref[] serviceRefs = serviceDomainMBean.getMonitoredBusinessServiceRefs();
if(serviceRefs != null && serviceRefs.length > 0) {
	AppLog.getLogger().notice("Found [" + serviceRefs.length + "] Business services");
	
	// Create a bitwise map for desired resource types.
    int typeFlag = 0;
    typeFlag = typeFlag | ResourceType.SERVICE.value();
    typeFlag = typeFlag | ResourceType.WEBSERVICE_OPERATION.value();
    typeFlag = typeFlag | ResourceType.URI.value();
    
    HashMap<Ref, ServiceResourceStatistic> resourcesMap = null;
    
    // Get cluster-level statistics.
    try {
         // Get statistics.
         AppLog.getLogger().notice("Now trying to get statistics for [" + serviceRefs.length + "] business services...");
         //resourcesMap = serviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, serverName);
         resourcesMap = serviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, null);
         
         // Print Statistic
         saveStatisticsToFile(resourcesMap);
    }
    catch (IllegalArgumentException iae) {
         AppLog.getLogger().notice("===============================");
         AppLog.getLogger().notice("Encountered IllegalArgumentException... Details:");
         AppLog.getLogger().notice(iae.getMessage());
         AppLog.getLogger().notice("Check if business reference OR bitmap are valid... !!!");
         AppLog.getLogger().notice("==================================");
         throw iae;
    }
    catch (DomainMonitoringDisabledException dmde) {
         // Statistics not available as monitoring is turned off at domain level.
         AppLog.getLogger().notice("==================================");
         AppLog.getLogger().notice("Statistics not available as monitoring is turned off at domain level.");
         AppLog.getLogger().notice("==============================");
         throw dmde;
    }
    catch (MonitoringException me) {
         // Internal problem... May be aggregation server is crashed...
         AppLog.getLogger().notice("================================");
         AppLog.getLogger().notice("ERROR: Statistics is not available... Check if aggregation server is crashed...");
         AppLog.getLogger().notice("=================================");
         throw me;
    }
} else {
	AppLog.getLogger().notice("Didn't find any Business services with monitoring enabled - Not possible to collect anything");
}
AppLog.getLogger().notice("");
//-------------------------------------------
    	
    	// -------------------------------------------
    	AppLog.getLogger().notice("");

    	Set<String> result = new TreeSet<String>();
    	Set<ObjectName> osbConfigs = getOsbResource(conn);
    	    	
        for (ObjectName config : osbConfigs) {
        	
        	String element = config.getKeyProperty(NAME);
        	AppLog.getLogger().notice("Checking if the the element [" + element + "] is a BusinessService");
        	
			// Check if BusinessService
	        if (element.startsWith("BusinessService$")) {
	        	
	        	element = ResourceNameNormaliser.normalise(MonitorProperties.BUSINESS_SERVICE_RESOURCE_TYPE, element);
	        	AppLog.getLogger().notice("Adding the element [" + element + "]");
	        	result.add(element);
	        }
        }
        return result;
        // -------------------------------------------
    }
    
    /**
     * 
     * @param statsMap
     * @throws Exception
     */
    private void saveStatisticsToFile(HashMap<Ref, ServiceResourceStatistic> statsMap) throws Exception {
         
    	if (statsMap == null) {
    		AppLog.getLogger().notice("******************************************************");
    		AppLog.getLogger().notice("ServiceResourceStatistics is null... Nothing to report");
    		AppLog.getLogger().notice("******************************************************");
    		return;
         }
         if (statsMap.size() == 0) {
        	 AppLog.getLogger().notice("*******************************************************");
        	 AppLog.getLogger().notice("ServiceResourceStatistics is empty... Nothing to report");
        	 AppLog.getLogger().notice("*******************************************************");
        	 return;
         }

         Set<Map.Entry<Ref, ServiceResourceStatistic>> set = statsMap.entrySet();
         
         // Print statistical information of each service
         for (Map.Entry<Ref, ServiceResourceStatistic> mapEntry : set) {
        	
        	AppLog.getLogger().notice("");
            AppLog.getLogger().notice("======= Printing statistics for service [" + mapEntry.getKey().getFullName() + "] =======");
			
			ServiceResourceStatistic serviceStats = mapEntry.getValue();

              ResourceStatistic[] resStatsArray = null;
              try {
                   resStatsArray = serviceStats.getAllResourceStatistics();
              }
              catch (MonitoringNotEnabledException mnee) {
                   
            	   // Statistics not available
            	   AppLog.getLogger().error("********************************************************************");
                   AppLog.getLogger().error("Monitoring is not enabled for this service - Please do something ...");
                   AppLog.getLogger().error("********************************************************************");
                   continue;
              }
              
              catch (InvalidServiceRefException isre) {
                   
            	   // Invalid service
            	   AppLog.getLogger().error("***************************************************************");
                   AppLog.getLogger().error("InvalidRef. Maybe this service is deleted - Please do something");
                   AppLog.getLogger().error("***************************************************************");
                   continue;
              }

              catch (MonitoringException me) {
                   
            	   // Statistics not available
            	   AppLog.getLogger().error("********************************************************************");
                   AppLog.getLogger().error("Failed to get statistics for this service...");
                   AppLog.getLogger().error("Details: " + me.getMessage());
                   me.printStackTrace();
                   AppLog.getLogger().error("********************************************************************");
                   continue;
              }
              
/*
			  // Print statistics
              for (ResourceStatistic resStats : resStatsArray) {
            	  
                   // Print resource information
            	   AppLog.getLogger().notice("");
                   AppLog.getLogger().notice("Resource name: [" + resStats.getName() + "] - Resource type: [" + resStats.getResourceType().toString() + "]");

                   // Now get and print statistics for this resource
                   StatisticValue[] statValues = resStats.getStatistics();
                   for (StatisticValue value : statValues) {
                	   
                        AppLog.getLogger().notice("  Statistic Name: [" + value.getName() + "] - Statistic Type: [" + value.getType().toString() + "]");

                        // Determine statistics type
                        if (value.getType() == StatisticType.INTERVAL) {
                        	
                             StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic)value;

                             // Print interval statistics values
                             AppLog.getLogger().notice("    Cnt Value: [" + is.getCount() + "]");
                             AppLog.getLogger().notice("    Min Value: [" + is.getMin() + "]");
                             AppLog.getLogger().notice("    Max Value: [" + is.getMax() + "]");
                             AppLog.getLogger().notice("    Sum Value: [" + is.getSum() + "]");
                             AppLog.getLogger().notice("    Avg Value: [" + is.getAverage() + "]");
                        }
                        else if (value.getType() == StatisticType.COUNT) {
                        	
                             StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic) value;

                             // Print count statistics value
                             AppLog.getLogger().notice("    Cnt Value: [" + cs.getCount() + "]");
                        }
                        else if (value.getType() == StatisticType.STATUS) {
                        	
                             StatisticValue.StatusStatistic ss = (StatisticValue.StatusStatistic)value;
                             // Print count statistics value
                             AppLog.getLogger().notice("    Initial Status: [" + ss.getInitialStatus() + "]");
                             AppLog.getLogger().notice("    Current Status: [" + ss.getCurrentStatus() + "]");
                        }
                   }
              }
              AppLog.getLogger().notice("=========================================");
*/
              
              // ----------------------------------------------------------------
              ResourceStatistic[] resultStatsArray = null;
              
              // FLOW_COMPONENT
              resultStatsArray = serviceStats.getResourceStatisticsByType(ResourceType.FLOW_COMPONENT);
              if(resultStatsArray != null && resultStatsArray.length > 0) {
           	   
            	  AppLog.getLogger().notice("");
            	  AppLog.getLogger().notice("  -------------------------------------");
            	  AppLog.getLogger().notice("  FLOW_COMPONENT");
            	  AppLog.getLogger().notice("  --------------");
            	  
            	  Map<String, String> report = generateReportStatisitics(resultStatsArray);
           	   	  printReportStatisitics(report);
           	      AppLog.getLogger().notice("  -------------------------------------");
              }
              
              // SERVICE
              resultStatsArray = serviceStats.getResourceStatisticsByType(ResourceType.SERVICE);
              if(resultStatsArray != null && resultStatsArray.length > 0) {
           	   
            	  AppLog.getLogger().notice("");
            	  AppLog.getLogger().notice("  -------------------------------------");
            	  AppLog.getLogger().notice("  SERVICE");
            	  AppLog.getLogger().notice("  -------");
            	  
            	  Map<String, String> report = generateReportStatisitics(resultStatsArray);
           	   	  printReportStatisitics(report);
           	   	  AppLog.getLogger().notice("  -------------------------------------");
              }
              
              // URI
              resultStatsArray = serviceStats.getResourceStatisticsByType(ResourceType.URI);
              if(resultStatsArray != null && resultStatsArray.length > 0) {
           	   
            	  AppLog.getLogger().notice("");
            	  AppLog.getLogger().notice("  -------------------------------------");
            	  AppLog.getLogger().notice("  URI");
            	  AppLog.getLogger().notice("  ---");
            	  
            	  Map<String, String> report = generateReportStatisitics(resultStatsArray);
           	   	  printReportStatisitics(report);
           	   	  AppLog.getLogger().notice("  -------------------------------------");
              }
              
              // WEBSERVICE_OPERATION
              resultStatsArray = serviceStats.getResourceStatisticsByType(ResourceType.WEBSERVICE_OPERATION);
              if(resultStatsArray != null && resultStatsArray.length > 0) {
           	   
            	  AppLog.getLogger().notice("");
            	  AppLog.getLogger().notice("  -------------------------------------");
            	  AppLog.getLogger().notice("  WEBSERVICE_OPERATION");
            	  AppLog.getLogger().notice("  --------------------");
            	  
            	  Map<String, String> report = generateReportStatisitics(resultStatsArray);
           	   	  printReportStatisitics(report);
           	   	  AppLog.getLogger().notice("  -------------------------------------");
              }
       	   	  // ----------------------------------------------------------------
         }
    }
    
    /**
     * 
     * @param resStatsArray
     * @return
     * @throws Exception
     */
    private Map<String, String> generateReportStatisitics(ResourceStatistic[] resStatsArray) throws Exception {
    	
    	Map<String, String> resourcesMap = new HashMap<String, String>();
    	
    	for (ResourceStatistic resourceStatistic : resStatsArray) {
    		
    		StatisticValue[] statisticValues = resourceStatistic.getStatistics();
    		for (StatisticValue statisticValue : statisticValues) {
         	   
                 String key = statisticValue.getName();

                 // Determine statistics type
                 if (statisticValue.getType() == StatisticType.INTERVAL) {
                      String value = "INTERVAL -> [Cnt, Min, Max, Sum, Avg]";
                      resourcesMap.put(key, value);
                 }
                 else if (statisticValue.getType() == StatisticType.COUNT) {
                      String value = "COUNT -> [Cnt]";
                      resourcesMap.put(key, value);
                 }
                 else if (statisticValue.getType() == StatisticType.STATUS) {                 	
                      String value = "STATUS -> [Initial, Current]";
                      resourcesMap.put(key, value);
                 }
            }
    	}
    	return resourcesMap;
    }
    
    /**
     * 
     * @param resourcesMap
     * @return
     * @throws Exception
     */
    private void printReportStatisitics(Map<String, String> resourcesMap) throws Exception {
    	    	
    	Set<String> keys = resourcesMap.keySet();
    	Iterator<String> iteratorKey = keys.iterator();
    	while(iteratorKey.hasNext()) {
    		String key = iteratorKey.next();
    		String value = resourcesMap.get(key);
    		
    		AppLog.getLogger().notice("    [" + key + "] - [" + value + "]");
    	}
    }
    
    /**
	 * Gets an instance of ServiceDomainMBean from the weblogic server.
	 *
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @throws Exception
	 */
    private void initServiceDomainMBean(DomainRuntimeServiceMBeanConnection conn) throws Exception {    	
    	InvocationHandler handler = new ServiceDomainMBeanInvocationHandler(conn.getJMXConnector());
		Object proxy = Proxy.newProxyInstance(ServiceDomainMBean.class.getClassLoader(), new Class[] { ServiceDomainMBean.class }, handler);
		serviceDomainMBean = (ServiceDomainMBean) proxy;
	}
}