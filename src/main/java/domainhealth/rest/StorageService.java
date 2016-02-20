package domainhealth.rest;

import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.MonitorProperties;
import domainhealth.core.statistics.ResourceNameNormaliser;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.frontend.data.DateAmountDataItem;
import domainhealth.frontend.data.DateAmountDataSet;
import domainhealth.frontend.data.Statistics;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import domainhealth.core.jmx.WebLogicMBeanPropConstants;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.AGENTS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.CONSUMERS_CURRENT_COUNT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.CONSUMERS_HIGH_COUNT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.CONSUMERS_TOTAL_COUNT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DESTINATIONS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DOWNTIME_HIGH;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DOWNTIME_TOTAL;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.FAILED_MESSAGES_TOTAL;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_SERVERS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.MESSAGES_CURRENT_COUNT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.MESSAGES_HIGH_COUNT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.MESSAGES_PENDING_COUNT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.MESSAGES_RECEIVED_COUNT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.REMOTE_END_POINTS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.SAF_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.UPTIME_HIGH;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.UPTIME_TOTAL;
import static domainhealth.core.statistics.MonitorProperties.JMSSVR_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.SAFAGENT_RESOURCE_TYPE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by chiovcr on 02/12/2014.
 */
@Path("/")
public class StorageService {

    DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy-HH-mm");

    @Context
    private ServletContext application;

    private StatisticsStorage statisticsStorage;

    /**
     * 
     */
    @PostConstruct
    public void initialize() {
        try {
            statisticsStorage = new StatisticsStorage((String) application.getAttribute(AppProperties.PropKey.STATS_OUTPUT_PATH_PROP.toString()));
        } catch (Exception ex) {
            AppLog.getLogger().error("Exception", ex);
        }
    }

    /**
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    //http://localhost:7001/domainhealth/rest/resources?startTime=01-09-2014-00-00&endTime=17-09-2016-0-00
    @GET
    @Path("resources")
    @Produces({MediaType.APPLICATION_JSON})
    public Map<String, Set<String>> getStats(
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime
    ) {
        Map<String, Set<String>> resourcesMap = new HashMap<String, Set<String>>();
        try {
            DateTime start = fmt.parseDateTime(startTime);
            DateTime end = fmt.parseDateTime(endTime);
            Interval interval = new Interval(start, end);
            
            resourcesMap.put(MonitorProperties.CORE_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.CORE_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.DATASOURCE_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.DATASOURCE_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.DESTINATION_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.DESTINATION_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.SAF_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.SAF_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.EJB_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.EJB_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.WORKMGR_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.WORKMGR_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.WEBAPP_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.WEBAPP_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.SVRCHNL_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.SVRCHNL_RESOURCE_TYPE));
            
            // Add the extensions of DH
			//resourcesMap.put(MonitorProperties.HOSTMACHINE_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.HOSTMACHINE_RESOURCE_TYPE));
			//resourcesMap.put(MonitorProperties.JVM_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.JVM_RESOURCE_TYPE));
            
			// Add the dashboard
			//resourcesMap.put(MonitorProperties.JMS_DASHBOARD_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.JMS_DASHBOARD_RESOURCE_TYPE));
			//resourcesMap.put(MonitorProperties.SAF_DASHBOARD_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.SAF_DASHBOARD_RESOURCE_TYPE));
			
        } catch (IOException ex) {
            AppLog.getLogger().error("Error while getting resources", ex);
        }
        return resourcesMap;
    }

    /**
     * 
     * @return
     */
    //http://localhost:7001/domainhealth/domain
    @GET
    @Path("/domain")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> getDomain() {
        DomainRuntimeServiceMBeanConnection conn = null;
        try {
            conn = new DomainRuntimeServiceMBeanConnection();
            Set<String> servers = statisticsStorage.getAllPossibleServerNames(conn);
            return servers;
        } catch (Exception e) {
            AppLog.getLogger().error("StorageService - unable to retrieve domain structure for domain's servers");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return null;
    }


    /**
     * 
     * @param userAgent
     * @param scope
     * @param startTime
     * @param endTime
     * @param resourceType
     * @param resource
     * @return
     */
    //http://localhost:7001/domainhealth/rest/stats/core?scope=ALL&startTime=ss&endTime=ss
    //http://localhost:7001/domainhealth/rest/stats/core/xdd?startTime=01-09-2014-00-00&endTime=17-11-2015-0-00
    //http://localhost:7001/domainhealth/rest/stats/datasource/xdd?startTime=01-09-2014-00-00&endTime=17-11-2015-0-00

    @GET
    @Path("stats/{resourceType}/{resource}")
    @Produces({MediaType.APPLICATION_JSON})
    public Map<String,List<Map>> getStats(	@HeaderParam("user-agent") String userAgent, 
    										@QueryParam("scope") Set<String> scope,
    										@QueryParam("startTime") String startTime,
    										@QueryParam("endTime") String endTime,
    										@PathParam("resourceType") String resourceType,
    										@PathParam("resource") String resource) {
        try {

			Map<String, List<Map>> result = new LinkedHashMap<>();

            DateTime start = fmt.parseDateTime(startTime);
            DateTime end = fmt.parseDateTime(endTime);
            Interval interval = new Interval(start, end);
            
// --------------------------------------------------------------------------
// TO CHECK
// --------
        	
        	// In case of scope definition, the "conn" object will not be set !!!
        	/*
        	DomainRuntimeServiceMBeanConnection conn = null;
            
            if (scope == null || scope.size() == 0) {
                conn = new DomainRuntimeServiceMBeanConnection();
                scope = statisticsStorage.getAllPossibleServerNames(conn);
            }
            */
            
            // For me, it should be : 
        	DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();;
        	
            if (scope == null || scope.size() == 0) {
                scope = statisticsStorage.getAllPossibleServerNames(conn);
            }
            // --------------------------------------------------------------------------
            
            Map<String, DateAmountDataSet> dataMap = null;

            for (String server : scope) {
                List<String> coreProps = new LinkedList<>();
                switch (resourceType) {
                
                	case MonitorProperties.CORE_RESOURCE_TYPE:
                		
                        coreProps.add(WebLogicMBeanPropConstants.OPEN_SOCKETS_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.HEAP_USED_CURRENT);
                        coreProps.add(WebLogicMBeanPropConstants.HEAP_SIZE_CURRENT);
                        coreProps.add(WebLogicMBeanPropConstants.HEAP_FREE_CURRENT);
                        coreProps.add(WebLogicMBeanPropConstants.HEAP_FREE_PERCENT);
                        coreProps.add(WebLogicMBeanPropConstants.EXECUTE_THREAD_TOTAL_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.HOGGING_THREAD_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.PENDING_USER_REQUEST_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.THREAD_POOL_QUEUE_LENGTH);
                        coreProps.add(WebLogicMBeanPropConstants.COMPLETED_REQUEST_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.EXECUTE_THREAD_IDLE_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.MIN_THREADS_CONSTRAINT_COMPLETED);
                        coreProps.add(WebLogicMBeanPropConstants.MIN_THREADS_CONSTRAINT_PENDING);
                        coreProps.add(WebLogicMBeanPropConstants.STANDBY_THREAD_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.THROUGHPUT);
                        coreProps.add(WebLogicMBeanPropConstants.TRANSACTION_TOTAL_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.TRANSACTION_COMMITTED_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.TRANSACTION_ROLLEDBACK_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.TRANSACTION_HEURISTICS_TOTAL_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.TRANSACTION_ABANDONED_TOTAL_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.TRANSACTIONS_ACTIVE_TOTAL_COUNT);
                		
                        resource = null;
                        break;
                        
                    case MonitorProperties.DATASOURCE_RESOURCE_TYPE:
                    	
                    	coreProps.add(WebLogicMBeanPropConstants.NUM_AVAILABLE);
                        coreProps.add(WebLogicMBeanPropConstants.NUM_UNAVAILABLE);
                        coreProps.add(WebLogicMBeanPropConstants.ACTIVE_CONNECTONS_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.CONNECTION_DELAY_TIME);
                        coreProps.add(WebLogicMBeanPropConstants.FAILED_RESERVE_REQUEST_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.FAILURES_TO_RECONNECT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.LEAKED_CONNECTION_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.WAITING_FOR_CONNECTION_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.WAITING_FOR_CONNECTION_FAILURES_TOTAL);
                        coreProps.add(WebLogicMBeanPropConstants.WAITING_SECONDS_HIGH_COUNT);
                    	
                        break;

                    case MonitorProperties.DESTINATION_RESOURCE_TYPE:
                    	
                    	coreProps.add(WebLogicMBeanPropConstants.MESSAGES_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.MESSAGES_RECEIVED_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.MESSAGES_PENDING_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.MESSAGES_HIGH_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.CONSUMERS_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.CONSUMERS_HIGH_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.CONSUMERS_TOTAL_COUNT);
                    	
                        break;

                    case MonitorProperties.EJB_RESOURCE_TYPE:
                    	
                    	coreProps.add(WebLogicMBeanPropConstants.BEANS_POOLED_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.BEAN_ACCESS_TOTAL_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.BEANS_INUSE_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.BEAN_WAITING_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.BEAN_WAITING_TOTAL_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.BEAN_TRANSACTIONS_COMMITTED_TOTAL_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.BEAN_TRANSACTIONS_ROLLEDBACK_TOTAL_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.BEAN_TRANSACTIONS_TIMEDOUT_TOTAL_COUNT);
                    	
                    	break;
                        
                    case MonitorProperties.SAF_RESOURCE_TYPE:
                    	
                    	coreProps.add(WebLogicMBeanPropConstants.MESSAGES_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.MESSAGES_PENDING_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.MESSAGES_RECEIVED_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.MESSAGES_HIGH_COUNT);
                    	
                        break;

                    case MonitorProperties.WEBAPP_RESOURCE_TYPE:
                    	
                    	coreProps.add(WebLogicMBeanPropConstants.SESSIONS_CURRENT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.SESSIONS_HIGH_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.SESSIONS_TOTAL_COUNT);
                    	
                        break;

                    case MonitorProperties.SVRCHNL_RESOURCE_TYPE:
                    	
                    	coreProps.add(WebLogicMBeanPropConstants.ACCEPT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.CONNECTIONS_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.CHNL_MESSAGES_RECEIVED_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.CHNL_MESSAGES_SENT_COUNT);
                    	
                        break;

                    case MonitorProperties.WORKMGR_RESOURCE_TYPE:
                    	
                    	coreProps.add(WebLogicMBeanPropConstants.COMPLETED_REQUESTS);
                        coreProps.add(WebLogicMBeanPropConstants.PENDING_REQUESTS);
                        coreProps.add(WebLogicMBeanPropConstants.STUCK_THREAD_COUNT);
                    	
                        break;
                        
                    case MonitorProperties.HOSTMACHINE_RESOURCE_TYPE:
                    	                    	
                    	coreProps.add(WebLogicMBeanPropConstants.JVM_INSTANCE_CORES_USED);
                        coreProps.add(WebLogicMBeanPropConstants.JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES);
                        coreProps.add(WebLogicMBeanPropConstants.NATIVE_PROCESSES_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_RX_MEGABYTES);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_RX_DROPPED);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_RX_ERRORS);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_RX_FRAME);
                        coreProps.add(WebLogicMBeanPropConstants.PENDING_REQUESTS);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_RX_OVERRUNS);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_MILLIONS_RX_PACKETS);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_TX_MEGABYTES);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_TX_CARRIER);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_TX_COLLISIONS);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_TX_DROPPED);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_TX_ERRORS);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_TX_OVERRUNS);
                        coreProps.add(WebLogicMBeanPropConstants.NETWORK_MILLIONS_TX_PACKETS);
                        coreProps.add(WebLogicMBeanPropConstants.PHYSICAL_MEMORY_USED_PERCENT);
                        coreProps.add(WebLogicMBeanPropConstants.PHYSICAL_SWAP_USED_PERCENT);
                        coreProps.add(WebLogicMBeanPropConstants.PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE);
                        coreProps.add(WebLogicMBeanPropConstants.PROCESSOR_USAGE_PERCENT);
                        coreProps.add(WebLogicMBeanPropConstants.ROOT_FILESYSTEM_USED_PERCENT);
                        coreProps.add(WebLogicMBeanPropConstants.TCP_CLOSE_WAIT_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.TCP_ESTABLISHED_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.TCP_LISTEN_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.TCP_TIME_WAIT_COUNT);
                        
                        coreProps.add(WebLogicMBeanPropConstants.AVAILABLE_PROCESSORS);
                        coreProps.add(WebLogicMBeanPropConstants.SYSTEM_LOAD_AVERAGE);
                        coreProps.add(WebLogicMBeanPropConstants.COMMITTED_VIRTUAL_MEMORY_SIZE_MEGABYTES);
                        coreProps.add(WebLogicMBeanPropConstants.FREE_PHYSICAL_MEMORY_SIZE_MEGABYTES);
                        coreProps.add(WebLogicMBeanPropConstants.FREE_SWAP_SPACE_SIZE_MEGABYTES);
                        coreProps.add(WebLogicMBeanPropConstants.MAX_FILE_DESCRIPTOR_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.OPEN_FILE_DESCRIPTOR_COUNT);
                        coreProps.add(WebLogicMBeanPropConstants.PROCESS_CPU_LOAD);
                        coreProps.add(WebLogicMBeanPropConstants.PROCESS_CPU_TIME);
                        coreProps.add(WebLogicMBeanPropConstants.SYSTEM_CPU_LOAD);
                        coreProps.add(WebLogicMBeanPropConstants.TOTAL_PHYSICAL_MEMORY_SIZE_MEGABYTES);
                        coreProps.add(WebLogicMBeanPropConstants.TOTAL_SWAP_SPACE_SIZE_MEGABYTES);
                        
                        // Is important to find the file to read
                        resource = MonitorProperties.HOST_MACHINE_MBEAN_NAME;
                        
                        break;
                        
                    case MonitorProperties.JVM_RESOURCE_TYPE:
                    	                    	
                    	coreProps.add(WebLogicMBeanPropConstants.HEAP_MEMORY_INIT);
                        coreProps.add(WebLogicMBeanPropConstants.HEAP_MEMORY_USED);
                        coreProps.add(WebLogicMBeanPropConstants.HEAP_MEMORY_COMMITTED);
                        coreProps.add(WebLogicMBeanPropConstants.HEAP_MEMORY_MAX);
                        
                        coreProps.add(WebLogicMBeanPropConstants.NON_HEAP_MEMORY_INIT);
                        coreProps.add(WebLogicMBeanPropConstants.NON_HEAP_MEMORY_USED);
                        coreProps.add(WebLogicMBeanPropConstants.NON_HEAP_MEMORY_COMMITTED);
                        coreProps.add(WebLogicMBeanPropConstants.NON_HEAP_MEMORY_MAX);
                        
                        coreProps.add(WebLogicMBeanPropConstants.EDEN_SPACE_INIT);
                        coreProps.add(WebLogicMBeanPropConstants.EDEN_SPACE_USED);
                        coreProps.add(WebLogicMBeanPropConstants.EDEN_SPACE_COMMITTED);
                        coreProps.add(WebLogicMBeanPropConstants.EDEN_SPACE_MAX);
                    	
                        coreProps.add(WebLogicMBeanPropConstants.SURVIVOR_SPACE_INIT);
                        coreProps.add(WebLogicMBeanPropConstants.SURVIVOR_SPACE_USED);
                        coreProps.add(WebLogicMBeanPropConstants.SURVIVOR_SPACE_COMMITTED);
                        coreProps.add(WebLogicMBeanPropConstants.SURVIVOR_SPACE_MAX);
                    	
                        coreProps.add(WebLogicMBeanPropConstants.TENURED_GEN_INIT);
                        coreProps.add(WebLogicMBeanPropConstants.TENURED_GEN_USED);
                        coreProps.add(WebLogicMBeanPropConstants.TENURED_GEN_COMMITTED);
                    	coreProps.add(WebLogicMBeanPropConstants.TENURED_GEN_MAX);
                    	
                    	coreProps.add(WebLogicMBeanPropConstants.PERM_GEN_INIT);
                    	coreProps.add(WebLogicMBeanPropConstants.PERM_GEN_USED);
                    	coreProps.add(WebLogicMBeanPropConstants.PERM_GEN_COMMITTED);
                    	coreProps.add(WebLogicMBeanPropConstants.PERM_GEN_MAX);
                        
                        // Is important to find the file to read
                        resource = MonitorProperties.JVM_MBEAN_NAME;
                        
                        break;                        
                }

                // Temp solution for ordering gui
                Collections.reverse(coreProps);
                Set prp = new LinkedHashSet(coreProps);
                dataMap = statisticsStorage.getPropertyData(resourceType, resource, prp, interval, server);
                
                for (String res:dataMap.keySet()) {

                    DateAmountDataSet dataSet  = dataMap.get(res);
                    
                    // Not used so commented
                    //String property = dataSet.getResourceProperty();
                    
                    List dataList = new LinkedList();
                    for (DateAmountDataItem dateAmountDataItem:dataSet.getData()) {
                        dataList.add(new BigDecimal[]{BigDecimal.valueOf(dateAmountDataItem.getDateTime().getTime()), BigDecimal.valueOf(dateAmountDataItem.getAmount())});
                    }

                    Map map = new LinkedHashMap();
                    map.put("name",server);
                    map.put("data",dataList);
                    
                    List<Map> listMap  = result.get(res);
                    if (listMap == null){
                        listMap = new ArrayList<>();
                        result.put(res,listMap);
                    }
                    listMap.add(map);
                }
            }

            long t1 = System.currentTimeMillis();
            
            // TODO add misssing data
            // addMissingData(result,start,end);
            
            long t2 = System.currentTimeMillis();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * 
     * @param userAgent
     * @param scope
     * @param resourceType
     * @param resource
     * @return
     */
    @GET
    @Path("dashboard/{resourceType}/{resource}")
    @Produces({MediaType.APPLICATION_JSON})
    public Map<String, Map<String, String>> getStatsForDashboard(	@HeaderParam("user-agent") String userAgent,
    													@QueryParam("scope") Set<String> scope,
    													@PathParam("resourceType") String resourceType,
    													@PathParam("resource") String resource) {
        try {
        	
// --------------------------------------------------------------------------
// TO CHECK
// --------
        	
        	// In case of scope definition, the "conn" object will not be set !!!
        	/*
        	DomainRuntimeServiceMBeanConnection conn = null;
            
            if (scope == null || scope.size() == 0) {
                conn = new DomainRuntimeServiceMBeanConnection();
                scope = statisticsStorage.getAllPossibleServerNames(conn);
            }
            */
            
            // For me, it should be : 
        	DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();;
        	
            if (scope == null || scope.size() == 0) {
                scope = statisticsStorage.getAllPossibleServerNames(conn);
            }
            // --------------------------------------------------------------------------
                        
            for (String server : scope) {
                List<String> coreProps = new LinkedList<>();
                switch (resourceType) {
                
                	case MonitorProperties.JMS_DASHBOARD_RESOURCE_TYPE:
                		
                		/*
                		coreProps.add(WebLogicMBeanPropConstants.NAME);
						coreProps.add(WebLogicMBeanPropConstants.MESSAGES_CURRENT_COUNT);
						coreProps.add(WebLogicMBeanPropConstants.MESSAGES_PENDING_COUNT);
						coreProps.add(WebLogicMBeanPropConstants.MESSAGES_RECEIVED_COUNT);
						coreProps.add(WebLogicMBeanPropConstants.MESSAGES_HIGH_COUNT);

						coreProps.add(WebLogicMBeanPropConstants.CONSUMERS_CURRENT_COUNT);
						coreProps.add(WebLogicMBeanPropConstants.CONSUMERS_HIGH_COUNT);
						coreProps.add(WebLogicMBeanPropConstants.CONSUMERS_TOTAL_COUNT);
						*/
                		
						return getJMSServerDashboard(conn, resource);
                		
                	case MonitorProperties.SAF_DASHBOARD_RESOURCE_TYPE:
                		
                		/*
                		coreProps.add(WebLogicMBeanPropConstants.NAME);
                		coreProps.add(WebLogicMBeanPropConstants.MESSAGES_CURRENT_COUNT);
        				coreProps.add(WebLogicMBeanPropConstants.MESSAGES_PENDING_COUNT);
						coreProps.add(WebLogicMBeanPropConstants.MESSAGES_RECEIVED_COUNT);
						coreProps.add(WebLogicMBeanPropConstants.MESSAGES_HIGH_COUNT);
                		
						coreProps.add(WebLogicMBeanPropConstants.FAILED_MESSAGES_TOTAL);
                		coreProps.add(WebLogicMBeanPropConstants.DOWNTIME_HIGH);
        				coreProps.add(WebLogicMBeanPropConstants.DOWNTIME_TOTAL);
						coreProps.add(WebLogicMBeanPropConstants.UPTIME_HIGH);
						coreProps.add(WebLogicMBeanPropConstants.UPTIME_TOTAL);
						*/
                		
                		return getSAFAgentDashboard(conn, resource);
                }
            }
                        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param data
     * @param startTime
     * @param endTime
     */
    private void addMissingData(Map<String,List<Map>> data,DateTime startTime,DateTime endTime){
        for (List<Map> list : data.values()) {
            for (Map map:list) {
                List<BigDecimal[]> dataList = (List<BigDecimal[]>)map.get("data");
                DateTime inter = startTime;
                // Loop through each day in the span
                while (inter.compareTo(endTime) < 0) {
                    // Go to next
                    inter = inter.plusHours(1);
                    if (!isDatePresent(inter,dataList)){
                        dataList.add(new BigDecimal[]{BigDecimal.valueOf(inter.getMillis()),null});
                    }
                }
                Collections.sort(dataList, new Comparator<BigDecimal[]>() {
                    @Override
                    public int compare(BigDecimal[] o1, BigDecimal[] o2) {
                        return o1[0].compareTo(o2[0]);
                    }
                });
            }
        }

    }

    private boolean isDatePresent(DateTime date,List<BigDecimal[]> dataList){
        int year = date.getYear();
        int month = date.getMonthOfYear();
        int day = date.getDayOfMonth();
        int hour = date.getHourOfDay();

        for (BigDecimal[] val : dataList) {
            BigDecimal timeasBD = (BigDecimal) val[0];
            DateTime datetime = new DateTime(timeasBD.longValue());
            int yearDT = datetime.getYear();
            int monthDT = datetime.getMonthOfYear();
            int dayDT = datetime.getDayOfMonth();
            int hourDT = datetime.getHourOfDay();
            if (year==yearDT && monthDT==month && dayDT==day && hourDT==hour )
                return true;
        }

        return false;
    }


    @GET
    @Path("test")
    @Produces({MediaType.APPLICATION_JSON})
    public List<List> getStatss(@QueryParam("startTime") String startTime,
                                @QueryParam("endTime") String endTime) {
    	
        //JSONConfiguration.mapped().rootUnwrapping(false).build();
        DateTime start = fmt.parseDateTime(startTime);
        DateTime end = fmt.parseDateTime(endTime);
        Interval interval = new Interval(start, end);
        //JSONConfiguration.mapped().rootUnwrapping(true).build();
        Statistics stat = new Statistics();
        stat.setA("a");
        stat.setB("b");
        List res = new ArrayList();
        List l1 = new ArrayList();
        List l2 = new ArrayList();
        l1.add(startTime);
        l1.add(100);
        l2.add(endTime);
        l2.add(101);
        res.add(l1);
        res.add(l2);
        return res;
    }
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @return
     */
    public Map<String, Map<String, String>> getJMSServerDashboard(DomainRuntimeServiceMBeanConnection conn, String jmsServerName){
		
    	Map<String, Map<String, String>> result = new LinkedHashMap<>();
    	
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			// Find the Admin server
			for (int index = 0; index < serverRuntimes.length; index++){
				if(DomainRuntimeServiceMBeanConnection.isThisTheAdminServer()){
			
					ObjectName serverRuntime = serverRuntimes[index]; 
			    	ObjectName jmsRuntime = conn.getChild(serverRuntime, JMS_RUNTIME);			    	
			        ObjectName[] jmsServers = conn.getChildren(jmsRuntime, JMS_SERVERS);
			        
			        for (ObjectName jmsServer : jmsServers)
			        {
			        	String currentJmsServerName = conn.getTextAttr(jmsServer, NAME);
			        	if(currentJmsServerName.equals(jmsServerName)){
			        		
			        		Map<String, String> metrics = new LinkedHashMap<String, String>();
			        		for (ObjectName destination : conn.getChildren(jmsServer, DESTINATIONS)) {
						    	
			        			String destinationName = ResourceNameNormaliser.normalise(JMSSVR_RESOURCE_TYPE, conn.getTextAttr(destination, NAME));
			        						        			
			        			metrics.put(MESSAGES_CURRENT_COUNT, new Integer((int)conn.getNumberAttr(destination, MESSAGES_CURRENT_COUNT)).toString());
			        			metrics.put(MESSAGES_PENDING_COUNT, new Integer((int)conn.getNumberAttr(destination, MESSAGES_PENDING_COUNT)).toString());
			        			metrics.put(MESSAGES_RECEIVED_COUNT, new Integer((int)conn.getNumberAttr(destination, MESSAGES_RECEIVED_COUNT)).toString());
			        			metrics.put(MESSAGES_HIGH_COUNT, new Integer((int)conn.getNumberAttr(destination, MESSAGES_HIGH_COUNT)).toString());
								
			        			metrics.put(CONSUMERS_CURRENT_COUNT, new Integer((int)conn.getNumberAttr(destination, CONSUMERS_CURRENT_COUNT)).toString());
			        			metrics.put(CONSUMERS_HIGH_COUNT, new Integer((int)conn.getNumberAttr(destination, CONSUMERS_HIGH_COUNT)).toString());
			        			metrics.put(CONSUMERS_TOTAL_COUNT, new Integer((int)conn.getNumberAttr(destination, CONSUMERS_TOTAL_COUNT)).toString());
			        			
			        			result.put(destinationName, metrics);
			        		}
			        	}			    
			        }
				}
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during generation of JMS dashboard");
		}
		return result;
	}
	
    /**
     * 
     * @param conn
     * @param safAgentName
     * @return
     */
    public Map<String, Map<String, String>> getSAFAgentDashboard(DomainRuntimeServiceMBeanConnection conn, String safAgentName){
		
		Map<String, Map<String, String>> result = new LinkedHashMap<>();
		
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			// Find the Admin server
			for (int index = 0; index < serverRuntimes.length; index++){
				if(DomainRuntimeServiceMBeanConnection.isThisTheAdminServer()){
			
					ObjectName serverRuntime = serverRuntimes[index]; 
			    	ObjectName safRuntime = conn.getChild(serverRuntime, SAF_RUNTIME);			    	
			        ObjectName[] safAgents = conn.getChildren(safRuntime, AGENTS);
			        
			        for (ObjectName safAgent : safAgents)
			        {			        	
			        	String currentSafAgentName = conn.getTextAttr(safAgent, NAME);
			        	if(currentSafAgentName.equals(safAgentName)){

			        		Map<String, String> metrics = new LinkedHashMap<String, String>();
			        		for (ObjectName destination : conn.getChildren(safAgent, REMOTE_END_POINTS)) {
			        			
						    	String destinationName = ResourceNameNormaliser.normalise(SAFAGENT_RESOURCE_TYPE, conn.getTextAttr(destination, NAME));
						    							    	
								metrics.put(MESSAGES_CURRENT_COUNT, new Integer((int)conn.getNumberAttr(destination, MESSAGES_CURRENT_COUNT)).toString());
			        			metrics.put(MESSAGES_PENDING_COUNT, new Integer((int)conn.getNumberAttr(destination, MESSAGES_PENDING_COUNT)).toString());
			        			metrics.put(MESSAGES_RECEIVED_COUNT, new Integer((int)conn.getNumberAttr(destination, MESSAGES_RECEIVED_COUNT)).toString());
			        			metrics.put(MESSAGES_HIGH_COUNT, new Integer((int)conn.getNumberAttr(destination, MESSAGES_HIGH_COUNT)).toString());
								
			        			metrics.put(DOWNTIME_HIGH, new Integer((int)conn.getNumberAttr(destination, DOWNTIME_HIGH)).toString());
								metrics.put(DOWNTIME_TOTAL, new Integer((int)conn.getNumberAttr(destination, DOWNTIME_TOTAL)).toString());
								metrics.put(UPTIME_HIGH, new Integer((int)conn.getNumberAttr(destination, UPTIME_HIGH)).toString());
								metrics.put(UPTIME_TOTAL, new Integer((int)conn.getNumberAttr(destination, UPTIME_TOTAL)).toString());
								metrics.put(FAILED_MESSAGES_TOTAL, new Integer((int)conn.getNumberAttr(destination, FAILED_MESSAGES_TOTAL)).toString());
								
			        			result.put(destinationName, metrics);
			        		}
			        	}			    
			        }
				}
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during generation of SAF dashboard");
		}
		return result;
	}
}