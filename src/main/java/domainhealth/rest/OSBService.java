package domainhealth.rest;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
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
import domainhealth.core.statistics.MonitorProperties;

/**
 */
@Path("/osbaction")
public class OSBService {
		
    @Context
    private ServletContext application;
    
    @Context
    private SecurityContext securityContext;
    
    /**
     * 
     * @return
     */
    @GET
    @Path("isOsbDomain/")
    @Produces({MediaType.APPLICATION_JSON})
    public boolean isOsbDomain() {
        return isClassExist("com.bea.wli.monitoring.ServiceDomainMBean");
    }
    
    /**
     * 
     * @param className
     * @return
     */
    private boolean isClassExist(String className) {
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
     * @param osbResourceType
     * @return
     */
    /*
    @GET
    @Path("details/{osbResourceType}/")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> getDetailsForOsbResourceType(	@HeaderParam("user-agent") String userAgent, 
														@QueryParam("scope") Set<String> scope,
														@PathParam("osbResourceType") String osbResourceType) {
    	
    	// Call the utility class because JERSEY instantiates all the WS at the first call (even if the call if not matching the OSB WS)
		// If the WL domain is not an OSB, then the loading of this class will fail due to import (not found)
		// Introspection could be used (string will be managed) but it's also OK (and much more readable) if the real OSB implementation is managed from another class.
		// This utility class will be instantiated/loaded only and only if the WL domain is a well an OSB domain (see Class.forName() call)
    	
    	if(!isOsbDomain()) {
    		AppLog.getLogger().error("This WL domain is not an OSB WL domain ...");
    		return null;
    	} else {
    		//AppLog.getLogger().notice("This WL domain is well an OSB WL domain");
    		
    		// osbResourceType is MonitorProperties.PROXY_SERVICE_RESOURCE_TYPE or MonitorProperties.BUSINESS_SERVICE_RESOURCE_TYPE
    		return new OSBServiceUtil().getDetailsForOsbType(osbResourceType);
    	}
    }
    */
    
    /**
     * 
     * @param userAgent
     * @param scope
     * @param resourceType
     * @return
     */
    @GET
    @Path("detail/{osbResourceType}/{resourceType}/")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> getDetailForResourceType(	@HeaderParam("user-agent") String userAgent, 
													@QueryParam("scope") Set<String> scope,
													@PathParam("osbResourceType") String osbResourceType,
													@PathParam("resourceType") String resourceType) {
    	
    	// Call the utility class because JERSEY instantiates all the WS at the first call (even if the call if not matching the OSB WS)
		// If the WL domain is not an OSB, then the loading of this class will fail due to import (not found)
		// Introspection could be used (string will be managed) but it's also OK (and much more readable) if the real OSB implementation is managed from another class.
		// This utility class will be instantiated/loaded only and only if the WL domain is a well an OSB domain (see Class.forName() call)
    	
    	if(!isOsbDomain()) {
    		AppLog.getLogger().error("This WL domain is not an OSB WL domain ...");
    		return null;
    	} else {
    		//AppLog.getLogger().notice("This WL domain is well an OSB WL domain");
    		
    		/*
    		resourceType could be for MonitorProperties.PROXY_SERVICE_RESOURCE_TYPE :
    			MonitorProperties.SERVICE
    			MonitorProperties.WEBSERVICE_OPERATION
    			MonitorProperties.FLOW_COMPONENT
    			
			resourceType could be for MonitorProperties.BUSINESS_SERVICE_RESOURCE_TYPE :
    			MonitorProperties.SERVICE
    			MonitorProperties.WEBSERVICE_OPERATION
    			MonitorProperties.URI
    		*/
    		
    		return new OSBServiceUtil().getDetailsForResourceType(osbResourceType, resourceType);
    	}
    }
    
    /**
     * 
     */
    @GET
    @Path("list/resource/{osbResourceType}/")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> listResourceTypeForOsbResourceType(	@HeaderParam("user-agent") String userAgent, 
															@QueryParam("scope") Set<String> scope,
															@PathParam("osbResourceType") String osbResourceType) {
    	
    	// Call the utility class because JERSEY instantiates all the WS at the first call (even if the call if not matching the OSB WS)
		// If the WL domain is not an OSB, then the loading of this class will fail due to import (not found)
		// Introspection could be used (string will be managed) but it's also OK (and much more readable) if the real OSB implementation is managed from another class.
		// This utility class will be instantiated/loaded only and only if the WL domain is a well an OSB domain (see Class.forName() call)
    	
    	if(!isOsbDomain()) {
    		AppLog.getLogger().error("This WL domain is not an OSB WL domain ...");
    		return null;
    	} else {
    		//AppLog.getLogger().notice("This WL domain is well an OSB WL domain");
    	
    		/*
    		For MonitorProperties.PROXY_SERVICE_RESOURCE_TYPE, we should returns the list :
    			MonitorProperties.SERVICE
    			MonitorProperties.WEBSERVICE_OPERATION
    			MonitorProperties.FLOW_COMPONENT
    			
    		For MonitorProperties.BUSINESS_SERVICE_RESOURCE_TYPE, we should return the list :
    			MonitorProperties.SERVICE
    			MonitorProperties.WEBSERVICE_OPERATION
    			MonitorProperties.URI
    		*/
    		
    		Set<String> resourceTypeList = new LinkedHashSet<String>();
    		
    		switch (osbResourceType) {
            
		    	case MonitorProperties.OSB_PS_TYPE:
		    		
		    		resourceTypeList.add(MonitorProperties.OSB_RESOURCE_TYPE_SERVICE);
		    		resourceTypeList.add(MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION);
		    		resourceTypeList.add(MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT);
		    		
		    		return resourceTypeList;
		    		
		    	case MonitorProperties.OSB_BS_TYPE:
		    		
		    		resourceTypeList.add(MonitorProperties.OSB_RESOURCE_TYPE_SERVICE);
		    		resourceTypeList.add(MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION);
		    		resourceTypeList.add(MonitorProperties.OSB_RESOURCE_TYPE_URI);
		    		
		    		return resourceTypeList;
		    		
				default:
					AppLog.getLogger().error("Wrong osbResourceType [" + osbResourceType + "] - Must be [" + MonitorProperties.OSB_PS_TYPE + "] or [" + MonitorProperties.OSB_BS_TYPE + "]");
					return null;
	    	}	
    	}
    }

    /**
     * 
     */
    @GET
    @Path("list/{osbResourceType}/")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> listResourcesForOsbResourceType(	@HeaderParam("user-agent") String userAgent, 
														@QueryParam("scope") Set<String> scope,
														@PathParam("osbResourceType") String osbResourceType) {
    	
    	// Call the utility class because JERSEY instantiates all the WS at the first call (even if the call if not matching the OSB WS)
		// If the WL domain is not an OSB, then the loading of this class will fail due to import (not found)
		// Introspection could be used (string will be managed) but it's also OK (and much more readable) if the real OSB implementation is managed from another class.
		// This utility class will be instantiated/loaded only and only if the WL domain is a well an OSB domain (see Class.forName() call)
    	
    	if(!isOsbDomain()) {
    		AppLog.getLogger().error("This WL domain is not an OSB WL domain ...");
    		return null;
    	} else {
    		//AppLog.getLogger().notice("This WL domain is well an OSB WL domain");
    		
    		// ...
    		// TO IMPLEMENT
    	}
    	
    	return null;
    }
}