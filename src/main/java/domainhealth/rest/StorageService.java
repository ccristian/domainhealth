package domainhealth.rest;


import commonj.work.WorkItem;
import domainhealth.backend.retriever.StatisticCapturer;
import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.env.ContextAwareWork;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.StorageUtil;
import domainhealth.frontend.data.DateAmountDataSet;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.NAME;

/**
 * Created by chiovcr on 02/12/2014.
 */
@Path("/statistics")
@Stateless
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
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Produces({MediaType.APPLICATION_JSON})
    public DateAmountDataSet getSnapshot() {
        DomainRuntimeServiceMBeanConnection conn = null;
        //get all servers from the domain !
        try {

            conn = new DomainRuntimeServiceMBeanConnection();

            String domainName  = conn.getDomainName();





           ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
            int length = serverRuntimes.length;
            for (int i = 0; i < length; i++) {
                final String serverName = conn.getTextAttr(serverRuntimes[i], NAME);


            }

        } catch (Exception e) {
            AppLog.getLogger().error(e.toString());
            e.printStackTrace();
            AppLog.getLogger().error("Statistics Retriever Background Service - unable to retrieve statistics for domain's servers for this iteration");
        } finally {
            if (conn != null) {
                conn.close();
            }
        }


        try {
            return StorageUtil.getPropertyData(statisticsStorage, "core", null, "State", new Date(), 1, "AdminServer");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
