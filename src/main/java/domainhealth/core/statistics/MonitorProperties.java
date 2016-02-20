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
package domainhealth.core.statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;

/**
 * Map of WebLogic MBean property names and their associated attributes 
 * (property name, property title, property units). Also provides constants
 * listing all the attributes that should be monitored.
 */
public class MonitorProperties {
	/**
	 * Gets the key for a property in the list
	 * 
	 * @param prop The property name
	 * @return The property key
	 */
	public static String key(String prop) {
		WLProperty property = propList.get(prop);
		
		if (property == null) {
			return null;
		} else {
			return property.getKey();
		}
	}

	/**
	 * Gets the title for a property in the list
	 * 
	 * @param prop The property name
	 * @return The property title
	 */
	public static String title(String prop) {
		WLProperty property = propList.get(prop);
		
		if (property == null) {
			return null;
		} else {
			return property.getTitle();
		}
	}

	/**
	 * Gets the units for a property in the list
	 * 
	 * @param prop The property name
	 * @return The property units
	 */
	public static String units(String prop) {
		WLProperty property = propList.get(prop);
		
		if (property == null) {
			return null;
		} else {
			return property.getUnits();
		}
	}

	/**
	 * 'weblogic.management.runtime.%sMBean' MBean type template
	 */
	public final static String RUNTIME_MBEAN_TYPE_TEMPLATE = "weblogic.management.runtime.%sMBean";	

	/**
	 * 'com.bea:Name=weblogic.kernel.Default,ServerRuntime=%s,Type=WorkManagerRuntime' MBean name template
	 */
	public final static String WKMGR_MBEAN_NAME_TEMPLATE = "com.bea:Name=weblogic.kernel.Default,ServerRuntime=%s,Type=WorkManagerRuntime";	

	/**
	 * 'JMSDashboard' MBean instance name
	 */
	// Added by gregoan
	// Commented by gregoan
	//public final static String JMS_DASHBOARD_MBEAN_NAME = "JMSDashboard";

	/**
	 * 'jmsdashboard=%s,name=JMSDashboard' MBean name template
	 */
	// Added by gregoan
	// Commented by gregoan
	//public final static String JMS_DASHBOARD_MBEAN_FULLNAME_TEMPLATE = "jmsdashboard:Location=%s,name=" + JMS_DASHBOARD_MBEAN_NAME;
	
	/**
	 * 'WLHostMachineStats' MBean instance name
	 */
	public final static String HOST_MACHINE_MBEAN_NAME = "WLHostMachineStats";

	/**
	 * 'wlhostmachinestats:Location=%s,name=WLHostMachineStats' MBean name template
	 */
	public final static String HOST_MACHINE_MBEAN_FULLNAME_TEMPLATE = "wlhostmachinestats:Location=%s,name=" + HOST_MACHINE_MBEAN_NAME;

	/**
	 * 'WLJvmStats' MBean instance name
	 */
	// Added by gregoan
	public final static String JVM_MBEAN_NAME = "WLJvmStats";
	
	/**
	 * 'wljvmstats:Location=%s,name=WLJvmStats' MBean name template
	 */
	// Added by gregoan
	public final static String JVM_MBEAN_FULLNAME_TEMPLATE = "wljvmstats:Location=%s,name=" + JVM_MBEAN_NAME;
	
	/**
	 * Default Name of core resource - empty string ''
	 */
	public final static String CORE_RSC_DEFAULT_NAME = "";
	
	/**
	 * Name of the 'core' category of resource for core server statistics
	 */
	public final static String CORE_RESOURCE_TYPE = "core";
	
	/**
	 * Name of the 'datasource' category of resource for Data Source related statistics
	 */
	public final static String DATASOURCE_RESOURCE_TYPE = "datasource";
	
	/**
	 * Name of the 'destination' category of resource for Destination related statistics
	 */
	public final static String DESTINATION_RESOURCE_TYPE = "destination";
	
	/**
	 * Name of the 'saf' category of resource for Destination related statistics
	 */
	public final static String SAF_RESOURCE_TYPE = "saf";
	
	/**
	 * Name of the 'webapp' category of resource for Web App related statistics
	 */
	public final static String WEBAPP_RESOURCE_TYPE = "webapp";	
	
	/**
	 * Name of the 'ejb' category of resource for EJB related statistics
	 */
	public final static String EJB_RESOURCE_TYPE = "ejb";	
	
	/**
	 * Name of the 'workmgr' category of resource for Work Manager related statistics
	 */
	public final static String WORKMGR_RESOURCE_TYPE = "workmgr";	
	
	/**
	 * Name of the 'svrchnl' category of resource for Server Channel related statistics
	 */
	public final static String SVRCHNL_RESOURCE_TYPE = "svrchnl";	

	/**
	 * Name of the 'hostmachine' category of resource for WL Host Machine custom MBean related statistics
	 */
	public final static String HOSTMACHINE_RESOURCE_TYPE = "hostmachine";
	
	/**
	 * Name of the 'jmssrv' category of resource for JMSDashboard custom MBean related statistics
	 */
	// Added by gregoan
	public final static String JMSSVR_RESOURCE_TYPE = "jmssrv";
	
	/**
	 * Name of the 'safagent' category of resource for JMSDashboard custom MBean related statistics
	 */
	// Added by gregoan
	public final static String SAFAGENT_RESOURCE_TYPE = "safagent";
	
	/**
	 * Name of the 'jvm' category of resource for WL Host Machine custom MBean related statistics
	 */
	// Added by gregoan
	public final static String JVM_RESOURCE_TYPE = "jvm";
	
	/**
	 * Name of the 'jmsdashboard' category of resource for JMS Dashboard related statistics
	 */
	// Added by gregoan
	public final static String JMS_DASHBOARD_RESOURCE_TYPE = "jmsdashboard";
	
	/**
	 * Name of the 'safdashboard' category of resource for SAF Dashboard related statistics
	 */
	// Added by gregoan
	public final static String SAF_DASHBOARD_RESOURCE_TYPE = "safdashboard";

	/**
	 * List of names or allowable resource types (eg. core, datasource)
	 */
	// Updated by gregoan
	//public final static List<String> LEGAL_RESOURCE_TYPES = Arrays.asList(CORE_RESOURCE_TYPE, DATASOURCE_RESOURCE_TYPE, DESTINATION_RESOURCE_TYPE, SAF_RESOURCE_TYPE, WEBAPP_RESOURCE_TYPE, EJB_RESOURCE_TYPE, WORKMGR_RESOURCE_TYPE, SVRCHNL_RESOURCE_TYPE, HOSTMACHINE_RESOURCE_TYPE);
	public final static List<String> LEGAL_RESOURCE_TYPES = Arrays.asList(CORE_RESOURCE_TYPE, DATASOURCE_RESOURCE_TYPE, DESTINATION_RESOURCE_TYPE, SAF_RESOURCE_TYPE, WEBAPP_RESOURCE_TYPE, EJB_RESOURCE_TYPE, WORKMGR_RESOURCE_TYPE, SVRCHNL_RESOURCE_TYPE, HOSTMACHINE_RESOURCE_TYPE, JMSSVR_RESOURCE_TYPE, SAFAGENT_RESOURCE_TYPE, JVM_RESOURCE_TYPE, JMS_DASHBOARD_RESOURCE_TYPE, SAF_DASHBOARD_RESOURCE_TYPE);

	/**
	 * List of Server MBean Attributes to be monitored
	 */
	public final static String[] SERVER_MBEAN_MONITOR_ATTR_LIST = {SERVER_STATE, OPEN_SOCKETS_CURRENT_COUNT};	

	/**
	 * List of JVM MBean Attributes to be monitored
	 */
	public final static String[] JVM_MBEAN_MONITOR_ATTR_LIST = {HEAP_SIZE_CURRENT, HEAP_FREE_CURRENT, HEAP_FREE_PERCENT};
		
	/**
	 * List of Thread Pool MBean Attributes to be monitored
	 */
	public final static String[] THREADPOOL_MBEAN_MONITOR_ATTR_LIST = {EXECUTE_THREAD_TOTAL_COUNT, HOGGING_THREAD_COUNT, PENDING_USER_REQUEST_COUNT, THREAD_POOL_QUEUE_LENGTH, COMPLETED_REQUEST_COUNT, EXECUTE_THREAD_IDLE_COUNT, MIN_THREADS_CONSTRAINT_COMPLETED, MIN_THREADS_CONSTRAINT_PENDING, STANDBY_THREAD_COUNT, THROUGHPUT};

	/**
	 * List of JTA MBean Attributes to be monitored
	 */
	public final static String[] JTA_MBEAN_MONITOR_ATTR_LIST = {TRANSACTION_TOTAL_COUNT, TRANSACTION_COMMITTED_COUNT, TRANSACTION_ROLLEDBACK_COUNT, TRANSACTION_HEURISTICS_TOTAL_COUNT, TRANSACTION_ABANDONED_TOTAL_COUNT, TRANSACTIONS_ACTIVE_TOTAL_COUNT};

	/**
	 * List of JDBC Data Source MBean Attributes to be monitored
	 */
	public final static String[] JDBC_MBEAN_MONITOR_ATTR_LIST = {NUM_AVAILABLE, NUM_UNAVAILABLE, ACTIVE_CONNECTONS_CURRENT_COUNT, CONNECTION_DELAY_TIME, FAILED_RESERVE_REQUEST_COUNT, FAILURES_TO_RECONNECT_COUNT, LEAKED_CONNECTION_COUNT, WAITING_FOR_CONNECTION_CURRENT_COUNT, WAITING_FOR_CONNECTION_FAILURES_TOTAL, WAITING_SECONDS_HIGH_COUNT};

	/**
	 * List of JMS Destination MBean Attributes to be monitored
	 */
	public final static String[] JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST = {MESSAGES_CURRENT_COUNT, MESSAGES_PENDING_COUNT, MESSAGES_RECEIVED_COUNT, MESSAGES_HIGH_COUNT, CONSUMERS_CURRENT_COUNT, CONSUMERS_HIGH_COUNT, CONSUMERS_TOTAL_COUNT};
	
	/**
	 * List of SAFAgent MBean Attributes to be monitored
	 */
	public final static String[] SAF_AGENT_MBEAN_MONITOR_ATTR_LIST = {MESSAGES_CURRENT_COUNT, MESSAGES_PENDING_COUNT, MESSAGES_RECEIVED_COUNT, MESSAGES_HIGH_COUNT};

	/**
	 * List of WebApp MBean Attributes to be monitored
	 */
	public final static String[] WEBAPP_MBEAN_MONITOR_ATTR_LIST = {SESSIONS_CURRENT_COUNT, SESSIONS_HIGH_COUNT, SESSIONS_TOTAL_COUNT};

	/**
	 * List of EJB Pool Runtime MBean Attributes to be monitored
	 */
	public final static String[] EJB_POOL_MBEAN_MONITOR_ATTR_LIST = {BEANS_POOLED_CURRENT_COUNT, BEAN_ACCESS_TOTAL_COUNT, BEANS_INUSE_CURRENT_COUNT, BEAN_WAITING_CURRENT_COUNT, BEAN_WAITING_TOTAL_COUNT};	

	/**
	 * List of EJB Transaction Runtime MBean Attributes to be monitored
	 */
	public final static String[] EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST = {BEAN_TRANSACTIONS_COMMITTED_TOTAL_COUNT, BEAN_TRANSACTIONS_ROLLEDBACK_TOTAL_COUNT, BEAN_TRANSACTIONS_TIMEDOUT_TOTAL_COUNT};	

	/**
	 * List of EJB MBean Attributes to be monitored
	 */
	public final static String[] EJB_MBEAN_MONITOR_ATTR_LIST = new String[EJB_POOL_MBEAN_MONITOR_ATTR_LIST.length + EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST.length];	
	static {
		System.arraycopy(EJB_POOL_MBEAN_MONITOR_ATTR_LIST, 0, EJB_MBEAN_MONITOR_ATTR_LIST, 0, EJB_POOL_MBEAN_MONITOR_ATTR_LIST.length);
		System.arraycopy(EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST, 0, EJB_MBEAN_MONITOR_ATTR_LIST, EJB_POOL_MBEAN_MONITOR_ATTR_LIST.length, EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST.length);
	}
	
	/**
	 * List of Work Manager MBean Attributes to be monitored
	 */
	public final static String[] WKMGR_MBEAN_MONITOR_ATTR_LIST = {COMPLETED_REQUESTS, PENDING_REQUESTS, STUCK_THREAD_COUNT};

	/**
	 * List of Server Channel MBean Attributes to be monitored
	 */
	public final static String[] SVR_CHANNEL_MBEAN_MONITOR_ATTR_LIST = {ACCEPT_COUNT, CONNECTIONS_COUNT, CHNL_MESSAGES_RECEIVED_COUNT, CHNL_MESSAGES_SENT_COUNT};	
	
	/**
	 * List of WLHOSTMACHINESTATS MBean Attributes to be monitored
	 */
	// Updated by gregoan
	//public final static String[] HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST = {JVM_INSTANCE_CORES_USED, JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES, NATIVE_PROCESSES_COUNT, NETWORK_RX_MEGABYTES, NETWORK_RX_DROPPED, NETWORK_RX_ERRORS, NETWORK_RX_FRAME, NETWORK_RX_OVERRUNS, NETWORK_MILLIONS_RX_PACKETS, NETWORK_TX_MEGABYTES, NETWORK_TX_CARRIER, NETWORK_TX_COLLISIONS,NETWORK_TX_DROPPED, NETWORK_TX_ERRORS, NETWORK_TX_OVERRUNS, NETWORK_MILLIONS_TX_PACKETS, PHYSICAL_MEMORY_USED_PERCENT, PHYSICAL_SWAP_USED_PERCENT, PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE, PROCESSOR_USAGE_PERCENT, ROOT_FILESYSTEM_USED_PERCENT, TCP_CLOSE_WAIT_COUNT, TCP_ESTABLISHED_COUNT, TCP_LISTEN_COUNT, TCP_TIME_WAIT_COUNT};
	public final static String[] HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST = {JVM_INSTANCE_CORES_USED, JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES, NATIVE_PROCESSES_COUNT, NETWORK_RX_MEGABYTES, NETWORK_RX_DROPPED, NETWORK_RX_ERRORS, NETWORK_RX_FRAME, NETWORK_RX_OVERRUNS, NETWORK_MILLIONS_RX_PACKETS, NETWORK_TX_MEGABYTES, NETWORK_TX_CARRIER, NETWORK_TX_COLLISIONS,NETWORK_TX_DROPPED, NETWORK_TX_ERRORS, NETWORK_TX_OVERRUNS, NETWORK_MILLIONS_TX_PACKETS, PHYSICAL_MEMORY_USED_PERCENT, PHYSICAL_SWAP_USED_PERCENT, PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE, PROCESSOR_USAGE_PERCENT, ROOT_FILESYSTEM_USED_PERCENT, TCP_CLOSE_WAIT_COUNT, TCP_ESTABLISHED_COUNT, TCP_LISTEN_COUNT, TCP_TIME_WAIT_COUNT, AVAILABLE_PROCESSORS, SYSTEM_LOAD_AVERAGE,	COMMITTED_VIRTUAL_MEMORY_SIZE_MEGABYTES, FREE_PHYSICAL_MEMORY_SIZE_MEGABYTES, FREE_SWAP_SPACE_SIZE_MEGABYTES, MAX_FILE_DESCRIPTOR_COUNT, OPEN_FILE_DESCRIPTOR_COUNT, PROCESS_CPU_LOAD, PROCESS_CPU_TIME, SYSTEM_CPU_LOAD, TOTAL_PHYSICAL_MEMORY_SIZE_MEGABYTES, TOTAL_SWAP_SPACE_SIZE_MEGABYTES};
	
	/**
	 * List of JVM MBean Attributes to be monitored (java version)
	 */
	// Added by gregoan
	public final static String[] JAVA_JVM_MBEAN_MONITOR_ATTR_LIST = {HEAP_MEMORY_INIT, HEAP_MEMORY_USED, HEAP_MEMORY_COMMITTED, HEAP_MEMORY_MAX, NON_HEAP_MEMORY_INIT, NON_HEAP_MEMORY_USED, NON_HEAP_MEMORY_COMMITTED, NON_HEAP_MEMORY_MAX, EDEN_SPACE_INIT, EDEN_SPACE_USED, EDEN_SPACE_COMMITTED, EDEN_SPACE_MAX, SURVIVOR_SPACE_INIT, SURVIVOR_SPACE_USED, SURVIVOR_SPACE_COMMITTED, SURVIVOR_SPACE_MAX, TENURED_GEN_INIT, TENURED_GEN_USED, TENURED_GEN_COMMITTED, TENURED_GEN_MAX, PERM_GEN_INIT, PERM_GEN_USED, PERM_GEN_COMMITTED, PERM_GEN_MAX};
	//public final static String[] JAVA_JVM_MBEAN_MONITOR_ATTR_LIST = {HEAP_MEMORY_INIT, HEAP_MEMORY_USED, HEAP_MEMORY_COMMITTED, HEAP_MEMORY_MAX, NON_HEAP_MEMORY_INIT, NON_HEAP_MEMORY_USED, NON_HEAP_MEMORY_COMMITTED, NON_HEAP_MEMORY_MAX};
	/**
	 * List of JMS dashboard MBean Attributes to be monitored
	 */
	// Added by gregoan
	public final static String[] JMS_DASHBOARD_MBEAN_MONITOR_ATTR_LIST = {NAME, MESSAGES_CURRENT_COUNT, MESSAGES_PENDING_COUNT, MESSAGES_RECEIVED_COUNT, MESSAGES_HIGH_COUNT, CONSUMERS_CURRENT_COUNT, CONSUMERS_HIGH_COUNT, CONSUMERS_TOTAL_COUNT};
	
	/**
	 * List of SAF dashboard MBean Attributes to be monitored
	 */
	// Added by gregoan
	public final static String[] SAF_DASHBOARD_MBEAN_MONITOR_ATTR_LIST = {NAME, MESSAGES_CURRENT_COUNT, MESSAGES_PENDING_COUNT, MESSAGES_RECEIVED_COUNT, MESSAGES_HIGH_COUNT, FAILED_MESSAGES_TOTAL, DOWNTIME_HIGH, DOWNTIME_TOTAL, UPTIME_HIGH, UPTIME_TOTAL};
	
	// Added by gregoan
	
	// -----------------------------------------------------------------
	// JMS actions on queues
	// ---------------------
	public final static String PAUSE_PRODUCTON = "pauseProduction";
	public final static String RESUME_PRODUCTON = "resumeProduction";
	public final static String PAUSE_CONSUMPTION = "pauseConsumption";
	public final static String RESUME_CONSUMPTION = "resumeConsumption";
	public final static String PAUSE_INSERTION = "pauseInsertion";
	public final static String RESUME_INSERTION = "resumeInsertion";
	// -----------------------------------------------------------------
	
	/**
	 * 'weblogic.kernel.Default' Default Work Manager name
	 */
	public static final String DEFAULT_WKMGR_NAME = "weblogic.kernel.Default";

	/**
	 * List of internal webapp & ejb names which should not be monitored
	 */
	public final static List<String> XAPPNAME_BLACKLIST = Arrays.asList("domainhealth", "console", "consolehelp", "bea_wls9_async_response", "bea_wls_cluster_internal", "bea_wls_deployment_internal", "bea_wls_internal", "_async", "Mejb", "bea_wls_diagnostics", "bea_wls_management_internal", "bea_wls_management_internal2", "uddi", "uddiexplorer", "wls-wsat");	

	/**
	 * "Time" property value units type
	 */
	public final static String TIME_UNITS = "Time";
	
	/**
	 * "Number" property value units type
	 */
	public final static String NUMBER_UNITS = "Number";

	/**
	 * "Number (millions)" property value units type
	 */
	public final static String NUMBER_MILLION_UNITS = "Number (millions)";

	/**
	 * "Requests per second" property value units type
	 */
	public final static String REQS_PER_SEC_UNITS = "Requests per second";

	/**
	 * "State" property value units type
	 */
	public final static String STATE_UNITS = "State";

	/**
	 * "Megabytes" property value units type
	 */
	public final static String MEGABYTES_UNITS = "Megabytes";

	/**
	 * "Bytes" property value units type
	 */
	public final static String BYTES_UNITS = "Bytes";

	/**
	 * "Percent" property value units type
	 */
	public final static String PERCENT_UNITS = "Percent";

	/**
	 * "Seconds" property value units type
	 */
	public final static String SECONDS_UNITS = "Seconds";

	/**
	 * "Milliseconds" property value units type
	 */
	public final static String MILLISECONDS_UNITS = "Milliseconds";

	/**
	 * "Name" property value units type
	 */
	public final static String NAME_UNITS = "Name";
	
	
	
	
	
	/**
	 * The 'showhosts' pamameter to indicate whether to show the 'Hosts' link 
	 * in the top menu.
	 */
	public final static String SHOW_HOSTS_PARAM = "showhosts";
	
	/**
	 * The 'showdashboards' pamameter to indicate whether to show the 'JMS Dashboard' link 
	 * in the top menu.
	 */
	public final static String SHOW_DASHBOARDS_PARAM = "showdashboards";
	
	/*
	// Servlet should be created with this piece of code
	
		// Indicate to only show Hosts top menu link if any data has been retrieved from WLHostMachineStats custom mbeans
		Set<String> hostmachinesSet = statisticsStorage.getResourceNamesFromPropsList(endDateTime, HOSTMACHINE_RESOURCE_TYPE);
		request.setAttribute(SHOW_HOSTS_PARAM, ((hostmachinesSet != null) && (!hostmachinesSet.isEmpty())));
		
		// Indicate to only show Jvm top menu link if any data has been retrieved from WLJvmStats custom mbeans
		Set<String> jvmSet = statisticsStorage.getResourceNamesFromPropsList(endDateTime, JVM_RESOURCE_TYPE);
		request.setAttribute(SHOW_HOSTS_PARAM, ((jvmSet != null) && (!jvmSet.isEmpty())));
		
		// Indicate to only show Hosts top menu link if any data has been retrieved from WLHostMachineStats custom mbeans
		boolean showDashboard = appProps.getBoolProperty(PropKey.SHOW_DASHBOARD_PROP);
		if(showDashboard){
			request.setAttribute(SHOW_DASHBOARDS_PARAM, true);
		}
	*/
	
	

	
	
	
	
	// Constants
	private final static Map<String, WLProperty> propList = new HashMap<String, WLProperty>();
	
	// Static initialiser of the map of WebLogic properties
	static {
		// Add Time property
		propList.put(DATE_TIME, new WLProperty(DATE_TIME, "Date-Time", TIME_UNITS));

		// Add Core properties
		propList.put(SERVER_STATE, new WLProperty(SERVER_STATE, "Server State", STATE_UNITS));
		propList.put(OPEN_SOCKETS_CURRENT_COUNT, new WLProperty(OPEN_SOCKETS_CURRENT_COUNT, "Open Sockets", NUMBER_UNITS));
		
		propList.put(HEAP_SIZE_CURRENT, new WLProperty(HEAP_SIZE_CURRENT, "Heap Size", MEGABYTES_UNITS)); 
		propList.put(HEAP_FREE_CURRENT, new WLProperty(HEAP_FREE_CURRENT, "Heap Free", MEGABYTES_UNITS)); 
		propList.put(HEAP_USED_CURRENT, new WLProperty(HEAP_USED_CURRENT, "Heap Used", MEGABYTES_UNITS)); 
		propList.put(HEAP_FREE_PERCENT, new WLProperty(HEAP_FREE_PERCENT, "Heap Free", PERCENT_UNITS));
		
		propList.put(EXECUTE_THREAD_TOTAL_COUNT, new WLProperty(EXECUTE_THREAD_TOTAL_COUNT, "Thread Pool Execute Threads", NUMBER_UNITS)); 
		propList.put(HOGGING_THREAD_COUNT, new WLProperty(HOGGING_THREAD_COUNT, "Thread Pool Hogging Threads", NUMBER_UNITS)); 
		propList.put(PENDING_USER_REQUEST_COUNT, new WLProperty(PENDING_USER_REQUEST_COUNT, "Thread Pool Pending User Requests", NUMBER_UNITS)); 
		propList.put(THREAD_POOL_QUEUE_LENGTH, new WLProperty(THREAD_POOL_QUEUE_LENGTH, "Thread Pool Queue Length", NUMBER_UNITS)); 
		propList.put(COMPLETED_REQUEST_COUNT, new WLProperty(COMPLETED_REQUEST_COUNT, "Thread Pool Completed Requests", NUMBER_UNITS)); 
		propList.put(EXECUTE_THREAD_IDLE_COUNT, new WLProperty(EXECUTE_THREAD_IDLE_COUNT, "Thread Pool Idle Threads", NUMBER_UNITS)); 
		propList.put(MIN_THREADS_CONSTRAINT_COMPLETED, new WLProperty(MIN_THREADS_CONSTRAINT_COMPLETED, "Thread Pool Min Threads Constraint Completed", NUMBER_UNITS)); 
		propList.put(MIN_THREADS_CONSTRAINT_PENDING, new WLProperty(MIN_THREADS_CONSTRAINT_PENDING, "Thread Pool Min Threads Constraint Pending", NUMBER_UNITS)); 
		propList.put(STANDBY_THREAD_COUNT, new WLProperty(STANDBY_THREAD_COUNT, "Thread Pool Standby Threads", NUMBER_UNITS));
		propList.put(THROUGHPUT, new WLProperty(THROUGHPUT, "Thread Pool Throughput", REQS_PER_SEC_UNITS)); 		
		propList.put(TRANSACTION_TOTAL_COUNT, new WLProperty(TRANSACTION_TOTAL_COUNT, "Transactions Total", NUMBER_UNITS)); 
		propList.put(TRANSACTION_COMMITTED_COUNT, new WLProperty(TRANSACTION_COMMITTED_COUNT, "Transaction Committed", NUMBER_UNITS)); 
		propList.put(TRANSACTION_ROLLEDBACK_COUNT, new WLProperty(TRANSACTION_ROLLEDBACK_COUNT, "Transaction RolledBack", NUMBER_UNITS)); 
		propList.put(TRANSACTION_HEURISTICS_TOTAL_COUNT, new WLProperty(TRANSACTION_HEURISTICS_TOTAL_COUNT, "Transaction Heuristics", NUMBER_UNITS)); 
		propList.put(TRANSACTION_ABANDONED_TOTAL_COUNT, new WLProperty(TRANSACTION_ABANDONED_TOTAL_COUNT, "Transaction Abandoned", NUMBER_UNITS)); 
		propList.put(TRANSACTIONS_ACTIVE_TOTAL_COUNT, new WLProperty(TRANSACTIONS_ACTIVE_TOTAL_COUNT, "Transactions Active", NUMBER_UNITS));

		// Add JDBC Data Source properties
		propList.put(NUM_AVAILABLE, new WLProperty(NUM_AVAILABLE, "Number Available", NUMBER_UNITS)); 
		propList.put(NUM_UNAVAILABLE, new WLProperty(NUM_UNAVAILABLE, "Number Unavailable", NUMBER_UNITS)); 
		propList.put(ACTIVE_CONNECTONS_CURRENT_COUNT, new WLProperty(ACTIVE_CONNECTONS_CURRENT_COUNT, "Active Connections", NUMBER_UNITS)); 
		propList.put(CONNECTION_DELAY_TIME, new WLProperty(CONNECTION_DELAY_TIME, "Average Connection Delay", MILLISECONDS_UNITS)); 
		propList.put(FAILED_RESERVE_REQUEST_COUNT, new WLProperty(FAILED_RESERVE_REQUEST_COUNT, "Failed Reserve Request", NUMBER_UNITS)); 
		propList.put(FAILURES_TO_RECONNECT_COUNT, new WLProperty(FAILURES_TO_RECONNECT_COUNT, "Failures To Reconnect", NUMBER_UNITS)); 
		propList.put(LEAKED_CONNECTION_COUNT, new WLProperty(LEAKED_CONNECTION_COUNT, "Leaked Connections", NUMBER_UNITS)); 
		propList.put(WAITING_FOR_CONNECTION_CURRENT_COUNT, new WLProperty(WAITING_FOR_CONNECTION_CURRENT_COUNT, "Waiting For Connection Current", NUMBER_UNITS)); 
		propList.put(WAITING_FOR_CONNECTION_FAILURES_TOTAL, new WLProperty(WAITING_FOR_CONNECTION_FAILURES_TOTAL, "Waiting For Connection Failures", NUMBER_UNITS)); 
		propList.put(WAITING_SECONDS_HIGH_COUNT, new WLProperty(WAITING_SECONDS_HIGH_COUNT, "Wait Seconds High", SECONDS_UNITS)); 				

		// Add JMS Destination properties
		propList.put(MESSAGES_CURRENT_COUNT, new WLProperty(MESSAGES_CURRENT_COUNT, "Messages Current", NUMBER_UNITS)); 
		propList.put(MESSAGES_PENDING_COUNT, new WLProperty(MESSAGES_PENDING_COUNT, "Messages Pending", NUMBER_UNITS)); 
		propList.put(MESSAGES_RECEIVED_COUNT, new WLProperty(MESSAGES_RECEIVED_COUNT, "Messages Received", NUMBER_UNITS)); 
		propList.put(MESSAGES_HIGH_COUNT, new WLProperty(MESSAGES_HIGH_COUNT, "Messages High", NUMBER_UNITS)); 
		propList.put(CONSUMERS_CURRENT_COUNT, new WLProperty(CONSUMERS_CURRENT_COUNT, "Consumers Current", NUMBER_UNITS)); 
		propList.put(CONSUMERS_HIGH_COUNT, new WLProperty(CONSUMERS_HIGH_COUNT, "Consumers High", NUMBER_UNITS));
		propList.put(CONSUMERS_TOTAL_COUNT, new WLProperty(CONSUMERS_TOTAL_COUNT, "Consumers Total", NUMBER_UNITS));
		
		// Add WebApp properties
		propList.put(SESSIONS_CURRENT_COUNT, new WLProperty(SESSIONS_CURRENT_COUNT, "Open Sessions Current", NUMBER_UNITS)); 
		propList.put(SESSIONS_HIGH_COUNT, new WLProperty(SESSIONS_HIGH_COUNT, "Open Sessions High", NUMBER_UNITS)); 
		propList.put(SESSIONS_TOTAL_COUNT, new WLProperty(SESSIONS_TOTAL_COUNT, "Open Sessions Total", NUMBER_UNITS)); 

		// Add EJB properties
		propList.put(BEANS_POOLED_CURRENT_COUNT, new WLProperty(BEANS_POOLED_CURRENT_COUNT, "Pooled Instances Current", NUMBER_UNITS)); 
		propList.put(BEAN_ACCESS_TOTAL_COUNT, new WLProperty(BEAN_ACCESS_TOTAL_COUNT, "Bean Access Total", NUMBER_UNITS)); 
		propList.put(BEANS_INUSE_CURRENT_COUNT, new WLProperty(BEANS_INUSE_CURRENT_COUNT, "Instances In Use Current", NUMBER_UNITS)); 
		propList.put(BEAN_WAITING_CURRENT_COUNT, new WLProperty(BEAN_WAITING_CURRENT_COUNT, "Waiting For Instance Current", NUMBER_UNITS)); 
		propList.put(BEAN_WAITING_TOTAL_COUNT, new WLProperty(BEAN_WAITING_TOTAL_COUNT, "Waiting For Instances Total", NUMBER_UNITS)); 
		propList.put(BEAN_TRANSACTIONS_COMMITTED_TOTAL_COUNT, new WLProperty(BEAN_TRANSACTIONS_COMMITTED_TOTAL_COUNT, "Transactions Committed", NUMBER_UNITS)); 
		propList.put(BEAN_TRANSACTIONS_ROLLEDBACK_TOTAL_COUNT, new WLProperty(BEAN_TRANSACTIONS_ROLLEDBACK_TOTAL_COUNT, "Transactions Rolledback", NUMBER_UNITS)); 
		propList.put(BEAN_TRANSACTIONS_TIMEDOUT_TOTAL_COUNT, new WLProperty(BEAN_TRANSACTIONS_TIMEDOUT_TOTAL_COUNT, "Transactions Timedout", NUMBER_UNITS)); 
				
		// Add Work Manager properties
		propList.put(COMPLETED_REQUESTS, new WLProperty(COMPLETED_REQUESTS, "Completed Requests", NUMBER_UNITS)); 
		propList.put(PENDING_REQUESTS, new WLProperty(PENDING_REQUESTS, "Pending Requests", NUMBER_UNITS)); 
		propList.put(STUCK_THREAD_COUNT, new WLProperty(STUCK_THREAD_COUNT, "Stuck Threads", NUMBER_UNITS)); 		

		// Add Server Channel properties
		propList.put(ACCEPT_COUNT, new WLProperty(ACCEPT_COUNT, "Accept Count", NUMBER_UNITS)); 	
		propList.put(CONNECTIONS_COUNT, new WLProperty(CONNECTIONS_COUNT, "Connections Count", NUMBER_UNITS)); 	
		propList.put(CHNL_MESSAGES_RECEIVED_COUNT, new WLProperty(CHNL_MESSAGES_RECEIVED_COUNT, "Messages Received", NUMBER_UNITS)); 	
		propList.put(CHNL_MESSAGES_SENT_COUNT, new WLProperty(CHNL_MESSAGES_SENT_COUNT, "Messages Sent", NUMBER_UNITS));

		// Add WLHostMachineStats properties (optional deployed custom MBean)
		// -> SIGAR implementation
		propList.put(JVM_INSTANCE_CORES_USED, new WLProperty(JVM_INSTANCE_CORES_USED, "JVM Instance Cores Used", NUMBER_UNITS)); 	
		propList.put(JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES, new WLProperty(JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES, "JVM Instance Physical Memory Used", MEGABYTES_UNITS)); 	
		propList.put(NATIVE_PROCESSES_COUNT, new WLProperty(NATIVE_PROCESSES_COUNT, "Native Processes Count", NUMBER_UNITS));
		propList.put(NETWORK_RX_MEGABYTES, new WLProperty(NETWORK_RX_MEGABYTES, "Network Rx Size", MEGABYTES_UNITS)); 	
		propList.put(NETWORK_RX_DROPPED, new WLProperty(NETWORK_RX_DROPPED, "Network Rx Dropped", NUMBER_UNITS)); 	
		propList.put(NETWORK_RX_ERRORS, new WLProperty(NETWORK_RX_ERRORS, "Network Rx Errors", NUMBER_UNITS)); 	
		propList.put(NETWORK_RX_FRAME, new WLProperty(NETWORK_RX_FRAME, "Network Rx Frame", NUMBER_UNITS)); 	
		propList.put(NETWORK_RX_OVERRUNS, new WLProperty(NETWORK_RX_OVERRUNS, "Network Rx Overruns", NUMBER_UNITS)); 	
		propList.put(NETWORK_MILLIONS_RX_PACKETS, new WLProperty(NETWORK_MILLIONS_RX_PACKETS, "Network Rx Packets", NUMBER_MILLION_UNITS)); 	
		propList.put(NETWORK_TX_MEGABYTES, new WLProperty(NETWORK_TX_MEGABYTES, "Network Tx Size", MEGABYTES_UNITS)); 	
		propList.put(NETWORK_TX_CARRIER, new WLProperty(NETWORK_TX_CARRIER, "Network Tx Carrier", NUMBER_UNITS)); 	
		propList.put(NETWORK_TX_COLLISIONS, new WLProperty(NETWORK_TX_COLLISIONS, "NetworkTxCollisions", NUMBER_UNITS)); 	
		propList.put(NETWORK_TX_DROPPED, new WLProperty(NETWORK_TX_DROPPED, "Network Tx Dropped", NUMBER_UNITS)); 	
		propList.put(NETWORK_TX_ERRORS, new WLProperty(NETWORK_TX_ERRORS, "Network Tx Errors", NUMBER_UNITS)); 	
		propList.put(NETWORK_TX_OVERRUNS, new WLProperty(NETWORK_TX_OVERRUNS, "Network Tx Overruns", NUMBER_UNITS)); 	
		propList.put(NETWORK_MILLIONS_TX_PACKETS, new WLProperty(NETWORK_MILLIONS_TX_PACKETS, "Network Tx Packets", NUMBER_MILLION_UNITS)); 	
		propList.put(PHYSICAL_MEMORY_USED_PERCENT, new WLProperty(PHYSICAL_MEMORY_USED_PERCENT, "Physical Memory Used", PERCENT_UNITS)); 	
		propList.put(PHYSICAL_SWAP_USED_PERCENT, new WLProperty(PHYSICAL_SWAP_USED_PERCENT, "Physical Swap Used", PERCENT_UNITS)); 	
		propList.put(PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE, new WLProperty(PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE, "Average Processor Workload", NUMBER_UNITS)); 	
		propList.put(PROCESSOR_USAGE_PERCENT, new WLProperty(PROCESSOR_USAGE_PERCENT, "Processor Usage", PERCENT_UNITS)); 	
		propList.put(ROOT_FILESYSTEM_USED_PERCENT, new WLProperty(ROOT_FILESYSTEM_USED_PERCENT, "Root Filesystem Used", PERCENT_UNITS)); 	
		propList.put(TCP_CLOSE_WAIT_COUNT, new WLProperty(TCP_CLOSE_WAIT_COUNT, "Tcp Close Wait Count", NUMBER_UNITS)); 	
		propList.put(TCP_ESTABLISHED_COUNT, new WLProperty(TCP_ESTABLISHED_COUNT, "Tcp Established Count", NUMBER_UNITS)); 	
		propList.put(TCP_LISTEN_COUNT, new WLProperty(TCP_LISTEN_COUNT, "Tcp Listen Count", NUMBER_UNITS)); 	
		propList.put(TCP_TIME_WAIT_COUNT, new WLProperty(TCP_TIME_WAIT_COUNT, "Tcp Time Wait Count", NUMBER_UNITS));
		
		// Added by gregoan
		// -> Java implementation)
		propList.put(AVAILABLE_PROCESSORS, new WLProperty(AVAILABLE_PROCESSORS, "Available Processor", NUMBER_UNITS));
		propList.put(SYSTEM_LOAD_AVERAGE, new WLProperty(SYSTEM_LOAD_AVERAGE, "System Load Average", NUMBER_UNITS));
		propList.put(COMMITTED_VIRTUAL_MEMORY_SIZE_MEGABYTES, new WLProperty(COMMITTED_VIRTUAL_MEMORY_SIZE_MEGABYTES, "Committed Virtual Memory Size", MEGABYTES_UNITS));
		propList.put(FREE_PHYSICAL_MEMORY_SIZE_MEGABYTES, new WLProperty(FREE_PHYSICAL_MEMORY_SIZE_MEGABYTES, "Free Physical Memory Size", MEGABYTES_UNITS));
		propList.put(FREE_SWAP_SPACE_SIZE_MEGABYTES, new WLProperty(FREE_SWAP_SPACE_SIZE_MEGABYTES, "Free Swap Space Size", MEGABYTES_UNITS));
		propList.put(MAX_FILE_DESCRIPTOR_COUNT, new WLProperty(MAX_FILE_DESCRIPTOR_COUNT, "Max File Descriptor Count", NUMBER_UNITS));
		propList.put(OPEN_FILE_DESCRIPTOR_COUNT, new WLProperty(OPEN_FILE_DESCRIPTOR_COUNT, "Open File Descriptor Count", NUMBER_UNITS));
		
		//propList.put(PROCESS_CPU_LOAD, new WLProperty(PROCESS_CPU_LOAD, "Process CPU Load", PERCENT_UNITS));
		propList.put(PROCESS_CPU_LOAD, new WLProperty(PROCESS_CPU_LOAD, "Process CPU Load", NUMBER_UNITS));
		
		propList.put(PROCESS_CPU_TIME, new WLProperty(PROCESS_CPU_TIME, "Process CPU Time", NUMBER_UNITS));
		propList.put(SYSTEM_CPU_LOAD, new WLProperty(SYSTEM_CPU_LOAD, "System CPU Load", PERCENT_UNITS));
		propList.put(TOTAL_PHYSICAL_MEMORY_SIZE_MEGABYTES, new WLProperty(TOTAL_PHYSICAL_MEMORY_SIZE_MEGABYTES, "Total Physical Memory Size", MEGABYTES_UNITS));
		propList.put(TOTAL_SWAP_SPACE_SIZE_MEGABYTES, new WLProperty(TOTAL_SWAP_SPACE_SIZE_MEGABYTES, "Total Swap Space Size", MEGABYTES_UNITS));
		
		// Added by gregoan
		// Add JVM properties
		propList.put(HEAP_MEMORY_INIT, new WLProperty(HEAP_MEMORY_INIT, "Heap Memory Init", MEGABYTES_UNITS));
		propList.put(HEAP_MEMORY_USED, new WLProperty(HEAP_MEMORY_USED, "Heap Memory Used", MEGABYTES_UNITS));
		propList.put(HEAP_MEMORY_COMMITTED, new WLProperty(HEAP_MEMORY_COMMITTED, "Heap Memory Committed", MEGABYTES_UNITS));
		propList.put(HEAP_MEMORY_MAX, new WLProperty(HEAP_MEMORY_MAX, "Heap Memory Max", MEGABYTES_UNITS));
		
		propList.put(NON_HEAP_MEMORY_INIT, new WLProperty(NON_HEAP_MEMORY_INIT, "NonHeap Memory Init", MEGABYTES_UNITS));
		propList.put(NON_HEAP_MEMORY_USED, new WLProperty(NON_HEAP_MEMORY_USED, "NonHeap Memory Used", MEGABYTES_UNITS));
		propList.put(NON_HEAP_MEMORY_COMMITTED, new WLProperty(NON_HEAP_MEMORY_COMMITTED, "NonHeap Memory Committed", MEGABYTES_UNITS));
		propList.put(NON_HEAP_MEMORY_MAX, new WLProperty(NON_HEAP_MEMORY_MAX, "NonHeap Memory Max", MEGABYTES_UNITS));
		
		propList.put(EDEN_SPACE_INIT, new WLProperty(EDEN_SPACE_INIT,"Eden Space Init", MEGABYTES_UNITS));
		propList.put(EDEN_SPACE_USED, new WLProperty(EDEN_SPACE_USED,"Eden Space Used", MEGABYTES_UNITS));
		propList.put(EDEN_SPACE_COMMITTED, new WLProperty(EDEN_SPACE_COMMITTED,"Eden Space Committed", MEGABYTES_UNITS));
		propList.put(EDEN_SPACE_MAX, new WLProperty(EDEN_SPACE_MAX,"Eden Space Max", MEGABYTES_UNITS));
		
		propList.put(SURVIVOR_SPACE_INIT, new WLProperty(SURVIVOR_SPACE_INIT,"Suvivor Space Init", MEGABYTES_UNITS));
		propList.put(SURVIVOR_SPACE_USED, new WLProperty(SURVIVOR_SPACE_USED,"Survivor Space Used", MEGABYTES_UNITS));
		propList.put(SURVIVOR_SPACE_COMMITTED, new WLProperty(SURVIVOR_SPACE_COMMITTED,"Survivor Space Committed", MEGABYTES_UNITS));
		propList.put(SURVIVOR_SPACE_MAX, new WLProperty(SURVIVOR_SPACE_MAX,"Survivor Space Max", MEGABYTES_UNITS));
		
		propList.put(TENURED_GEN_INIT, new WLProperty(TENURED_GEN_INIT,"Tenured Gen Init", MEGABYTES_UNITS));
		propList.put(TENURED_GEN_USED, new WLProperty(TENURED_GEN_USED,"Tenured Gen Used", MEGABYTES_UNITS));
		propList.put(TENURED_GEN_COMMITTED, new WLProperty(TENURED_GEN_COMMITTED,"Tenured Gen Committed", MEGABYTES_UNITS));
		propList.put(TENURED_GEN_MAX, new WLProperty(TENURED_GEN_MAX,"Tenured Gen Max", MEGABYTES_UNITS));
		
		propList.put(PERM_GEN_INIT, new WLProperty(PERM_GEN_INIT,"Perm Gen Init", MEGABYTES_UNITS));
		propList.put(PERM_GEN_USED, new WLProperty(PERM_GEN_USED,"Perm Gen Used", MEGABYTES_UNITS));
		propList.put(PERM_GEN_COMMITTED, new WLProperty(PERM_GEN_COMMITTED,"Perm Gen Committed", MEGABYTES_UNITS));
		propList.put(PERM_GEN_MAX, new WLProperty(PERM_GEN_MAX,"Perm Gen Max", MEGABYTES_UNITS));
	}
}

/**
 * Encapsulates a WebLogic property key-title-units triplet
 */
class WLProperty {
	/**
	 * Creates a new WebLogic property
	 * 
	 * @param key Property key
	 * @param title Property title
	 * @param units Property units
	 */
	public WLProperty(String key, String title, String units) {
		this.key = key;
		this.title = title;
		this.units = units;
	}

	/**
	 * Gets the key for the property
	 * 
	 * @return Property key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the title for the property
	 * 
	 * @return Property title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Gets the units for the property
	 * 
	 * @return Property units
	 */
	public String getUnits() {
		return units;
	}

	// Members
	private final String key;
	private final String title;
	private final String units;
}
