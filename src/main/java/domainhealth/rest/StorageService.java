package domainhealth.rest;


import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.frontend.data.DateAmountDataSet;
import domainhealth.frontend.data.rest.Domain;
import domainhealth.frontend.data.rest.Server;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.ServletContext;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import java.util.*;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;

/**
 * Created by chiovcr on 02/12/2014.
 */
@Path("/")
public class StorageService {

    @Context
    private ServletContext application;

    StatisticsStorage statisticsStorage;

    @PostConstruct
    public void initialize() {
        System.out.println("In PostConstruct");
        try {
            statisticsStorage = new StatisticsStorage((String) application.getAttribute(AppProperties.PropKey.STATS_OUTPUT_PATH_PROP.toString()));
        } catch (Exception sqle) {
            sqle.printStackTrace();
        }
    }


    //http://localhost:7001/domainhealth/rest/statistics/core?scope=ALL&startTime=ss&endTime=ss
    @GET
    @Path("stats/{resource}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getStats(@QueryParam("scope") List<String> scope,@QueryParam("startTime") String startTime,@QueryParam("endTime") String endTime, @PathParam("resource") String resource) {
        String temp = resource;
        return temp+"-"+startTime+"-"+endTime+"==="+scope;
    }

    @GET
    @Path("/domain")
    @Produces({MediaType.APPLICATION_JSON})
    public Domain getDomain() {
        Domain domain;
        DomainRuntimeServiceMBeanConnection conn = null;
        try {

            conn = new DomainRuntimeServiceMBeanConnection();
            String domainName = conn.getDomainName();
            domain = new Domain(domainName);
            ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
            int length = serverRuntimes.length;
            for (int i = 0; i < length; i++) {
                final String serverName = conn.getTextAttr(serverRuntimes[i], NAME);
                Server server = new Server(serverName);
                domain.addServer(server);
            }
            return domain;
        } catch (Exception e) {
            AppLog.getLogger().error(e.toString());
            e.printStackTrace();
            AppLog.getLogger().error("Statistics Retriever Background Service - unable to retrieve domain structure for domain's servers for this iteration");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }


        return null;
    }






}
