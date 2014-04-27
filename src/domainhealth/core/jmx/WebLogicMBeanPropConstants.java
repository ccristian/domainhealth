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
package domainhealth.core.jmx;

/**
 * Common WebLogic MBeans property names.
 */
public interface WebLogicMBeanPropConstants {
	/**
	 * 'ServerRuntime' MBean property
	 */
	public final static String SERVER_RUNTIME = "ServerRuntime";

	/**
	 * 'ServerRuntimes' MBean property
	 */
	public final static String SERVER_RUNTIMES = "ServerRuntimes";

	/**
	 * 'DomainRuntime' MBean property
	 */
	public final static String DOMAIN_RUNTIME = "DomainRuntime";		

	/**
	 * 'DomainConfiguration' MBean property
	 */
	public final static String DOMAIN_CONFIGURATION = "DomainConfiguration";

	/**
	 * 'ConfigurationManager' MBean property
	 */
	public final static String CONFIGURATION_MGR = "ConfigurationManager";
	
	/**
	 * 'JVMRuntime' MBean property
	 */
	public final static String JVM_RUNTIME = "JVMRuntime";	

	/**
	 * 'JRockitRuntime' MBean property
	 */
	public final static String JROCKIT_RUNTIME = "JRockitRuntime";	

	/**
	 * 'ThreadPoolRuntime' MBean property
	 */
	public final static String THREAD_POOL_RUNTIME = "ThreadPoolRuntime";	

	/**
	 * 'WorkManagerRuntime' MBean property
	 */
	public final static String WORK_MANAGER_RUNTIME = "WorkManagerRuntime";	

	/**
	 * 'WorkManagerRuntimes' MBean property
	 */
	public final static String WORK_MANAGER_RUNTIMES = "WorkManagerRuntimes";	

	/**
	 * 'JTARuntime' MBean property
	 */
	public final static String JTA_RUNTIME = "JTARuntime";	

	/**
	 * 'JDBCServiceRuntime' MBean property
	 */
	public final static String JDBC_SERVICE_RUNTIME = "JDBCServiceRuntime";	

	/**
	 * 'JDBCDataSourceRuntime' MBean property
	 */
	public final static String JDBC_DATASOURCE_RUNTIME = "JDBCDataSourceRuntime";	

	/**
	 * 'JMSRuntime' MBean property
	 */
	public final static String JMS_RUNTIME = "JMSRuntime";	
	
	
	/**
	 * 'SAFRuntime' MBean property
	 */
	public final static String SAF_RUNTIME = "SAFRuntime";	


	/**
	 * 'JMSDestinationRuntime' MBean property
	 */
	public final static String JMS_DESTINATION_RUNTIME = "JMSDestinationRuntime";	
	
	
	/**
	 * 'SafAgentRuntime' MBean property
	 */
	public final static String SAF_AGENT_RUNTIME = "SAFAgentRuntime";	
	

	/**
	 * 'ApplicationRuntimes' MBean property
	 */
	public final static String APPLICATION_RUNTIMES = "ApplicationRuntimes";	

	/**
	 * 'ComponentRuntimes' MBean property
	 */
	public final static String COMPONENT_RUNTIMES = "ComponentRuntimes";	

	/**
	 * 'WebAppComponentRuntime' MBean property
	 */
	public final static String WEBAPP_COMPONENT_RUNTIME = "WebAppComponentRuntime";	

	/**
	 * 'EJBComponentRuntime' MBean property
	 */
	public final static String EJB_COMPONENT_RUNTIME = "EJBComponentRuntime";	

	/**
	 * 'EJBRuntimes' MBean property
	 */
	public final static String EJB_RUNTIMES = "EJBRuntimes";	

	/**
	 * 'PoolRuntime' MBean property
	 */
	public final static String POOL_RUNTIME = "PoolRuntime";	

	/**
	 * 'EJBPoolRuntime' MBean property
	 */
	public final static String EJB_POOL_RUNTIME = "EJBPoolRuntime";	

	/**
	 * 'TransactionRuntime' MBean property
	 */
	public final static String TRANSACTION_RUNTIME = "TransactionRuntime";	

	/**
	 * 'EJBTransactionRuntime' MBean property
	 */
	public final static String EJB_TRANSACTION_RUNTIME = "EJBTransactionRuntime";	

	/**
	 * 'JMSResource' MBean property
	 */
	public final static String JMS_RESOURCE = "JMSResource";	

	/**
	 * 'Servers' MBean property
	 */
	public final static String SERVERS = "Servers";	

	/**
	 * 'JDBCDataSourceRuntimeMBeans' MBean property
	 */
	public final static String JDBC_DATA_SOURCE_RUNTIMES = "JDBCDataSourceRuntimeMBeans";	
	
	/**
	 * 'JMSServers' MBean property
	 */
	public final static String JMS_SERVERS = "JMSServers";	

	/**
	 * 'Destinations' MBean property
	 */
	public final static String DESTINATIONS = "Destinations";	

	/**
	 * 'JDBCSystemResources' MBean property
	 */
	public final static String JDBC_SYSTEM_RESOURCES = "JDBCSystemResources";	
	
	/**
	 * 'JDBCResource' MBean property
	 */
	public final static String JDBC_RESOURCE = "JDBCResource";	
		
	/**
	 * 'JDBCDataSourceParams' MBean property
	 */
	public final static String JDBC_DATA_SOURCE_PARAMS = "JDBCDataSourceParams";	

	/**
	 * 'DataSourceList' MBean property
	 */
	public final static String DATA_SOURCE_LIST_PARAMS = "DataSourceList";	
	
	/**
	 * 'JMSSystemResources' MBean property
	 */
	public final static String JMS_SYSTEM_RESOURCES = "JMSSystemResources";	

	/**
	 * 'UniformDistributedQueues' MBean property
	 */
	public final static String UNIFORM_DISTRIBUTED_QUEUES = "UniformDistributedQueues";	

	/**
	 * 'UniformDistributedTopics' MBean property
	 */
	public final static String UNIFORM_DISTRIBUTED_TOPCIS = "UniformDistributedTopics";	

	/**
	 * 'DistributedQueues' MBean property
	 */
	public final static String DISTRIBUTED_QUEUES = "DistributedQueues";	

	/**
	 * 'DistributedTopics' MBean property
	 */
	public final static String DISTRIBUTED_TOPCIS = "DistributedTopics";	

	/**
	 * 'Queues' MBean property
	 */
	public final static String QUEUES = "Queues";	

	/**
	 * 'Topics' MBean property
	 */
	public final static String TOPICS = "Topics";	

	/**
	 * 'AppDeployments' MBean property
	 */
	public final static String APP_DEPLOYMENTS = "AppDeployments";	

	/**
	 * 'ModuleType' MBean property
	 */
	public final static String MODULE_TYPE = "ModuleType";
	
	/**
	 * 'Name' MBean property
	 */
	public final static String NAME = "Name";
	
	/**
	 * 'Description' MBean property
	 */
	public final static String DESCRIPTION = "Description";

	/**
	 * 'Type' MBean property
	 */
	public final static String TYPE = "Type";

	/**
	 * 'Targets' MBean property
	 */
	public final static String TARGETS = "Targets";

	/**
	 * 'AdministrationURL' MBean property
	 */
	public final static String ADMINISTRATION_URL = "AdministrationURL";	

	/**
	 * 'AdminServerListenPortSecure' MBean property
	 */
	public final static String IS_ADMIN_SERVER_PORT_SECURED = "AdminServerListenPortSecure";
	
	/**
	 * 'AdminServerHost' MBean property
	 */
	public final static String ADMIN_SERVER_HOSTNAME = "AdminServerHost";
	
	/**
	 * 'AdminServerListenPort' MBean property
	 */
	public final static String ADMIN_SERVER_PORT = "AdminServerListenPort";
	
	/**
	 * 'AdminServer' MBean property
	 */
	public final static String IS_ADMIN_SERVER = "AdminServer";
	
	/**
	 * 'AdminServerName' MBean property
	 */
	public final static String ADMIN_SERVER_NAME = "AdminServerName";
	
	/**
	 * 'DomainVersion' MBean property
	 */
	public final static String DOMAIN_VERSION = "DomainVersion";
	
	/**
	 * 'WLDFSystemResources' MBean property
	 */
	public final static String WLDF_SYS_RESOURCES = "WLDFSystemResources";
	
	/**
	 * 'WLDFResource' MBean property
	 */
	public final static String WLDF_RESOURCE = "WLDFResource";
	
	/**
	 * 'Instrumentation' MBean property
	 */
	public final static String INSTRUMENTATION = "Instrumentation";
	
	/**
	 * 'WatchNotification' MBean property
	 */
	public final static String WATCHNOTIFICATION = "WatchNotification";
	
	/**
	 * 'Harvester' MBean property
	 */
	public final static String HARVESTER = "Harvester";

	/**
	 * 'Enabled' MBean property
	 */
	public final static String ENABLED = "Enabled";
	
	/**
	 * 'SamplePeriod' MBean property
	 */
	public final static String SAMPLE_PERIOD = "SamplePeriod";
	
	/*
	 * 'KnownType' MBean property
	 */
	public final static String KNOWN_TYPE = "KnownType";

	/*
	 * 'HarvestedInstances' MBean property
	 */
	public final static String HARVTESTED_INSTANCES = "HarvestedInstances";
	
	/*
	 * 'HarvestedAttributes' MBean property
	 */
	public final static String HARVTESTED_ATTRS = "HarvestedAttributes";

	/*
	 * 'ServerDiagnosticConfig' MBean property
	 */
	public final static String SERVER_DIAG_CONFIG = "ServerDiagnosticConfig";

	/*
	 * 'DataRetirementEnabled' MBean property
	 */
	public final static String DATA_RETIREMNT_ENABLED = "DataRetirementEnabled";
	
	/*
	 * 'ArchiveName' MBean property
	 */
	public final static String ARCHIVE_NAME = "ArchiveName";
	
	/*
	 * 'RetirementAge' MBean property
	 */
	public final static String RETIREMENT_AGE = "RetirementAge";
	
	/*
	 * 'RetirementTime' MBean property
	 */
	public final static String RETIREMENT_TIME = "RetirementTime";
	
	/*
	 * 'RetirementPeriod' MBean property
	 */
	public final static String RETIREMENT_PERIOD = "RetirementPeriod";
	
	/*
	 * 'WLDFDataRetirements' MBean property
	 */
	public final static String WLDF_DATA_RETIREMENT = "WLDFDataRetirements";
	
	/**
	 * 'DateTime' property
	 */
	public final static String DATE_TIME = "DateTime";			
	
	/**
	 * 'State' MBean property
	 */
	public final static String SERVER_STATE = "State";
	
	/**
	 * 'OpenSocketsCurrentCount' MBean property
	 */
	public final static String OPEN_SOCKETS = "OpenSocketsCurrentCount";

	/**
	 * 'HeapSizeCurrent' MBean property
	 */
	public final static String HEAP_SIZE_CURRENT = "HeapSizeCurrent"; 
	
	/**
	 * 'HeapFreeCurrent' MBean property
	 */
	public final static String HEAP_FREE_CURRENT = "HeapFreeCurrent"; 
	
	/**
	 * 'HeapUsedCurrent' MBean property
	 */
	public final static String HEAP_USED_CURRENT = "HeapUsedCurrent"; 
	
	/**
	 * 'HeapFreePercent' MBean property
	 */
	public final static String HEAP_FREE_PERCENT = "HeapFreePercent"; 
	
	/**
	 * 'ExecuteThreadTotalCount' MBean property
	 */
	public final static String EXECUTE_THREAD_TOTAL_COUNT = "ExecuteThreadTotalCount"; 
	
	/**
	 * 'HoggingThreadCount' MBean property
	 */
	public final static String HOGGING_THREAD_COUNT = "HoggingThreadCount"; 
	
	/**
	 * 'PendingUserRequestCount' MBean property
	 */
	public final static String PENDING_USER_REQUEST_COUNT = "PendingUserRequestCount"; 
	
	/**
	 * 'QueueLength' MBean property
	 */
	public final static String THREAD_POOL_QUEUE_LENGTH = "QueueLength"; 
	
	/**
	 * 'CompletedRequestCount' MBean property
	 */
	public final static String COMPLETED_REQUEST_COUNT = "CompletedRequestCount"; 

	/**
	 * 'ExecuteThreadIdleCount' MBean property
	 */
	public final static String EXECUTE_THREAD_IDLE_COUNT = "ExecuteThreadIdleCount"; 

	/**
	 * 'MinThreadsConstraintsCompleted' MBean property
	 */
	public final static String MIN_THREADS_CONSTRAINT_COMPLETED = "MinThreadsConstraintsCompleted"; 

	/**
	 * 'MinThreadsConstraintsPending' MBean property
	 */
	public final static String MIN_THREADS_CONSTRAINT_PENDING = "MinThreadsConstraintsPending"; 

	/**
	 * 'StandbyThreadCount' MBean property
	 */
	public final static String STANDBY_THREAD_COUNT = "StandbyThreadCount"; 

	/**
	 * 'Throughput' MBean property
	 */
	public final static String THROUGHPUT = "Throughput"; 
    	
	/**
	 * 'CompletedRequests' MBean property
	 */
	public final static String COMPLETED_REQUESTS = "CompletedRequests"; 
	
	/**
	 * 'PendingRequests' MBean property
	 */
	public final static String PENDING_REQUESTS = "PendingRequests"; 
	
	/**
	 * 'StuckThreadCount' MBean property
	 */
	public final static String STUCK_THREAD_COUNT = "StuckThreadCount"; 

	/**
	 * 'TransactionTotalCount' MBean property
	 */
	public final static String TRANSACTION_TOTAL_COUNT = "TransactionTotalCount"; 
	
	/**
	 * 'TransactionCommittedTotalCount' MBean property
	 */
	public final static String TRANSACTION_COMMITTED_COUNT = "TransactionCommittedTotalCount"; 
	
	/**
	 * 'TransactionRolledBackTotalCount' MBean property
	 */
	public final static String TRANSACTION_ROLLEDBACK_COUNT = "TransactionRolledBackTotalCount"; 
	
	/**
	 * 'TransactionHeuristicsTotalCount' MBean property
	 */
	public final static String TRANSACTION_HEURISTICS_TOTAL_COUNT = "TransactionHeuristicsTotalCount"; 
	
	/**
	 * 'TransactionAbandonedTotalCount' MBean property
	 */
	public final static String TRANSACTION_ABANDONED_TOTAL_COUNT = "TransactionAbandonedTotalCount"; 
	
	/**
	 * 'ActiveTransactionsTotalCount' MBean property
	 */
	public final static String TRANSACTIONS_ACTIVE_TOTAL_COUNT = "ActiveTransactionsTotalCount"; 			
	
	/**
	 * 'NumAvailable' MBean property
	 */
	public final static String NUM_AVAILABLE = "NumAvailable"; 
	
	/**
	 * 'NumUnavailable' MBean property
	 */
	public final static String NUM_UNAVAILABLE = "NumUnavailable"; 
	
	/**
	 * 'ActiveConnectionsCurrentCount' MBean property
	 */
	public final static String ACTIVE_CONNECTONS_CURRENT_COUNT = "ActiveConnectionsCurrentCount"; 
	
	/**
	 * 'ConnectionDelayTime' MBean property
	 */
	public final static String CONNECTION_DELAY_TIME = "ConnectionDelayTime"; 
	
	/**
	 * 'FailedReserveRequestCount' MBean property
	 */
	public final static String FAILED_RESERIVE_REQUEST_COUNT = "FailedReserveRequestCount"; 
	
	/**
	 * 'FailuresToReconnectCount' MBean property
	 */
	public final static String FAILURES_TO_RECONNECT_COUNT = "FailuresToReconnectCount"; 
	
	/**
	 * 'LeakedConnectionCount' MBean property
	 */
	public final static String LEAKED_CONNECTION_COUNT = "LeakedConnectionCount"; 
	
	/**
	 * 'WaitingForConnectionCurrentCount' MBean property
	 */
	public final static String WAITING_FOR_CONNECTION_CURRENT_COUNT = "WaitingForConnectionCurrentCount"; 
	
	/**
	 * 'WaitingForConnectionFailureTotal' MBean property
	 */
	public final static String WAITING_FOR_CONNECTION_FAILURES_TOTAL = "WaitingForConnectionFailureTotal"; 
	
	/**
	 * 'WaitSecondsHighCount' MBean property
	 */
	public final static String WAITING_SECONDS_HIGH_COUNT = "WaitSecondsHighCount"; 				
	
	/**
	 * 'MessagesCurrentCount' MBean property
	 */
	public final static String MESSAGES_CURRENT_COUNT = "MessagesCurrentCount"; 
	
	/**
	 * 'MessagesPendingCount' MBean property
	 */
	public final static String MESSAGES_PENDING_COUNT = "MessagesPendingCount"; 
	
	/**
	 * 'MessagesReceivedCount' MBean property
	 */
	public final static String MESSAGES_RECEIVED_COUNT = "MessagesReceivedCount"; 
	
	/**
	 * 'MessagesHighCount' MBean property
	 */
	public final static String MESSAGES_HIGH_COUNT = "MessagesHighCount"; 	
		
	
	/**
	 * 'ConsumersCurrentCount' MBean property
	 */
	public final static String CONSUMERS_CURRENT_COUNT = "ConsumersCurrentCount"; 

	/**
	 * 'ConsumersHighCount' MBean property
	 */
	public final static String CONSUMERS_HIGH_COUNT = "ConsumersHighCount";

	/**
	 * 'ConsumersHighCount' MBean property
	 */
	public final static String CONSUMERS_TOTAL_COUNT = "ConsumersTotalCount";
	
	/**
	 * 'OpenSessionsCurrentCount' MBean property
	 */
	public final static String SESSIONS_CURRENT_COUNT = "OpenSessionsCurrentCount";

	/**
	 * 'OpenSessionsHighCount' MBean property
	 */
	public final static String SESSIONS_HIGH_COUNT = "OpenSessionsHighCount";

	/**
	 * 'SessionsOpenedTotalCount' MBean property
	 */
	public final static String SESSIONS_TOTAL_COUNT = "SessionsOpenedTotalCount";

	/**
	 * 'PooledBeansCurrentCount' MBean property
	 */
	public final static String BEANS_POOLED_CURRENT_COUNT = "PooledBeansCurrentCount";

	/**
	 * 'AccessTotalCount' MBean property
	 */
	public final static String BEAN_ACCESS_TOTAL_COUNT = "AccessTotalCount";

	/**
	 * 'BeansInUseCurrentCount' MBean property
	 */
	public final static String BEANS_INUSE_CURRENT_COUNT = "BeansInUseCurrentCount";
	
	/**
	 * 'WaiterCurrentCount' MBean property
	 */
	public final static String BEAN_WAITING_CURRENT_COUNT = "WaiterCurrentCount";
	
	/**
	 * 'WaiterTotalCount' MBean property
	 */
	public final static String BEAN_WAITING_TOTAL_COUNT = "WaiterTotalCount";
	
	/**
	 * 'TransactionsCommittedTotalCount' MBean property
	 */
	public final static String BEAN_TRANSACTIONS_COMMITTED_TOTAL_COUNT = "TransactionsCommittedTotalCount";
	
	/**
	 * 'TransactionsRolledBackTotalCount' MBean property
	 */
	public final static String BEAN_TRANSACTIONS_ROLLEDBACK_TOTAL_COUNT = "TransactionsRolledBackTotalCount";

	/**
	 * 'TransactionsTimedOutTotalCount' MBean property
	 */
	public final static String BEAN_TRANSACTIONS_TIMEDOUT_TOTAL_COUNT = "TransactionsTimedOutTotalCount";	
	
	/**
	 * 'ServerChannelRuntimeMBean' MBean property
	 */
	public final static String SERVER_CHANNEL_RUNTIME = "ServerChannelRuntime";	

	/**
	 * 'AcceptCount' MBean property
	 */
	public final static String ACCEPT_COUNT = "AcceptCount";

	/**
	 * 'ConnectionsCount' MBean property
	 */
	public final static String CONNECTIONS_COUNT = "ConnectionsCount";

	/**
	 * 'MessagesReceivedCount' MBean property
	 */
	public final static String CHNL_MESSAGES_RECEIVED_COUNT = "MessagesReceivedCount";

	/**
	 * 'MessagesSentCount' MBean property
	 */
	public final static String CHNL_MESSAGES_SENT_COUNT = "MessagesSentCount";
	
	/**
	 * 'wlhostmachinestats.mbeans.WLHostMachineStats' MBean property
	 */
	public final static String HOST_MACHINE_MBEAN = "wlhostmachinestats.mbeans.WLHostMachineStats";

	/**
	 * 'JVMInstanceCoresUsed' MBean property
	 */
	public final static String JVM_INSTANCE_CORES_USED = "JVMInstanceCoresUsed";

	/**
	 * 'JVMInstancePhysicalMemoryUsedMegabytes' MBean property
	 */
	public final static String JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES = "JVMInstancePhysicalMemoryUsedMegabytes";

	/**
	 * 'NativeProcessesCount' MBean property
	 */
	public final static String NATIVE_PROCESSES_COUNT = "NativeProcessesCount";

	/**
	 * 'NetworkRxMegabytes' MBean property
	 */
	public final static String NETWORK_RX_MEGABYTES = "NetworkRxMegabytes";

	/**
	 * 'NetworkRxDropped' MBean property
	 */
	public final static String NETWORK_RX_DROPPED = "NetworkRxDropped";

	/**
	 * 'NetworkRxErrors' MBean property
	 */
	public final static String NETWORK_RX_ERRORS = "NetworkRxErrors";

	/**
	 * 'NetworkRxFrame' MBean property
	 */
	public final static String NETWORK_RX_FRAME = "NetworkRxFrame";

	/**
	 * 'NetworkRxOverruns' MBean property
	 */
	public final static String NETWORK_RX_OVERRUNS = "NetworkRxOverruns";

	/**
	 * 'NetworkRxMillionPackets' MBean property
	 */
	public final static String NETWORK_MILLIONS_RX_PACKETS = "NetworkRxMillionPackets";

	/**
	 * 'NetworkTxMegabytes' MBean property
	 */
	public final static String NETWORK_TX_MEGABYTES = "NetworkTxMegabytes";

	/**
	 * 'NetworkTxCarrier' MBean property
	 */
	public final static String NETWORK_TX_CARRIER = "NetworkTxCarrier";

	/**
	 * 'NetworkTxCollisions' MBean property
	 */
	public final static String NETWORK_TX_COLLISIONS = "NetworkTxCollisions";

	/**
	 * 'NetworkTxDropped' MBean property
	 */
	public final static String NETWORK_TX_DROPPED = "NetworkTxDropped";

	/**
	 * 'NetworkTxErrors' MBean property
	 */
	public final static String NETWORK_TX_ERRORS = "NetworkTxErrors";

	/**
	 * 'NetworkTxOverruns' MBean property
	 */
	public final static String NETWORK_TX_OVERRUNS = "NetworkTxOverruns";

	/**
	 * 'NetworkTxMillionPackets' MBean property
	 */
	public final static String NETWORK_MILLIONS_TX_PACKETS = "NetworkTxMillionPackets";

	/**
	 * 'PhysicalMemoryUsedPercent' MBean property
	 */
	public final static String PHYSICAL_MEMORY_USED_PERCENT = "PhysicalMemoryUsedPercent";

	/**
	 * 'PhysicalSwapUsedPercent' MBean property
	 */
	public final static String PHYSICAL_SWAP_USED_PERCENT = "PhysicalSwapUsedPercent";

	/**
	 * 'ProcessorLastMinuteWorkloadAverage' MBean property
	 */
	public final static String PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE = "ProcessorLastMinuteWorkloadAverage";

	/**
	 * 'ProcessorUsagePercent' MBean property
	 */
	public final static String PROCESSOR_USAGE_PERCENT = "ProcessorUsagePercent";

	/**
	 * 'RootFilesystemUsedPercent' MBean property
	 */
	public final static String ROOT_FILESYSTEM_USED_PERCENT = "RootFilesystemUsedPercent";

	/**
	 * 'TcpCloseWaitCount' MBean property
	 */
	public final static String TCP_CLOSE_WAIT_COUNT = "TcpCloseWaitCount";

	/**
	 * 'TcpEstablishedCount' MBean property
	 */
	public final static String TCP_ESTABLISHED_COUNT = "TcpEstablishedCount";

	/**
	 * 'TcpListenCount' MBean property
	 */
	public final static String TCP_LISTEN_COUNT = "TcpListenCount";

	/**
	 * 'TcpTimeWaitCount' MBean property
	 */
	public final static String TCP_TIME_WAIT_COUNT = "TcpTimeWaitCount";
}
