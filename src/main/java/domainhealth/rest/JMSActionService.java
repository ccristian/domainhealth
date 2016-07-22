package domainhealth.rest;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.AGENTS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DESTINATIONS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_SERVERS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.REMOTE_END_POINTS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.SAF_RUNTIME;
import static domainhealth.core.statistics.MonitorProperties.JMSSVR_RESOURCE_TYPE;
import static domainhealth.core.statistics.MonitorProperties.SAFAGENT_RESOURCE_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.jms.TextMessage;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
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

import weblogic.jms.common.JMSMessageId;
import weblogic.jms.extensions.JMSMessageInfo;
import weblogic.jms.extensions.WLMessage;
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
    /*
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
    */
    
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
    		AppLog.getLogger().error("The username is not logged/authenticated");
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
    @Path("jmsdestination/{action}/{jmsServer}/{destination}")
    //@Path("jmsdestination/{jmsServer}/{destination}/{action}")
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
/*
// Not used so commented to avoid usage
    @GET
    @Path("jmsserver/{action}/{jmsServer}")
    //@Path("jmsserver/{jmsServer}/{action}")
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
*/
    
    /**
     * 
     * @param userAgent
     * @param scope
     * @param safAgent
     * @param saf
     * @param action
     * @return
     */
    @GET
    @Path("safdestination/{action}/{safAgent}/{saf}")
    //@Path("safdestination/{safAgent}/{saf}/{action}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean safDestinationAction(	@HeaderParam("user-agent") String userAgent, 
											@QueryParam("scope") Set<String> scope,
											@PathParam("safAgent") String safAgent,
											@PathParam("saf") String saf,
											@PathParam("action") String action) {
        try {

        	if(isAuthenticated()) {
        		
        		//AppLog.getLogger().notice("The username is allowed - Executing the action");
        		
	        	if(isValidActionForSafRemoteDestination(action)) {
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
/*
// Not used so commented to avoid usage
    @GET
    @Path("safagent/{action}/{safAgent}")
    //@Path("safagent/{safAgent}/{action}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean safAgentAction(	@HeaderParam("user-agent") String userAgent, 
									@QueryParam("scope") Set<String> scope,
									@PathParam("safAgent") String safAgent,
									@PathParam("action") String action) {
        try {
        	
        	if(isAuthenticated()) {
				
        		//AppLog.getLogger().notice("The username is allowed - Executing the action");
        		
	        	if(isValidActionForSafAgent(action)) {
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
*/
    
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
/*
// Not used so commented to avoid usage
    private boolean isValidActionForSafAgent(String action) {
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
*/
    
    /**
     * 
     * @param action
     * @return
     */
    private boolean isValidActionForSafRemoteDestination(String action) {
    	switch (action) {
        
	    	case MonitorProperties.PAUSE_INCOMING:
	    		return true;
	    		
	    	case MonitorProperties.RESUME_INCOMING:
	    		return true;
	    		
	    	case MonitorProperties.PAUSE_FORWARDING:
				return true;
	    		
	    	case MonitorProperties.RESUME_FORWARDING:
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
			
			//String username = securityContext.getUserPrincipal().getName();
			//AppLog.getLogger().notice("The restriction mode is set");
			//AppLog.getLogger().notice("Principal is [" + username + "]");

			String restrictedRoles = appProps.getProperty(PropKey.RESTRICTED_ROLES);
			List<String> rolesList = tokenizeRestrictedRolesText(restrictedRoles);
			    			
			Iterator<String> iteratorRolesList = rolesList.iterator();
	        boolean allowed = false;
	        	
	        while (iteratorRolesList.hasNext()) {
	            String role = iteratorRolesList.next();
	
	            if(securityContext.isUserInRole(role)) {
	            	//AppLog.getLogger().notice("The username [" + username + "] is part of the role [" + role + "]");
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
	    	AppLog.getLogger().error("The restriction mode is not set - This action cannot be executed without to be authenticated");
	    	return false;
	    }
    }
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param destinationName
     * @param action
     * @return
     */
    private boolean executeDestinationOperation(DomainRuntimeServiceMBeanConnection conn, String jmsServerName, String destinationName, String action){
		    	
		try {
			ObjectName destinationObjectName = getJmsDestination(conn, jmsServerName, destinationName );
			if(destinationObjectName != null) {
			
				conn.invoke(destinationObjectName, action, null, null);
				String username = securityContext.getUserPrincipal().getName();
				AppLog.getLogger().notice("The username [" + username + "] executed the action [" + action + "] for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]");
				return true;
			}
			
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of action [" + action + "] for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]", ex);
		}
		
		AppLog.getLogger().error("Action [" + action + "] to be executed for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "] was not executed");
		AppLog.getLogger().error("   -> Possible reason is wrong JMS server or JMS destination");
		return false;
	}
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param destinationName
     * @param action
     * @return
     */
    /*
    @GET
    @Path("jmsmessages/print/{jmsServerName}/{destinationName}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean printMessages(@PathParam("jmsServerName") String jmsServerName, @PathParam("destinationName") String destinationName) {
    		    	
		try {
			
			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName destination = getJmsDestination(conn, jmsServerName, destinationName );
			if(destination != null) {
			
				String messages = (String) conn.invoke(destination, "getMessages", new Object[]{"", 0}, new String[] {String.class.getName(), Integer.class.getName()});
				long cursorSize = (long) conn.invoke(destination, "getCursorSize", new Object[]{messages}, new String[] {String.class.getName()});
	
				CompositeData[] compositeDatas = (CompositeData[]) conn.invoke(destination, "getNext", new Object[]{messages, (int)cursorSize}, new String[] {String.class.getName(), Integer.class.getName()});
				if(compositeDatas != null) {
					
					AppLog.getLogger().notice("There is [" + compositeDatas.length + "] messages found in the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]");
					for(int indexCompositeDatas = 0; indexCompositeDatas < compositeDatas.length; indexCompositeDatas ++) {
						
						CompositeData compositeData = compositeDatas[indexCompositeDatas];
						JMSMessageInfo jmsMessageInfo = new JMSMessageInfo(compositeData);
						JMSMessageId jmsMessageId = jmsMessageInfo.getMessage().getMessageId();
						
						AppLog.getLogger().notice("   MessageId is [" + jmsMessageId + "]");
					}
				} else {
					AppLog.getLogger().notice("None messages found");
				}
				return true;
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of getMessages for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]", ex);
			return false;
		}
		
		AppLog.getLogger().error("getMessages method failed to be executed for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "] was not executed");
		AppLog.getLogger().error("   -> Possible reason is wrong JMS server or JMS destination");
		return false;
	}
	*/
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param destinationName
     * @param action
     * @return
     */
    /*
    @GET
    @Path("jmsmessage/print/{jmsServerName}/{destinationName}/{messageId}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean printMessage(@PathParam("jmsServerName") String jmsServerName, @PathParam("destinationName") String destinationName, @PathParam("messageId") String messageId) {
    		    	
		try {
			
			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName destination = getJmsDestination(conn, jmsServerName, destinationName );
			if(destination != null) {
				
				AppLog.getLogger().notice("Looking for the message [ID:" + messageId + "] in the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]");
				CompositeData compositeData = (CompositeData) conn.invoke(destination, "getMessage", new Object[]{"ID:" + messageId}, new String[] {String.class.getName()});
				
				if(compositeData != null) {
					JMSMessageInfo jmsMessageInfo = new JMSMessageInfo(compositeData);
					WLMessage wlMessage = jmsMessageInfo.getMessage();
					
					if (wlMessage instanceof TextMessage) {
						String message = ((TextMessage)wlMessage).getText();
						AppLog.getLogger().notice("   Message is Text [" + message + "]");
						//return message;
						return true;
					} else if (wlMessage instanceof ObjectMessage) {
						Object message = ((ObjectMessage)wlMessage).getObject();
						AppLog.getLogger().notice("   Message is Object [" + message + "]");
						return true;
					} else {
						AppLog.getLogger().notice("   Not a Text or Object Message");
						AppLog.getLogger().notice("   Message is [" + wlMessage.toString() + "]");
						return false;
					}
				} else {
					AppLog.getLogger().notice("-> Cannot find the message [" + messageId + "] ...");
					return false;
				}
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of getMessage with ID [" + messageId + "] for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]", ex);
			return false;
		}
		
		AppLog.getLogger().error("getMessage method failed to be executed for the ID [" + messageId + "] for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "] was not executed");
		AppLog.getLogger().error("   -> Possible reason is wrong JMS message ID, JMS server or JMS destination");
		return false;
	}
    */
    
    /**
     * 
     * @param jmsServerFromName
     * @param destinationFromName
     * @param jmsServerToName
     * @param destinationToName
     * @param messageId
     * @return
     */
    @GET
    @Path("jmsmessage/move/from/{jmsServerFromName}/{destinationFromName}/to/{jmsServerToName}/{destinationToName}/message/{messageId}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean moveMessage(	@PathParam("jmsServerFromName") String jmsServerFromName,
				    			@PathParam("destinationFromName") String destinationFromName,
				    			@PathParam("jmsServerToName") String jmsServerToName,
				    			@PathParam("destinationToName") String destinationToName,
				    			@PathParam("messageId") String messageId) {
		try {
			
			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
			
			ObjectName fromDestination = getJmsDestination(conn, jmsServerFromName, destinationFromName);
			ObjectName targetDestination = getJmsDestination(conn, jmsServerToName, destinationToName);
			
			if(fromDestination != null && targetDestination != null) {
		        				
				CompositeData destinationInfo = (CompositeData) conn.getObjectAttr(targetDestination, "DestinationInfo");
				if(destinationInfo != null) {
					
					// Move the message
					AppLog.getLogger().notice("Moving the message ID [" + messageId + "] from the queue [" + destinationFromName + "] deployed on JMS server [" + jmsServerFromName + "] in the queue [" + destinationToName + "] deployed on JMS server [" + jmsServerToName + "]");
					
					String selector = "JMSMessageID='ID:" + messageId + "'";
					
					Integer nb = (Integer) conn.invoke(fromDestination, "moveMessages", new Object[]{selector, destinationInfo}, new String[] {String.class.getName(), CompositeData.class.getName()});
					if(nb != null) {
						AppLog.getLogger().notice("-> [" + nb + "] message(s) have been moved ...");
						return true;
					}
				}
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of moveMessages with ID [" + messageId + "] from the queue [" + destinationFromName + "] deployed on JMS server [" + jmsServerFromName + "] in the queue [" + destinationToName + "] deployed on JMS server [" + jmsServerToName + "]");
			return false;
		}
		
		AppLog.getLogger().error("moveMessages method failed to be executed for the ID [" + messageId + "] from the queue [" + destinationFromName + "] deployed on JMS server [" + jmsServerFromName + "] in the queue [" + destinationToName + "] deployed on JMS server [" + jmsServerToName + "]");
		AppLog.getLogger().error("   -> Possible reason is wrong JMS message ID, JMS server or JMS destination");
		return false;
	}
    
    /**
     * 
     */
    @GET
    @Path("jmsservers/list")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> getJmsServers() {
    	
    	Set<String> jmsServersList = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    	try {
			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName[] jmsServers = getJmsServers(conn);			

			if(jmsServers != null) {
				
			    for (ObjectName jmsServer : jmsServers)
			    {
			    	String jmsServerName = conn.getTextAttr(jmsServer, NAME);
			    	jmsServersList.add(jmsServerName);
			    }
    		}
		    return jmsServersList;
			
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of getJmsServers", ex);
			return jmsServersList;
		}
	}
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param destinationName
     * @param action
     * @return
     */
    @GET
    @Path("jmsdestinations/list/{jmsServerName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> getJmsDestinations(	@PathParam("jmsServerName") String jmsServerName) {
    	
    	Set<String> jmsDestinationsList = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    	try {
			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName[] destinations = getJmsDestinations(conn, jmsServerName);
			
			if(destinations != null) {
				
				// Update the name of the resource
	    		for (ObjectName destination : destinations) {
			    	
	    			String destinationName = ResourceNameNormaliser.normalise(JMSSVR_RESOURCE_TYPE, conn.getTextAttr(destination, NAME));
	    			jmsDestinationsList.add(destinationName);
	    		}
			}
			return jmsDestinationsList;
			
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of getJmsDestinations to get the list of destinations deployed on JMS server [" + jmsServerName + "]", ex);
			AppLog.getLogger().error("   -> Possible reason is wrong JMS server");
			return jmsDestinationsList;
		}
	}
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param destinationName
     * @param action
     * @return
     */
    @GET
    @Path("jmsmessages/list/{jmsServerName}/{destinationName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> getJmsMessages(	@PathParam("jmsServerName") String jmsServerName,
				    					@PathParam("destinationName") String destinationName) {
    	
    	Set<String> jmsMessages = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    	try {
			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName destination = getJmsDestination(conn, jmsServerName, destinationName);
			if(destination != null) {
			
				String messages = (String) conn.invoke(destination, "getMessages", new Object[]{"", 0}, new String[] {String.class.getName(), Integer.class.getName()});
				long cursorSize = (long) conn.invoke(destination, "getCursorSize", new Object[]{messages}, new String[] {String.class.getName()});
	
				CompositeData[] compositeDatas = (CompositeData[]) conn.invoke(destination, "getNext", new Object[]{messages, (int)cursorSize}, new String[] {String.class.getName(), Integer.class.getName()});
				if(compositeDatas != null) {
					
					AppLog.getLogger().notice("There is [" + compositeDatas.length + "] messages found in the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]");
					for(int indexCompositeDatas = 0; indexCompositeDatas < compositeDatas.length; indexCompositeDatas ++) {
						
						CompositeData compositeData = compositeDatas[indexCompositeDatas];
						JMSMessageInfo jmsMessageInfo = new JMSMessageInfo(compositeData);
						JMSMessageId jmsMessageId = jmsMessageInfo.getMessage().getMessageId();
						
						AppLog.getLogger().notice("   MessageId is [" + jmsMessageId + "]");
						jmsMessages.add(jmsMessageId.toString());
					}
				} else {
					AppLog.getLogger().notice("None messages found");
				}
			}			
			return jmsMessages;
			
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of getJmsMessages for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]", ex);
			AppLog.getLogger().error("   -> Possible reason is wrong JMS server or JMS destination");
			return jmsMessages;
		}		
	}
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param destinationName
     * @param action
     * @return
     */
    @GET
    @Path("jmsmessage/get/{jmsServerName}/{destinationName}/{messageId}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getMessage(@PathParam("jmsServerName") String jmsServerName, @PathParam("destinationName") String destinationName, @PathParam("messageId") String messageId) {
    		    	
		try {
			
			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName destination = getJmsDestination(conn, jmsServerName, destinationName );
			if(destination != null) {
				
				AppLog.getLogger().notice("Looking for the message [ID:" + messageId + "] in the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]");
				CompositeData compositeData = (CompositeData) conn.invoke(destination, "getMessage", new Object[]{"ID:" + messageId}, new String[] {String.class.getName()});
				
				if(compositeData != null) {
					JMSMessageInfo jmsMessageInfo = new JMSMessageInfo(compositeData);
					WLMessage wlMessage = jmsMessageInfo.getMessage();
					
					if (wlMessage instanceof TextMessage) {
						String message = ((TextMessage)wlMessage).getText();
						AppLog.getLogger().notice("   Message is Text [" + message + "]");
						return message;
						
					} /*else if (wlMessage instanceof ObjectMessage) {
						Object message = ((ObjectMessage)wlMessage).getObject();
						AppLog.getLogger().notice("   Message is Object [" + message + "]");
						return true;
					} */ else {
						AppLog.getLogger().notice("   Not a Text or Object Message");
						AppLog.getLogger().notice("   Message is [" + wlMessage.toString() + "]");
						return null;
					}
				} else {
					AppLog.getLogger().notice("-> Cannot find the message [" + messageId + "] ...");
					return null;
				}
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of getMessage with ID [" + messageId + "] for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "]", ex);
			return null;
		}
		
		AppLog.getLogger().error("getMessage method failed to be executed for the ID [" + messageId + "] for the destination [" + destinationName + "] deployed on JMS server [" + jmsServerName + "] was not executed");
		AppLog.getLogger().error("   -> Possible reason is wrong JMS message ID, JMS server or JMS destination");
		return null;
	}
    
    /**
     * 
     * @param conn
     * @return
     */
    private ObjectName[] getJmsServers(DomainRuntimeServiceMBeanConnection conn) {
    
    	try {
    		
    		Set<ObjectName> jmsServersList = new TreeSet<ObjectName>();
	    	ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			for (int index = 0; index < serverRuntimes.length; index++){
				
				ObjectName serverRuntime = serverRuntimes[index];
		    	ObjectName jmsRuntime = conn.getChild(serverRuntime, JMS_RUNTIME);
		    	
		    	// Get the list for the WL server
		        ObjectName[] jmsServers = conn.getChildren(jmsRuntime, JMS_SERVERS);
		        
		        // Add all the elements to the global list
	    		for (ObjectName jmsServer : jmsServers) {
	    			jmsServersList.add(jmsServer);
	    		}
			}
			
			ObjectName[] jmsServersObjectList = new ObjectName[jmsServersList.size()];
			jmsServersObjectList = jmsServersList.toArray(jmsServersObjectList);
			return jmsServersObjectList;
			
			/*
			// Return the global list
			ObjectName[] jmsServersObjectList = new ObjectName[jmsServersList.size()];
			Iterator<ObjectName> jmsServersListIterator = jmsServersList.iterator();
			int index = 0;
			while(jmsServersListIterator.hasNext()) {
				jmsServersObjectList[index] = jmsServersListIterator.next();
				index ++;
    		}
    		return jmsServersObjectList;
    		*/
			
	    } catch(Exception ex){
			AppLog.getLogger().error("Error during execution of getJmsServers", ex);
			return null;
		}
    }
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @return
     */
    private ObjectName[] getJmsDestinations(DomainRuntimeServiceMBeanConnection conn, String jmsServerName) {
    
    	try {
	    	ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			for (int index = 0; index < serverRuntimes.length; index++){
				
				ObjectName serverRuntime = serverRuntimes[index];
		    	ObjectName jmsRuntime = conn.getChild(serverRuntime, JMS_RUNTIME);
		        ObjectName[] jmsServers = conn.getChildren(jmsRuntime, JMS_SERVERS);
		        
		    	// Get a reference on Target destination
			    for (ObjectName jmsServer : jmsServers)
			    {
			    	String currentJmsServerName = conn.getTextAttr(jmsServer, NAME);
			    	if(currentJmsServerName.equals(jmsServerName)){
			    		
			    		ObjectName[] jmsDestinations = conn.getChildren(jmsServer, DESTINATIONS);
			    		return jmsDestinations;
			    	}
			    }
			}
	    } catch(Exception ex){
			AppLog.getLogger().error("Error during execution of getJmsDestinations - Cannot find the destinations on JMS server [" + jmsServerName + "]", ex);
			return null;
		}
	    return null;
    }
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param destinationName
     * @return
     */
    private ObjectName getJmsDestination(DomainRuntimeServiceMBeanConnection conn, String jmsServerName, String destinationName ) {
    
    	try {
	    	ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			for (int index = 0; index < serverRuntimes.length; index++){
				
				ObjectName serverRuntime = serverRuntimes[index];
		    	ObjectName jmsRuntime = conn.getChild(serverRuntime, JMS_RUNTIME);
		        ObjectName[] jmsServers = conn.getChildren(jmsRuntime, JMS_SERVERS);
		        
		    	// Get a reference on Target destination
			    for (ObjectName jmsServer : jmsServers)
			    {
			    	String currentJmsServerName = conn.getTextAttr(jmsServer, NAME);		        	
			    	if(currentJmsServerName.equals(jmsServerName)){
			    		
			    		// Find the correct destination
			    		for (ObjectName destination : conn.getChildren(jmsServer, DESTINATIONS)) {
					    	
			    			String currentDestinationName = ResourceNameNormaliser.normalise(JMSSVR_RESOURCE_TYPE, conn.getTextAttr(destination, NAME));
			    			if(currentDestinationName.equals(destinationName)) {
			    				return destination;
			    			}
			    		}
			    	}
			    }
			}
	    } catch(Exception ex){
			AppLog.getLogger().error("Error during execution of getDestination - Cannot find the destination [" + destinationName + "] on JMS server [" + jmsServerName + "]", ex);
			return null;
		}
	    return null;
    }
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param action
     * @return
     */
/*
// Not used so commented to avoid usage
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
*/
    /**
     * 
     * @param conn
     * @param safAgentName
     * @param safName
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
    
/*
// Not used so commented to avoid usage
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
*/
}