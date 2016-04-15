package domainhealth.rest;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
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
import domainhealth.core.jmx.WebLogicMBeanException;

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
    public String soaDetail(	@HeaderParam("user-agent") String userAgent, 
								@QueryParam("scope") Set<String> scope) {
    	    	
    	DomainRuntimeServiceMBeanConnection conn = null;
    	try {
            conn = new DomainRuntimeServiceMBeanConnection();
            getDetails(conn);
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the soaDetail method", ex);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    	
    	return "";
    }
    
    
    /**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
    private void getDetails(DomainRuntimeServiceMBeanConnection conn) throws Exception {

		System.out.println("");
		System.out.println("SOAService::getDetails()");
		
		/*
		ArrayList<String> list = new ArrayList<String>();
		
		//list.add("oracle.dms:type=soainfra_bpmn_requests,name=/soainfra/engines/bpmn/requests/system");
		//list.add("oracle.dms:type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system");
		
		ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
		for (int index = 0; index < serverRuntimes.length; index++){
	        
	        ObjectName serverRuntime = serverRuntimes[index];
	        String wlServerName = conn.getTextAttr(serverRuntime, WebLogicMBeanPropConstants.NAME);
	        
	        //list.add("oracle.dms:Location=" + wlServerName + ",type=soainfra_bpmn_requests,name=/soainfra/engines/bpmn/requests/system");
	        list.add("oracle.dms:Location=" + wlServerName + ",type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system");
		}
				
		for(int i = 0; i < list.size(); i++) {
			try {
				String mBeanName = list.get(i).toString();
				
				//MBeanServerConnection mBeanServer = conn.getMBeanServerConnection();
				//displayAll(mBeanServer,new ObjectName(mBeanName));
				
				displayAll(conn, mBeanName); 
			} catch(Exception ex) {
				System.out.println("Error : " + ex.getMessage());
			}
		}
		*/
		
		
		//String query = "oracle.dms:type=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*";
		
		// BPEL components
		//String query = "oracle.dms:type=soainfra_component,soainfra_component_type=bpel,soainfra_composite_label=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*";
		String query = "oracle.dms:type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system";
		
		/*
		MBeanServerConnection mBeanServer = conn.getMBeanServerConnection();
		ObjectName objectName = new ObjectName(query);
		Set<ObjectInstance> mbeans = mBeanServer.queryMBeans(objectName, null);
		System.out.println("NB of MBeans for [" + query + "] : " + mbeans.size());
		*/
		
		Set<ObjectInstance> mbeans = conn.getElementByQueryMBeans(query);
		mbeans = conn.getElementByQueryMBeans(query);
		System.out.println("NB of MBeans for [" + query + "] : " + mbeans.size());
		
		Iterator<ObjectInstance> iterator = mbeans.iterator();
		while (iterator.hasNext()) {
			
			ObjectInstance mbean = (ObjectInstance)iterator.next();
			
			System.out.println("------------------------------");
			System.out.println("MBean: [" + mbean.getObjectName() + "]");
			
			//MBeanInfo info = mBeanServer.getMBeanInfo(mbean.getObjectName());
			//MBeanAttributeInfo[] attrInfos = info.getAttributes();
			//
			//for (int i = 0; i < attrInfos.length; i++) {
			//	String attrName = attrInfos[i].getName();
			//	
			//	String attrDescription = attrInfos[i].getDescription();
			//	String attrType = attrInfos[i].getType();
			//	Object attrValue = mBeanServer.getAttribute(mbean.getObjectName(), attrName);
			//	System.out.println("Name: [" + attrName + "] Value: [" + attrValue + "] Description: [" + attrDescription + "] Type: [" + attrType + "]");
			//}
			
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
			System.out.println(maxTime);
			*/
			
			/*
			// Is working for BPEL ("oracle.dms:type=soainfra_component,soainfra_component_type=bpel,soainfra_composite_label=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*")
			String element = conn.getTextAttr(mbean.getObjectName(), "Name");
			System.out.println("\tName : " + element);
			
			double completed = conn.getNumberAttr(mbean.getObjectName(), "successfulInstanceProcessingTime_completed");
			System.out.println("\tCompleted : " + completed);
			
			double minTime = conn.getNumberAttr(mbean.getObjectName(), "successfulInstanceProcessingTime_minTime");
			System.out.println("\tMinTime : " + minTime);
			
			double avg = conn.getNumberAttr(mbean.getObjectName(), "successfulInstanceProcessingTime_avg");
			System.out.println("\tAVG : " + avg);
			
			double maxTime = conn.getNumberAttr(mbean.getObjectName(), "successfulInstanceProcessingTime_maxTime");
			System.out.println("\tMaxTime : " + maxTime);
			*/
			
			// Is working for BPEL ("oracle.dms:type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system")
			String element = conn.getTextAttr(mbean.getObjectName(), "Name");
			System.out.println("\tName : " + element);
			
			double activeCount = conn.getNumberAttr(mbean.getObjectName(), "active_count");
			System.out.println("\tActivecount : " + activeCount);
			
			double avgMsgExecTimeVount = conn.getNumberAttr(mbean.getObjectName(), "avgMsgExecTime_count");
			System.out.println("\tAvgMsgExecTimeCount : " + avgMsgExecTimeVount);
		}
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
        System.out.println(SEPARATOR);
        
        //for (ObjectName mbean : conn.queryNames(pattern, null)) {
        for (ObjectName mbean : conn.getElementByQueryNames(pattern)) {
            System.out.println(display.toString(mbean));
            System.out.println(SEPARATOR);
        }
    }
    */
}