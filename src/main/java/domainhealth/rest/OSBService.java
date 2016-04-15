package domainhealth.rest;

import java.util.Set;
import java.util.TreeSet;

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

import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.MonitorProperties;
//import com.bea.wli.config.Ref;
//import com.bea.wli.monitoring.ServiceDomainMBean;
import domainhealth.core.env.AppLog;

/**
 */
@Path("/osbaction")
public class OSBService {
	
	//private ServiceDomainMBean serviceDomainMBean = null;
	
    @Context
    private ServletContext application;
    
    @Context
    private SecurityContext securityContext;
    
    /**
     * 
     * @param className
     * @return
     */
    private boolean isClass(String className) {
        try  {
            Class.forName(className);
            return true;
        }  catch (final ClassNotFoundException e) {
            return false;
        }
    }
    
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
    @Path("osbDetail/{type}/")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> osbDetail(	@HeaderParam("user-agent") String userAgent, 
									@QueryParam("scope") Set<String> scope,
									@PathParam("type") String type) {
    	
    	if(!isClass("com.bea.wli.monitoring.ServiceDomainMBean")) return null;
    	
    	DomainRuntimeServiceMBeanConnection conn = null;
    	try {
        	
            conn = new DomainRuntimeServiceMBeanConnection();
        	switch (type) {
            
		    	case MonitorProperties.PROXY_SERVICE:
		    		return getProxyServices(conn);
		    		
		    	case MonitorProperties.BUSINESS_SERVICE:
		    		return getBusinessServices(conn);
		    		
				default:
					return null;
        	}	
        } catch (Exception ex) {
        	AppLog.getLogger().error("Problem inside the osbDetail method", ex);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return null;
    }
    
    /**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
    private Set<ObjectName> getOsbResource(DomainRuntimeServiceMBeanConnection conn) throws Exception {
    	
        Set<ObjectName> osbConfigs = conn.getElementByQueryNames("com.oracle.osb:Type=ResourceConfigurationMBean,*");
        return osbConfigs;
    }
    
    /**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
    private Set<String> getProxyServices(DomainRuntimeServiceMBeanConnection conn) throws Exception {

/*
System.out.println("");

getServiceDomainMBean(conn);

System.out.println("OSBService::getProxyServices() - serviceDomainMBean.getDomainName() [" + serviceDomainMBean.getDomainName() + "]");

Ref[] serviceRefs = serviceDomainMBean.getMonitoredProxyServiceRefs();
if(serviceRefs != null && serviceRefs.length > 0) {
	System.out.println("OSBService::getProxyServices() - Found [" + serviceRefs.length + "] Proxy services");
} else {
	System.out.println("OSBService::getProxyServices() - Didn't find any Proxy services");
}
*/

    	Set<String> result = new TreeSet<String>();
    	Set<ObjectName> osbConfigs = getOsbResource(conn);
    	    	
        for (ObjectName config : osbConfigs) {
        	
        	String canonicalName = config.getCanonicalName();
        	        	
System.out.println("OSBService::getProxyServices() - canonicalName is [" + canonicalName + "]");
    
/*
try {
	
	ObjectName objectName = new ObjectName(canonicalName);
	String proxyName = conn.getTextAttr(objectName, NAME);
	System.out.println("OSBService::getProxyServices() - proxyName is [" + proxyName + "]");
	
	//String resourceType = conn.getTextAttr(objectName, "ResourceType");
	//System.out.println("OSBService::getProxyServices() - resourceType is [" + resourceType + "]");
	
	MBeanInfo tempMBean = conn.getMBeanInfo(canonicalName);
	
	if(tempMBean != null) {
		String[] fieldNames = tempMBean.getDescriptor().getFieldNames();
		if(fieldNames != null) {
			for(int index = 0 ; index < fieldNames.length; index ++) {
				System.out.println("OSBService::getProxyServices() - fieldNames[index] is " + fieldNames[index]);
			}
		}
		
		MBeanOperationInfo[] mbeanOperationInfos = tempMBean.getOperations();
		if(mbeanOperationInfos != null) {
			for(int index = 0; index < mbeanOperationInfos.length; index ++) {
				MBeanOperationInfo mbeanOperationInfo = mbeanOperationInfos[index];
				System.out.println("OSBService::getProxyServices() - mbeanOperationInfo.getName() is " + mbeanOperationInfo.getName());
			}
		}
	} else {
		System.out.println("tempMBean is null ...");
	}
	
} catch (Exception ex) {
}
System.out.println("");
*/

/*
        	Hashtable<String, String> keyList = config.getKeyPropertyList();
        	Enumeration<String> enumKey = keyList.keys();
        	while(enumKey.hasMoreElements()) {
        		
        		String key = enumKey.nextElement();
        		String value = config.getKeyProperty(key);
        		
System.out.println("   OSBService::getProxyServices() - Key [" + key + "] - Value [" + value + "]");        		
        	}
System.out.println("");
*/
        	String element = config.getKeyProperty("Name");
        	
System.out.println("OSBService::getProxyServices() - Checking the element [" + element + "]");
        	
			// Check if ProxyService
	        if (element.startsWith("ProxyService$")) {
	        	
System.out.println("OSBService::getProxyServices() - Adding the element [" + element + "]");

	        	result.add(element);
	        }
        }
        return result;
    }
    
    /**
	 * Gets an instance of ServiceDomainMBean from the weblogic server.
	 *
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @throws Exception
	 */
    /*
    private void getServiceDomainMBean(DomainRuntimeServiceMBeanConnection conn) throws Exception {    	
    	InvocationHandler handler = new ServiceDomainMBeanInvocationHandler(conn.getJMXConnector());
		Object proxy = Proxy.newProxyInstance(ServiceDomainMBean.class.getClassLoader(), new Class[] { ServiceDomainMBean.class }, handler);
		serviceDomainMBean = (ServiceDomainMBean) proxy;
	}
	*/
	
    
    
    
    
    
    
    
    /**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
    private Set<String> getBusinessServices(DomainRuntimeServiceMBeanConnection conn) throws Exception {

System.out.println("OSBService::getBusinessServices()");

    	Set<String> result = new TreeSet<String>();
    	Set<ObjectName> osbConfigs = getOsbResource(conn);
    	    	
        for (ObjectName config : osbConfigs) {
        	
        	String element = config.getKeyProperty("Name");
        	
System.out.println("OSBService::getBusinessServices() - Checking the element [" + element + "]");
        	
			// Check if BusinessService
	        if (element.startsWith("BusinessService$")) {
	        	
System.out.println("OSBService::getBusinessServices() - Adding the element [" + element + "]");

	        	result.add(element);
	        }
        }
        return result;
    }
}