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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import java.util.*;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;

/**
 * Created by chiovcr on 02/12/2014.
 */
@Path("/statistics")
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


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("data")
    public Domain getDataFor() {
        return null;
    }

    @GET
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Produces({MediaType.APPLICATION_JSON})
    public Domain getSnapshot() {

        Domain domain;
        DomainRuntimeServiceMBeanConnection conn = null;
        //get all servers from the domain !
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
                List<String> resCore = new ArrayList<String>();
                resCore.add("State");
                resCore.add("OpenSocketsCurrentCount");
                resCore.add("HeapSizeCurrent");
                resCore.add("HeapFreeCurrent");
                resCore.add("HoggingThreadCount");
                resCore.add("PendingUserRequestCount");
                resCore.add("TransactionRolledBackTotalCount");
                DateTime start = new DateTime(2004, 12, 25, 0, 0, 0, 0);
                DateTime end = new DateTime(2016, 1, 1, 0, 0, 0, 0);
                Interval interval = new Interval(start, end);
                System.out.println("-------------------------"+statisticsStorage.getPropertyData("core", null, "OpenSocketsCurrentCount", interval, "AdminServer")
                );

                System.out.println("-------------------------"+statisticsStorage.getPropertyData("datasource", null, "OpenSocketsCurrentCount", interval, "AdminServer")
                );


                /*
                Properties props = statisticsStorage.retrieveOneDayResoureNameList(new Date(),"datasource");
                List<String> resDatasource = new ArrayList<String>();
                resDatasource.add("ActiveConnectionsCurrentCount");
                resDatasource.add("FailedReserveRequestCount");
                resDatasource.add("WaitingForConnectionCurrentCount");

                for (Object property:props.keySet()){
                    List<DateAmountDataSet> res = statisticsStorage.getPropertyData("datasource", (String) property, resDatasource, new Date(), 1, "AdminServer");
                    server.getStatistics().addAll(res);
                }
*/


            }
            System.out.println(domain);
            return domain;
        } catch (Exception e) {
            AppLog.getLogger().error(e.toString());
            e.printStackTrace();
            AppLog.getLogger().error("Statistics Retriever Background Service - unable to retrieve statistics for domain's servers for this iteration");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }


        return null;
    }


}
