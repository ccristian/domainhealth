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
package domainhealth.frontend.display;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.*;

import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties.PropKey;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;
import static domainhealth.core.util.DateUtil.*;
import static domainhealth.core.statistics.MonitorProperties.*;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.DateUtil;
import static domainhealth.frontend.display.GraphScopedAttributeUtils.*;
import static domainhealth.frontend.display.HttpServletUtils.*;

/**
 * The main Model-View-Controller (MVC) Controller servlet which establishes 
 * the date-time window to show statistics for, collects some important data 
 * from the Model into servlet request attributes, for future retrieval by 
 * View JSPs and then forwards to the required View JSP to show the results 
 * page (eg. core stats page, data-source page. destination page). The View 
 * JSPs generate the actual the HTML result page which in turn containing 
 * links to the Graph image URLs where the actual graphs are each generated 
 * by the LineChartImageGeneratorServlet. 
 */
public class ShowPageControllerServlet extends HttpServlet {
	/**
	 * Servlet initialiser which establishes the root path of the collected 
	 * statistics directories 
	 * 
	 * @throws ServletException Indicates that the root statistics path could not be established
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {
		statisticsStorage = new StatisticsStorage((String) getServletContext().getAttribute(PropKey.STATS_OUTPUT_PATH_PROP.toString()));
	}

	/**
	 * Forwards request to the process() method for main processing 
	 * 
	 * @param request The HTTP Servlet request
	 * @param response The HTTP Servlet response
	 * @throws ServletException Indicates a problem processing the HTTP servlet request
	 * @throws IOException Indicates a problem processing the HTTP servlet request
	 *
	 * @see domainhealth.display.GraphicalStatsServlet#processs(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processs(request, response);
	}

	/**
	 * Forwards request to the process() method for main processing 
	 * 
	 * @param request The HTTP Servlet request
	 * @param response The HTTP Servlet response
	 * @throws ServletException Indicates a problem processing the HTTP servlet request
	 * @throws IOException Indicates a problem processing the HTTP servlet request
	 *
	 * @see domainhealth.display.GraphicalStatsServlet#processs(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processs(request, response);		
	}

	/**
	 * Processes the date-time, duration and direction parameters, establishes
	 * the date-time window to show statistics for and determines which view 
	 * (JSP) to forward to. The resulting JSP is depends on whether core, 
	 * data-source or destination statistics or other are to be displayed.
	 * 
	 * @param request The HTTP Servlet request
	 * @param response The HTTP Servlet response
	 * @throws ServletException Indicates a problem processing the HTTP servlet request
	 * @throws IOException Indicates a problem processing the HTTP servlet request
	 */
	private void processs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DomainRuntimeServiceMBeanConnection conn = null;

		try {
			String resourceType = getResourceType(HttpServletUtils.getServletMapping(request));

			if (resourceType == null) {
				response.sendError(SC_NOT_FOUND, "Main DomainHealth controller servlet does not recognise the resource url: " + request.getServletPath());				
			} else {
				Date dateTime = getDateTimeParam(request);			
				int durationMins = getDurationParam(request);
				String direction = getDirectionParam(request);
				Date newDateTime = determineNewTimeWindowEndDate(dateTime, direction, durationMins);
				conn = new DomainRuntimeServiceMBeanConnection();
				setGeneralRequestAttributes(request, conn, durationMins, newDateTime, resourceType);
				setResourceRequestAttributes(request, conn, newDateTime, resourceType);
				setOkHTTPResponseHeaders(response);
				request.getRequestDispatcher(MAIN_DISPLAY_JSP_VIEW_URL_PATH).forward(request, response);
			}
		} catch (ParseException pe) {
			throw new ServletException(pe);
		} catch (WebLogicMBeanException we) {
			throw new ServletException(we);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	/**
	 * Set standard HTTP headers and no caching directives when an OK HTTP 
	 * response needs to be sent.
	 * 
	 * @param response The HTTP Servlet response
	 */
	private void setOkHTTPResponseHeaders(HttpServletResponse response) {
		response.setContentType(HTML_CONTENT_TYPE);
		response.setStatus(SC_OK);
		response.setHeader(PRAGMA_HEADER, NO_CACHE_HEADER_VALUE);
		response.setDateHeader(EXPIRES_HEADER, PAST_DATE_HEADER_VALUE); 
		response.setHeader(CACHE_CONTROL_HEADER, NO_CACHE_HEADER_VALUE);
	}
	
	/**
	 * Set the general set of servlet request attributes to appropriate values
	 * ready for use by tags in the result view JSPs. 
	 * 
	 * @param request The HTTP Servlet request
	 * @param conn JMX connection to domain runtime
	 * @param durationMins The amount of minutes to show data for
	 * @param endDateTime The end date-time for the window of time to show data for
	 * @param resourceType The type of resource to show stats for (eg. core, datasource)
	 * @throws ParseException Indicates a problem calculate new end time
	 * @throws IOException Indicates a problem calculate new end time
	 * @throws WebLogicMBeanException Indicates a problem calculate new end time
	 */
	private void setGeneralRequestAttributes(HttpServletRequest request, DomainRuntimeServiceMBeanConnection conn, int durationMins, Date endDateTime, String resourceType) throws ParseException, IOException, WebLogicMBeanException {
		DateFormat secondDateFormat = new SimpleDateFormat(DATETIME_PARAM_FORMAT);
		DateFormat displayDateFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT);			
		DateFormat displayDayFormat = new SimpleDateFormat(DISPLAY_DAY_FORMAT);			
		DateFormat displayTimeFormat = new SimpleDateFormat(DISPLAY_TIME_FORMAT);			
		request.setAttribute(DATETIME_PARAM, secondDateFormat.format(endDateTime));
		request.setAttribute(DURATION_MINS_PARAM, new Integer(durationMins));		
		request.setAttribute(DISPLAY_DATE_PARAM, displayDateFormat.format(endDateTime));
		request.setAttribute(DISPLAY_DAY_PARAM, displayDayFormat.format(endDateTime));
		request.setAttribute(DISPLAY_TIME_PARAM, displayTimeFormat.format(endDateTime));
		request.setAttribute(SCOPE_PARAM, getScopeParam(request));		
		request.setAttribute(ALL_SERVER_NAMES_PARAM, getAllPossibleServerNames(conn));
		request.setAttribute(CONTEXT_URL_PARAM, request.getContextPath());
		request.setAttribute(RESOURCE_TYPE_PARAM, resourceType);					
	}

	/**
	 * Set the resource specific (eg. datasource, destination, etc) set of 
	 * servlet request attributes to appropriate values ready for use by tags 
	 * in the specific result view JSP being targetted. 
	 * 
	 * @param request The HTTP Servlet request
	 * @param conn JMX connection to domain runtime
	 * @param endDateTime The end date-time for the window of time to show data for
	 * @param resourceType The type of resource to show stats for (eg. core, datasource)
	 * @throws ServletException Indicates a problem calculate new end time
	 * @throws IOException Indicates a problem calculate new end time
	 * @throws WebLogicMBeanException Indicates a problem calculate new end time
	 */
	private void setResourceRequestAttributes(HttpServletRequest request, DomainRuntimeServiceMBeanConnection conn, Date endDateTime, String resourceType) throws ServletException, IOException, WebLogicMBeanException {	
		if (resourceType.equals(CORE_RESOURCE_TYPE)) {
			request.setAttribute(RESOURCE_NAME_PARAM, resourceType);		
			request.setAttribute(PAGE_URL_PARAM, request.getContextPath() + URL_PATH_SEPERATOR + resourceType + STATS_SERVLET_MAPPING_SUFFIX);
			request.setAttribute(PAGE_TITLE, CORE_PAGE_TITLE);
			request.setAttribute(MENU_TITLE, CORE_MENU_TITLE);	
		} else if (resourceType.equals(HOSTMACHINE_RESOURCE_TYPE)) {
			request.setAttribute(RESOURCE_NAME_PARAM, resourceType);		
			request.setAttribute(PAGE_URL_PARAM, request.getContextPath() + URL_PATH_SEPERATOR + resourceType + STATS_SERVLET_MAPPING_SUFFIX);
			request.setAttribute(PAGE_TITLE, HOSTMACHINE_PAGE_TITLE);
			request.setAttribute(MENU_TITLE, HOSTMACHINE_MENU_TITLE);											
		} else {
			String resourceName = HttpServletUtils.getEndPathFromURL(request);
			request.setAttribute(RESOURCE_NAME_PARAM, resourceName);
			String pageURL = request.getContextPath() + URL_PATH_SEPERATOR + resourceType + STATS_SERVLET_MAPPING_SUFFIX;
			
			if (resourceName != null) {
				pageURL += URL_PATH_SEPERATOR + resourceName;
			}

			request.setAttribute(PAGE_URL_PARAM, pageURL);

			if (resourceType.equals(DATASOURCE_RESOURCE_TYPE)) {
				request.setAttribute(PAGE_TITLE, (resourceName == null) ? DATASOURCES_MENU_TITLE : resourceName + DATASOURCE_PAGE_TITLE);
				request.setAttribute(MENU_TITLE, DATASOURCES_MENU_TITLE);				
				request.setAttribute(RESOURCES_LIST_PARAM,  statisticsStorage.getResourceNamesFromPropsList(endDateTime, DATASOURCE_RESOURCE_TYPE));			
			} else if (resourceType.equals(DESTINATION_RESOURCE_TYPE)) {
				request.setAttribute(PAGE_TITLE, (resourceName == null) ? DESTINATIONS_MENU_TITLE : resourceName + DESTINATION_PAGE_TITLE);
				request.setAttribute(MENU_TITLE, DESTINATIONS_MENU_TITLE);				
				request.setAttribute(RESOURCES_LIST_PARAM,  statisticsStorage.getResourceNamesFromPropsList(endDateTime, DESTINATION_RESOURCE_TYPE));			
			} else if (resourceType.equals(WEBAPP_RESOURCE_TYPE)) {
				request.setAttribute(PAGE_TITLE, (resourceName == null) ? WEBAPPS_MENU_TITLE : resourceName + WEBAPP_PAGE_TITLE);
				request.setAttribute(MENU_TITLE, WEBAPPS_MENU_TITLE);				
				request.setAttribute(RESOURCES_LIST_PARAM,  statisticsStorage.getResourceNamesFromPropsList(endDateTime, WEBAPP_RESOURCE_TYPE));			
			} else if (resourceType.equals(EJB_RESOURCE_TYPE)) {
				request.setAttribute(PAGE_TITLE, (resourceName == null) ? EJBS_MENU_TITLE : resourceName + EJB_PAGE_TITLE);
				request.setAttribute(MENU_TITLE, EJBS_MENU_TITLE);				
				request.setAttribute(RESOURCES_LIST_PARAM,  statisticsStorage.getResourceNamesFromPropsList(endDateTime, EJB_RESOURCE_TYPE));						
			} else if (resourceType.equals(WORKMGR_RESOURCE_TYPE)) {
				request.setAttribute(PAGE_TITLE, (resourceName == null) ? WORKMGRS_MENU_TITLE : resourceName + WORKMGR_PAGE_TITLE);
				request.setAttribute(MENU_TITLE, WORKMGRS_MENU_TITLE);				
				request.setAttribute(RESOURCES_LIST_PARAM,  statisticsStorage.getResourceNamesFromPropsList(endDateTime, WORKMGR_RESOURCE_TYPE));			
			} else if (resourceType.equals(SVRCHNL_RESOURCE_TYPE)) {
				request.setAttribute(PAGE_TITLE, (resourceName == null) ? SVRCHNLS_MENU_TITLE : resourceName + SVRCHNL_PAGE_TITLE);
				request.setAttribute(MENU_TITLE, SVRCHNLS_MENU_TITLE);				
				request.setAttribute(RESOURCES_LIST_PARAM, statisticsStorage.getResourceNamesFromPropsList(endDateTime, SVRCHNL_RESOURCE_TYPE));			
			} else {
				throw new ServletException("Unable to map servlet path '" + request.getServletPath() + "' to a known resource type to show statistics for");
			}			
		}

		// Indicate to only show Hosts top menu link if any data has been retrieved from WLHostMachineStats custom mbeans
		Set<String> hostmachinesSet = statisticsStorage.getResourceNamesFromPropsList(endDateTime, HOSTMACHINE_RESOURCE_TYPE);
		request.setAttribute(SHOW_HOSTS_PARAM, ((hostmachinesSet != null) && (!hostmachinesSet.isEmpty())));							
	}

	/**
	 * Derive the resource type name from the servlet mapping specified in 
	 * the servlet path.
	 * 
	 * @param servletMapping The servlet-mapping from the servlet-path
	 * @return The resource type name (eg. datasource)
	 * @throws ServletException Indicates a problem occurred trying to determine the resource type
	 */
	private String getResourceType(String servletMapping) throws ServletException {
		String resourceType = null;
		int resourceTypeEndPos = servletMapping.indexOf(STATS_SERVLET_MAPPING_SUFFIX);
	
		if (resourceTypeEndPos > 0) {
			resourceType = servletMapping.substring(0, resourceTypeEndPos);
			
			if (!LEGAL_RESOURCE_TYPES.contains(resourceType)) {
				resourceType = null;
			}
		} 

		return resourceType;
	}
	
	/**
	 * Return the set of name of all servers currently configured for the 
	 * WebLogic domain.
	 * 
	 * @param conn JMX connection to domain runtime
	 * @return The names of all servers in the doamin
	 * @throws WebLogicMBeanException Indicates a problem reading the domain's configuration settings.
	 */
	private Set<String> getAllPossibleServerNames(DomainRuntimeServiceMBeanConnection conn) throws WebLogicMBeanException {
		Set<String> serverNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		ObjectName[] servers = conn.getChildren(conn.getDomainConfiguration(), SERVERS);			

		for (ObjectName server : servers) {
			serverNames.add(conn.getTextAttr(server, NAME));
		}
				
		return serverNames;
	}
	
	/**
	 * Given a date-time, duration and direction, establishes the required end-
	 * date for the date-time window of data which is required to be retrieved
	 * and shown from the recorded statistics. Note: If the end time is less 
	 * than the duration minutes ('durationMins') from the start of the 
	 * selected day, then the end time is increased to the maximum duration 
	 * minutes in that day.
	 * 
	 * @param currentEndDateTime The current date-time
	 * @param direction The direction to get the next date-time for
	 * @param durationMins The amount of minutes time window to retreive
	 * @return The end date of the new date-time windows to show statistics for
	 * @throws ParseException Indicates a problem calculating the new end date-time
	 * @throws IOException  Indicates a problem calculating the new end date-time
	 * @throws WebLogicMBeanException Indicates a problem calculating the new end date-time
	 */
	private Date determineNewTimeWindowEndDate(Date currentEndDateTime, String direction, int durationMins) throws ParseException, IOException, WebLogicMBeanException {
		Date endDate = null;
		AppLog.getLogger().debug(" -- "); 
		AppLog.getLogger().debug("TIME-CALC input: currentDateTime=" + currentEndDateTime + ", direction=" + direction + ", durationMins=" + durationMins); 
	
		if (direction.equals(CURRENT_DIRECTION_VAL)) {
			endDate = currentEndDateTime;			
		} else if (direction.equals(FIRST_DATE_DIRECTION_VAL)) {
			Date earliestDate = statisticsStorage.getEarliestRecordedDateTime();
			
			if (earliestDate != null) {
				endDate = DateUtil.getLaterTime(earliestDate, durationMins);
			} else {
				AppLog.getLogger().warning("Search for earliest date CSV file returned null; using current date time");
				endDate = currentEndDateTime;
			}
		} else if (direction.equals(LAST_DATE_DIRECTION_VAL)) {
			endDate = new Date();
		} else if (direction.equals(NEXT_DATE_DIRECTION_VAL)) {
			endDate = DateUtil.getNextDay(currentEndDateTime);
		} else if (direction.equals(PREVIOUS_DATE_DIRECTION_VAL)) {
			endDate = DateUtil.getPreviousDay(currentEndDateTime);		
			Date earliestDate = statisticsStorage.getEarliestRecordedDateTime();
			
			if (earliestDate != null) {
				Date minDate = DateUtil.getLaterTime(earliestDate, durationMins);
				
				if (endDate.compareTo(minDate) < 0) {
					endDate = minDate;
				}
			} else {
				AppLog.getLogger().warning("Search for earliest date CSV file returned null when calculating previous day; using current date time");
				endDate = currentEndDateTime;
			}
		} else if (direction.equals(FIRST_TIME_DIRECTION_VAL)) {
			endDate = DateUtil.getLaterTime(DateUtil.getStartTimeOfDay(currentEndDateTime), durationMins);
			Date earliestDate = statisticsStorage.getEarliestRecordedDateTime();
			
			if (earliestDate != null) {
				Date minDate = DateUtil.getLaterTime(earliestDate, durationMins);

				if (endDate.compareTo(minDate) < 0) {
					endDate = minDate;
				} 
			}
		} else if (direction.equals(LAST_TIME_DIRECTION_VAL)) {
			endDate = DateUtil.getEndTimeOfDayOrNow(currentEndDateTime);
		} else if (direction.equals(NEXT_TIME_DIRECTION_VAL)) {
			endDate = DateUtil.getLaterTime(currentEndDateTime, durationMins);
		} else if (direction.equals(PREVIOUS_TIME_DIRECTION_VAL)) {
			endDate = DateUtil.getEarlierTime(currentEndDateTime, durationMins);
			Date minDate = DateUtil.getLaterTime(DateUtil.getStartTimeOfDay(currentEndDateTime), durationMins);
			
			if (endDate.compareTo(minDate) < 0) {
				endDate = minDate;
			}

			Date earliestDate = statisticsStorage.getEarliestRecordedDateTime();
			
			if (earliestDate != null) {
				Date newMinDate = DateUtil.getLaterTime(earliestDate, durationMins);
				
				if (endDate.compareTo(newMinDate) < 0) {
					endDate = newMinDate;
				} 
			}			
		} else {
			throw new IllegalArgumentException("Illegal " + DIRECTION_PARAM + " parameter value: " + direction);
		}

		Date maxDate = new Date();
		
		if (endDate.compareTo(maxDate) > 0) {
			endDate = maxDate;
		}			

		AppLog.getLogger().debug("Display Direction Calculator: End date before checking full duration: " + endDate); 
		endDate = DateUtil.getFullDurationTime(endDate, durationMins); 
		AppLog.getLogger().debug("Display Direction Calculator: End date after checking full duration: " + endDate); 
		return endDate;
	}

	// Members
	private StatisticsStorage statisticsStorage = null;
	
	// Constants
	private final static long serialVersionUID = 1L;
	private final static String MAIN_DISPLAY_JSP_VIEW_URL_PATH = "/WEB-INF/jsp/maindisplay.jsp";
	private final static String STATS_SERVLET_MAPPING_SUFFIX = "stats";
	private final static String CORE_PAGE_TITLE = "Core";
	private final static String CORE_MENU_TITLE = "Core";	
	private final static String DATASOURCE_PAGE_TITLE = " data source";
	private final static String DATASOURCES_MENU_TITLE = "Data Sources";	
	private final static String DESTINATION_PAGE_TITLE = " destination";
	private final static String DESTINATIONS_MENU_TITLE = "Destinations";	
	private final static String WEBAPP_PAGE_TITLE = " web-app";
	private final static String WEBAPPS_MENU_TITLE = "Web-Apps";	
	private final static String EJB_PAGE_TITLE = " ejb";
	private final static String EJBS_MENU_TITLE = "EJBs";	
	private final static String WORKMGR_PAGE_TITLE = " work manager";
	private final static String WORKMGRS_MENU_TITLE = "Work Managers";	
	private final static String SVRCHNL_PAGE_TITLE = " server channel";
	private final static String SVRCHNLS_MENU_TITLE = "Server Channels";	
	private final static String HOSTMACHINE_PAGE_TITLE = "Host Machine";
	private final static String HOSTMACHINE_MENU_TITLE = "Host Machine";	
}
