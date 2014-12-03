package domainhealth.rest;


import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.DateUtil;
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
            return getPropertyData("core",null,"HeapUsedCurrent",new Date(),300,"AdminServer");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private DateAmountDataSet getPropertyData(String resourceType, String resourceName, String resourceProperty, Date endDateTime, int durationMins, String serverName) throws IOException {
        DateAmountDataSet resultDataSet = null;
        Date startDateTime = DateUtil.getEarlierTime(endDateTime, durationMins);
        File file = statisticsStorage.getResourceStatisticsCSV(endDateTime, serverName, resourceType, resourceName);

        if ((file == null) || (!file.exists())) {
            return new DateAmountDataSet();
        }

        int propertyPosition = statisticsStorage.getPropertyPositionInStatsFile(resourceType, resourceName, endDateTime, serverName, resourceProperty);

        if (propertyPosition < 0) {
            return new DateAmountDataSet();
        }

        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(file));
            resultDataSet = generatePropertyDataSet(in, startDateTime, endDateTime, propertyPosition);
        } finally {
            if (in != null) {
                try { in.close(); } catch (Exception e) {}
            }
        }

        return resultDataSet;
    }


    private DateAmountDataSet generatePropertyDataSet(BufferedReader in, Date startDateTime, Date endDateTime, int propertyPosition) throws IOException {
        DateAmountDataSet resultDataSet = new DateAmountDataSet();
        DateFormat secondDateFormat = new SimpleDateFormat(DISPLAY_DATETIME_FORMAT);
        Date dateTime = null;
        StringBuilder currentProperty = new StringBuilder();
        int positionCount = 0;
        int readChar = 0;
        char character = 0;
        boolean skipCurrentLine = true;

        while ((readChar = in.read()) >= 0) {
            character = (char) readChar;

            if ((character == CRG_RETURN) || (character == NEW_LINE)) {
                skipCurrentLine = false;
                currentProperty = new StringBuilder();
                positionCount = 0;
            } else {
                if (skipCurrentLine) {
                    continue;
                } else if (character == SEPARATOR_CHAR) {
                    if (positionCount == 0) {
                        String dateTimeText = currentProperty.toString();

                        try {
                            // Get first property as date-time
                            dateTime = secondDateFormat.parse(dateTimeText);
                        } catch(Exception e) {
                            // Skip corrupted line
                            skipCurrentLine = true;
                            AppLog.getLogger().debug(getClass() + ".generatePropertyDataSet() skipping corrupt line when getting date-time. Cause: " + e.toString());
                        }

                        if (dateTime.before(startDateTime)) {
                            // Skip lines which are before current start date
                            skipCurrentLine = true;
                        } else if (dateTime.after(endDateTime)) {
                            // Skip rest of file if after current end date
                            break;
                        }
                    } else if (positionCount == propertyPosition) {
                        try {
                            // Add found property
                            double propValue = Double.parseDouble(currentProperty.toString());
                            resultDataSet.add(dateTime, propValue);
                            skipCurrentLine = true;
                        } catch (Exception e) {
                            // Skip corrupted line
                            AppLog.getLogger().debug(getClass() + ".generatePropertyDataSet() skipping corrupt line, propertyPosition=" + propertyPosition + ", dateTime=" + dateTime + ". Cause: " + e.toString());
                            //System.out.println("CSV ERROR: " + getClass() + ".generatePropertyDataSet() skipping corrupt line, propertyPosition=" + propertyPosition + ", dateTime=" + dateTime + ". Cause: " + e.toString());
                        }
                    }

                    currentProperty = new StringBuilder();
                    positionCount ++;
                } else {
                    currentProperty.append(character);
                }
            }
        }

        return resultDataSet;
    }

}
