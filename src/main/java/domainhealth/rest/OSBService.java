package domainhealth.rest;

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

/*
import com.bea.wli.config.Ref;
import com.bea.wli.monitoring.DomainMonitoringDisabledException;
import com.bea.wli.monitoring.InvalidServiceRefException;
import com.bea.wli.monitoring.MonitoringException;
import com.bea.wli.monitoring.MonitoringNotEnabledException;
import com.bea.wli.monitoring.ResourceStatistic;
import com.bea.wli.monitoring.ResourceType;
import com.bea.wli.monitoring.ServiceDomainMBean;
import com.bea.wli.monitoring.ServiceResourceStatistic;
import com.bea.wli.monitoring.StatisticType;
import com.bea.wli.monitoring.StatisticValue;
*/
import domainhealth.core.env.AppLog;

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
    private boolean isOsbDomain() {
        return isClass("com.bea.wli.monitoring.ServiceDomainMBean");
    }
    
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
    	
    	if(!isOsbDomain()) {
    		AppLog.getLogger().error("This WL domain is not an OSB WL domain ...");
    		return null;
    	} else {
    		AppLog.getLogger().notice("This WL domain is well an OSB WL domain");
    		
    		// Call the utility class because JERSEY instantiates all the WS at the first call (even if the call if not matching the OSB WS)
    		// If the WL domain is not an OSB, then the loading of this class will fail due to import (not found)
    		// Introspection could be used (string will be managed) but it's alsi OK (and much more readable) if the real OSB implementation is managed from another class.
    		// This utility class will be instantiated/loaded only and only if the WL domain is a well an OSB domain (see Class.forName() call)
    		return new OSBServiceUtil().osbDetail(type);
    	}
    }
}