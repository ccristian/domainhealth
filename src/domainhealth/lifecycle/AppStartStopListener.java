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
package domainhealth.lifecycle;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import domainhealth.core.env.AppLog;
import domainhealth.core.env.AppProperties;
import domainhealth.core.env.AppProperties.PropKey;
import domainhealth.backend.retriever.RetrieverBackgroundService;

/**
 * Application start/deploy and stop/undeploy event listener to initialise and
 * destroy the resources required by DomainHealth. Not using a class which 
 * implements 'ServletContextListener' because this class's methods need to be 
 * run as a privileged user, using a 'runas' entry in web.xml, which is only 
 * possible for servlets.  
 */
public class AppStartStopListener extends GenericServlet {
	/**
	 * NOT IMPLEMENTED. Always throws a Servlet Exception because this method should never be invoked
	 * 
	 * @param request The HTTP Servlet request
	 * @param response The HTTP Servlet response
	 * @throws ServletException Indicates a problem processing the HTTP servlet request
	 * @throws IOException Indicates a problem processing the HTTP servlet request
	 *
	 * @see javax.servlet.GenericServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		throw new ServletException("NOT IMPLEMENTED");		
	}

	/**
	 * Initialises the start/stop listener, which starts the Statistics
	 * Retriever background daemon process which will run repeatedly.
	 * 
	 * @throws ServletException Indicates a problem initialising the servlet
	 *
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {		
		AppLog.getLogger().notice("Starting DomainHealth application");
		AppProperties appProps = new AppProperties(getServletContext());
		String outputPath = appProps.getProperty(PropKey.STATS_OUTPUT_PATH_PROP);		
		
		if (outputPath == null) {
			throw new ServletException("Neither a JVM start-up '-D parameter nor a web.xml context-param has been defined for parameter '" + PropKey.STATS_OUTPUT_PATH_PROP + "' to specify the root path of the CSV output path");
		}

		retrieverBackgroundService = new RetrieverBackgroundService(appProps);
		retrieverBackgroundService.startup();		
	}

	/**
	 * Destroys the start/stop listener, which signals to the Retriever 
	 * background daemon process to stop.
	 *
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public void destroy() {
		AppLog.getLogger().notice("Stopping DomainHealth application");
		retrieverBackgroundService.shutdown();
	}
	
	// Members
	private RetrieverBackgroundService retrieverBackgroundService = null;
	
	// Constants
	private static final long serialVersionUID = 1L;	
}
