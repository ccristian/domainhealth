package domainhealth.rest;


import com.sun.jersey.api.json.JSONWithPadding;
import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.MonitorProperties;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.frontend.data.DateAmountDataItem;
import domainhealth.frontend.data.DateAmountDataSet;
import domainhealth.frontend.data.Statistics;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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
            AppLog.getLogger().error("Exception", sqle);
        }
    }


    //http://localhost:7001/domainhealth/rest/resources?startTime=01-09-2014-00-00&endTime=17-09-2016-0-00
    @GET
    @Path("resources")
    @Produces({MediaType.APPLICATION_JSON})
    public Map<String, Set<String>> getStats(
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime
    ) {
        Map<String, Set<String>> resourcesMap = new HashMap<String, Set<String>>();
        try {
            DateTime start = fmt.parseDateTime(startTime);
            DateTime end = fmt.parseDateTime(endTime);
            Interval interval = new Interval(start, end);
            resourcesMap.put(MonitorProperties.CORE_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.CORE_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.DATASOURCE_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.DATASOURCE_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.DESTINATION_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.DESTINATION_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.SAF_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.SAF_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.EJB_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.EJB_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.WORKMGR_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.WORKMGR_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.WEBAPP_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.WEBAPP_RESOURCE_TYPE));
            resourcesMap.put(MonitorProperties.SVRCHNL_RESOURCE_TYPE, statisticsStorage.getResourceNamesFromPropsListForInterval(interval, MonitorProperties.SVRCHNL_RESOURCE_TYPE));
        } catch (IOException e) {
            AppLog.getLogger().error("Error while getting resources", e);
        }
        return resourcesMap;
    }


    //http://localhost:7001/domainhealth/domain
    @GET
    @Path("/domain")
    @Produces({MediaType.APPLICATION_JSON})
    public Set<String> getDomain() {
        DomainRuntimeServiceMBeanConnection conn = null;
        try {
            conn = new DomainRuntimeServiceMBeanConnection();
            Set<String> servers = statisticsStorage.getAllPossibleServerNames(conn);
            return servers;
        } catch (Exception e) {
            AppLog.getLogger().error("StorageService - unable to retrieve domain structure for domain's servers");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return null;
    }


    //http://localhost:7001/domainhealth/rest/stats/core?scope=ALL&startTime=ss&endTime=ss
    //http://localhost:7001/domainhealth/rest/stats/core/xdd?startTime=01-09-2014-00-00&endTime=17-11-2015-0-00
    //http://localhost:7001/domainhealth/rest/stats/datasource/xdd?startTime=01-09-2014-00-00&endTime=17-11-2015-0-00

    @GET
    @Path("stats/{resourceType}/{resource}")
    @Produces({MediaType.APPLICATION_JSON})
    public Map<String, List<Map>> getStats(@HeaderParam("user-agent") String userAgent, @QueryParam("scope") Set<String> scope,
                                           @QueryParam("startTime") String startTime,
                                           @QueryParam("endTime") String endTime,
                                           @PathParam("resourceType") String resourceType,
                                           @PathParam("resource") String resource) {

        try {
            Map<String, List<Map>> result = new LinkedHashMap<>();
            DateTime start = fmt.parseDateTime(startTime);
            DateTime end = fmt.parseDateTime(endTime);
            Interval interval = new Interval(start, end);
            DomainRuntimeServiceMBeanConnection conn = null;

            // //ex: StorageUtil.getPropertyData(statisticsStorage,"core",null,"HeapUsedCurrent",new Date(),1,"AdminServer");
            if (scope == null || scope.size() == 0) {
                conn = new DomainRuntimeServiceMBeanConnection();
                scope = statisticsStorage.getAllPossibleServerNames(conn);
            }
            Map<String, DateAmountDataSet> dataMap = null;
            for (String server : scope) {
                List<String> properties = new LinkedList<>();
                switch (resourceType) {
                    case "core":
                        properties.add("HeapUsedCurrent");
                        properties.add("OpenSocketsCurrentCount");
                        properties.add("HeapSizeCurrent");
                        properties.add("HeapFreeCurrent");
                        properties.add("HeapFreePercent");
                        properties.add("ExecuteThreadTotalCount");
                        properties.add("HoggingThreadCount");
                        properties.add("PendingUserRequestCount");
                        properties.add("QueueLength");
                        properties.add("CompletedRequestCount");
                        properties.add("ExecuteThreadIdleCount");
                        properties.add("MinThreadsConstraintsCompleted");
                        properties.add("MinThreadsConstraintsPending");
                        properties.add("StandbyThreadCount");
                        properties.add("Throughput");
                        properties.add("TransactionTotalCount");
                        properties.add("TransactionCommittedTotalCount");
                        properties.add("TransactionRolledBackTotalCount");
                        properties.add("TransactionHeuristicsTotalCount");
                        properties.add("TransactionAbandonedTotalCount");
                        properties.add("ActiveTransactionsTotalCount");
                        resource = null;
                        break;
                    case "datasource":
                        properties.add("NumAvailable");
                        properties.add("NumUnavailable");
                        properties.add("ActiveConnectionsCurrentCount");
                        properties.add("ConnectionDelayTime");
                        properties.add("FailedReserveRequestCount");
                        properties.add("FailuresToReconnectCount");
                        properties.add("LeakedConnectionCount");
                        properties.add("WaitingForConnectionCurrentCount");
                        properties.add("WaitingForConnectionFailureTotal");
                        properties.add("WaitSecondsHighCount");
                        break;
                    case "destination":
                        properties.add("MessagesCurrentCount");
                        properties.add("MessagesReceivedCount");
                        properties.add("MessagesPendingCount");
                        properties.add("MessagesHighCount");
                        properties.add("ConsumersCurrentCount");
                        properties.add("ConsumersHighCount");
                        properties.add("ConsumersTotalCount");
                        break;

                    case "ejb":
                        properties.add("PooledBeansCurrentCount");
                        properties.add("AccessTotalCount");
                        properties.add("BeansInUseCurrentCount");
                        properties.add("WaiterCurrentCount");
                        properties.add("WaiterTotalCount");
                        properties.add("TransactionsCommittedTotalCount");
                        properties.add("TransactionsRolledBackTotalCount");
                        properties.add("TransactionsTimedOutTotalCount");
                        break;
                    case "saf":
                        properties.add("MessagesCurrentCount");
                        properties.add("MessagesPendingCount");
                        properties.add("MessagesReceivedCount");
                        properties.add("MessagesHighCount");
                        break;

                    case "webapp":
                        properties.add("OpenSessionsCurrentCount");
                        properties.add("OpenSessionsHighCount");
                        properties.add("SessionsOpenedTotalCount");
                        break;

                    case "svrchnl":
                        properties.add("AcceptCount");
                        properties.add("ConnectionsCount");
                        properties.add("MessagesReceivedCount");
                        properties.add("MessagesSentCount");
                        break;

                    case "workmgr":
                        properties.add("CompletedRequests");
                        properties.add("PendingRequests");
                        properties.add("StuckThreadCount");
                        break;

                }

                //temp solution for ordering gui
                Collections.reverse(properties);
                Set prp = new LinkedHashSet(properties);
                dataMap = statisticsStorage.getPropertyData(resourceType, resource, prp, interval, server);

                //transform the result for proper format
                for (String res : dataMap.keySet()) {
                    DateAmountDataSet dataSet = dataMap.get(res);
                    String property = dataSet.getResourceProperty();
                    List dataList = new LinkedList();
                    for (DateAmountDataItem dateAmountDataItem : dataSet.getData()) {
                        dataList.add(new BigDecimal[]{BigDecimal.valueOf(dateAmountDataItem.getDateTime().getTime()), BigDecimal.valueOf(dateAmountDataItem.getAmount())});
                    }
                    Map map = new LinkedHashMap();
                    map.put("name", server);
                    map.put("data", dataList);
                    List<Map> listMap = result.get(res);
                    if (listMap == null) {
                        listMap = new ArrayList<>();
                        result.put(res, listMap);
                    }
                    listMap.add(map);
                }

            }
            long t1 = System.currentTimeMillis();
            // TODO add misssing data
            // addMissingData(result,start,end);
            long t2 = System.currentTimeMillis();
            return result;
        } catch (Exception e) {
            AppLog.getLogger().error("StorageService - unable to retrieve domain structure for domain's servers",e);
        }
        return null;
    }



    @GET
    @Path("jsonp/domain")
    @Produces({"application/javascript"})
    public JSONWithPadding getDomainJsonp(@QueryParam("callback") String callback) {
        Set<String> servers = getDomain();
        return new JSONWithPadding(servers, callback);
    }

    @GET
    @Path("jsonp/resources")
    @Produces({"application/javascript"})
    public JSONWithPadding getStatsJsonp(
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime, @QueryParam("callback") String callback) {
        Map<String, Set<String>> resourcesMap = getStats(startTime, endTime);
        return new JSONWithPadding(resourcesMap, callback);
    }

    @GET
    @Path("jsonp/stats/{resourceType}/{resource}")
    @Produces({"application/javascript"})
    public JSONWithPadding getStatsJsonp(@HeaderParam("user-agent") String userAgent, @QueryParam("scope") Set<String> scope,
                                         @QueryParam("startTime") String startTime,
                                         @QueryParam("endTime") String endTime,
                                         @PathParam("resourceType") String resourceType,
                                         @PathParam("resource") String resource, @QueryParam("callback") String callback) {

        Map<String, List<Map>> result = getStats(userAgent, scope, startTime, endTime, resourceType, resource);
        return new JSONWithPadding(result, callback);
    }

    //very uneffective method to be removed soon,
    private void addMissingData(Map<String, List<Map>> data, DateTime startTime, DateTime endTime) {
        for (List<Map> list : data.values()) {
            for (Map map : list) {
                List<BigDecimal[]> dataList = (List<BigDecimal[]>) map.get("data");
                DateTime inter = startTime;
                // Loop through each day in the span
                while (inter.compareTo(endTime) < 0) {
                    // Go to next
                    inter = inter.plusHours(1);
                    if (!isDatePresent(inter, dataList)) {
                        dataList.add(new BigDecimal[]{BigDecimal.valueOf(inter.getMillis()), null});
                    }
                }
                Collections.sort(dataList, new Comparator<BigDecimal[]>() {
                    @Override
                    public int compare(BigDecimal[] o1, BigDecimal[] o2) {
                        return o1[0].compareTo(o2[0]);
                    }
                });
            }
        }

    }

    private boolean isDatePresent(DateTime date, List<BigDecimal[]> dataList) {
        int year = date.getYear();
        int month = date.getMonthOfYear();
        int day = date.getDayOfMonth();
        int hour = date.getHourOfDay();

        for (BigDecimal[] val : dataList) {
            BigDecimal timeasBD = (BigDecimal) val[0];
            DateTime datetime = new DateTime(timeasBD.longValue());
            int yearDT = datetime.getYear();
            int monthDT = datetime.getMonthOfYear();
            int dayDT = datetime.getDayOfMonth();
            int hourDT = datetime.getHourOfDay();
            if (year == yearDT && monthDT == month && dayDT == day && hourDT == hour)
                return true;
        }

        return false;
    }


    // test method ...to be removed on cleaning
    @GET
    @Path("test")
    @Produces({MediaType.APPLICATION_JSON})
    public List<List> getStatss(@QueryParam("startTime") String startTime,
                                @QueryParam("endTime") String endTime) {
        //JSONConfiguration.mapped().rootUnwrapping(false).build();
        DateTime start = fmt.parseDateTime(startTime);
        DateTime end = fmt.parseDateTime(endTime);
        Interval interval = new Interval(start, end);
        //JSONConfiguration.mapped().rootUnwrapping(true).build();
        Statistics stat = new Statistics();
        stat.setA("a");
        stat.setB("b");
        List res = new ArrayList();
        List l1 = new ArrayList();
        List l2 = new ArrayList();
        l1.add(startTime);
        l1.add(100);
        l2.add(endTime);
        l2.add(101);
        res.add(l1);
        res.add(l2);
        return res;
    }


}
