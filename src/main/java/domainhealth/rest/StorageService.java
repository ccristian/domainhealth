package domainhealth.rest;


import domainhealth.core.env.AppProperties;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.StorageUtil;
import domainhealth.frontend.data.DateAmountDataSet;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;

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

        //get all servers from the domain !


        try {
            return StorageUtil.getPropertyData(statisticsStorage,"core",null,"State",new Date(),1,"AdminServer");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }




}
