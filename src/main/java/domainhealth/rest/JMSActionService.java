package domainhealth.rest;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.AGENTS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DESTINATIONS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_SERVERS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.SAF_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.REMOTE_END_POINTS;

import static domainhealth.core.statistics.MonitorProperties.JMSSVR_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.SAFAGENT_RESOURCE_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.env.AppProperties.PropKey;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.MonitorProperties;
import domainhealth.core.statistics.ResourceNameNormaliser;

/**
 */
@Path("/jmsaction")
public class JMSActionService {

	private final static String RESTRICTED_ROLES_TOKENIZER_PATTERN = ",\\s*";
	
    @Context
    private ServletContext application;
    
    @Context
    private SecurityContext securityContext;
    
    /**
     * 
     */
    @PostConstruct
    public void initialize() {
    }
    
    /**
     * 
     * @return
     */
    @GET
    @Path("validateAccess")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean validateAccess(@Context SecurityContext securityContext) {
    
		AppProperties appProps = new AppProperties(application);
		Boolean restrictedAction = new Boolean(appProps.getProperty(PropKey.RESTRICTED_ACTION));

		if(restrictedAction) {
			
System.out.println("JMSActionService::validateSecuredAccess() - The restriction mode is set");
System.out.println("JMSActionService::validateSecuredAccess() - Principal is [" + securityContext.getUserPrincipal().getName() + "]");

			String restrictedRoles = appProps.getProperty(PropKey.RESTRICTED_ROLES);
			List<String> rolesList = tokenizeRestrictedRolesText(restrictedRoles);
			
System.out.println("JMSActionService::validateSecuredAccess() - restrictedRoles is [" + restrictedRoles + "]");
			
			Iterator<String> iteratorRolesList = rolesList.iterator();
	        boolean allowed = false;
	        	
	        while (iteratorRolesList.hasNext()) {
	            String role = iteratorRolesList.next();
	
	            if(securityContext.isUserInRole(role)) {
System.out.println("JMSActionService::validateSecuredAccess() - The username is part of the role [" + role + "]");
	            	allowed = true;
	                break;
	    		} else {
System.out.println("JMSActionService::validateSecuredAccess() - The username IS NOT part of the role [" + role + "]");
				}
	        }
	        
	        if(allowed){
System.out.println("JMSActionService::validateSecuredAccess() - The user is allowed");
	        	return true;
	        } else{
System.out.println("JMSActionService::validateSecuredAccess() - The user is not allowed");
return false;
	        }
		}
else{
	System.out.println("JMSActionService::validateSecuredAccess() - The restriction mode is not set");
	return false;
}		
    }
    
    /**
	 * Gets list of names of web-app and ejb components which should not have 
	 * statistics collected and shown.
	 * 
	 * @param blacklistText The text containing comma separated list of names to ignore
	 * @return A strongly type list of names to ignore
	 */
	private List<String> tokenizeRestrictedRolesText(String restrictedRolesText) {
		List<String> restrictedRoles = new ArrayList<String>();
		String[] restrictedRolesArray = null;
		
		if (restrictedRolesText != null) {
			restrictedRolesArray = restrictedRolesText.split(RESTRICTED_ROLES_TOKENIZER_PATTERN);
		}
		
		if ((restrictedRolesArray != null) && (restrictedRolesArray.length > 0)) {
			restrictedRoles = Arrays.asList(restrictedRolesArray);
		} else {
			restrictedRoles = new ArrayList<String>();
		}
		return restrictedRoles;
	}
    
    /**
     * 
     * @return
     */
    @GET
    @Path("isUserInRole/{role}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean isUserInRole(@Context SecurityContext securityContext,
    							@PathParam("role") String role) {
    	
    	if(securityContext.getUserPrincipal() != null) {
    		String username = securityContext.getUserPrincipal().getName();
    		if(securityContext.isUserInRole(role)) {
    			AppLog.getLogger().notice("The username [" + username + "] is part of the role [" + role + "]");
    			return true;
    		} else {
    			AppLog.getLogger().error("The username [" + username + "] is not part of the role [" + role + "]");
    			return false;
    		}
    	} else {
    		AppLog.getLogger().notice("The username is not logged/authenticated");
    		return false;
    	}
    }
    
    /**
     * 
     * @param userAgent
     * @param scope
     * @param jmsServer
     * @param queue
     * @param action
     * @return
     */
    @GET
    @Path("jmsdestination/{jmsServer}/{destination}/{action}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean jmsDestinationAction(	@HeaderParam("user-agent") String userAgent, 
											@QueryParam("scope") Set<String> scope,
											@PathParam("jmsServer") String jmsServer,
											@PathParam("destination") String destination,
											@PathParam("action") String action) {
        try {
        	
        	if(isAuthenticated()) {
        		
        		//AppLog.getLogger().notice("The username is allowed - Executing the action");
        		
	        	if(isValidActionForDestination(action)) {
	        		DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
	        		return executeDestinationOperation(conn, jmsServer, destination, action);
	        	} else {
	        		AppLog.getLogger().error("The action [" + action + "] is not a valid action - Nothing will be done");
	        	}
        	} else{
        		AppLog.getLogger().error("The username is not allowed - Skipping the action");
        	}
            	
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the destinationAction method", ex);
        }
        return false;
    }
    
    /**
     * 
     * @param userAgent
     * @param scope
     * @param jmsServer
     * @param action
     * @return
     */
    @GET
    @Path("jmsserver/{jmsServer}/{action}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean jmsServerAction(	@HeaderParam("user-agent") String userAgent, 
									@QueryParam("scope") Set<String> scope,
									@PathParam("jmsServer") String jmsServer,
									@PathParam("action") String action) {
        try {

        	if(isAuthenticated()) {
    			
        		//AppLog.getLogger().notice("The username is allowed - Executing the action");
        		
	        	if(isValidActionForDestination(action)) {
	        		DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
	        		return executeDestinationOperation(conn, jmsServer, action);
	        	} else {
	        		AppLog.getLogger().error("The action [" + action + "] is not a valid action - Nothing will be done");
	        	}
        	} else{
        		AppLog.getLogger().error("The username is not allowed - Skipping the action");
        	}
            	
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the jmsServerAction method", ex);
        }
        return false;
    }
    
    /**
     * 
     * @param userAgent
     * @param scope
     * @param jmsServer
     * @param queue
     * @param action
     * @return
     */
    @GET
    @Path("safdestination/{safAgent}/{saf}/{action}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean safDestinationAction(	@HeaderParam("user-agent") String userAgent, 
											@QueryParam("scope") Set<String> scope,
											@PathParam("safAgent") String safAgent,
											@PathParam("saf") String saf,
											@PathParam("action") String action) {
        try {

        	if(isAuthenticated()) {
        		
        		//AppLog.getLogger().notice("The username is allowed - Executing the action");
        		
	        	if(isValidActionForSaf(action)) {
	        		DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
	        		return executeSafOperation(conn, safAgent, saf, action);
	        	} else {
	        		AppLog.getLogger().error("The action [" + action + "] is not a valid action - Nothing will be done");
	        	}
        	} else{
        		AppLog.getLogger().error("The username is not allowed - Skipping the action");
        	}
            	
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the safAction method", ex);
        }
        return false;
    }
    
    /**
     * 
     * @param userAgent
     * @param scope
     * @param jmsServer
     * @param action
     * @return
     */
    @GET
    @Path("safagent/{safAgent}/{action}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean safAgentAction(	@HeaderParam("user-agent") String userAgent, 
									@QueryParam("scope") Set<String> scope,
									@PathParam("safAgent") String safAgent,
									@PathParam("action") String action) {
        try {
        	
        	if(isAuthenticated()) {
				
        		//AppLog.getLogger().notice("The username is allowed - Executing the action");
        		
	        	if(isValidActionForSaf(action)) {
	        		DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
	        		return executeSafOperation(conn, safAgent, action);
	        	} else {
	        		AppLog.getLogger().error("The action [" + action + "] is not a valid action - Nothing will be done");
	        	}
        	} else{
        		AppLog.getLogger().error("The username is not allowed - Skipping the action");
        	}
            	
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the safAgentAction method", ex);
        }
        return false;
    }
    
    /**
     * 
     * @param action
     * @return
     */
    private boolean isValidActionForDestination(String action) {
    	switch (action) {
        
	    	case MonitorProperties.PAUSE_PRODUCTON:
	    		return true;
	    		
	    	case MonitorProperties.RESUME_PRODUCTON:
	    		return true;
	    		
	    	case MonitorProperties.PAUSE_CONSUMPTION:
				return true;
	    		
	    	case MonitorProperties.RESUME_CONSUMPTION:
				return true;
	    		
	    	case MonitorProperties.PAUSE_INSERTION:
				return true;
	    		
	    	case MonitorProperties.RESUME_INSERTION:
				return true;
				
			default:
				return false;
	    }
    }
    
    /**
     * 
     * @param action
     * @return
     */
    private boolean isValidActionForSaf(String action) {
    	switch (action) {
        
	    	case MonitorProperties.PAUSE_INCOMING:
	    		return true;
	    		
	    	case MonitorProperties.RESUME_INCOMING:
	    		return true;
	    		
	    	case MonitorProperties.PAUSE_FORWARDING:
				return true;
	    		
	    	case MonitorProperties.RESUME_FORWARDING:
				return true;
	    		
	    	case MonitorProperties.PAUSE_RECEIVING:
				return true;
	    		
	    	case MonitorProperties.RESUME_RECEIVING:
				return true;
				
			default:
				return false;
	    }
    }
    
    /**
     * 
     * @return
     */
    private boolean isAuthenticated() {
    	
    	AppProperties appProps = new AppProperties(application);
		Boolean restrictedAction = new Boolean(appProps.getProperty(PropKey.RESTRICTED_ACTION));

		if(restrictedAction) {
			
			String username = securityContext.getUserPrincipal().getName();
			AppLog.getLogger().notice("The restriction mode is set");
			AppLog.getLogger().notice("Principal is [" + username + "]");

			String restrictedRoles = appProps.getProperty(PropKey.RESTRICTED_ROLES);
			List<String> rolesList = tokenizeRestrictedRolesText(restrictedRoles);
			    			
			Iterator<String> iteratorRolesList = rolesList.iterator();
	        boolean allowed = false;
	        	
	        while (iteratorRolesList.hasNext()) {
	            String role = iteratorRolesList.next();
	
	            if(securityContext.isUserInRole(role)) {
	            	AppLog.getLogger().notice("The username [" + username + "] is part of the role [" + role + "]");
	            	allowed = true;
	                break;
	    		}
	        }
	        
	        if(allowed){
	        	return true;
	        } else{
	        	return false;
	        }
		} else{
	    	AppLog.getLogger().notice("The restriction mode is not set - This action cannot be executed without to be authenticated");
	    	return false;
	    }
    }
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param queueName
     * @param action
     * @return
     */
    private boolean executeDestinationOperation(DomainRuntimeServiceMBeanConnection conn, String jmsServerName, String queueName, String action){
		    	
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			for (int index = 0; index < serverRuntimes.length; index++){
				
				ObjectName serverRuntime = serverRuntimes[index]; 
		    	ObjectName jmsRuntime = conn.getChild(serverRuntime, JMS_RUNTIME);			    	
		        ObjectName[] jmsServers = conn.getChildren(jmsRuntime, JMS_SERVERS);
		        
		        // Find correct JMS server
		        for (ObjectName jmsServer : jmsServers)
		        {
		        	String currentJmsServerName = conn.getTextAttr(jmsServer, NAME);			        	
		        	if(currentJmsServerName.equals(jmsServerName)){
		        		
		        		// Find the correct destination
		        		for (ObjectName destination : conn.getChildren(jmsServer, DESTINATIONS)) {
					    	
		        			String destinationName = ResourceNameNormaliser.normalise(JMSSVR_RESOURCE_TYPE, conn.getTextAttr(destination, NAME));
		        			//String destinationName = conn.getTextAttr(destination, NAME);
		        			if(destinationName.equals(queueName)) {
		        				
		        				conn.invoke(destination, action, null, null);
		        				String username = securityContext.getUserPrincipal().getName();
		        				AppLog.getLogger().notice("The username [" + username + "] executed the action [" + action + "] for the queue [" + queueName + "] deployed on JMS server [" + jmsServerName + "]");
		        				return true;
		        			}
		        		}	
		        	}	    
		        }
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of action [" + action + "] for the queue [" + queueName + "] deployed on JMS server [" + jmsServerName + "]", ex);
		}
		
		AppLog.getLogger().error("Action [" + action + "] to be executed for the queue [" + queueName + "] deployed on JMS server [" + jmsServerName + "] was not executed");
		AppLog.getLogger().error("   -> Possible reason is wrong JMS server or JMS queue name");
		return false;
	}
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param action
     * @return
     */
    private boolean executeDestinationOperation(DomainRuntimeServiceMBeanConnection conn, String jmsServerName, String action){
		    	
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			for (int index = 0; index < serverRuntimes.length; index++){
				
				ObjectName serverRuntime = serverRuntimes[index]; 
		    	ObjectName jmsRuntime = conn.getChild(serverRuntime, JMS_RUNTIME);			    	
		        ObjectName[] jmsServers = conn.getChildren(jmsRuntime, JMS_SERVERS);
		        
		        // Find correct JMS server
		        for (ObjectName jmsServer : jmsServers)
		        {
		        	String currentJmsServerName = conn.getTextAttr(jmsServer, NAME);			        	
		        	if(currentJmsServerName.equals(jmsServerName)){
		        		
        				conn.invoke(jmsServer, action, null, null);
        				String username = securityContext.getUserPrincipal().getName();
        				AppLog.getLogger().notice("The username [" + username + "] executed the action [" + action + "] for the JMS server [" + jmsServerName + "]");
        				return true;
		        	}	    
		        }
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of action [" + action + "] for the JMS server [" + jmsServerName + "]", ex);
		}
		
		AppLog.getLogger().error("Action [" + action + "] to be executed for the JMS server [" + jmsServerName + "] was not executed");
		AppLog.getLogger().error("   -> Possible reason is wrong JMS server");
		return false;
	}
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param queueName
     * @param action
     * @return
     */
    private boolean executeSafOperation(DomainRuntimeServiceMBeanConnection conn, String safAgentName, String safName, String action){
		
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			for (int index = 0; index < serverRuntimes.length; index++){
		        
		        ObjectName serverRuntime = serverRuntimes[index];
                ObjectName safRuntime = conn.getChild(serverRuntime, SAF_RUNTIME);
                ObjectName[] safServers = conn.getChildren(safRuntime, AGENTS);
		        
		        // Find correct JMS server
		        for (ObjectName safAgent : safServers)
		        {
		        	String currentSafAgentName = conn.getTextAttr(safAgent, NAME);			        	
		        	if(currentSafAgentName.equals(safAgentName)){
		        		
		        		// Find the correct element
		        		for (ObjectName remoteEndPoint : conn.getChildren(safAgent, REMOTE_END_POINTS)) {
					    	
		        			String currentSafName = ResourceNameNormaliser.normalise(SAFAGENT_RESOURCE_TYPE, conn.getTextAttr(remoteEndPoint, NAME));
		        			//String currentSafName = conn.getTextAttr(destination, NAME);
		        			if(currentSafName.equals(safName)) {
		        				
		        				conn.invoke(remoteEndPoint, action, null, null);
		        				String username = securityContext.getUserPrincipal().getName();
		        				AppLog.getLogger().notice("The username [" + username + "] executed the action [" + action + "] for the SAF [" + safName + "] deployed on SAF agent [" + safAgentName + "]");
		        				return true;
		        			}
		        		}	
		        	}	    
		        }
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of action [" + action + "] for the SAF [" + safName + "] deployed on SAF agent [" + safAgentName + "]", ex);
		}
		
		AppLog.getLogger().error("Action [" + action + "] to be executed for the SAF [" + safName + "] deployed on SAF agent [" + safAgentName + "] was not executed");
		AppLog.getLogger().error("   -> Possible reason is wrong SAF agent or SAF name");
		return false;
	}
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param action
     * @return
     */
    private boolean executeSafOperation(DomainRuntimeServiceMBeanConnection conn, String safAgentName, String action){
    	
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			for (int index = 0; index < serverRuntimes.length; index++){
				
				ObjectName serverRuntime = serverRuntimes[index];
                ObjectName safRuntime = conn.getChild(serverRuntime, SAF_RUNTIME);
                ObjectName[] safAgents = conn.getChildren(safRuntime, AGENTS);
		        
		        // Find correct SAF agent
		        for (ObjectName safAgent : safAgents)
		        {
		        	String currentSafAgentName = conn.getTextAttr(safAgent, NAME);			        	
		        	if(currentSafAgentName.equals(safAgent)){
		        		
        				conn.invoke(safAgent, action, null, null);
        				String username = securityContext.getUserPrincipal().getName();
        				AppLog.getLogger().notice("The username [" + username + "] executed the action [" + action + "] for the SAF agent [" + safAgent + "]");
        				return true;
		        	}	    
		        }
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of action [" + action + "] for the SAF agent [" + safAgentName + "]", ex);
		}
		
		AppLog.getLogger().error("Action [" + action + "] to be executed for the SAF agent [" + safAgentName + "] was not executed");
		AppLog.getLogger().error("   -> Possible reason is wrong SAF agent");
		return false;
	}
}