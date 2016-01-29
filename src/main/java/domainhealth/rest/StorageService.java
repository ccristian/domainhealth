package domainhealth.rest;


import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.MonitorProperties;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.frontend.data.DateAmountDataItem;
import domainhealth.frontend.data.DateAmountDataSet;
import domainhealth.frontend.data.Domain;
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
    public Map<String,List<Map>> getStats(@QueryParam("scope") Set<String> scope,
                                                                @QueryParam("startTime") String startTime,
                                                                @QueryParam("endTime") String endTime,
                                                                @PathParam("resourceType") String resourceType,
                                                                @PathParam("resource") String resource) {

        try {




            Map<String, List<Map>> result = new HashMap<String, List<Map>>();

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
                Set<String> coreProps = new HashSet<String>();
                switch (resourceType) {
                    case "core":
                        coreProps.add("HeapUsedCurrent");
                        coreProps.add("OpenSocketsCurrentCount");
                        coreProps.add("HeapSizeCurrent");
                        coreProps.add("HeapFreeCurrent");
                        coreProps.add("HeapFreePercent");
                        coreProps.add("ExecuteThreadTotalCount");
                        coreProps.add("HoggingThreadCount");
                        coreProps.add("PendingUserRequestCount");
                        coreProps.add("QueueLength");
                        coreProps.add("CompletedRequestCount");
                        coreProps.add("ExecuteThreadIdleCount");
                        coreProps.add("MinThreadsConstraintsCompleted");
                        coreProps.add("MinThreadsConstraintsPending");
                        coreProps.add("StandbyThreadCount");
                        coreProps.add("Throughput");
                        coreProps.add("TransactionTotalCount");
                        coreProps.add("TransactionCommittedTotalCount");
                        coreProps.add("TransactionRolledBackTotalCount");
                        coreProps.add("TransactionHeuristicsTotalCount");
                        coreProps.add("TransactionAbandonedTotalCount");
                        coreProps.add("ActiveTransactionsTotalCount");

                        resource = null;




                        //result.put(server, );
                        break;
                    case "datasource":
                        coreProps.add("NumAvailable");
                        coreProps.add("NumUnavailable");
                        coreProps.add("ActiveConnectionsCurrentCount");
                        coreProps.add("ConnectionDelayTime");
                        coreProps.add("FailedReserveRequestCount");
                        coreProps.add("FailuresToReconnectCount");
                        coreProps.add("LeakedConnectionCount");
                        coreProps.add("WaitingForConnectionCurrentCount");
                        coreProps.add("WaitingForConnectionFailureTotal");
                        coreProps.add("WaitSecondsHighCount");
                        break;

                    case "destination":
                        coreProps.add("MessagesCurrentCount");
                        coreProps.add("MessagesReceivedCount");
                        coreProps.add("MessagesPendingCount");
                        coreProps.add("MessagesHighCount");
                        coreProps.add("ConsumersCurrentCount");
                        coreProps.add("ConsumersHighCount");
                        coreProps.add("ConsumersTotalCount");
                        break;

                    case "ejb":
                        coreProps.add("PooledBeansCurrentCount");
                        coreProps.add("AccessTotalCount");
                        coreProps.add("BeansInUseCurrentCount");
                        coreProps.add("WaiterCurrentCount");
                        coreProps.add("WaiterTotalCount");
                        coreProps.add("TransactionsCommittedTotalCount");
                        coreProps.add("TransactionsRolledBackTotalCount");
                        coreProps.add("TransactionsTimedOutTotalCount");
                        break;
                    case "saf":
                        coreProps.add("MessagesCurrentCount");
                        coreProps.add("MessagesPendingCount");
                        coreProps.add("MessagesReceivedCount");
                        coreProps.add("MessagesHighCount");
                        break;

                    case "webapp":
                        coreProps.add("OpenSessionsCurrentCount");
                        coreProps.add("OpenSessionsHighCount");
                        coreProps.add("SessionsOpenedTotalCount");
                        break;

                    case "svrchnl":
                        coreProps.add("AcceptCount");
                        coreProps.add("ConnectionsCount");
                        coreProps.add("MessagesReceivedCount");
                        coreProps.add("MessagesSentCount");
                        break;

                    case "workmgr":
                        coreProps.add("CompletedRequests");
                        coreProps.add("PendingRequests");
                        coreProps.add("StuckThreadCount");
                        break;






                }

                dataMap = statisticsStorage.getPropertyData(resourceType, resource, coreProps, interval, server);
                for (String res:dataMap.keySet()) {

                    DateAmountDataSet dataSet  = dataMap.get(res);
                    String property = dataSet.getResourceProperty();

                    List dataList = new LinkedList();
                    for (DateAmountDataItem dateAmountDataItem:dataSet.getData()) {
                        dataList.add(new BigDecimal[]{BigDecimal.valueOf(dateAmountDataItem.getDateTime().getTime()), BigDecimal.valueOf(dateAmountDataItem.getAmount())});
                    }

                    Map map = new LinkedHashMap();
                    map.put("name",server);
                    map.put("data",dataList);
                    List<Map> listMap  = result.get(res);
                    if (listMap==null){
                        listMap = new ArrayList<>();
                        result.put(res,listMap);
                    }
                    listMap.add(map);
                }

            }

            long t1 = System.currentTimeMillis();
            // TODO add misssing data
            // addMissingData(result,start,end);
            long t2 = System.currentTimeMillis();
            return result;
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
        }

        return null;
    }

    private void addMissingData(Map<String,List<Map>> data,DateTime startTime,DateTime endTime){
        for (List<Map> list : data.values()) {
            for (Map map:list) {
                List<BigDecimal[]> dataList = (List<BigDecimal[]>)map.get("data");
                DateTime inter = startTime;
                // Loop through each day in the span
                while (inter.compareTo(endTime) < 0) {
                    // Go to next
                    inter = inter.plusHours(1);
                    if (!isDatePresent(inter,dataList)){
                        dataList.add(new BigDecimal[]{BigDecimal.valueOf(inter.getMillis()),null});
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

    private boolean isDatePresent(DateTime date,List<BigDecimal[]> dataList){
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
            if (year==yearDT && monthDT==month && dayDT==day && hourDT==hour )
                return true;
        }

        return false;
    }


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
