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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.*;
import domainhealth.backend.retriever.DataRetrievalException;
import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.env.AppProperties.PropKey;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.jmx.WebLogicMBeanPropConstants;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;
import static domainhealth.core.util.DateUtil.*;
import static domainhealth.core.statistics.MonitorProperties.*;
import domainhealth.core.statistics.ResourceNameNormaliser;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.core.util.DateUtil;
import domainhealth.frontend.data.JMSServerSummaryData;
import domainhealth.frontend.data.SAFAgentSummaryData;
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
		
		// Added by gregoan
		AppProperties appProps = new AppProperties(getServletContext());
		componentBlacklist = tokenizeBlacklistText(appProps.getProperty(PropKey.COMPONENT_BLACKLIST_PROP));
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
			
		// **************************************************************
		// Added by gregoan
		} else if (resourceType.equals(JMSSVR_RESOURCE_TYPE)) {
			
			request.setAttribute(PAGE_URL_PARAM, request.getContextPath() + URL_PATH_SEPERATOR + resourceType + DASHBOARD_SERVLET_MAPPING_SUFFIX);
			
			request.setAttribute(PAGE_TITLE, JMSSRV_DASHBOARD_PAGE_TITLE);
			request.setAttribute(MENU_TITLE, JMSSRV_DASHBOARD_MENU_TITLE);				
			request.setAttribute(RESOURCES_LIST_PARAM, getJMSServersList(conn));
			
			String resourceName = HttpServletUtils.getEndPathFromURL(request);
			
			// Resource is selected so dashboard should be generated
			if (resourceName != null)
			{
				request.setAttribute(RESOURCE_NAME_PARAM, resourceName);
					
				Set<JMSServerSummaryData> jmsServerSummaryData = getJMSServerDashboard(conn, resourceName);
					
				// Put the dashboard in the HttpRequest
				request.setAttribute("jmsServerSummary", jmsServerSummaryData);
			}
			else
			{
				// RESOURCE_NAME_PARAM shouldn't be set otherwise "Select resource from left menu" will not be printed (see maindisplay.jsp)
				//request.setAttribute(RESOURCE_NAME_PARAM, resourceType);
			}

		} else if (resourceType.equals(SAFAGENT_RESOURCE_TYPE)) {
			
			request.setAttribute(PAGE_URL_PARAM, request.getContextPath() + URL_PATH_SEPERATOR + resourceType + DASHBOARD_SERVLET_MAPPING_SUFFIX);
			
			request.setAttribute(PAGE_TITLE, SAFAGENT_DASHBOARD_PAGE_TITLE);
			request.setAttribute(MENU_TITLE, SAFAGENT_DASHBOARD_MENU_TITLE);
			request.setAttribute(RESOURCES_LIST_PARAM, getSAFAgentList(conn));
			
			String resourceName = HttpServletUtils.getEndPathFromURL(request);
			
			// Resource is selected so dashboard should be generated
			if (resourceName != null)
			{
				request.setAttribute(RESOURCE_NAME_PARAM, resourceName);
					
				Set<SAFAgentSummaryData> safAgentSummaryData = getSAFAgentDashboard(conn, resourceName);

				// Put the dashboard in the HttpRequest
				request.setAttribute("safAgentSummary", safAgentSummaryData);
			}
			else
			{
				// RESOURCE_NAME_PARAM shouldn't be set otherwise "Select resource from left menu" will not be printed (see maindisplay.jsp)
				//request.setAttribute(RESOURCE_NAME_PARAM, resourceType);
			}
		// **************************************************************
			
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
			} else if (resourceType.equals(SAF_RESOURCE_TYPE)) {
				request.setAttribute(PAGE_TITLE, (resourceName == null) ? SAF_MENU_TITLE : resourceName + SAF_PAGE_TITLE);
				request.setAttribute(MENU_TITLE, SAF_MENU_TITLE);				
				request.setAttribute(RESOURCES_LIST_PARAM,  statisticsStorage.getResourceNamesFromPropsList(endDateTime, SAF_RESOURCE_TYPE));			
			}else if (resourceType.equals(WEBAPP_RESOURCE_TYPE)) {
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
		
		// Added by gregoan the 03/06/2014
		// JMSDashboard is a table without statistics on metrics so we need to check if there is a MBean or not
		// @TODO :
		//    - Should check if MBean is available or not -> see StatisticCapturerJMXPoll::logHostMachineStats()
		//    - Should use the web.xml to know if dashboard should be generated
		request.setAttribute(SHOW_JMSSRV_DASHBOARDS_PARAM, true);
		request.setAttribute(SHOW_SAFAGENT_DASHBOARDS_PARAM, true);
		
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
		
		// Added by gregoan
		// If < 0, it's maybe a dashboard type
		if(resourceTypeEndPos < 0)	resourceTypeEndPos = servletMapping.indexOf(DASHBOARD_SERVLET_MAPPING_SUFFIX);
				
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
	 * @return The names of all servers in the domain
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
	private final static String SAF_MENU_TITLE = "Saf";
	private final static String SAF_PAGE_TITLE = " saf";
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
	
	// *************************************************************************
	// Added by gregoan
	private final static String DASHBOARD_SERVLET_MAPPING_SUFFIX = "dashboard";
	
	private final static String JMSSRV_DASHBOARD_PAGE_TITLE = "JMS Dashboard";
	private final static String JMSSRV_DASHBOARD_MENU_TITLE = "JMS Dashboard";
	
	private final static String SAFAGENT_DASHBOARD_PAGE_TITLE = "SAF Dashboard";
	private final static String SAFAGENT_DASHBOARD_MENU_TITLE = "SAF Dashboard";
	
	/**
	 * 
	 * @param conn
	 * @return
	 */
	public Set<String> getJMSServersList(DomainRuntimeServiceMBeanConnection conn){
		
		Set<String> jmsSet = new HashSet<String>();
		
		try{
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();			
			
			//Find the Admin server
			for (int index = 0; index < serverRuntimes.length; index++){
				if(DomainRuntimeServiceMBeanConnection.isThisTheAdminServer()){
					
					ObjectName serverRuntime = serverRuntimes[index];
					String serverName = "";
					
					try {
			        	serverName = conn.getTextAttr(serverRuntime, NAME);			        	
			        	ObjectName jmsRuntime = conn.getChild(serverRuntime, JMS_RUNTIME);
			            ObjectName[] jmsServers = conn.getChildren(jmsRuntime, JMS_SERVERS);
			            
			            for (ObjectName jmsServer : jmsServers)
			            {
			            	//String currentJmsServerName = conn.getTextAttr(jmsServer, NAME);
			            	String currentJmsServerName = ResourceNameNormaliser.normalise(JMSSVR_RESOURCE_TYPE, conn.getTextAttr(jmsServer, NAME));

							if (!getComponentBlacklist().contains(currentJmsServerName)) {
								jmsSet.add(currentJmsServerName);
							}			            	
			            	//jmsSet.add(currentJmsServerName);
			            }
			        
			        } catch (Exception e) {
						throw new DataRetrievalException("Problem getting " + JMSSVR_RESOURCE_TYPE + " resources for server " + serverName, e);
					}
				}
			}
		
		} catch(Exception ex){
			System.out.println("ShowPageControllerServlet::getJMSServersList() - Error to get the JMS servers list");
		}
		return jmsSet;
	}
	
	/**
	 * 
	 * @param conn
	 * @return
	 */
	public Set<String> getSAFAgentList(DomainRuntimeServiceMBeanConnection conn){
		
		Set<String> safSet = new HashSet<String>();
		
		try{
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();			
			
			//Find the Admin server
			for (int index = 0; index < serverRuntimes.length; index++){
				if(DomainRuntimeServiceMBeanConnection.isThisTheAdminServer()){
					
					ObjectName serverRuntime = serverRuntimes[index];
					String serverName = "";
					
					try {
			        	serverName = conn.getTextAttr(serverRuntime, NAME);
			        	ObjectName safRuntime = conn.getChild(serverRuntime, SAF_RUNTIME);
			            ObjectName[] safAgents = conn.getChildren(safRuntime, AGENTS);
			            
			            for (ObjectName safAgent : safAgents)
			            {
			            	//String currentSafAgentName = conn.getTextAttr(safAgent, NAME);		            	
			            	String currentSafAgentName = ResourceNameNormaliser.normalise(SAFAGENT_RESOURCE_TYPE, conn.getTextAttr(safAgent, NAME));
			            	
			            	if (!getComponentBlacklist().contains(currentSafAgentName)) {
			            		safSet.add(currentSafAgentName);
							}
			            	//safSet.add(currentSafAgentName);
			            }
			        
			        } catch (Exception e) {
						throw new DataRetrievalException("Problem getting " + SAFAGENT_RESOURCE_TYPE + " resources for server " + serverName, e);
					}
				}
			}
		
		} catch(Exception ex){
			System.out.println("ShowPageControllerServlet::getSAFAgentList() - Error to get the SAF agent list");
		}
		return safSet;
	}
	
	/**
	 * 
	 * @param conn
	 * @return
	 */
	public Set<JMSServerSummaryData> getJMSServerDashboard(DomainRuntimeServiceMBeanConnection conn, String jmsServerName){
		
		Set<JMSServerSummaryData> jmsServerSummary = new TreeSet<JMSServerSummaryData>(new JMSServerSummaryData());
		
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			//Find the Admin server
			for (int index = 0; index < serverRuntimes.length; index++){
				if(DomainRuntimeServiceMBeanConnection.isThisTheAdminServer()){
			
					ObjectName serverRuntime = serverRuntimes[index]; 
			    	ObjectName jmsRuntime = conn.getChild(serverRuntime, JMS_RUNTIME);			    	
			        ObjectName[] jmsServers = conn.getChildren(jmsRuntime, JMS_SERVERS);
			        
			        for (ObjectName jmsServer : jmsServers)
			        {
			        	String currentJmsServerName = conn.getTextAttr(jmsServer, NAME);
			        	
			        	if(currentJmsServerName.equals(jmsServerName)){
			        		
			        		for (ObjectName destination : conn.getChildren(jmsServer, DESTINATIONS)) {

//System.out.println("");
			        			JMSServerSummaryData jmsSummaryData = new JMSServerSummaryData();
						    	
			        			//String destinationName = getRealDestinationName(conn.getTextAttr(destination, NAME));
			        			String destinationName = ResourceNameNormaliser.normalise(JMSSVR_RESOURCE_TYPE, conn.getTextAttr(destination, NAME));
			        			
//System.out.println("ShowPageControllerServlet::getJMSServerDashboard() - destinationName = " + conn.getTextAttr(destination, NAME));
//System.out.println("ShowPageControllerServlet::getJMSServerDashboard() - destinationName = " + destinationName);
			        									    	
								jmsSummaryData.setDestinationName(destinationName);
								jmsSummaryData.setMessagesCurrentCount(new Long((int)conn.getNumberAttr(destination, MESSAGES_CURRENT_COUNT)).toString());
								jmsSummaryData.setMessagesPendingCount(new Long((int)conn.getNumberAttr(destination, MESSAGES_PENDING_COUNT)).toString());
								jmsSummaryData.setMessagesReceivedCount(new Long((int)conn.getNumberAttr(destination, MESSAGES_RECEIVED_COUNT)).toString());
								jmsSummaryData.setMessagesHighCount(new Long((int)conn.getNumberAttr(destination, MESSAGES_HIGH_COUNT)).toString());
								jmsSummaryData.setConsumersCurrentCount(new Long((int)conn.getNumberAttr(destination, CONSUMERS_CURRENT_COUNT)).toString());
								jmsSummaryData.setConsumersHighCount(new Long((int)conn.getNumberAttr(destination, CONSUMERS_HIGH_COUNT)).toString());
								jmsSummaryData.setConsumersTotalCount(new Long((int)conn.getNumberAttr(destination, CONSUMERS_TOTAL_COUNT)).toString());
								
								// Add the information for the JMS server
								jmsServerSummary.add(jmsSummaryData);
			        		}
			        	}			    
			        }
				}
			}
		} catch(Exception ex){
			System.out.println("ShowPageControllerServlet::getJMSServerDashboard() - Error to genarate the JMS dashboard");
		}
		return jmsServerSummary;
	}
	
	/**
	 * 
	 * @param conn
	 * @return
	 */
	public Set<SAFAgentSummaryData> getSAFAgentDashboard(DomainRuntimeServiceMBeanConnection conn, String safAgentName){
		
		Set<SAFAgentSummaryData> safAgentSummary = new TreeSet<SAFAgentSummaryData>(new SAFAgentSummaryData());
		
		try {
			
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();
			
			//Find the Admin server
			for (int index = 0; index < serverRuntimes.length; index++){
				if(DomainRuntimeServiceMBeanConnection.isThisTheAdminServer()){
			
					ObjectName serverRuntime = serverRuntimes[index]; 
			    	ObjectName safRuntime = conn.getChild(serverRuntime, SAF_RUNTIME);			    	
			        ObjectName[] safAgents = conn.getChildren(safRuntime, AGENTS);
			        
			        for (ObjectName safAgent : safAgents)
			        {
			        	String currentSafAgentName = conn.getTextAttr(safAgent, NAME);
			        	
			        	if(currentSafAgentName.equals(safAgentName)){
			        		
//System.out.println("");	
			        		for (ObjectName destination : conn.getChildren(safAgent, REMOTE_END_POINTS)) {
			        			
			        			SAFAgentSummaryData safAgentSummaryData = new SAFAgentSummaryData();
			        			
						    	//String destinationName = getRealDestinationName(conn.getTextAttr(destination, NAME));
						    	String destinationName = ResourceNameNormaliser.normalise(SAFAGENT_RESOURCE_TYPE, conn.getTextAttr(destination, NAME));
						    	
//System.out.println("ShowPageControllerServlet::getSAFAgentDashboard() - destinationName = " + conn.getTextAttr(destination, NAME));
//System.out.println("ShowPageControllerServlet::getSAFAgentDashboard() - destinationName = " + destinationName);
						    							    	
						    	safAgentSummaryData.setDestinationName(destinationName);
						    	safAgentSummaryData.setMessagesCurrentCount(new Long((int)conn.getNumberAttr(destination, MESSAGES_CURRENT_COUNT)).toString());
						    	safAgentSummaryData.setMessagesPendingCount(new Long((int)conn.getNumberAttr(destination, MESSAGES_PENDING_COUNT)).toString());
								safAgentSummaryData.setMessagesReceivedCount(new Long((int)conn.getNumberAttr(destination, MESSAGES_RECEIVED_COUNT)).toString());
								safAgentSummaryData.setMessagesHighCount(new Long((int)conn.getNumberAttr(destination, MESSAGES_HIGH_COUNT)).toString());
								
								safAgentSummaryData.setDowntimeHigh(new Long((int)conn.getNumberAttr(destination, DOWNTIME_HIGH)).toString());
								safAgentSummaryData.setDowntimeTotal(new Long((int)conn.getNumberAttr(destination, DOWNTIME_TOTAL)).toString());
								safAgentSummaryData.setUptimeHigh(new Long((int)conn.getNumberAttr(destination, UPTIME_HIGH)).toString());
								safAgentSummaryData.setUptimeTotal(new Long((int)conn.getNumberAttr(destination, UPTIME_TOTAL)).toString());
								
								safAgentSummaryData.setFailedMessagesTotal(new Long((int)conn.getNumberAttr(destination, FAILED_MESSAGES_TOTAL)).toString());
								
								// Add the information for the SAF agent
								safAgentSummary.add(safAgentSummaryData);
			        		}
			        	}			    
			        }
				}
			}
		} catch(Exception ex){
			System.out.println("ShowPageControllerServlet::getJMSServerDashboard() - Error to genarate the JMS dashboard");
		}
		return safAgentSummary;
	}
    
    /**
	 * Resolves the real queue or topic name from any module and/or server 
	 * name which may be included in name of the destinaton on s specific 
	 * server. For example, resolve "MyServer!MyModule@MyQueue" or 
	 * "MyModule@MyQueue" to "MyQueue" 
	 * 
	 * @param destinationName The full name of the queue/topic hosted on a server
	 * @return The real queue/topic name
	 */
	private String getRealDestinationName(String destinationName)
	{
		String realName = destinationName;
		int startPos = destinationName.indexOf(DESTINATION_MODULE_PHYSICALDEST);

		if (startPos < 0)
		{
			startPos = destinationName.indexOf(DESTINATION_SERVER_MODULE_SEPARATOR);
		}
		
		if (startPos > 0)
		{
			realName = destinationName.substring(startPos + 1);
		}
		
		return transformComponentName(realName);
	}
    
    /*
	*/
	private String transformComponentName(String componentName)
	{
		// Replace "/" with "."
		return componentName.replace("/", ".");
	}
	
	/**
	 * Gets list of names of web-app and ejb components which should not have 
	 * statistics collected and shown.
	 * 
	 * @param blacklistText The text containing comma separated list of names to ignore
	 * @return A strongly type list of names to ignore
	 */
	private List<String> tokenizeBlacklistText(String blacklistText) {
		List<String> blacklist = new ArrayList<String>();
		String[] blacklistArray = null;
		
		if (blacklistText != null) {
			blacklistArray = blacklistText.split(BLACKLIST_TOKENIZER_PATTERN);
		}
		
		if ((blacklistArray != null) && (blacklistArray.length > 0)) {
			blacklist = Arrays.asList(blacklistArray);
		} else {
			blacklist = new ArrayList<String>();
		}
				
		return blacklist;
	}
	
	/**
	 * Returns the list of component names to be ignored (the blacklist) 
	 * 
	 * @return The blacklist of component names
	 */
	protected List<String> getComponentBlacklist() {
		return componentBlacklist;
	}
	private final static String BLACKLIST_TOKENIZER_PATTERN = ",\\s*";
	private List<String> componentBlacklist;
	
	private static final char DESTINATION_MODULE_PHYSICALDEST = '@';
	private static final char DESTINATION_SERVER_MODULE_SEPARATOR = '!';
	
	// ********************************************************************************************
}