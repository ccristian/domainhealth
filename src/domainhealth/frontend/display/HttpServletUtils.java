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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import domainhealth.core.env.AppLog;

/**
 * Constants for HTML, PNG and other WWW related artifacts for HTTP Servlets
 */
public class HttpServletUtils {
	/**
	 * The 'text/html' mime-type
	 */
	public final static String HTML_CONTENT_TYPE = "text/html";
	
	/**
	 * The 'image/png' mime-type 
	 */
	public final static String PNG_CONTENT_TYPE = "image/png";
	
	/**
	 * The '/' element separator in a URL 
	 */
	public final static char URL_PATH_SEPERATOR = '/';
	
	/**
	 * The '.' resource name suffix often found at the end of a URL
	 */
	public final static char URL_SUFFIX_SEPERATOR = '.';
	
	/**
	 * The 'Pragma' HTTP Header
	 */
	public final static String PRAGMA_HEADER = "Pragma";

	/**
	 * The 'Expires' HTTP Header
	 */
	public final static String EXPIRES_HEADER = "Expires";

	/**
	 * The 'Cache-Control' HTTP Header
	 */
	public final static String CACHE_CONTROL_HEADER = "Cache-Control";

	/**
	 * The 'No-Cache' HTTP Header value
	 */
	public final static String NO_CACHE_HEADER_VALUE = "no-cache";
	
	/**
	 * The 'Past-Date' HTTP Header value
	 */
	public final static int PAST_DATE_HEADER_VALUE = 0;

	/**
	 * Get the last element of the URL which called the servlet (ie. text 
	 * following last occurrence of / in the URL).
	 * 
	 * @param request The http request
	 * @return The last element text
	 * @throws ServletException Indicates a problem identifying the element from the URL
	 */
	public static String getEndPathFromURL(HttpServletRequest request) throws ServletException {
		String element = request.getPathInfo();
		
		if ((element == null) || (element.length() <= 1)) {
			element = null;
		} else {		
			int lastPathSeparator = element.lastIndexOf(URL_PATH_SEPERATOR);
			
			if (lastPathSeparator >= 0) {
				element = element.substring(lastPathSeparator + 1);
			}
		}
		
		return element;
	}

	/**
	 * Get the value of a text parameter from the servlet request, or default 
	 * value, if not specified.
	 * 
	 * @param request The http request
	 * @param paramName The name of the parameter to retrieve.
	 * @param paramDefault The value to use if the parameter is not present
	 * @return The value of the parameter, or default value if not specified
	 */
	public static String getTextParamOrDefaultValue(HttpServletRequest request, String paramName, String paramDefault) {
		String value = request.getParameter(paramName);
		
		if ((value == null) || (value.length() <=0)) {
			value = paramDefault;
		}
		
		return value;
	}	

	/**
	 * Get the value of a int parameter from the servlet request, or default 
	 * value, if not specified.
	 * 
	 * @param request The http request
	 * @param paramName The name of the parameter to retrieve.
	 * @param paramDefault The value to use if the parameter is not present
	 * @return The value of the parameter, or default value if not specified
	 */
	public static int getIntParamOrDefaultValue(HttpServletRequest request, String paramName, int paramDefault) {
		String valueText = request.getParameter(paramName);
		int value = paramDefault;

		if ((valueText != null) && (valueText.length() > 0)) {
			try {
				value = Integer.parseInt(valueText);
			} catch (Exception e) {
				AppLog.getLogger().info("Servlet request parameter '" + paramName + "' cannot be parsed to an int: " + valueText);
			}
		}
		return value;
	}

	/**
	 * Get the value of a date parameter from the servlet request, or default 
	 * value, if not specified.
	 * 
	 * @param request The http request
	 * @param paramName The name of the parameter to retrieve.
	 * @param dateFormat The format for conversion of the text to date
	 * @return The value of the parameter, or default value if not specified
	 */
	public static Date getDateParamOrNow(HttpServletRequest request, String paramName, String dateFormat) {
		DateFormat paramDateFormat = new SimpleDateFormat(dateFormat);
		String valueText = request.getParameter(paramName);
		Date dateTime = null;

		if ((valueText != null) && (valueText.length() > 0)) {
			try {
				dateTime = paramDateFormat.parse(valueText);
			} catch (Exception e) {
				AppLog.getLogger().info("Servlet request parameter '" + paramName + "' cannot be parsed to an int: " + valueText);
			}
		}
		
		if (dateTime == null) {
			dateTime = new Date();
		}
		
		return dateTime;
	}
	
	public static String getServletMapping(HttpServletRequest request) {
		String path = request.getServletPath();
		
		if ((path != null) && (path.length() > 0)) {
			return path.substring(1);			
		} else {
			return null;
		}
	}
}
