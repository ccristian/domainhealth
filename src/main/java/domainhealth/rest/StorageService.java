package domainhealth.rest;


import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.MonitorProperties;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.frontend.data.DateAmountDataSet;
import domainhealth.frontend.data.rest.Domain;
import domainhealth.frontend.data.rest.Server;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.ServletContext;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.util.*;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;

/**
 * Created by chiovcr on 02/12/2014.
 */
@Path("/")
public class StorageService {

    DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy-HH-mm");


    @Context
    private ServletContext application;

    StatisticsStorage statisticsStorage;

    @PostConstruct
    public void initialize() {
        try {
            statisticsStorage = new StatisticsStorage((String) application.getAttribute(AppProperties.PropKey.STATS_OUTPUT_PATH_PROP.toString()));
        } catch (Exception sqle) {
            sqle.printStackTrace();
        }
    }


    //http://localhost:7001/domainhealth/rest/statistics/core?scope=ALL&startTime=ss&endTime=ss
    @GET
    @Path("stats/{resourceType}/{resource}")
    @Produces({MediaType.APPLICATION_JSON})
    public String getStats(@QueryParam("scope") List<String> scope,
                           @QueryParam("startTime") String startTime,
                           @QueryParam("endTime") String endTime,
                           @PathParam("resourceType") String resourceType,
                           @PathParam("resource") String resource) {
        String temp = resource;
        //statisticsStorage.getResourceNamesFromPropsListForInterval();
        return temp + "-" + startTime + "-" + endTime + "===" + scope;
    }

    //http://localhost:7001/domainhealth/rest/resources/workmgr?startTime=01-09-2014-00-00&endTime=17-09-2015-0-00
    @GET
    @Path("resources")
    @Produces({MediaType.APPLICATION_JSON})
    public Map<String,Set<String>> getStats(
                                @QueryParam("startTime") String startTime,
                                @QueryParam("endTime") String endTime
                               ) {

        Map<String,Set<String>> resourcesMap = new HashMap<String, Set<String>>();
        try {
            DateTime start = fmt.parseDateTime(startTime);
            DateTime end = fmt.parseDateTime(endTime);
            Interval interval = new Interval(start, end);

                resourcesMap.put(MonitorProperties.CORE_RESOURCE_TYPE,statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.CORE_RESOURCE_TYPE));
                resourcesMap.put(MonitorProperties.DATASOURCE_RESOURCE_TYPE,statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.DATASOURCE_RESOURCE_TYPE));
                resourcesMap.put(MonitorProperties.DESTINATION_RESOURCE_TYPE,statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.DESTINATION_RESOURCE_TYPE));
                resourcesMap.put(MonitorProperties.SAF_RESOURCE_TYPE,statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.SAF_RESOURCE_TYPE));
                resourcesMap.put(MonitorProperties.EJB_RESOURCE_TYPE,statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.EJB_RESOURCE_TYPE));
                resourcesMap.put(MonitorProperties.WORKMGR_RESOURCE_TYPE,statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.WORKMGR_RESOURCE_TYPE));
                resourcesMap.put(MonitorProperties.WEBAPP_RESOURCE_TYPE,statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.WEBAPP_RESOURCE_TYPE));
                resourcesMap.put(MonitorProperties.SVRCHNL_RESOURCE_TYPE,statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.SVRCHNL_RESOURCE_TYPE));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resourcesMap;
    }

    @GET
    @Path("/domain")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> getDomain() {
        Domain domain;
        DomainRuntimeServiceMBeanConnection conn = null;
        try {
            conn = new DomainRuntimeServiceMBeanConnection();
            Set<String> servers = statisticsStorage.getAllPossibleServerNames(conn);
            return servers;
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
