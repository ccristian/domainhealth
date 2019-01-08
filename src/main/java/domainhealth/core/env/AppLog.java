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

import weblogic.logging.NonCatalogLogger;

/**
 * Wrapper around a logging implementation to enable application debug, info
 * notice, warning error, and critical logging messages to be directed to the 
 * appropriate logging sub-system. This implementation currently just provides
 * a simple wrapper around the WebLogic NonCatalogLogger logging API.  
 */
public class AppLog {
	/**
	 * Creates a new WebLogic non-catalog logger identified by the application 
	 * name (DomainHealth)
	 */
	protected AppLog() {
		log = new NonCatalogLogger(DH_APP_NAME);
	}
	
	/**
	 * Implements singleton pattern returning the shared logger instance
	 * 
	 * @return The instance of the logger
	 */
	public static AppLog getLogger() {
		return instance;
	}

	/**
	 * Logs a critical message
	 * 
	 * @param msg The message to be logged
	 */
	public void critical(String msg) {
		log.critical(msg);
	}

	/**
	 * Logs an error message
	 * 
	 * @param msg The message to be logged
	 */
	public void error(String msg) {
		log.error(msg);
	}

	/**
	 * Logs an error messageand cause exception
	 * 
	 * @param msg The message to be logged
	 * @param t The root cause throwable instance
	 */
	public void error(String msg, Throwable t) {
		log.error(msg, t);
	}

	/**
	 * Logs a warning message
	 * 
	 * @param msg The message to be logged
	 */
	public void warning(String msg) {
		log.warning(msg);
	}

	/**
	 * Logs a notice message
	 * 
	 * @param msg The message to be logged
	 */	
	public void notice(String msg) {
		log.notice(msg);
	}

	/**
	 * Logs an info message
	 * 
	 * @param msg The message to be logged
	 */
	public void info(String msg) {
		log.info(msg);
	}

	/**
	 * Logs a debug message
	 * 
	 * @param msg The message to be logged
	 */
	public void debug(String msg) {
		if (debugToStandardOut) {
			System.out.println(DH_APP_NAME + ": DEBUG - " + msg);
		} else {
			log.debug(msg);
		}
	}

	/**
	 * Logs a debug message and cause exception
	 * 
	 * @param msg The message to be logged
	 * @param t The root cause throwable instance
	 */
	public void debug(String msg, Throwable t) {
		if (debugToStandardOut) {
			System.out.println(DH_APP_NAME + ": DEBUG - " + msg);
			t.printStackTrace();
		} else {
			log.debug(msg, t);
		}		
	}

	// Members
	private final NonCatalogLogger log;
	private static final AppLog instance = new AppLog();

	// Constants
	private static final String DH_APP_NAME = "DomainHealth";
	private static final boolean debugToStandardOut = false;
}
