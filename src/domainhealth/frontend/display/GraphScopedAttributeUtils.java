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

import static domainhealth.core.util.DateUtil.DATETIME_PARAM_FORMAT;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;


/**
 * Name and access methods for storing and retrieving scoped page / request / 
 * session / application servlet attributes/parameters for the DomainHealth 
 * web application
 */
public class GraphScopedAttributeUtils {	
	/**
	 * The web-app URL root context (eg. 'domainhealth') attribute/parameter
	 */
	public final static String CONTEXT_URL_PARAM = "contexturl";
	
	/**
	 * The URL of the current invoked servletpage attribute/parameter
	 */
	public final static String PAGE_URL_PARAM = "pageurl";
	
	/**
	 * The title to display for the current page attribute/parameter
	 */
	public final static String PAGE_TITLE = "pagetitle";

	/**
	 * The title to display for the current page attribute/parameter
	 */
	public final static String MENU_TITLE = "menutitle";
	
	/**
	 * The current required date-time attribute/parameter
	 */
	public final static String DATETIME_PARAM = "datetime";
	
	/**
	 * The current required duration attribute/parameter
	 */
	public final static String DURATION_MINS_PARAM = "duration";
	
	/**
	 * The current required direction attribute/parameter
	 */
	public final static String DIRECTION_PARAM = "direction";

	/**
	 * The current required scope (ALL or specific named servicer) 
	 * attribute/parameter
	 */
	public final static String SCOPE_PARAM = "scope";

	/**
	 * The 'showhosts' pamameter to indicate whether to show the 'Hosts' link 
	 * in the top menu.
	 */
	public final static String SHOW_HOSTS_PARAM = "showhosts";
	
	/**
	 * The 'showjmssrvdashboards' pamameter to indicate whether to show the 'JMS Dashboard' link 
	 * in the top menu.
	 */
	// Added by gregoan
	public final static String SHOW_JMSSRV_DASHBOARDS_PARAM = "showjmssrvdashboards";
	
	/**
	 * The 'showsafagentdashboards' pamameter to indicate whether to show the 'SAF Dashboard' link 
	 * in the top menu.
	 */
	// Added by gregoan
	public final static String SHOW_SAFAGENT_DASHBOARDS_PARAM = "showsafagentdashboards";

	/**
	 * The list of names of all servers in domain attribute/parameter
	 */
	public final static String ALL_SERVER_NAMES_PARAM = "servernames";

	/**
	 * The name of the resource to have stats displayed for attribute/parameter
	 */	
	public final static String RESOURCE_NAME_PARAM = "resourcename";

	/**
	 * The type of resources to have stats displayed for attribute/parameter
	 */	
	public final static String RESOURCE_TYPE_PARAM = "resourcetype";

	/**
	 * The current date to display attribute/parameter
	 */
	public final static String DISPLAY_DATE_PARAM = "displaydate";
	
	/**
	 * The current day name to display attribute/parameter
	 */
	public final static String DISPLAY_DAY_PARAM = "displayday";
		
	/**
	 * The current time to display attribute/parameter
	 */
	public final static String DISPLAY_TIME_PARAM = "displaytime";
	
	/**
	 * The list of resources of a specific type (jdbc,jms,etc) attribute/parameter
	 */	
	public final static String RESOURCES_LIST_PARAM = "resources";
	
	/**
	 * The 'current' value for the direction attribute/parameter
	 */
	public final static String CURRENT_DIRECTION_VAL = "current";

	/**
	 * The 'ALL' value for the scope attribute/parameter
	 */
	public final static String ALL_SERVERS_SCOPE_VAL = "ALL";
	
	/**
	 * The 'firstdate' value for the direction attribute/parameter
	 */
	public final static String FIRST_DATE_DIRECTION_VAL = "firstdate";
	
	/**
	 * The 'lastdate' value for the direction attribute/parameter
	 */
	public final static String LAST_DATE_DIRECTION_VAL = "lastdate";
	
	/**
	 * The 'nextdate' value for the direction attribute/parameter
	 */
	public final static String NEXT_DATE_DIRECTION_VAL = "nextdate";
	
	/**
	 * The 'previousdate' value for the direction attribute/parameter
	 */
	public final static String PREVIOUS_DATE_DIRECTION_VAL = "previousdate";
	
	/**
	 * The 'firsttime' value for the direction attribute/parameter
	 */
	public final static String FIRST_TIME_DIRECTION_VAL = "firsttime";
	
	/**
	 * The 'lasttime' value for the direction attribute/parameter
	 */
	public final static String LAST_TIME_DIRECTION_VAL = "lasttime";
	
	/**
	 * The 'nexttime' value for the direction attribute/parameter
	 */
	public final static String NEXT_TIME_DIRECTION_VAL = "nexttime";
	
	/**
	 * The 'previoustime' value for the direction attribute/parameter
	 */
	public final static String PREVIOUS_TIME_DIRECTION_VAL = "previoustime";	
	
	/**
	 * The default minutes (ie. '30') the duration attribute/parameter
	 */
	public final static int DURATION_MINS_VAL_DEFAULT = 30;	
	
	/**
	 * The default value (ie. 'current') for the direction attribute/parameter
	 */
	public final static String DIRECTION_VAL_DEFAULT = CURRENT_DIRECTION_VAL;	

	/**
	 * The default value (ie. 'ALL') for the scope attribute/parameter
	 */
	public final static String SCOPE_VAL_DEFAULT = ALL_SERVERS_SCOPE_VAL;

	/**
	 * Retrieves the 'datetime' parameter from the request
	 * 
	 * @param request The HTTP Servlet Request
	 * @return The value of the 'datetime' parameter
	 */
	public static Date getDateTimeParam(HttpServletRequest request) {
		return HttpServletUtils.getDateParamOrNow(request, DATETIME_PARAM, DATETIME_PARAM_FORMAT);
	}

	/**
	 * Retrieves the 'direction' parameter from the request
	 * 
	 * @param request The HTTP Servlet Request
	 * @return The value of the 'direction' parameter
	 */
	public static String getDirectionParam(HttpServletRequest request) {
		return HttpServletUtils.getTextParamOrDefaultValue(request, DIRECTION_PARAM, DIRECTION_VAL_DEFAULT);
	}	

	/**
	 * Retrieves the 'scope' parameter from the request
	 * 
	 * @param request The HTTP Servlet Request
	 * @return The value of the 'scope' parameter
	 */
	public static String getScopeParam(HttpServletRequest request) {
		return HttpServletUtils.getTextParamOrDefaultValue(request, SCOPE_PARAM, SCOPE_VAL_DEFAULT);
	}
	
	/**
	 * Retrieves the 'duration' parameter from the request
	 * 
	 * @param request The HTTP Servlet Request
	 * @return The value of the 'duration' parameter
	 */
	public static int getDurationParam(HttpServletRequest request) {
		return HttpServletUtils.getIntParamOrDefaultValue(request, DURATION_MINS_PARAM,	DURATION_MINS_VAL_DEFAULT);
	}
}
