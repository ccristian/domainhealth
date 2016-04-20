package domainhealth.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanPropConstants;

/**
 */
@Path("/soaaction")
public class SOAService {
	
	public final static String SEPARATOR = "----------------------------------------------------------------------";
	
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
     * @param userAgent
     * @param scope
     * @param jmsServer
     * @param queue
     * @param action
     * @return
     */
    @GET
    @Path("soaDetail/")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> soaDetail(	@HeaderParam("user-agent") String userAgent, 
								@QueryParam("scope") Set<String> scope) {
    	    	
    	try {
    		DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
            getDetails(conn);
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the soaDetail method", ex);
        }
    	
Set<String> results = new TreeSet<String>();
results.add("Code Executed properly ...");

return results;
    }
    
    /**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
    private void getDetails(DomainRuntimeServiceMBeanConnection conn) throws Exception {

    	AppLog.getLogger().notice("");
		
// -----------------------------------------
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("oracle.dms:type=soainfra_bpmn_requests,name=/soainfra/engines/bpmn/requests/system");
		list.add("oracle.dms:type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system");
		
		list.add("oracle.dms:type=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*");
		
// Nothing is visible - Check if it makes sense or not ...
//list.add("oracle.dms:type=soainfra_component,soainfra_component_type=bpel,soainfra_composite_label=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*");
		
		ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
		for (int index = 0; index < serverRuntimes.length; index++){
	        
	        ObjectName serverRuntime = serverRuntimes[index];
	        String wlServerName = conn.getTextAttr(serverRuntime, WebLogicMBeanPropConstants.NAME);
	        
	        list.add("oracle.dms:Location=" + wlServerName + ",type=soainfra_bpmn_requests,name=/soainfra/engines/bpmn/requests/system");
	        list.add("oracle.dms:Location=" + wlServerName + ",type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system");
		}
				
		for(int i = 0; i < list.size(); i++) {
			try {
				String mBeanName = list.get(i).toString();
				
//MBeanServerConnection mBeanServer = conn.getMBeanServerConnection();
//displayAll(mBeanServer,new ObjectName(mBeanName));
//displayAll(conn, mBeanName); 
				
				Set<ObjectInstance> mbeans = conn.getElementByQueryMBeans(mBeanName);
				mbeans = conn.getElementByQueryMBeans(mBeanName);
				AppLog.getLogger().notice("NB of MBeans for [" + mBeanName + "] : " + mbeans.size());
				
				Iterator<ObjectInstance> iterator = mbeans.iterator();
				while (iterator.hasNext()) {
					
					ObjectInstance mbean = (ObjectInstance)iterator.next();
					
					AppLog.getLogger().notice("------------------------------");
					AppLog.getLogger().notice("MBean: [" + mbean.getObjectName() + "]");
					
					String element = conn.getTextAttr(mbean.getObjectName(), "Name");
					AppLog.getLogger().notice("\tName : " + element);
					
					element = conn.getTextAttr(mbean.getObjectName(), "soainfra_composite");
					AppLog.getLogger().notice("\tCompositeName : " + element);
					
					element = conn.getTextAttr(mbean.getObjectName(), "soainfra_composite_revision");
					AppLog.getLogger().notice("\tCompositeRevision : " + element);
					
				}
				
			} catch(Exception ex) {
				AppLog.getLogger().notice("Error : " + ex.getMessage());
			}
		}
// -----------------------------------------
		
/*
		//String query = "oracle.dms:type=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*";
		
		// BPEL components
		//String query = "oracle.dms:type=soainfra_component,soainfra_component_type=bpel,soainfra_composite_label=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*";
		String query = "oracle.dms:type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system";
		
		//MBeanServerConnection mBeanServer = conn.getMBeanServerConnection();
		//ObjectName objectName = new ObjectName(query);
		//Set<ObjectInstance> mbeans = mBeanServer.queryMBeans(objectName, null);
		//AppLog.getLogger().notice("NB of MBeans for [" + query + "] : " + mbeans.size());
		
		Set<ObjectInstance> mbeans = conn.getElementByQueryMBeans(query);
		mbeans = conn.getElementByQueryMBeans(query);
		AppLog.getLogger().notice("NB of MBeans for [" + query + "] : " + mbeans.size());
		
		Iterator<ObjectInstance> iterator = mbeans.iterator();
		while (iterator.hasNext()) {
			
			ObjectInstance mbean = (ObjectInstance)iterator.next();
			
			AppLog.getLogger().notice("------------------------------");
			AppLog.getLogger().notice("MBean: [" + mbean.getObjectName() + "]");
			
			//MBeanInfo info = mBeanServer.getMBeanInfo(mbean.getObjectName());
			//MBeanAttributeInfo[] attrInfos = info.getAttributes();
			//
			//for (int i = 0; i < attrInfos.length; i++) {
			//	String attrName = attrInfos[i].getName();
			//	
			//	String attrDescription = attrInfos[i].getDescription();
			//	String attrType = attrInfos[i].getType();
			//	Object attrValue = mBeanServer.getAttribute(mbean.getObjectName(), attrName);
			//	AppLog.getLogger().notice("Name: [" + attrName + "] Value: [" + attrValue + "] Description: [" + attrDescription + "] Type: [" + attrType + "]");
			//}
*/
			
			/*
			// Is working for BPEL ("oracle.dms:type=soainfra_component,soainfra_component_type=bpel,soainfra_composite_label=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*")
			String element = (String)mBeanServer.getAttribute(mbean.getObjectName(), "Name");
			System.out.print(element + "\t");
			Integer completed = (Integer)mBeanServer.getAttribute(mbean.getObjectName(), "successfulInstanceProcessingTime_completed");
			System.out.print(completed + "\t");
			Long minTime = (Long)mBeanServer.getAttribute(mbean.getObjectName(), "successfulInstanceProcessingTime_minTime");
			System.out.print(minTime + "\t");
			Double avg = (Double)mBeanServer.getAttribute(mbean.getObjectName(), "successfulInstanceProcessingTime_avg");
			System.out.print(avg + "\t");
			Long maxTime = (Long)mBeanServer.getAttribute(mbean.getObjectName(), "successfulInstanceProcessingTime_maxTime");
			AppLog.getLogger().notice(maxTime);
			*/
			
			/*
			// Is working for BPEL ("oracle.dms:type=soainfra_component,soainfra_component_type=bpel,soainfra_composite_label=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*")
			String element = conn.getTextAttr(mbean.getObjectName(), "Name");
			AppLog.getLogger().notice("\tName : " + element);
			
			double completed = conn.getNumberAttr(mbean.getObjectName(), "successfulInstanceProcessingTime_completed");
			AppLog.getLogger().notice("\tCompleted : " + completed);
			
			double minTime = conn.getNumberAttr(mbean.getObjectName(), "successfulInstanceProcessingTime_minTime");
			AppLog.getLogger().notice("\tMinTime : " + minTime);
			
			double avg = conn.getNumberAttr(mbean.getObjectName(), "successfulInstanceProcessingTime_avg");
			AppLog.getLogger().notice("\tAVG : " + avg);
			
			double maxTime = conn.getNumberAttr(mbean.getObjectName(), "successfulInstanceProcessingTime_maxTime");
			AppLog.getLogger().notice("\tMaxTime : " + maxTime);
			*/
			
/*
			// Is working for BPEL ("oracle.dms:type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system")
			String element = conn.getTextAttr(mbean.getObjectName(), "Name");
			AppLog.getLogger().notice("\tName : " + element);
			
			double activeCount = conn.getNumberAttr(mbean.getObjectName(), "active_count");
			AppLog.getLogger().notice("\tActivecount : " + activeCount);
			
			double avgMsgExecTimeVount = conn.getNumberAttr(mbean.getObjectName(), "avgMsgExecTime_count");
			AppLog.getLogger().notice("\tAvgMsgExecTimeCount : " + avgMsgExecTimeVount);
		}
*/
    }
    
    /**
     * 
     * @param conn
     * @param pattern
     * @throws IOException
     * @throws JMException
     * @throws WebLogicMBeanException 
     */
    /*
    //private static void displayAll(MBeanServerConnection conn, ObjectName pattern) throws IOException, JMException {
    private static void displayAll(DomainRuntimeServiceMBeanConnection conn, String pattern) throws IOException, JMException, WebLogicMBeanException {
        
    	//final JVMMBeanDataDisplay display = new JVMMBeanDataDisplay(conn);
    	final JVMMBeanDataDisplay display = new JVMMBeanDataDisplay(conn.getMBeanServerConnection());
        AppLog.getLogger().notice(SEPARATOR);
        
        //for (ObjectName mbean : conn.queryNames(pattern, null)) {
        for (ObjectName mbean : conn.getElementByQueryNames(pattern)) {
            AppLog.getLogger().notice(display.toString(mbean));
            AppLog.getLogger().notice(SEPARATOR);
        }
    }
    */
}