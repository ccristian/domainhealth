package domainhealth.rest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

public class OSBServiceUtil {

	private ServiceDomainMBean serviceDomainMBean = null;

	public OSBServiceUtil() {

		try {
			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
			initServiceDomainMBean(conn);
		} catch (Exception ex) {
			AppLog.getLogger().error("Problem during init of ServiceDomainMBean object ...", ex);
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	/*
	public Set<String> getDetailsForOsbType(String osbResourceType) {

		try {
			getServices(osbResourceType);
		} catch (Exception ex) {
			AppLog.getLogger().error("Problem inside getDetailsForOsbType method - Mesage is [" + ex.getMessage() + "]", ex);
		}
		return null;
	}
	*/
	
	/**
	 * 
	 * @param osbResourceType
	 * @param resourceType
	 * @return
	 */
	public Set<String> getDetailsForResourceType(String osbResourceType, String resourceType) {

		try {

			Ref[] serviceRefs = getRefForOsbType(osbResourceType);

			if (serviceRefs != null && serviceRefs.length > 0) {
				AppLog.getLogger().notice("Found [" + serviceRefs.length + "] elements of type [" + osbResourceType + "]");

				// Bitwise map for desired resource types.
				int typeFlag = 0;

				ResourceType resourceTypeEnum = getResourceType(resourceType);
				if (resourceTypeEnum == null) {

					AppLog.getLogger().error("------------------------------------------------");
					AppLog.getLogger().error("Not possible to get informations about resource [" + resourceType + "]");
					AppLog.getLogger().error("------------------------------------------------");
					return null;
				}

				typeFlag = typeFlag | resourceTypeEnum.value();
				HashMap<Ref, ServiceResourceStatistic> resourcesMap = null;

				// Get cluster-level statistics.
				try {
					// Get statistics.
					AppLog.getLogger().notice("Generating statistics for [" + serviceRefs.length + "] elements of type [" + osbResourceType + "]");
					resourcesMap = getStatisticForOsbType(osbResourceType, serviceRefs, typeFlag);
					AppLog.getLogger().notice("-> We found [" + resourcesMap.size() + "] elements of type [" + osbResourceType + "]");

					// Print Statistic
					printStatistics(resourcesMap);
				} catch (IllegalArgumentException iae) {

					AppLog.getLogger().error("------------------------------------------------");
					AppLog.getLogger().error("Encountered IllegalArgumentException... Details:");
					AppLog.getLogger().error(iae.getMessage());
					AppLog.getLogger().error("Check if reference OR bitmap are valid... !!!");
					AppLog.getLogger().error("------------------------------------------------");
					throw iae;
				} catch (DomainMonitoringDisabledException dmde) {

					// Statistics not available as monitoring is turned off at domain level.
					AppLog.getLogger().error("------------------------------------------------");
					AppLog.getLogger().error("Statistics not available as monitoring is turned off at domain level.");
					AppLog.getLogger().error("------------------------------------------------");
					throw dmde;
				} catch (MonitoringException me) {

					// Internal problem... May be aggregation server is crashed...
					AppLog.getLogger().error("------------------------------------------------");
					AppLog.getLogger().error("Statistics is not available... Check if aggregation server is crashed...");
					AppLog.getLogger().error("------------------------------------------------");
					throw me;
				}

			} else {
				AppLog.getLogger().error("Didn't find any element with monitoring enabled - Not possible to collect anything for elements of type [" + osbResourceType + "]");
			}

		} catch (Exception ex) {
			AppLog.getLogger().error("Problem to get the details of OSB resource [" + osbResourceType + "] and ResourceType [" + resourceType + "]", ex);
		}
		return null;
	}

	/**
	 * 
	 * @param resourceType
	 * @return
	 */
	private ResourceType getResourceType(String resourceType) {

		try {
			// Try from the "string" enum value
			return ResourceType.valueOf(resourceType.toUpperCase());
		} catch (Exception ex) {

			// Try from internal DH constants (probably coming from WS)
			switch (resourceType) {

			case MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT:
				return ResourceType.FLOW_COMPONENT;

			case MonitorProperties.OSB_RESOURCE_TYPE_SERVICE:
				return ResourceType.SERVICE;

			case MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION:
				return ResourceType.WEBSERVICE_OPERATION;

			case MonitorProperties.OSB_RESOURCE_TYPE_URI:
				return ResourceType.URI;

			default:
				AppLog.getLogger().error("Wrong resourceType [" + resourceType + "]");
				return null;
			}
		}
	}

	/**
	 * 
	 * @param osbResourceType
	 * @return
	 * @throws Exception
	 */
	private Ref[] getRefForOsbType(String osbResourceType) throws Exception {

		switch (osbResourceType) {

		case MonitorProperties.OSB_PS_TYPE:
			return serviceDomainMBean.getMonitoredProxyServiceRefs();

		case MonitorProperties.OSB_BS_TYPE:
			return serviceDomainMBean.getMonitoredBusinessServiceRefs();

		default:
			AppLog.getLogger().error("Wrong osbResourceType [" + osbResourceType + "] - Must be [" + MonitorProperties.OSB_PS_TYPE + "] or [" + MonitorProperties.OSB_BS_TYPE + "]");
			return null;
		}
	}

	/**
	 * 
	 * @param osbResourceType
	 * @param serviceRefs
	 * @param typeFlag
	 * @return
	 * @throws Exception
	 */
	private HashMap<Ref, ServiceResourceStatistic> getStatisticForOsbType(String osbResourceType, Ref[] serviceRefs, int typeFlag) throws Exception {

		if (serviceRefs != null && serviceRefs.length > 0) {

			switch (osbResourceType) {

			case MonitorProperties.OSB_PS_TYPE:
				return serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, null);

			case MonitorProperties.OSB_BS_TYPE:
				return serviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, null);

			default:
				return null;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	/*
	 * private Set<ObjectName> getOsbResource(DomainRuntimeServiceMBeanConnection conn) throws Exception {
	 * 
	 * Set<ObjectName> osbConfigs = conn.getElementByQueryNames( "com.oracle.osb:Type=ResourceConfigurationMBean,*"); return osbConfigs; }
	 */

	/**
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private Set<String> getProxyServices() throws Exception {
		// -------------------------------------------
		AppLog.getLogger().notice("");

		Ref[] serviceRefs = serviceDomainMBean.getMonitoredProxyServiceRefs();
		if (serviceRefs != null && serviceRefs.length > 0) {
			AppLog.getLogger().notice("Found [" + serviceRefs.length + "] Proxy services");

			// Create a bitwise map for desired resource types.
			int typeFlag = 0;
			typeFlag = typeFlag | ResourceType.SERVICE.value();
			typeFlag = typeFlag | ResourceType.WEBSERVICE_OPERATION.value();
			typeFlag = typeFlag | ResourceType.FLOW_COMPONENT.value();
			
			HashMap<Ref, ServiceResourceStatistic> resourcesMap = null;

			// Get cluster-level statistics.
			try {
				// Get statistics.
				AppLog.getLogger().notice("Now trying to get statistics for [" + serviceRefs.length + "] proxy services...");
				// resourcesMap = serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, serverName);
				resourcesMap = serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, null);

				// Print Statistic
				printStatistics(resourcesMap);
			} catch (IllegalArgumentException iae) {

				AppLog.getLogger().error("------------------------------------------------");
				AppLog.getLogger().error("Encountered IllegalArgumentException... Message is [" + iae.getMessage() + "]"); 
				AppLog.getLogger().error("Check if proxy reference OR bitmap are valid... !!!");
				AppLog.getLogger().error("------------------------------------------------");
				throw iae;
			} catch (DomainMonitoringDisabledException dmde) {

				// Statistics not available as monitoring is turned off at domain level.
				AppLog.getLogger().error("------------------------------------------------");
				AppLog.getLogger().error("Statistics not available as monitoring is turned off at domain level.");
				AppLog.getLogger().error("------------------------------------------------");
				throw dmde;
			} catch (MonitoringException me) {

				// Internal problem... May be aggregation server is crashed...
				AppLog.getLogger().error("------------------------------------------------");
				AppLog.getLogger().error("Statistics is not available... Check if aggregation server is crashed...");
				AppLog.getLogger().error("------------------------------------------------");
				throw me;
			}
		} else {
			AppLog.getLogger().warning("Didn't find any Proxy services with monitoring enabled - Not possible to collect anything");
		}
		AppLog.getLogger().notice("");
		// -------------------------------------------

		// -------------------------------------------
		Set<String> result = new TreeSet<String>();
		return result;
		// -------------------------------------------
	}

	/**
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private Set<String> getBusinessServices() throws Exception {
		// -------------------------------------------
		AppLog.getLogger().notice("");

		Ref[] serviceRefs = serviceDomainMBean.getMonitoredBusinessServiceRefs();
		if (serviceRefs != null && serviceRefs.length > 0) {
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
				// resourcesMap = sserviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, serverName);
				resourcesMap = serviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, null);

				// Print Statistic
				printStatistics(resourcesMap);
			} catch (IllegalArgumentException iae) {

				AppLog.getLogger().error("------------------------------------------------");
				AppLog.getLogger().error("Encountered IllegalArgumentException... Message is [" + iae.getMessage() + "]"); 
				AppLog.getLogger().error("Check if business reference OR bitmap are valid... !!!");
				AppLog.getLogger().error("------------------------------------------------");
				throw iae;
			} catch (DomainMonitoringDisabledException dmde) {

				// Statistics not available as monitoring is turned off at domain level.
				AppLog.getLogger().error("------------------------------------------------");
				AppLog.getLogger().error("Statistics not available as monitoring is turned off at domain level.");
				AppLog.getLogger().error("------------------------------------------------");
				throw dmde;
			} catch (MonitoringException me) {

				// Internal problem... May be aggregation server is crashed...
				AppLog.getLogger().error("------------------------------------------------");
				AppLog.getLogger().error("Statistics is not available... Check if aggregation server is crashed...");
				AppLog.getLogger().error("------------------------------------------------");
				throw me;
			}
		} else {
			AppLog.getLogger().warning("Didn't find any Business services with monitoring enabled - Not possible to collect anything");
		}
		AppLog.getLogger().notice("");
		// -------------------------------------------

		// -------------------------------------------
		Set<String> result = new TreeSet<String>();
		return result;
		// -------------------------------------------
	}
	
	/**
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private void getServices(String resourceType) throws Exception {
		// -------------------------------------------
		AppLog.getLogger().notice("");
		
		Ref[] serviceRefs = null;
		String resourceTypeFormatted = "";
		int typeFlag = 0;

		// -------------------------------------------
		if(MonitorProperties.OSB_PS_TYPE.equals(resourceType)) {
			
			serviceRefs = serviceDomainMBean.getMonitoredProxyServiceRefs();
			resourceTypeFormatted = "ProxyService";
			
			typeFlag = typeFlag | ResourceType.SERVICE.value();
			typeFlag = typeFlag | ResourceType.WEBSERVICE_OPERATION.value();
			typeFlag = typeFlag | ResourceType.FLOW_COMPONENT.value();
			
		} else if(MonitorProperties.OSB_BS_TYPE.equals(resourceType)) {
			
			serviceRefs = serviceDomainMBean.getMonitoredBusinessServiceRefs();
			resourceTypeFormatted = "BusinessService";
			
			typeFlag = typeFlag | ResourceType.SERVICE.value();
			typeFlag = typeFlag | ResourceType.WEBSERVICE_OPERATION.value();
			typeFlag = typeFlag | ResourceType.URI.value();
			
		} else {
			
			// Should never happen but ...
			AppLog.getLogger().error("------------------------------------------------");
			AppLog.getLogger().error("The [" + resourceType + "] is unknown");
			AppLog.getLogger().error("------------------------------------------------");
		}
		// -------------------------------------------
		
		// -------------------------------------------
		if (serviceRefs != null && serviceRefs.length > 0) {
			AppLog.getLogger().notice("Trying to get statistics for the [" + serviceRefs.length + "] " +  resourceTypeFormatted + " ...");
			
			HashMap<Ref, ServiceResourceStatistic> resourcesMap = null;
			
			// Get cluster-level statistics.
			try {
				
				if(MonitorProperties.OSB_PS_TYPE.equals(resourceType)) {
					
					// resourcesMap = serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, serverName);
					resourcesMap = serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, null);
					
				} else if(MonitorProperties.OSB_BS_TYPE.equals(resourceType)) {
				
					// resourcesMap = serviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, serverName);
					resourcesMap = serviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, null);
				}
				
				// Print Statistic
				if(resourcesMap!= null) printStatistics(resourcesMap);
				else {
					AppLog.getLogger().error("------------------------------");
					AppLog.getLogger().error("Didn't find any statistics ...");
					AppLog.getLogger().error("------------------------------");
				}
			} catch (IllegalArgumentException iae) {

				AppLog.getLogger().error("------------------------------------------------");
				AppLog.getLogger().error("Encountered IllegalArgumentException... Message is [" + iae.getMessage() + "]"); 
				AppLog.getLogger().error("Check if business reference OR bitmap are valid... !!!");
				AppLog.getLogger().error("------------------------------------------------");
				throw iae;
			} catch (DomainMonitoringDisabledException dmde) {

				// Statistics not available as monitoring is turned off at domain level.
				AppLog.getLogger().error("------------------------------------------------");
				AppLog.getLogger().error("Statistics not available as monitoring is turned off at domain level.");
				AppLog.getLogger().error("------------------------------------------------");
				throw dmde;
			} catch (MonitoringException me) {

				// Internal problem... May be aggregation server is crashed...
				AppLog.getLogger().error("------------------------------------------------");
				AppLog.getLogger().error("Statistics is not available... Check if aggregation server is crashed...");
				AppLog.getLogger().error("------------------------------------------------");
				throw me;
			}
		} else {
			AppLog.getLogger().warning("Didn't find any [" + resourceTypeFormatted + "] with monitoring enabled - Not possible to collect anything");
		}
		// -------------------------------------------
	}
	
	/**
	 * 
	 * @param statisticName
	 * @return
	 */
	private boolean isValidStatisticName(String statisticName) {

		if (MonitorProperties.OSB_STATISTIC_LIST.contains(statisticName)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param statsMap
	 * @throws Exception
	 */
	private void printStatistics(HashMap<Ref, ServiceResourceStatistic> statsMap) throws Exception {

		if (statsMap == null) {
			AppLog.getLogger().warning("------------------------------------------------------");
			AppLog.getLogger().warning("ServiceResourceStatistics is null... Nothing to report");
			AppLog.getLogger().warning("------------------------------------------------------");
			return;
		}
		if (statsMap.size() == 0) {
			AppLog.getLogger().warning("-------------------------------------------------------");
			AppLog.getLogger().warning("ServiceResourceStatistics is empty... Nothing to report");
			AppLog.getLogger().warning("------------------------------------------------------");
			return;
		}

		Set<Map.Entry<Ref, ServiceResourceStatistic>> set = statsMap.entrySet();

		// Print statistical information of each service
		for (Map.Entry<Ref, ServiceResourceStatistic> mapEntry : set) {

			AppLog.getLogger().notice("");
			// AppLog.getLogger().notice("----- Printing statistics for service [" + mapEntry.getKey().getFullName() + "] -----");
			AppLog.getLogger().notice("----- Printing statistics for service [" + mapEntry.getKey().getLocalName() + "] -----");

			ServiceResourceStatistic serviceStats = mapEntry.getValue();

			ResourceStatistic[] resStatsArray = null;
			try {

				// Get all the statistics
				resStatsArray = serviceStats.getAllResourceStatistics();
			} catch (MonitoringNotEnabledException mnee) {

				// Statistics not available
				AppLog.getLogger().error("--------------------------------------------------------------------");
				AppLog.getLogger().error("Monitoring is not enabled for this service - Please check ...");
				AppLog.getLogger().error("--------------------------------------------------------------------");
				continue;
			}

			catch (InvalidServiceRefException isre) {

				// Invalid service
				AppLog.getLogger().error("---------------------------------------------------------------");
				AppLog.getLogger().error("InvalidRef. Maybe this service is deleted - Please check ...");
				AppLog.getLogger().error("---------------------------------------------------------------");
				continue;
			}

			catch (MonitoringException me) {

				// Statistics not available
				AppLog.getLogger().error("--------------------------------------------------------------------");
				AppLog.getLogger().error("Failed to get statistics for this service...");
				AppLog.getLogger().error("Details: " + me.getMessage());
				AppLog.getLogger().error("--------------------------------------------------------------------");
				continue;
			}

			// ----------------------------------------------------------------
			// Print statistics
			for (ResourceStatistic resStats : resStatsArray) {

				// Print resource information
				AppLog.getLogger().notice("");
				AppLog.getLogger().notice("Resource name: [" + resStats.getName() + "] - Resource type: [" + resStats.getResourceType().toString() + "]");

				// Now get and print statistics for this resource
				StatisticValue[] statValues = resStats.getStatistics();
				for (StatisticValue value : statValues) {

					if (isValidStatisticName(value.getName()))
						AppLog.getLogger().notice("  Statistic Name: [" + value.getName() + "] - Statistic Type: [" + value.getType().toString() + "]");
					else
						AppLog.getLogger().notice("  Statistic Name: [" + value.getName() + " - UNKNOWN] - Statistic Type: [" + value.getType().toString() + "]");

					// Determine statistics type
					if (value.getType() == StatisticType.INTERVAL) {

						StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic) value;

						// Print interval statistics values
						AppLog.getLogger().notice("    Cnt Value: [" + is.getCount() + "]");
						AppLog.getLogger().notice("    Min Value: [" + is.getMin() + "]");
						AppLog.getLogger().notice("    Max Value: [" + is.getMax() + "]");
						AppLog.getLogger().notice("    Sum Value: [" + is.getSum() + "]");
						AppLog.getLogger().notice("    Avg Value: [" + is.getAverage() + "]");
					} else if (value.getType() == StatisticType.COUNT) {

						StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic) value;

						// Print count statistics value
						AppLog.getLogger().notice("    Cnt Value: [" + cs.getCount() + "]");

					} else if (value.getType() == StatisticType.STATUS) {

						// Is used in 12.1.3
						StatisticValue.StatusStatistic ss = (StatisticValue.StatusStatistic) value;
						// Print count statistics value
						AppLog.getLogger().notice("    Initial Status: [" + ss.getInitialStatus() + "]");
						AppLog.getLogger().notice("    Current Status: [" + ss.getCurrentStatus() + "]");
					}
				}
			}
			// ----------------------------------------------------------------
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