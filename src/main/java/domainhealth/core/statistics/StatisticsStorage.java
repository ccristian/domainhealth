//Copyright (C) 2008-2013 Paul Done . All rights reserved.
//This file is part of the DomainHealth software distribution. Refer to the
//file LICENSE in the root of the DomainHealth distribution.
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
//POSSIBILITY OF SUCH DAMAGE.
package domainhealth.core.statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;

import static java.io.File.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.management.ObjectName;

import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.jmx.WebLogicMBeanPropConstants;

import static domainhealth.core.statistics.MonitorProperties.*;
import static domainhealth.core.util.DateUtil.*;

import domainhealth.core.util.DateUtil;
import domainhealth.core.util.FileUtil;
import domainhealth.frontend.data.DateAmountDataSet;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Provides access and persistence to the file-system based storage for
 * captured Domain Health statistics. Under the covers, this is a series
 * or sub-directories and CSV files divided by day, server and resource-type
 * (eg. datasource, destination).
 */
public class StatisticsStorage {
    /**
     * Newline char
     */
    public final static char NEW_LINE = '\n';

    /**
     * Carriage return char
     */
    public final static char CRG_RETURN = '\r';

    /**
     * The comma separator used in the CSV files to separate different
     * property values
     */
    public static final char SEPARATOR_CHAR = ',';

    /**
     * The comma separator used in the CSV files to separate different
     * property values
     */
    public static final String SEPARATOR = "" + SEPARATOR_CHAR;

    /**
     * Create providing the root of the directory which is used to hold
     * captured CSV files.
     *
     * @param rootDirectoryPath Root path of statistics directory
     */
    public StatisticsStorage(String rootDirectoryPath) {
        this.rootDirectoryPath = rootDirectoryPath;
    }

    /**
     * Get the root of the directory which is used to hold captured CSV files
     *
     * @return Root path of statistics directory
     */
    public String getRootDirectoryPath() {
        return rootDirectoryPath;
    }

    /**
     * Gets the CSV file for storing/retrieving statistics for a given
     * resource on a given server for a given day.
     *
     * @param dateTime     The datetime indicating which day look for a CSV for
     * @param serverName   The name of the server to get the property from
     * @param resourceType The type of resource (eg. core, datasource)
     * @param resourceName The name of the resource
     * @return The file handle on the statistics CSV file for the resource
     * @throws IOException Indicates problem accessing statistics directories/files
     */
    public File getResourceStatisticsCSV(Date dateTime, String serverName, String resourceType, String resourceName) throws IOException {
        return FileUtil.retrieveFile(getDayServerResourceCSVPath(dateTime, serverName, resourceType, resourceName));
    }

    /**
     * Adds the result text row to the CSV file, first adding a CSV header row
     * if it doesn't already have one.
     *
     * @param dateTime     The datetime indicating which day to look for a CSV for
     * @param serverName   The name of the server to set the properties for
     * @param resourceType The type of resource (eg. core, datasource)
     * @param resourceName The name of the resource
     * @param headerLine   Provider of the header text to add as first row to CSV file (if CSV file is currently empty)
     * @param contentLine  Provider of the result row text to append to the CSV
     * @throws IOException            Indicates problem writing to the CSV file.
     * @throws WebLogicMBeanException Indicates a problem with the content provider retrieving the row data
     */
    public void appendToResourceStatisticsCSV(Date dateTime, String serverName, String resourceType, String resourceName, String headerLine, String contentLine) throws IOException {
        FileUtil.createOrRetrieveDir(getDayServerResourceDirectoryPath(dateTime, serverName, resourceType));
        String filepath = getDayServerResourceCSVPath(dateTime, serverName, resourceType, resourceName);

        PrintWriter out = null;

        try {
            File file = FileUtil.createOrRetrieveFile(filepath);

            if ((contentLine != null) && (contentLine.length() > 0)) {
                out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

                if (file.length() <= 0) {
                    out.println(headerLine);
                }

                out.println(contentLine);
                out.flush();
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Find the earliest recorded time in the Core statistics CSV file, for the
     * earliest Core statistics CSV file that has been captured
     *
     * @return The earliest recorded date-time
     * @throws WebLogicMBeanException Indicates problem accessing Admin Server JMX tree
     * @throws IOException            Indicates problem accessing statistics directories/files
     */
    public Date getEarliestRecordedDateTime() throws WebLogicMBeanException, IOException {
        DomainRuntimeServiceMBeanConnection conn = null;

        try {
            Date earliestDate = getEarliestRecordedDayDirectory();
            conn = new DomainRuntimeServiceMBeanConnection();
            ObjectName domainConfig = conn.getDomainConfiguration();
            String adminServerName = conn.getTextAttr(domainConfig, WebLogicMBeanPropConstants.ADMIN_SERVER_NAME);
            String earliestCoreCSVPath = getDayServerResourceCSVPath(earliestDate, adminServerName, CORE_RESOURCE_TYPE, MonitorProperties.CORE_RSC_DEFAULT_NAME);
            File earliestCoreCSVFile = FileUtil.retrieveFile(earliestCoreCSVPath);
            return getFirstDateTimeInCSV(earliestCoreCSVFile);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Find the position of a property name in the first line of a given
     * resource's statistics CSV file (starting at zero for the first property
     * listed)
     *
     * @param resourceType The type of resource (eg. core, datasource)
     * @param resourceName The name of the resource
     * @param dateTime     The datetime indicating which day look for a CSV for
     * @param serverName   The name of the server to get the property from
     * @param property     The name of the property to look for
     * @return Zero based index of the property name position
     * @throws IOException Indicates problem accessing statistics directories/files
     */
    public int getPropertyPositionInStatsFile(String resourceType, String resourceName, Date dateTime, String serverName, String property) throws IOException {
        int propertyPosition = -1;
        BufferedReader in = null;

        try {
            File file = getResourceStatisticsCSV(dateTime, serverName, resourceType, resourceName);
            in = new BufferedReader(new FileReader(file));
            StringBuilder currentProperty = new StringBuilder();
            int readChar = 0;
            char character = 0;
            int positionCount = 0;

            while ((readChar = in.read()) >= 0) {
                character = (char) readChar;

                if ((character == CRG_RETURN) || (character == NEW_LINE)) {
                    break;
                } else {
                    if (character == SEPARATOR_CHAR) {
                        if (currentProperty.toString().equalsIgnoreCase(property)) {
                            propertyPosition = positionCount;
                            break;
                        } else {
                            currentProperty = new StringBuilder();
                            positionCount++;
                        }
                    } else {
                        currentProperty.append(character);
                    }
                }
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return propertyPosition;
    }

    /**
     * Get list of names of all possible monitoring mbean instances from a
     * statistics property file for a specific day. Reading/writing has to be
     * serialized by resource type file to avoid concurrency issues where two
     * two threads may attempt to write bytes to the same props file at the
     * same time (see DH bug#3406293).
     *
     * @param dateTime     The datetime indicating which day to look for a props file for
     * @param resourceType The type of resource (eg. core, datasource)
     * @return The list of mbean instances names
     * @throws FileNotFoundException Indicates file containing name list could not be found
     * @throws IOException           Indicates file containing name list could not be read
     */
    public Properties retrieveOneDayResoureNameList(Date dateTime, String resourceType) throws IOException {
        Properties propList = new Properties();

        // Lock per resource type enabling threads reading/writing from/to
        // different resource files to still work in parallel
        synchronized (getResourceMonitorObject(resourceType)) {
            InputStream propsIn = null;

            try {
                File file = FileUtil.retrieveFile(getDayResourcePropListFilePath(dateTime, resourceType));

                if (file != null) {
                    propsIn = new FileInputStream(file);
                    propList.load(propsIn);
                }
            } finally {
                if (propsIn != null) {
                    try {
                        propsIn.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return propList;
    }

    /**
     * Saves a list of names of all possible monitoring work managers to a
     * statistics property file for a specific day. Reading/writing has to
     * be serialised by resource type file to avoid concurrency issues where
     * two threads may attempt to write bytes to the same props file at the
     * same time (see DH bug#3406293).
     *
     * @param dateTime      The date time indicating which time day to save the file for
     * @param resourceType  The type of resource (eg. core, datasource)
     * @param extraPropList List of new property names to add
     * @throws IOException Indicates that list could not be saved to file
     */
    public void appendSavedOneDayResourceNameList(Date dateTime, String resourceType, Properties extraPropList) throws IOException {
        // Lock per resource type enabling threads reading/writing from/to
        // different resource files to still work in parallel
        synchronized (getResourceMonitorObject(resourceType)) {
            Properties propList = retrieveOneDayResoureNameList(dateTime, resourceType);
            OutputStream propsOut = null;

            try {
                FileUtil.createOrRetrieveDir(getDayDirectoryPath(dateTime));
                File file = FileUtil.createOrRetrieveFile(getDayResourcePropListFilePath(dateTime, resourceType));
                propsOut = new FileOutputStream(file);
                propList.putAll(extraPropList);
                propList.store(propsOut, PROP_LIST_CMNT_PREFIX + resourceType);
            } finally {
                if (propsOut != null) {
                    try {
                        propsOut.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Get lock monitor object for specific resource type (eg. core, work-
     * manager, destination). This level of re-direction is required to avoid
     * one big long-lived lock across all resource files. Reading/writing has
     * to be serialized by resource type file to avoid concurrency issues
     * where two threads may attempt to write bytes to the same props file at
     * the same time (see DH bug#3406293).
     *
     * @param resourceType The type of resource (eg. core, datasource)
     * @return The unique monitor object for that resource type
     */
    public Object getResourceMonitorObject(String resourceType) {
        Object monitorObject = null;

        // Short lived big lock to get more fine-grained monitor object
        // to then be locked on for the subsequent slow File I/O work
        synchronized (resourceMonitorObjects) {
            monitorObject = resourceMonitorObjects.get(resourceType);

            if (monitorObject == null) {
                monitorObject = new Object();
                resourceMonitorObjects.put(resourceType, monitorObject);
            }
        }

        return monitorObject;
    }

    /**
     * Get the list of resource names (keys) from the key=value pairs listed
     * in a file on the filesystem.
     *
     * @param dateTime     The datetime indicating which day to look for a props filefor
     * @param resourceType The type of resource (eg. core, datasource)
     * @return The set of parameter names
     * @throws IOException Indicates a problem retrieving the props file or its contents
     */
    public Set<String> getResourceNamesFromPropsList(Date dateTime, String resourceType) throws IOException {
        Set<String> resourceKeys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        Properties properties;
        properties = retrieveOneDayResoureNameList(dateTime, resourceType);
        Enumeration<Object> keysEnum = properties.keys();

        while (keysEnum.hasMoreElements()) {
            resourceKeys.add((String) keysEnum.nextElement());
        }
        System.out.println(resourceKeys);
        return resourceKeys;
    }

    /**
     * Clean up day statistics directories for all the days older than the
     * number of days to be retained from the current day.
     *
     * @param numDaysToRetain The number of days to retain CSV statistics for
     * @return True if clean-up was successful or not required.
     */
    public boolean cleanupOldDirectories(int numDaysToRetain) {
        AppLog.getLogger().info("Cleaning CSV statistics files older than " + numDaysToRetain + " days");
        Date currentDay = new Date();
        int previousNumDaysIndex = numDaysToRetain;
        boolean notYetRemovedAllOldDirs = true;

        while (notYetRemovedAllOldDirs) {
            Date previousDay = DateUtil.getNthPreviousDay(currentDay, previousNumDaysIndex);
            String dayDirectoryPath = getDayDirectoryPath(previousDay);
            File dayDirectoryFile = new File(dayDirectoryPath);

            if (!dayDirectoryFile.exists()) {
                notYetRemovedAllOldDirs = false;
            } else {
                if (FileUtil.deleteRecursive(dayDirectoryFile)) {
                    AppLog.getLogger().info("Removed CSV statistics directory: " + dayDirectoryPath);
                } else {
                    AppLog.getLogger().error("Failed to delete directory: " + dayDirectoryPath + ", aborting further cleanup of this directory for this pass");
                }

                currentDay = previousDay;
                previousNumDaysIndex = 1;
            }
        }

        return notYetRemovedAllOldDirs;
    }

    /**
     * Get the root of the directory which is used to hold captured CSV files
     * for a given date
     *
     * @param dateTime Date-time to find directory for
     * @return Root path of data-time's statistics directory
     */
    private String getDayDirectoryPath(Date dateTime) {
        DateFormat dayDateFormat = new SimpleDateFormat(DATE_PATH_FORMAT);
        return rootDirectoryPath + separatorChar + dayDateFormat.format(dateTime);
    }

    /**
     * Get the path of the directory containing CSV statistics for a given
     * resource type on a given day.
     *
     * @param dateTime     The datetime indicating which day to look for a CSV directory for
     * @param serverName   The name of the server to get the statistics directory for
     * @param resourceType The type of resource (eg. core, datasource)
     * @return The path of the directory containing the CSV files
     */
    private String getDayServerResourceDirectoryPath(Date dateTime, String serverName, String resourceType) {
        String parentDirPath = getDayDirectoryPath(dateTime) + separatorChar + serverName;
        return String.format("%s%s%s", parentDirPath, separatorChar, resourceType);
    }

    /**
     * Get the path of CSV statistics file for a given resource for a given
     * resource type for a given server on a given day.
     *
     * @param dateTime     The datetime indicating which day to look for a CSV for
     * @param serverName   The name of the server to get the CSV file path for
     * @param resourceType The type of resource (eg. core, datasource)
     * @param resourceName The name of the resource
     * @return The file path of the specific statistics CSV file
     */
    private String getDayServerResourceCSVPath(Date dateTime, String serverName, String resourceType, String resourceName) {
        if (resourceName == null) {
            resourceName = "";
        } else if (resourceName.length() > 0) {
            resourceName += "_";
        }

        DateFormat dayDateFormat = new SimpleDateFormat(DATE_PATH_FORMAT);
        String dirPath = getDayServerResourceDirectoryPath(dateTime, serverName, resourceType);
        return String.format("%s%s%s_%s_%s%s%s", dirPath, separatorChar, resourceType, serverName, resourceName, dayDateFormat.format(dateTime), CSV_SUFFIX);
    }

    /**
     * Get the path of CSV statistics file for a given resource for a given
     * resource type for a given server on a given day.
     *
     * @param serverName   The name of the server to get the CSV file path for
     * @param resourceType The type of resource (eg. core, datasource)
     * @param resourceName The name of the resource
     * @return The file path of the specific statistics CSV file
     */
    private Map<File, Date> getServerResourceCSVPath(Interval interval, String serverName, String resourceType, String resourceName) throws IOException {
        Map<File, Date> daysMap = new HashMap<File, Date>();
        DateFormat dayDateFormat = new SimpleDateFormat(DATE_PATH_FORMAT);
        if (resourceName == null) {
            resourceName = "";
        } else if (resourceName.length() > 0) {
            resourceName += "_";
        }
        DateTime start = interval.getStart();
        DateTime stop = interval.getEnd();
        DateTime inter = start;
        // Loop through each day in the span
        while (inter.compareTo(stop) < 0) {
            //System.out.println(inter);
            // Go to next
            Date date = inter.toDate();
            String dirPath = getDayServerResourceDirectoryPath(date, serverName, resourceType);
            if (dirPath != null) {

                String fileName = String.format("%s%s%s_%s_%s%s%s", dirPath, separatorChar, resourceType, serverName, resourceName, dayDateFormat.format(date), CSV_SUFFIX);
                File file = FileUtil.retrieveFile(fileName);
                if (file != null) {
                    System.out.println(dirPath);
                    daysMap.put(file, date);
                }
            }
            inter = inter.plusDays(1);
        }
        return daysMap;
    }

    /**
     * Get the property list of instances of a resource type for a given day.
     *
     * @param dateTime     The datetime indicating which day look for a CSV for
     * @param resourceType The type of resource (eg. core, datasource)
     * @return The property list for day's resource
     */
    private String getDayResourcePropListFilePath(Date dateTime, String resourceType) {
        DateFormat dayDateFormat = new SimpleDateFormat(DATE_PATH_FORMAT);
        String dirPath = getDayDirectoryPath(dateTime);
        return String.format("%s%s%s_%s_%s%s", dirPath, separatorChar, resourceType, RESOURCE_LIST_FILENAME_SUFFIX, dayDateFormat.format(dateTime), PROPS_SUFFIX);
    }

    /**
     * Find the earliest recorded time in a given Core statistics CSV file
     *
     * @param file The Core Statistics CSV file to read
     * @return The earliest recorded date-time in the CSV file
     * @throws IOException Indicates problem accessing statistics directories/files
     */
    private Date getFirstDateTimeInCSV(File file) throws IOException {
        if ((file == null) || (!file.exists())) {
            return null;
        }

        Date dateTime = null;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(file));
            StringBuilder dateTimeText = new StringBuilder();
            int lineNumber = 0;
            boolean previousCharIsAlsoEndline = false;
            int readChar = 0;
            char character = 0;

            while ((readChar = in.read()) >= 0) {
                character = (char) readChar;

                if ((character == CRG_RETURN) || (character == NEW_LINE)) {
                    if (!previousCharIsAlsoEndline) {
                        lineNumber++;

                        if (lineNumber > 1) {
                            throw new IOException("Unable to locate first recorded data-time field in CSV file: " + file.getAbsolutePath());
                        }
                    }

                    previousCharIsAlsoEndline = true;
                } else if (lineNumber == 1) {
                    if (character == SEPARATOR_CHAR) {
                        break;
                    } else {
                        dateTimeText.append(character);
                    }

                    previousCharIsAlsoEndline = false;
                } else {
                    previousCharIsAlsoEndline = false;
                }
            }

            DateFormat secondDateFormat = new SimpleDateFormat(DISPLAY_DATETIME_FORMAT);
            dateTime = secondDateFormat.parse(dateTimeText.toString());
        } catch (ParseException pe) {
            AppLog.getLogger().error(pe.toString());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return dateTime;
    }

    /**
     * Find the day that a directory for day statistic CSV files had been
     * created for.
     *
     * @return Date of earliest captured CSV statistics day directory
     * @throws IOException Indicates problem accessing statistics directories/files
     */
    private Date getEarliestRecordedDayDirectory() throws IOException {
        File rootDir = FileUtil.retrieveDir(rootDirectoryPath);

        if (rootDir == null) {
            return new Date();
        }

        SortedSet<String> dirnameSortedSet = new TreeSet<String>(Arrays.asList(rootDir.list()));
        DateFormat dayDateFormat = new SimpleDateFormat(DATE_PATH_FORMAT);
        Date earliestDate = null;

        for (String dirname : dirnameSortedSet) {
            try {
                earliestDate = dayDateFormat.parse(dirname);
                break;
            } catch (Exception e) {
                // If can't parse directory name as date string then skip and try next dir
            }
        }

        if (earliestDate == null) {
            earliestDate = new Date();
        }

        return earliestDate;
    }

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
    public DateAmountDataSet getPropertyData(String resourceType, String resourceName, String resourceProperty, Date endDateTime, int durationMins, String serverName) throws IOException {
        DateAmountDataSet resultDataSet = null;
        Date startDateTime = DateUtil.getEarlierTime(endDateTime, durationMins);
        File file = this.getResourceStatisticsCSV(endDateTime, serverName, resourceType, resourceName);
        if ((file == null) || (!file.exists())) {
            return new DateAmountDataSet();
        }
        int propertyPosition = this.getPropertyPositionInStatsFile(resourceType, resourceName, endDateTime, serverName, resourceProperty);
        if (propertyPosition < 0) {
            return new DateAmountDataSet();
        }
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(file));
            resultDataSet = generatePropertyDataSet(in, startDateTime, endDateTime, propertyPosition, resourceType, resourceName, resourceProperty);
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

    public DateAmountDataSet getPropertyData(String resourceType, String resourceName, String resourceProperty, Interval interval, String serverName) throws IOException {
        DateAmountDataSet resultDataSet = new DateAmountDataSet();
        Map<File, Date> csvLocationPerFile = this.getServerResourceCSVPath(interval, serverName, resourceType, resourceName);
        Collection<File> files = csvLocationPerFile.keySet();
        for (File file : files) {
            if ((file == null) || (!file.exists())) {
                continue;
                //return new DateAmountDataSet();
            }
            //later to review because the file is already located for a each day from the interval
            //so getting it one more time does not make sense
            int propertyPosition = this.getPropertyPositionInStatsFile(resourceType, resourceName, csvLocationPerFile.get(file), serverName, resourceProperty);
            if (propertyPosition < 0) {
                continue;
            }
            BufferedReader in = null;

            try {
                in = new BufferedReader(new FileReader(file));
                resultDataSet.addDataSet(generatePropertyDataSet(in, interval.getStart().toDate(), interval.getEnd().toDate(), propertyPosition, resourceType, resourceName, resourceProperty));
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                    }
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
    public DateAmountDataSet generatePropertyDataSet(BufferedReader in, Date startDateTime, Date endDateTime, int propertyPosition, String resourceType, String resourceName, String resourceProperty) throws IOException {
        DateAmountDataSet resultDataSet = new DateAmountDataSet(resourceType, resourceName, resourceProperty);
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
                            AppLog.getLogger().debug(StatisticsStorage.class + ".generatePropertyDataSet() skipping corrupt line when getting date-time. Cause: " + e.toString());
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
                            AppLog.getLogger().debug(StatisticsStorage.class + ".generatePropertyDataSet() skipping corrupt line, propertyPosition=" + propertyPosition + ", dateTime=" + dateTime + ". Cause: " + e.toString());
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


    public List<DateAmountDataSet> getPropertiesData(String resourceType, String resourceName, List<String> resourcesProperty, Date endDateTime, int durationMins, String serverName) throws IOException {

        List<DateAmountDataSet> resultDataSet = new ArrayList<DateAmountDataSet>();
        //ex: StorageUtil.getPropertyData(statisticsStorage,"core",null,"HeapUsedCurrent",new Date(),1,"AdminServer");
        Date startDateTime = DateUtil.getEarlierTime(endDateTime, durationMins);
        File file = this.getResourceStatisticsCSV(endDateTime, serverName, resourceType, resourceName);
        if ((file == null) || (!file.exists())) {
            return new ArrayList<DateAmountDataSet>();
        }
        BufferedReader in = null;
        try {

            for (String resourceProperty : resourcesProperty) {
                in = new BufferedReader(new FileReader(file));

                int propertyPosition = this.getPropertyPositionInStatsFile(resourceType, resourceName, endDateTime, serverName, resourceProperty);
                if (propertyPosition < 0) {
                    return new ArrayList<DateAmountDataSet>();
                }
                resultDataSet.add(generatePropertyDataSet(in, startDateTime, endDateTime, propertyPosition, resourceType, resourceName, resourceProperty));
            }
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

    // Constants
    private final static String CSV_SUFFIX = ".csv";
    private final static String PROPS_SUFFIX = ".props";
    private final static String RESOURCE_LIST_FILENAME_SUFFIX = "list";
    private final static String PROP_LIST_CMNT_PREFIX = "List of instances available on the server to monitor for resource type: ";
    private final static Map<String, Object> resourceMonitorObjects = new HashMap<String, Object>();

    // Members
    private final String rootDirectoryPath;
}
