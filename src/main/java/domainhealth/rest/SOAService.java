package domainhealth.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
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
import domainhealth.core.env.AppProperties;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.jmx.WebLogicMBeanPropConstants;
import domainhealth.core.util.BlacklistUtil;

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
		
/*
		// Global query
		list.add("oracle.dms:type=soainfra_bpmn_requests,name=/soainfra/engines/bpmn/requests/system");
		list.add("oracle.dms:type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system");
		
		// Local query
		//ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
		//for (int index = 0; index < serverRuntimes.length; index++){
	    //    
	    //    ObjectName serverRuntime = serverRuntimes[index];
	    //    String wlServerName = conn.getTextAttr(serverRuntime, WebLogicMBeanPropConstants.NAME);
	    //    
	    //    list.add("oracle.dms:Location=" + wlServerName + ",type=soainfra_bpmn_requests,name=/soainfra/engines/bpmn/requests/system");
	    //    list.add("oracle.dms:Location=" + wlServerName + ",type=soainfra_bpel_requests,name=/soainfra/engines/bpel/requests/system");	        
		//}
		
		//list.add("oracle.dms:type=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*");
		//list.add("oracle.dms:type=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=*,name=*");
		
		list.add("oracle.dms:type=soainfra_message_processing,name=/soainfra/engines/message_processing/bpel");
		list.add("oracle.dms:type=soainfra_message_processing,name=/soainfra/engines/message_processing/bpmn");
		list.add("oracle.dms:type=soainfra_message_processing,name=/soainfra/engines/message_processing/decision");
		
		// -----------------------------------------------------
		// Paulo
		// -----
		//list.add("oracle.dms:soainfra_composite_revision=*,name=*,type=soainfra_composite_label,soainfra_domain=default,soainfra_composite=*");
		list.add("oracle.dms:soainfra_composite_revision=*,name=*,type=soainfra_composite_label,soainfra_domain=*,soainfra_composite=*");
		//list.add("oracle.dms:type=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,name=*");
		list.add("oracle.dms:type=*,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=*,name=*");
		
		// SOAPlatformStatus -> isReady (key)
		// Name: [SOAPlatformStatus]>
		// Value: [javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=oracle.fabric.management.deployedcomposites.mbean.Status,items=((itemName=info,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=isReady,itemType=javax.management.openmbean.SimpleType(name=java.lang.Boolean)))),contents={info=null, isReady=true})]>
		list.add("oracle.soa.config:name=soa-infra,j2eeType=CompositeLifecycleConfig,Application=soa-infra");
		
		// Mode, State
		//list.add("oracle.soa.config:partition=default,j2eeType=SCAComposite,revision=*,label=*,Application=soa-infra,wsconfigtype=WebServicesConfig,name=*");
		list.add("oracle.soa.config:partition=*,j2eeType=SCAComposite,revision=*,label=*,Application=soa-infra,wsconfigtype=WebServicesConfig,name=*");
		
		// Name, Parent, soainfra_composite, soainfra_composite_revision, CompositeState_value
		//list.add("oracle.dms:soainfra_composite_revision=*,name=*,type=soainfra_composite_label,soainfra_domain=default,soainfra_composite=*");
		list.add("oracle.dms:soainfra_composite_revision=*,name=*,type=soainfra_composite_label,soainfra_domain=*,soainfra_composite=*");
		
		// -----------------------------------------------------
		// Reference should exists for those
		// businessFaults_count, systemFaults_count
		//list.add("oracle.dms:soainfra_composite_label=*,type=soainfra_component,soainfra_component_type=bpel,soainfra_composite=*,soainfra_composite_revision=*,soainfra_domain=default,name=*");
		list.add("oracle.dms:soainfra_composite_label=*,type=soainfra_component,soainfra_component_type=bpel,soainfra_composite=*,soainfra_composite_revision=*,soainfra_domain=*,name=*");
		
		// -----------------------------------------------------
		// Instances should be created/running for those
		// successfulInstanceProcessingTime_avg
		//list.add("oracle.dms:soainfra_composite_label=*,type=soainfra_component,soainfra_component_type=bpmn,soainfra_composite=*,soainfra_composite_revision=*,soainfra_domain=default,name=*");
		list.add("oracle.dms:soainfra_composite_label=*,type=soainfra_component,soainfra_component_type=bpmn,soainfra_composite=*,soainfra_composite_revision=*,soainfra_domain=*,name=*");
		// -----------------------------------------------------
		
		// businessFaults_count, systemFaults_count
		//list.add("oracle.dms:soainfra_composite_label=*,type=soainfra_component,soainfra_component_type=bpel,soainfra_composite=*,soainfra_composite_revision=*,soainfra_domain=default,name=*");
		list.add("oracle.dms:soainfra_composite_label=*,type=soainfra_component,soainfra_component_type=bpel,soainfra_composite=*,soainfra_composite_revision=*,soainfra_domain=*,name=*");
		
		// processIncomingMessages_avg
		//list.add("oracle.dms:soainfra_composite_assembly_member_type=SERVICEs,soainfra_composite_label=*,name=*,soainfra_composite_assembly_member=*,soainfra_Ports=PORTs,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,type=soainfra_Binding");
		list.add("oracle.dms:soainfra_composite_assembly_member_type=SERVICEs,soainfra_composite_label=*,name=*,soainfra_composite_assembly_member=*,soainfra_Ports=PORTs,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=*,type=soainfra_Binding");

		// processOutboundMessages_avg
		//list.add("oracle.dms:soainfra_composite_assembly_member_type=REFERENCEs,soainfra_composite_label=*,name=*,soainfra_composite_assembly_member=BEPLWebService,soainfra_Ports=PORTs,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=default,type=soainfra_Binding");
		list.add("oracle.dms:soainfra_composite_assembly_member_type=REFERENCEs,soainfra_composite_label=*,name=*,soainfra_composite_assembly_member=BEPLWebService,soainfra_Ports=PORTs,soainfra_composite_revision=*,soainfra_composite=*,soainfra_domain=*,type=soainfra_Binding");
		
		// -----------------------------------------------------
		// Mesh
		// Async Messages
		// PostErrors_count, PostEvents_count, Posts_active, Posts_avg, Posts_completed, Posts_maxActive, Posts_maxTime, Posts_minTime, Posts_time

		// Sync Messages
		// RequestErrors_count, RequestEvents_count, Requests_active, Requests_avg, Requests_completed, Requests_maxActive, Requests_maxTime, Requests_minTime, Requests_time
		list.add("oracle.dms:type=soainfra_Mesh,name=/soainfra/mesh");
		// -----------------------------------------------------
*/
		// -----------------------------------------------------
		//list.add("...");
		
		String mBeanName = "oracle.soa.config:name=soa-infra,j2eeType=CompositeLifecycleConfig,Application=soa-infra";
					
		// -----------------------------------------------------
		// Get a mbeanServer instance
		MBeanServerConnection mBeanServer = conn.getMBeanServerConnection();
		try {
//			Set<ObjectInstance> mbeans = conn.getElementByQueryMBeans(mBeanName);
			Set<ObjectName> mbeansName = conn.getElementByQueryNames(mBeanName);
			
			AppLog.getLogger().notice("");
			AppLog.getLogger().notice("NB of MBeans for [" + mBeanName + "] : " + mbeansName.size());
			
			Iterator<ObjectName> iteratorNames = mbeansName.iterator();
			while (iteratorNames.hasNext()) {
				
				ObjectName mbean = (ObjectName)iteratorNames.next();
				CompositeData compositeData = (CompositeData)mBeanServer.getAttribute(mbean, "SOAPlatformStatus");
				
				if(compositeData != null) {
					
					/*
					// Print all the elements
					Set<String> keys = compositeData.getCompositeType().keySet();
					Iterator<String> iteratorKeys = keys.iterator();
					while (iteratorKeys.hasNext()) {
						String key = iteratorKeys.next();
						AppLog.getLogger().notice("    Key: [" + key + "]");
						AppLog.getLogger().notice("    Value: [" + compositeData.get(key) + "]");
					}
					*/
					
					AppLog.getLogger().notice("(1) isReady [" + compositeData.get("isReady") + "]");
					AppLog.getLogger().notice("");
				}
				
				// Object representation
				//Object objectSoaPlatformStatus = conn.getObjectAttr(mbean, "SOAPlatformStatus");
				//AppLog.getLogger().notice("    SOAPlatformStatus: [" + objectSoaPlatformStatus + "]");
				
				// Cast the Object into CompositeData
				CompositeData cdSoaPlatformStatus = (CompositeData)conn.getObjectAttr(mbean, "SOAPlatformStatus");
				AppLog.getLogger().notice("    [isReady] is " + cdSoaPlatformStatus.get("isReady"));
				
				// ----------------------------------------------
				// Casting issue
				// -> javax.management.openmbean.CompositeDataSupport cannot be cast to javax.management.ObjectName
//				objectSoaPlatformStatus = conn.getChild(mbean, "SOAPlatformStatus");
//				AppLog.getLogger().notice("(4) SOAPlatformStatus: [" + objectSoaPlatformStatus + "]");
				
//				cdSoaPlatformStatus = (CompositeData)conn.getChild(mbean, "SOAPlatformStatus");
//				AppLog.getLogger().notice("(5) SOAPlatformStatus: [" + cdSoaPlatformStatus + "]");
				// ----------------------------------------------
			}
			// -------------------------------------------------------
			
			
			
			
			
			
			
/*
			Iterator<ObjectInstance> iterator = mbeans.iterator();
			while (iterator.hasNext()) {
				
				ObjectInstance mbean = (ObjectInstance)iterator.next();
				ObjectName mBeanObjectName = mbean.getObjectName();
				
				AppLog.getLogger().notice("------------------------------");
				AppLog.getLogger().notice("MBean: [" + mBeanObjectName + "]");
				
				MBeanInfo info = mBeanServer.getMBeanInfo(mBeanObjectName);
				MBeanAttributeInfo[] attrInfos = info.getAttributes();
				
				AppLog.getLogger().notice("NB of Attributes for [" + mBeanName + "] : " + attrInfos.length);
				
				for (int indexAttribute = 0; indexAttribute < attrInfos.length; indexAttribute++) {
					
					String attrName = attrInfos[indexAttribute].getName();
					if(attrName.equals("SOAPlatformStatus")) {
						
						CompositeData compositeData = (CompositeData)mBeanServer.getAttribute(mBeanObjectName, attrName);
						if(compositeData != null) {
							
							Set<String> keys = compositeData.getCompositeType().keySet();
							Iterator<String> iteratorKeys = keys.iterator();
							while (iteratorKeys.hasNext()) {
								String key = iteratorKeys.next();
								AppLog.getLogger().notice("    Key: [" + key + "]");
								AppLog.getLogger().notice("    Value: [" + compositeData.get(key) + "]");
							}
							
							AppLog.getLogger().notice("");
						}
						
//						Object attrValue = mBeanServer.getAttribute(mBeanObjectName, attrName);
//						String attrType = attrInfos[indexAttribute].getType();
//												
//						AppLog.getLogger().notice("  Name: [" + attrName + "]");
//						AppLog.getLogger().notice("  Value: [" + attrValue + "]");
//						AppLog.getLogger().notice("  Type: [" + attrType + "]");
						AppLog.getLogger().notice("");
					} else {
						AppLog.getLogger().notice("-> Skipped the attribute [" + attrName + "] ...");
					}
				}
			}
*/		
		} catch(Exception ex) {
			AppLog.getLogger().notice("Error : " + ex.getMessage());
		}
		// -----------------------------------------------------
		
/*
		// -----------------------------------------------------
		// Only to analyze the relevance attribute to monitor/collect
		// Get a mbeanServer instance
		MBeanServerConnection mBeanServer = conn.getMBeanServerConnection();
		for(int indexMBean = 0; indexMBean < list.size(); indexMBean++) {
			try {
				
				String mBeanName = list.get(indexMBean).toString();
				
				Set<ObjectInstance> mbeans = conn.getElementByQueryMBeans(mBeanName);
				
				AppLog.getLogger().notice("");
				AppLog.getLogger().notice("NB of MBeans for [" + mBeanName + "] : " + mbeans.size());
				
				Iterator<ObjectInstance> iterator = mbeans.iterator();
				while (iterator.hasNext()) {
					
					ObjectInstance mbean = (ObjectInstance)iterator.next();
					ObjectName mBeanObjectName = mbean.getObjectName(); 
					
					AppLog.getLogger().notice("------------------------------");
					AppLog.getLogger().notice("MBean: [" + mBeanObjectName + "]");
					
					MBeanInfo info = mBeanServer.getMBeanInfo(mBeanObjectName);
					MBeanAttributeInfo[] attrInfos = info.getAttributes();
					
					for (int indexAttribute = 0; indexAttribute < attrInfos.length; indexAttribute++) {
						
						String attrName = attrInfos[indexAttribute].getName();
						Object attrValue = mBeanServer.getAttribute(mBeanObjectName, attrName);
						
						//String attrDescription = attrInfos[indexAttribute].getDescription();
						//String attrType = attrInfos[indexAttribute].getType();
						
						AppLog.getLogger().notice(" Name: [" + attrName + "]");
						AppLog.getLogger().notice(" Value: [" + attrValue + "]");
						AppLog.getLogger().notice("");
					}
				}
				
			} catch(Exception ex) {
				AppLog.getLogger().notice("Error : " + ex.getMessage());
			}
		}
		// -----------------------------------------------------
*/
    }
}