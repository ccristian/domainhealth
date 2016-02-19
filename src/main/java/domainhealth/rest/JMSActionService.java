package domainhealth.rest;

import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.MonitorProperties;
import domainhealth.core.statistics.ResourceNameNormaliser;

import javax.annotation.PostConstruct;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DESTINATIONS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_RUNTIME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.JMS_SERVERS;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;
import static domainhealth.core.statistics.MonitorProperties.JMSSVR_RESOURCE_TYPE;

import java.util.*;

/**
 * Created by chiovcr on 02/12/2014.
 */
@Path("/jmsaction")
public class JMSActionService {

    @Context
    private ServletContext application;

    
    /**
     * 
     */
    @PostConstruct
    public void initialize() {
    	
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
    @Path("queue/{jmsServer}/{queue}/{action}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean queueAction(	@HeaderParam("user-agent") String userAgent, 
								@QueryParam("scope") Set<String> scope,
								@PathParam("jmsServer") String jmsServer,
								@PathParam("queue") String queue,
								@PathParam("action") String action) {
        try {

			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();;
			
        	if(isValidAction(action)) {
        		return executeOperation(conn, jmsServer, queue, action);
        	} else {
        		AppLog.getLogger().error("The action [" + action + "] is not a valid action - Nothing will be done");
        	}
            	
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the action method", ex);
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
    @Path("server/{jmsServer}/{action}")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean serverAction(	@HeaderParam("user-agent") String userAgent, 
									@QueryParam("scope") Set<String> scope,
									@PathParam("jmsServer") String jmsServer,
									@PathParam("action") String action) {
        try {

			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();;
			
        	if(isValidAction(action)) {
        		return executeOperation(conn, jmsServer, action);
        	} else {
        		AppLog.getLogger().error("The action [" + action + "] is not a valid action - Nothing will be done");
        	}
            	
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the action method", ex);
        }
        return false;
    }
    
    /**
     * 
     * @param action
     * @return
     */
    private boolean isValidAction(String action) {
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
     * @param conn
     * @param jmsServerName
     * @param queueName
     * @param action
     * @return
     */
    private boolean executeOperation(DomainRuntimeServiceMBeanConnection conn, String jmsServerName, String queueName, String action){
		    	
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			// Find the Admin server
			for (int index = 0; index < serverRuntimes.length; index++){
				
				// Get the admin server
				if(DomainRuntimeServiceMBeanConnection.isThisTheAdminServer()){
			
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
			        			if(destinationName.equals(queueName)) {
			        				
			        				conn.invoke(destination, action, null, null);
			        				
			        				System.out.println("Action [" + action + "] executed for the queue [" + queueName + "] deployed on JMS server [" + jmsServerName + "]");
			        				AppLog.getLogger().info("Action [" + action + "] executed for the queue [" + queueName + "] deployed on JMS server [" + jmsServerName + "]");
			        				return true;
			        			}
			        		}	
			        	}	    
			        }
				}
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of action [" + action + "] for the queue [" + queueName + "] deployed on JMS server [" + jmsServerName + "]", ex);
		}
		
		AppLog.getLogger().info("Action [" + action + "] to be executed for the queue [" + queueName + "] deployed on JMS server [" + jmsServerName + "] was not executed");
		AppLog.getLogger().info("-> Possible reason is wrong JMS server or JMS queue name");
		return false;
	}
    
    /**
     * 
     * @param conn
     * @param jmsServerName
     * @param action
     * @return
     */
    private boolean executeOperation(DomainRuntimeServiceMBeanConnection conn, String jmsServerName, String action){
		    	
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			// Find the Admin server
			for (int index = 0; index < serverRuntimes.length; index++){
				
				// Get the admin server
				if(DomainRuntimeServiceMBeanConnection.isThisTheAdminServer()){
			
					ObjectName serverRuntime = serverRuntimes[index]; 
			    	ObjectName jmsRuntime = conn.getChild(serverRuntime, JMS_RUNTIME);			    	
			        ObjectName[] jmsServers = conn.getChildren(jmsRuntime, JMS_SERVERS);
			        
			        // Find correct JMS server
			        for (ObjectName jmsServer : jmsServers)
			        {
			        	String currentJmsServerName = conn.getTextAttr(jmsServer, NAME);			        	
			        	if(currentJmsServerName.equals(jmsServerName)){
			        		
	        				conn.invoke(jmsServer, action, null, null);
	        				
	        				System.out.println("Action [" + action + "] executed for the JMS server [" + jmsServerName + "]");
	        				AppLog.getLogger().info("Action [" + action + "] executed for the JMS server [" + jmsServerName + "]");
	        				return true;
			        	}	    
			        }
				}
			}
		} catch(Exception ex){
			AppLog.getLogger().error("Error during execution of action [" + action + "] for the JMS server [" + jmsServerName + "]", ex);
		}
		
		AppLog.getLogger().info("Action [" + action + "] to be executed for the JMS server [" + jmsServerName + "] was not executed");
		AppLog.getLogger().info("-> Possible reason is wrong JMS server");
		return false;
	}
}