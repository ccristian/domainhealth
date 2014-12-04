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
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.ObjectName;

import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.jmx.WebLogicMBeanPropConstants;
import static domainhealth.core.statistics.MonitorProperties.*; 
import static domainhealth.core.util.DateUtil.*;
import domainhealth.core.util.DateUtil;
import domainhealth.core.util.FileUtil;

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
	 * @param dateTime The datetime indicating which day look for a CSV for 
	 * @param serverName The name of the server to get the property from
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
	 * @param dateTime The datetime indicating which day to look for a CSV for 
	 * @param serverName The name of the server to set the properties for
	 * @param resourceType The type of resource (eg. core, datasource)
	 * @param resourceName The name of the resource
	 * @param header Provider of the header text to add as first row to CSV file (if CSV file is currently empty)
	 * @param contentProvider Provider of the resut row text to append to the CSV 
	 * @throws IOException Indicates problem writing to the CSV file.
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
				try { out.close(); } catch (Exception e) { e.printStackTrace(); }									
			}
		}
	}
	
	/**
	 * Find the earliest recorded time in the Core statistics CSV file, for the 
	 * earliest Core statistics CSV file that has been captured
	 * 
	 * @return The earliest recorded date-time
	 * @throws WebLogicMBeanException Indicates problem accessing Admin Server JMX tree
	 * @throws IOException Indicates problem accessing statistics directories/files
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
	 * @param dateTime The datetime indicating which day look for a CSV for 
	 * @param serverName The name of the server to get the property from
	 * @param property The name of the property to look for
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
							positionCount ++;				
						}
					} else {
						currentProperty.append(character);
					}
				}
			}
		} finally {
			if (in != null) {
				try { in.close(); } catch (Exception e) { e.printStackTrace(); }									
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
	 * @param dateTime The datetime indicating which day to look for a props file for 
	 * @param resourceType The type of resource (eg. core, datasource)
	 * @return The list of mbean instances names
	 * @throws FileNotFoundException Indicates file containing name list could not be found
	 * @throws IOException Indicates file containing name list could not be read 
	 */
	public Properties retrieveOneDayResoureNameList(Date dateTime, String resourceType) throws IOException {
		Properties propList = new Properties();

		// Lock per resource type enabling threads reading/writing from/to 
		// different resource files to still work in parallel
		synchronized(getResourceMonitorObject(resourceType)) {			
			InputStream propsIn = null;

			try {
				File file = FileUtil.retrieveFile(getDayResourcePropListFilePath(dateTime, resourceType));
				
				if (file != null) {
					propsIn = new FileInputStream(file);
					propList.load(propsIn);
				}
			} finally {
				if (propsIn != null) {
					try { propsIn.close(); } catch (Exception e) { e.printStackTrace();	}
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
	 * @param dateTime The date time indicating which time day to save the file for 
	 * @param resourceType The type of resource (eg. core, datasource)
	 * @param extraPropList List of new property names to add
	 * @throws IOException Indicates that list could not be saved to file
	 */
	public void appendSavedOneDayResourceNameList(Date dateTime, String resourceType, Properties extraPropList) throws IOException {
		// Lock per resource type enabling threads reading/writing from/to 
		// different resource files to still work in parallel
		synchronized(getResourceMonitorObject(resourceType)) {			
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
					try { propsOut.close(); } catch (Exception e) { e.printStackTrace();	}
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
	 * @param dateTime The datetime indicating which day to look for a props filefor 
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
	 * @param dateTime The datetime indicating which day to look for a CSV directory for 
	 * @param serverName The name of the server to get the statistics directory for
	 * @param resourceType The type of resource (eg. core, datasource)
	 * @return The path of the directory containing the CSV files
	 */
	private String getDayServerResourceDirectoryPath(Date dateTime,	String serverName, String resourceType) {
		String parentDirPath = getDayDirectoryPath(dateTime) + separatorChar + serverName;
		return String.format("%s%s%s", parentDirPath, separatorChar, resourceType);
	}

	/**
	 * Get the path of CSV statistics file for a given resource for a given 
	 * resource type for a given server on a given day.
	 * 
	 * @param dateTime The datetime indicating which day to look for a CSV for 
	 * @param serverName The name of the server to get the CSV file path for
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
	 * Get the property list of instances of a resource type for a given day.
	 * 
	 * @param dateTime The datetime indicating which day look for a CSV for 
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
		} catch(ParseException pe) {
				AppLog.getLogger().error(pe.toString());
		} finally {
			if (in != null) {
				try { in.close(); } catch (Exception e) { e.printStackTrace(); }									
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

	// Constants
	private final static String CSV_SUFFIX = ".csv";
	private final static String PROPS_SUFFIX = ".props";
	private final static String RESOURCE_LIST_FILENAME_SUFFIX = "list";
	private final static String PROP_LIST_CMNT_PREFIX = "List of intances available on the server to monitor for resource type: ";
	private final static Map<String, Object> resourceMonitorObjects = new HashMap<String, Object>();

	// Members
	private final String rootDirectoryPath;
}
