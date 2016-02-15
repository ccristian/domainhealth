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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ObjectName;

import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.jmx.EditServiceMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.util.ProductVersionUtil;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;
import static domainhealth.core.statistics.MonitorProperties.*;

/** 
 * Responsible for creating a WLDF Module in the WebLogic domain, configured
 * to log a set of required statistics into a Harvester Archive for the Domain
 * Health application to subsequently query.
 */
public class HarvesterWLDFModuleCreator {
	/**
	 * Harvester module constructor.
	 * 
	 * @param queryIntervalMillis The interval in milliseconds between successive statistic collecitons
	 * @param domainhealthVersionNumber The version number of DomainHealth and the WLDF module which should be present or created
	 * @param wlsVersionNumber The version of the host WebLogic Domain
	 */
	public HarvesterWLDFModuleCreator(int queryIntervalMillis, String domainhealthVersionNumber, String wlsVersionNumber) {
		this.queryIntervalMillis = queryIntervalMillis;
		this.domainhealthVersionNumber = domainhealthVersionNumber;
		this.wlsVersionNumber = wlsVersionNumber;
		moduleDescription = String.format(MODULE_DESC_TMPLT, domainhealthVersionNumber);
	}
	
	/**
	 * Determines whether the current WebLogic domain is capable of using WLDF
	 * statistic harvesting to drive Domain Health. Specifically, sees if there
	 * is already a configured WLDF module targeted to any of the servers in 
	 * the domain. If this is the case, it means that WLDF harvesting cannot 
	 * be used for Domain Health because current versions of WebLogic only 
	 * support having one WLDF Module configured and targeted to a specific
	 * server, at any one time. For WebLogic version 12.1.2 or greater, 
	 * multiple WLDF versions can be targeted to the same servers in which 
	 * case this method will ALWAYS return true.
	 * 
	 * @return True of WLDF harvesting can be use for Domain Health statistics retrieval; otherwise false
	 * @throws WebLogicMBeanException Indicates that there is a problem in trying to query the doamin to see if WLDF can be used
	 */
	public boolean isDomainHealthAbleToUseWLDF() throws WebLogicMBeanException {
		if (ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y(wlsVersionNumber, WLS_MIN_VERSION_FOR_MULTI_WLDF_MODULES)) {
			return true;
		}
		
		DomainRuntimeServiceMBeanConnection domainSvcConn = null;

		try {
			domainSvcConn = new DomainRuntimeServiceMBeanConnection();
			
			if (doOtherTargetedWLDFSystemModulesExist(domainSvcConn)) {
				return false;
			}
		} finally {
			if (domainSvcConn != null) {
				domainSvcConn.close();
			}
		}		

		return true;
	}

	/**
	 * Create a new WLDF Module for harvesting various server statistics and 
	 * target this to every server in the domain (including admin server). 
	 * Also creates a data retirement policy for these harvested statistics 
	 * for every server in the domain, to help clear up old WLDF data.
	 * 
	 * @throws WebLogicMBeanException Indicates that there was a problem with trying to create a WLDF Module
	 */
	public void createIfNeeded() throws WebLogicMBeanException {
		AppLog.getLogger().debug("Attempting to create WLDF harvester module");
		EditServiceMBeanConnection editSvcConn = null;
		boolean edited = false;

		try {
			DomainRuntimeServiceMBeanConnection domainSvcConn = null;
			ExistingDHModuleType existingDHModuleType = ExistingDHModuleType.CURRENT_MODULE;
			
			try { 
				domainSvcConn = new DomainRuntimeServiceMBeanConnection();
				existingDHModuleType = whatTypeOfDHModuleAlreadyExists(domainSvcConn);
			} finally {
				if (domainSvcConn != null) {
					try { domainSvcConn.close(); } catch (Exception e) { e.printStackTrace();	}
				}				
			}
			
			AppLog.getLogger().debug("Existing WLDF module type: " + existingDHModuleType);
			editSvcConn = new EditServiceMBeanConnection();

			// Unfortunately have to do the module 'delete' action in a different
			// edit session to the 'create' action, further down, for things to work ok :(
			if ((existingDHModuleType == ExistingDHModuleType.OLDER_MODULE) || (existingDHModuleType == ExistingDHModuleType.MISSING_TARGETS_MODULE)) {			
				edited = true;
				ObjectName domainConfig = editSvcConn.startEdit();
				deleteOldDHHarvesterModuleAndPolicy(editSvcConn, domainConfig);
				editSvcConn.saveAndActivate();
				editSvcConn.cancelEdit();
			}
			
			if ((existingDHModuleType == ExistingDHModuleType.DOES_NOT_EXIST) || (existingDHModuleType == ExistingDHModuleType.OLDER_MODULE)  || (existingDHModuleType == ExistingDHModuleType.MISSING_TARGETS_MODULE)) {			
				edited = true;
				ObjectName domainConfig = editSvcConn.startEdit();
				createNewDHHarvesterModuleAndPolicy(editSvcConn, domainConfig);
				editSvcConn.saveAndActivate();
			}
		} catch (Exception e) {
			AppLog.getLogger().critical("Unable to configure WDLF Harvester module. Cause: " + e.getMessage());
			AppLog.getLogger().debug("WLDF module creation failure cause", e);

			if ((editSvcConn != null) && (edited)) {
				editSvcConn.cancelEdit();
			}
			
			throw new WebLogicMBeanException("DomainHealth failed to configure WDLF Harvester module and retirement policy", e);
		} finally {
			if (editSvcConn != null) {
				try { editSvcConn.close(); } catch (Exception e) { e.printStackTrace();	}
			}
		}
	}

	/**
	 * Looks at domain's current configuration to see if it already has a 
	 * different (non Domain Health) WLDF Module configured and targeted to 
	 * one or more servers.
	 * 
	 * @param conn Domain MBean server connection
	 * @return True if another WLDF Module exists and is targeted; otherwise false
	 * @throws WebLogicMBeanException Indicates that there was a problem querying the domain's config
	 */
	private boolean doOtherTargetedWLDFSystemModulesExist(DomainRuntimeServiceMBeanConnection conn) throws WebLogicMBeanException {
		ObjectName domainConfig = conn.getDomainConfiguration();
		ObjectName[] sysModules = conn.getChildren(domainConfig, WLDF_SYS_RESOURCES);
		
		for (ObjectName sysModule : sysModules) {
			String sysModName = conn.getTextAttr(sysModule, NAME);
			
			if (!sysModName.equals(HARVESTER_MODULE_NAME)) {
				ObjectName[] targets = conn.getChildren(sysModule, TARGETS);
				
				if ((targets !=null) && (targets.length > 0)) {
					AppLog.getLogger().warning("Unable to create WLDF Harvester Module because a targeted WLDF system module ('" + sysModName + "') already exists");
					return true;
				}
			}
		}
		
		return false;
	}	
	
	/**
	 * Determines if there is already a Domain Health WLDF module configured 
	 * and if so, whether this is the current required version or an older 
	 * version (by inspecting the module version number stored at the end of 
	 * the module's description text string).
	 * 
	 * @param conn Domain MBean server connection
	 * @return module type: Not Existing, Older Version or Current Version
	 * @throws WebLogicMBeanException Indicates that there was a problem querying the domain's config
	 */
	private ExistingDHModuleType whatTypeOfDHModuleAlreadyExists(DomainRuntimeServiceMBeanConnection conn) throws WebLogicMBeanException {
		ObjectName domainConfig = conn.getDomainConfiguration();
		ObjectName wldfResource =  (ObjectName) conn.invoke(domainConfig, LOOKUP_WLDFRSC_OPERTN, 
				new Object[] { HARVESTER_MODULE_NAME }, LOOKUP_WLDFRSC_PARAMTYPES);

		if (wldfResource != null) {
			String description = conn.getTextAttr(wldfResource, DESCRIPTION);

			if (doesModuleDescriptionContainCurrentModuleVersion(description)) {
				if (isModuleTargettedToAllServers(conn, wldfResource)) {
					return ExistingDHModuleType.CURRENT_MODULE;
				} else {
					return ExistingDHModuleType.MISSING_TARGETS_MODULE;				
				}
			} else {
				return ExistingDHModuleType.OLDER_MODULE;							
			}
		} else {
			return ExistingDHModuleType.DOES_NOT_EXIST;
		}
	}

	/**
	 * Determine if the current Domain Health harvester module is targetted to
	 * all current configured servers in the domain.
	 * 
	 * @param conn Domain MBean server connection
	 * @param wldfResource Existing DomainHealth havester module resource
	 * @return True if current module targetted to all configured servers, otherwise false
	 * @throws WebLogicMBeanException Indicates that there was a problem querying the domain's config
	 */
	private boolean isModuleTargettedToAllServers(DomainRuntimeServiceMBeanConnection conn, ObjectName wldfResource) throws WebLogicMBeanException {
		ObjectName[] servers = conn.getChildren(conn.getDomainConfiguration(), SERVERS);
		ObjectName[] targets = conn.getChildren(wldfResource, TARGETS);
		
		if ((targets !=null) && (targets.length == servers.length)) {
			return true;							
		} else {
			return false;
		}
	}
	
	/**
	 * Utility method determining whether a WLDF Module description string 
	 * contains a version number which is equal to the current version number.
	 *  
	 * @param description The module description text
	 * @return True if version matches; false if no version found or version is not same 
	 */
	public boolean doesModuleDescriptionContainCurrentModuleVersion(String description) {
		Matcher matcher = MODULE_VERSION_EXTRACTOR_PATTERN.matcher(description);
		boolean found = matcher.find();
		
		if ((found) && (matcher.groupCount() > 0) && (matcher.group(1).equals(domainhealthVersionNumber))) {
			return true;
		} else {
			return false;				
		}
	}

	/**
	 * Delete an old version of the Domain Health WLDF Harvesting module from 
	 * the domain, if it exists, and also any old Domain Health retirement 
	 * policies for the servers in the domain.
	 * 
	 * @param conn Connection to the domain's edit service
	 * @param domainConfig Handle on the domain config MBean
	 * @throws WebLogicMBeanException Indicates problem occurred in deleting resources
	 */
	private void deleteOldDHHarvesterModuleAndPolicy(EditServiceMBeanConnection conn, ObjectName domainConfig) throws WebLogicMBeanException {		
		ObjectName wldfResource =  (ObjectName) conn.invoke(domainConfig, LOOKUP_WLDFRSC_OPERTN, 
				new Object[] { HARVESTER_MODULE_NAME }, LOOKUP_WLDFRSC_PARAMTYPES);
		conn.invoke(domainConfig, DESTROY_WLDFRSC_OPERTN, new Object[] {wldfResource}, DESTROY_WLDFRSC_PARAMTYPES);

		ObjectName[] servers = conn.getChildren(domainConfig, SERVERS);

		for (ObjectName server : servers) {
			String serverName = conn.getTextAttr(server, NAME);
			ObjectName serverDiagConf = conn.getChild(server, SERVER_DIAG_CONFIG);
			ObjectName retirePolicy = (ObjectName) conn.invoke(serverDiagConf, LOOKUP_RETIREPOLICY_OPERTN, 
					new Object[] {String.format(RETIRE_POLICY_NAME_TEMPLATE, serverName)}, LOOKUP_RETIREPOLICY_PARAMTYPES);
		
			if (retirePolicy != null) {
				conn.invoke(serverDiagConf, DESTROY_RETIREPOLICY_OPERTN, new Object[] {retirePolicy}, DESTROY_RETIREPOLICY_PARAMTYPES);			
			}
		}
		
		AppLog.getLogger().notice("Removed old version of DomainHealth WLDF Harvester Module: " + HARVESTER_MODULE_NAME);
	}

	/**
	 * Create a new WLDF Harvester Module specifically for Domain Health usage 
	 * and target to all servers in domain (including admin server). Adds the 
	 * list of MBeans instance names to monitor and their attributes 
	 * (metrics). Also adds a data retirement policy for old statistics still 
	 * present in the Harvester Archive after a set time, one per server. When 
	 * creating the WLDF Module, uses end of the description field to record 
	 * what version of Domain Health this module is intended for.
	 * 
	 * @param conn Connection to the domain's edit service
	 * @param domainConfig Handle on the domain config MBean
	 * @throws WebLogicMBeanException Indicates problem occurred in creating resources
	 */
	private void createNewDHHarvesterModuleAndPolicy(EditServiceMBeanConnection conn, ObjectName domainConfig) throws WebLogicMBeanException {
		ObjectName wldfResource = (ObjectName) conn.invoke(domainConfig, CREATE_WLDFRSC_OPERTN, 
				new Object[] {HARVESTER_MODULE_NAME}, CREATE_WLDFRSC_PARAMTYPES);	
		conn.setTextAttr(wldfResource, DESCRIPTION, String.format(moduleDescription));
		ObjectName[] servers = conn.getChildren(domainConfig, SERVERS);
		String[] serverNames = new String[servers.length]; 

		for (int i = 0; i < servers.length; i++) {
			serverNames[i] = conn.getTextAttr(servers[i], NAME);
			conn.addTarget(wldfResource, servers[i]);
		}

		ObjectName wldfRsc = conn.getChild(wldfResource, WLDF_RESOURCE);
		ObjectName instrumentation = conn.getChild(wldfRsc, INSTRUMENTATION);
		conn.setBooleanAttr(instrumentation, ENABLED, false);
		ObjectName watchNotification = conn.getChild(wldfRsc, WATCHNOTIFICATION);
		conn.setBooleanAttr(watchNotification, ENABLED, false);
		ObjectName harvester = conn.getChild(wldfRsc, HARVESTER);
		conn.setBooleanAttr(harvester, ENABLED, true);
		conn.setNumberAttr(harvester, SAMPLE_PERIOD, queryIntervalMillis);
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, SERVER_RUNTIME), SERVER_MBEAN_MONITOR_ATTR_LIST, true);
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JVM_RUNTIME), JVM_MBEAN_MONITOR_ATTR_LIST, true);
		
// Check how to add the other JVM values
		
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JROCKIT_RUNTIME), JVM_MBEAN_MONITOR_ATTR_LIST, true);		
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, THREAD_POOL_RUNTIME), THREADPOOL_MBEAN_MONITOR_ATTR_LIST, true);
		/* Example of restricting mbean type query to a fixed set of known mbean instance names
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, WORK_MANAGER_RUNTIME), 
				getDefaultWorkManagerServerTextObjectNames(serverNames), WKMGR_MBEAN_MONITOR_ATTR_LIST, true);
		*/
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JTA_RUNTIME), JTA_MBEAN_MONITOR_ATTR_LIST, true);
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JDBC_DATASOURCE_RUNTIME), JDBC_MBEAN_MONITOR_ATTR_LIST, true);
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, JMS_DESTINATION_RUNTIME), JMS_DESTINATION_MBEAN_MONITOR_ATTR_LIST, true);
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, SAF_AGENT_RUNTIME), SAF_AGENT_MBEAN_MONITOR_ATTR_LIST, true);
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, WEBAPP_COMPONENT_RUNTIME), WEBAPP_MBEAN_MONITOR_ATTR_LIST, true);
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, EJB_POOL_RUNTIME), EJB_POOL_MBEAN_MONITOR_ATTR_LIST, true);
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, EJB_TRANSACTION_RUNTIME), EJB_TRANSACTION_MBEAN_MONITOR_ATTR_LIST, true);		
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, WORK_MANAGER_RUNTIME), WKMGR_MBEAN_MONITOR_ATTR_LIST, true);
		addMetric(conn, harvester, String.format(RUNTIME_MBEAN_TYPE_TEMPLATE, SERVER_CHANNEL_RUNTIME), SVR_CHANNEL_MBEAN_MONITOR_ATTR_LIST, true);
		addMetric(conn, harvester, HOST_MACHINE_MBEAN, HOST_MACHINE_STATS_MBEAN_MONITOR_ATTR_LIST, false);
		createNewRetirementPolicy(conn, domainConfig);
		AppLog.getLogger().notice("Created new DomainHealth WLDF Harvester Module called: " + HARVESTER_MODULE_NAME + " (" + domainhealthVersionNumber + ")");
	}

	/**
	 * Adds a new set of metrics (list of names of attributes for all MBean 
	 * instances of a specific MBean type) to be harvested by the WLDF 
	 * Harvester Module.
	 * 
	 * @param conn Connection to the domain's edit service
	 * @param harvester The created harvester module mbean
	 * @param mBeanType The type of mbean to monitor
	 * @param attrNames The list of attributes for the mbean type which should be harvested
	 * @param knownWLSMBeanType Is this a known weblogic type of mbean
	 * @throws WebLogicMBeanException
	 */
	private void addMetric(EditServiceMBeanConnection conn, ObjectName harvester, String mBeanType, String[] attrNames, boolean knownWLSMBeanType) throws WebLogicMBeanException {
		addMetric(conn, harvester, mBeanType, null, attrNames, knownWLSMBeanType);
	}

	/**
	 * Adds a new set of metrics (list of names of attributes for a specific 
	 * MBean type and set of named MBean instances) to be harvested by the WLDF 
	 * Harvester Module.
	 * 
	 * @param conn Connection to the domain's edit service
	 * @param harvester The created harvester module mbean
	 * @param mBeanType The type of mbean to monitor
	 * @param txtObjectNames The name of the mbean instances to monitor (or null) if all instances of the mbean type should be monitored 
	 * @param attrNames The list of attributes for the mbean type which should be harvested
	 * @param knownWLSMBeanType Is this a known weblogic type of mbean
	 * @throws WebLogicMBeanException
	 */
	private void addMetric(EditServiceMBeanConnection conn, ObjectName harvester, String mBeanType, String[] txtObjectNames, String[] attrNames, boolean knownWLSMBeanType) throws WebLogicMBeanException {
		ObjectName harvestedType = (ObjectName) conn.invoke(harvester, CREATE_HARVSTTYPE_OPERTN, 
				new Object[] {mBeanType}, CREATE_HARVSTTYPE_PARAMTYPES);
		conn.setBooleanAttr(harvestedType, KNOWN_TYPE, knownWLSMBeanType);				
		
		if ((txtObjectNames != null) && (txtObjectNames.length > 0)) {
			conn.setObjectAttr(harvestedType, HARVTESTED_INSTANCES, txtObjectNames);			
		}
		
		conn.setObjectAttr(harvestedType, HARVTESTED_ATTRS, attrNames);
	}

	/**
	 * Example only function - not currently used. Shows how to build up a 
	 * list of object name instances for a specific mbean type ready to be 
	 * used for restricting what instances of an mbean should have harvested 
	 * metrics set.
	 * 
	 * @param serverNames The list of server names that that host default work manager instances which should be harvested
	 * @return The names of all the Default Work Manager MBean instances to collect metrics for 
	 */
	static String[] getDefaultWorkManagerServerTextObjectNames(String[] serverNames) {
		String[] textObjNames = new String[serverNames.length];
		
		for (int i = 0; i < serverNames.length; i++) {
			textObjNames[i] = String.format(WKMGR_MBEAN_NAME_TEMPLATE, serverNames[i]);
		}
		
		return textObjNames;
	}

	/**
	 * Creates a WLDF Harvester Archive retirement policy for each server in 
	 * the domain. Only one Harvester Archive retirement policy can be defined 
	 * per servers so for any servers which already have a policy defined, no 
	 * new one will be added (even if its settings are different).
	 *  
	 * @param conn Connection to the domain's edit service
	 * @param domainConfig Handle on the domain config MBean
	 * @throws WebLogicMBeanException Indicates problem occurred in creating resources
	 */
	private void createNewRetirementPolicy(EditServiceMBeanConnection conn, ObjectName domainConfig) throws WebLogicMBeanException {		
		ObjectName[] servers = conn.getChildren(domainConfig, SERVERS);

		for (ObjectName server : servers) {
			String serverName = conn.getTextAttr(server, NAME);
			ObjectName serverDiagConf = conn.getChild(server, SERVER_DIAG_CONFIG);
			
			if (doesAnyHarvesterRetirementPolicyAlreadyExist(conn, serverDiagConf)) {
				AppLog.getLogger().warning("Unable to create new harvester retirement policy for server '" + serverName + "' because one already exists");
				continue;
			}

			conn.setBooleanAttr(serverDiagConf, DATA_RETIREMNT_ENABLED, true);
			ObjectName retirePolicy = (ObjectName) conn.invoke(serverDiagConf, CREATE_RETIREPOLICY_OPERTN, 
					new Object[] {String.format(RETIRE_POLICY_NAME_TEMPLATE, serverName)}, CREATE_RETIREPOLICY_PARAMTYPES);	
			conn.setBooleanAttr(retirePolicy, ENABLED, true);
			conn.setTextAttr(retirePolicy, ARCHIVE_NAME, HAVESTER_ARCHIVE_NAME);
			conn.setNumberAttr(retirePolicy, RETIREMENT_AGE, OLD_DATA_AGE_HOURS);
			conn.setNumberAttr(retirePolicy, RETIREMENT_TIME, DATA_RETIREMENT_START_HOUR_OF_DAY);
			conn.setNumberAttr(retirePolicy, RETIREMENT_PERIOD, DATA_RETIREMENT_PERIOD_HOURS);
		}
	}

	/**
	 * For a given server, sees if a WLDF Harvester Archive retirement policy 
	 * is already configured for that server.
	 * 
	 * @param conn Connection to the domain's edit service
	 * @param serverDiagConf Server diagnostic configuration mbean
	 * @return True if policy already exists; otherwise false
	 * @throws WebLogicMBeanException Indicates problem occurred in checking configuration
	 */
	private boolean doesAnyHarvesterRetirementPolicyAlreadyExist(EditServiceMBeanConnection conn, ObjectName serverDiagConf) throws WebLogicMBeanException {
		ObjectName[] retirementPolicies = conn.getChildren(serverDiagConf, WLDF_DATA_RETIREMENT);
		
		for (ObjectName rtmntPolicy : retirementPolicies) {
			if (conn.getTextAttr(rtmntPolicy, ARCHIVE_NAME).equals(HAVESTER_ARCHIVE_NAME)) {
				return true;
			}
		}
		
		return false;
	}
	
	// Members
	private final int queryIntervalMillis;
	private final String domainhealthVersionNumber;
	private final String wlsVersionNumber;
	private final String moduleDescription;
	
	// Constants
	private static final String WLS_MIN_VERSION_FOR_MULTI_WLDF_MODULES = "12.1.2";
	private final static String HARVESTER_MODULE_NAME = "DomainHealth_WLDFHarvesterModule";
	private final static String RETIRE_POLICY_NAME_TEMPLATE = "DomainHealth_WLDFRetirePolicy_%s";
	private final static String MODULE_DESC_TMPLT = "WLDF Module for the 'DomainHealth' monitoring application. Harvests important Core, JDBC. JMS, WebApp, EJB, Work Manager and Server Channel statistics for each server in the domain, ready to be queried by the DomainHealth web application running on the domain's Admin Server. v%s.";
	private final static Pattern MODULE_VERSION_EXTRACTOR_PATTERN = Pattern.compile(".*\\. v(\\d+\\.\\d+\\.?\\d*[a-zA-Z]*\\d*)\\.$");
	private final static String HAVESTER_ARCHIVE_NAME = "HarvestedDataArchive";
	private final static int OLD_DATA_AGE_HOURS = 1;
	private final static int DATA_RETIREMENT_START_HOUR_OF_DAY = 2;	
	private final static int DATA_RETIREMENT_PERIOD_HOURS = 6;
	private static final String LOOKUP_WLDFRSC_OPERTN = "lookupWLDFSystemResource";
	private static final String[] LOOKUP_WLDFRSC_PARAMTYPES = new String [] {String.class.getCanonicalName()};
	private static final String DESTROY_WLDFRSC_OPERTN = "destroyWLDFSystemResource";
	private static final String[] DESTROY_WLDFRSC_PARAMTYPES = new String [] {ObjectName.class.getCanonicalName()};
	private static final String CREATE_WLDFRSC_OPERTN = "createWLDFSystemResource";
	private static final String[] CREATE_WLDFRSC_PARAMTYPES = new String [] {String.class.getCanonicalName()};
	private static final String CREATE_HARVSTTYPE_OPERTN = "createHarvestedType";
	private static final String[] CREATE_HARVSTTYPE_PARAMTYPES = new String [] {String.class.getCanonicalName()};
	private static final String CREATE_RETIREPOLICY_OPERTN = "createWLDFDataRetirementByAge";
	private static final String[] CREATE_RETIREPOLICY_PARAMTYPES = new String [] {String.class.getCanonicalName()};
	private static final String LOOKUP_RETIREPOLICY_OPERTN = "lookupWLDFDataRetirementByAge";
	private static final String[] LOOKUP_RETIREPOLICY_PARAMTYPES = new String [] {String.class.getCanonicalName()};
	private static final String DESTROY_RETIREPOLICY_OPERTN = "destroyWLDFDataRetirementByAge";
	private static final String[] DESTROY_RETIREPOLICY_PARAMTYPES = new String [] {ObjectName.class.getCanonicalName()};
	
	// Enums
	private enum ExistingDHModuleType {DOES_NOT_EXIST, OLDER_MODULE, CURRENT_MODULE, MISSING_TARGETS_MODULE}
}
