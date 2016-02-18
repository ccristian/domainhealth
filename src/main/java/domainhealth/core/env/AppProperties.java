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
package domainhealth.core.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

/**
 * Provides access to application level properties which may be populated from 
 * a variety of places, including -D JVM parameters, web.xml context-params
 * and props files bundled as resources in the WAR archive. Additionally, the 
 * same property key-value pairs are stored as servlet context 
 * (application-scope) attributes for easy use from Tab Libraries in view 
 * jsps, if/where required.
 * 
 * Note: Property key syntax uses '_' (underscore) rather than '-' (slash)
 * because some of the keys are used in JSP Expression Language (EL) clauses
 * where the '-' symbol is interpreted as 'subtract'. 
 */
public class AppProperties extends Properties {
	/**
	 * The keys for each property (calling toString() for these will give you the property name)
	 */
	public enum PropKey {
		/**
		 * The always use jmxpoll" property name ("dh_always_use_jmxpoll")
		 */
		ALWAYS_USE_JMXPOLL_PROP { public String toString() { return "dh_always_use_jmxpoll"; } },
		
		/**
		 * The root statistics directory path property name ("dh_stats_output_path")
		 */
		STATS_OUTPUT_PATH_PROP { public String toString() { return "dh_stats_output_path"; } },

		/**
		 * The the number of seconds between statistics collection queries property name ("dh_query_interval_secs")
		 */
		QUERY_INTERVAL_SECS_PROP { public String toString() { return "dh_query_interval_secs"; } },

		/**
		 * The the number of seconds between statistics collection queries property name ("dh_component_blacklist")
		 */
		COMPONENT_BLACKLIST_PROP { public String toString() { return "dh_component_blacklist"; } },

		/**
		 * The number of days of CSV files to retain. Use 0 or -1 to disable CSV cleanup ("dh_component_blacklist")
		 */
		CSV_RETAIN_NUM_DAYS { public String toString() { return "dh_csv_retain_num_days"; } },
		
		/**
		 * The domain health version number property name ("dh_version_number")
		 */
		VERSION_NUMBER_PROP { public String toString() { return "dh_version_number"; } },

		/**
		 * The domain health version date property name ("dh_version_date")
		 */
		VERSION_DATE_PROP { public String toString() { return "dh_version_date"; } },
		
		/**
		 * The domain health version date property name ("dh_showdashboard")
		 */
		SHOW_DASHBOARD_PROP { public String toString() { return "dh_showdashboard"; } }
	};

	/**
	 * Populate the set of named application properties (and servlet context 
	 * attributes) from the following sources in order of decreasing 
	 * precedence:
	 * 
	 *   1. -D JVM parameters
	 *   2. web.xml context-params
	 *   3. Base properties provided ina props file in the WAR archive
	 *   
	 * @param sc The servlet context
	 */
	public AppProperties(ServletContext sc) {
		InputStream in = null;
		Properties baseProps = new Properties();
		
		try {
			in = sc.getResourceAsStream(VERSION_PROPS_FILEPATH);
			baseProps.load(in);			
		} catch (IOException e) {
			AppLog.getLogger().warning("Unable to load base properties from WAR internal path: " + VERSION_PROPS_FILEPATH);
		} finally {
			try { in.close(); } catch (Exception e) { e.printStackTrace(); }
		}
						
		loadProps(sc, baseProps);
	}

	/**
	 * Get property value
	 * 
	 * @param key The property key
	 * @return The property text value
	 */
    public String getProperty(PropKey key) {
    	return getProperty(key.toString());
    }
    
	/**
	 * Get boolean version of property value, returning false if value can't 
	 * be coerced into a boolean.
	 * 
	 * @param key The property key to look up
	 * @return The boolean value of property
	 */
	public boolean getBoolProperty(PropKey key) {
		return Boolean.parseBoolean(getProperty(key.toString()));		
	}

	/**
	 * Get integer version of property value, returning -1 if value can't be
	 * coerced into an integer.
	 * 
	 * @param key The property key to look up
	 * @return The integer value of property
	 */
	public int getIntProperty(PropKey key) {
		int result = -1;
		String textVal = getProperty(key.toString());
		
		if ((textVal != null) && (textVal.length() > 0)) {
			try {
				result = Integer.parseInt(textVal);
			} catch (NumberFormatException e) {
				result = -1;
			}
		}
		
		return result;
	}

	/**
	 * Load the properties trying each of the 3 sources in turn for a matching
	 * property.
	 * 
	 * @param sc The servlet context
	 * @param baseProps The base properties to use if not overriden from another source
	 */
	private void loadProps(ServletContext sc, Properties baseProps) {
		for (PropKey key : PropKey.values()) {
			// -D system property
			String value = System.getProperty(key.toString());
			
			// web.xml context-param property
			if ((value == null) || (value.length() <= 0)) {
				value = sc.getInitParameter(key.toString());
			}
			
			// base properties file property
			if ((value == null) || (value.length() <= 0)) {
				value = baseProps.getProperty(key.toString());
			}
			
			// finally save this property 
			if ((value != null) && (value.length() > 0)) {
				setProperty(key.toString(), value);
				sc.setAttribute(key.toString(), value);
			}
		}
	}
	
	// Constants
	private static final long serialVersionUID = 1L;
	private static final String VERSION_PROPS_FILEPATH = "/WEB-INF/version.props";
}
