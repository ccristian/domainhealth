package domainhealth.core.util;

import domainhealth.core.env.AppLog;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.frontend.data.DateAmountDataSet;

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
 * Created by chiovcr on 03/12/2014.
 */
public class StorageUtil {


    /**
     * Collects the required statistics for a given server instance's resource
     * property, adding the retrieved series of data-time/amount data values to
     * the graph for the server
     *
     * @param endDateTime      The end date-time of the window of statistics to plot
     * @param durationMins     The duration of minutes of the window of statistics to plot
     * @param serverName       The name of the WebLogic server to retrieve and process results for
     * @param resourceType     The type of resource to plot (ie. core, data-source or destination)
     * @param resourceName     The name of the core/data-source/destination resource to plot
     * @param resourceProperty The property of the core/data-source/destination resource to plot
     * @return The series of date-time/amount data items to be plotted as a line on a graph for a specific server
     * @throws java.io.IOException Indicates a problem in collecting and processing the resource results
     */
    public static DateAmountDataSet getPropertyData(StatisticsStorage statisticsStorage, String resourceType, String resourceName, String resourceProperty, Date endDateTime, int durationMins, String serverName) throws IOException {
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
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }

        return resultDataSet;
    }

    /**
     * For a given property column in a CSV file, collects together all
     * statistic values for that property between a specific start and end date.
     *
     * @param in               The CSV file reader handle to read statisics from
     * @param startDateTime    The start date-time of the window of statistics to plot
     * @param endDateTime      The end date-time of the window of statistics to plot
     * @param propertyPosition The colum position in the CSV file, for the property we want to get stats for
     * @return The set of retrieved stats for the specific property within the specified time window
     * @throws IOException Indicates that the statistics could not be retrieved properly from the CSV file
     */
    public static DateAmountDataSet generatePropertyDataSet(BufferedReader in, Date startDateTime, Date endDateTime, int propertyPosition) throws IOException {
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
                        } catch (Exception e) {
                            // Skip corrupted line
                            skipCurrentLine = true;
                            AppLog.getLogger().debug(StorageUtil.class + ".generatePropertyDataSet() skipping corrupt line when getting date-time. Cause: " + e.toString());
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
                            AppLog.getLogger().debug(StorageUtil.class + ".generatePropertyDataSet() skipping corrupt line, propertyPosition=" + propertyPosition + ", dateTime=" + dateTime + ". Cause: " + e.toString());
                            //System.out.println("CSV ERROR: " + getClass() + ".generatePropertyDataSet() skipping corrupt line, propertyPosition=" + propertyPosition + ", dateTime=" + dateTime + ". Cause: " + e.toString());
                        }
                    }

                    currentProperty = new StringBuilder();
                    positionCount++;
                } else {
                    currentProperty.append(character);
                }
            }
        }

        return resultDataSet;
    }


}
