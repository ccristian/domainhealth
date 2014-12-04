package domainhealth.rest;


import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.DateUtil;
import domainhealth.core.util.StorageUtil;
import domainhealth.frontend.data.DateAmountDataSet;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static domainhealth.core.statistics.StatisticsStorage.CRG_RETURN;
import static domainhealth.core.statistics.StatisticsStorage.NEW_LINE;
import static domainhealth.core.statistics.StatisticsStorage.SEPARATOR_CHAR;
import static domainhealth.core.util.DateUtil.DISPLAY_DATETIME_FORMAT;

/**
 * Created by chiovcr on 02/12/2014.
 */
@Path("/statistics")
@Stateless
public class StorageService {
    // The Java method will process HTTP GET requests
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
    public DateAmountDataSet getClichedMessage() {
        try {
            return StorageUtil.getPropertyData(statisticsStorage,"core",null,"HeapUsedCurrent",new Date(),300,"AdminServer");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }




}
