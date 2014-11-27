// Copyright (C) 2013 Paul Done, Oracle Corporation UK Ltd.
// This utility is provided "as is", without technical support, and with no
// warranty, express or implied, as to its usefulness for any purpose.
package domainhealth.core.jmx;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;

import java.net.MalformedURLException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;


/**
 * Creates an WebLogic JMX Connection to WebLogic's Server Runtime Service 
 * Tree. See description of WebLogicMBeanConnection for more info. Used 
 * primarily to retrieve data about a Server Runtime plus get a read
 * only view on the domain's current configuration.
 *  
 * @see WebLogicMBeanConnection
 */
public class ServerRuntimeServiceMBeanConnection extends WebLogicMBeanConnection {
	/**
	 * Create a new connection to a REMOTE WebLogic server's Server Runtime
	 * Service MBean Tree.
	 * 
	 * @see WebLogicMBeanException
	 * 
	 * @param protocol The server connection protocol (ie. 't3' or 't3s')
	 * @param host The hostname/ip-address of the target server
	 * @param port The listen admin port of the target server
	 * @param username A WebLogic Administrator username to connect with 
	 * @param password A WebLogic Administrator password to connect with
	 * @throws WebLogicMBeanException Indicates that a JMX connection to server could not be made
	 */
	public ServerRuntimeServiceMBeanConnection(String protocol, String host, int port, String username, String password) throws WebLogicMBeanException {
		super(protocol, host, port, username, password, SERVER_RUNTIME_SERVICE_NAME);
	}

	/**
	 * Create a new connection to a REMOTE WebLogic server's Server Runtime 
	 * Service MBean Tree.
	 * 
	 * @see WebLogicMBeanException
	 * 
	 * @param serverURL The target server URL
	 * @throws WebLogicMBeanException Indicates that a JMX connection to server could not be made
	 * @throws MalformedURLException Indicates the server URL text is incorrect format
	 */
	public ServerRuntimeServiceMBeanConnection(String serverURL) throws WebLogicMBeanException, MalformedURLException {
		super(new JMXServiceURL(JMX_URL_PREFIX + serverURL + JNDI_ROOT + SERVER_RUNTIME_SERVICE_NAME));
	}

	/**
	 * Create a new connection to the LOCAL WebLogic server's Server Runtime 
	 * Service MBean Tree.
	 * 
	 * @see WebLogicMBeanException
	 *
	 * @throws WebLogicMBeanException Indicates that a JMX connection to server could not be made
	 */
	public ServerRuntimeServiceMBeanConnection() throws WebLogicMBeanException {
		super(SERVER_RUNTIME_SERVICE_NAME);
	}

	/**
	 * Gets the root Server Runtime MBean
	 * 
	 * @return The root Server Runtime MBean 
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public ObjectName getServerRuntime() throws WebLogicMBeanException {
		try {
			return (ObjectName) getConn().getAttribute(serverRuntimeServiceMBean, SERVER_RUNTIME);
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	// Constants
	private static final String JMX_URL_PREFIX = "service:jmx:";
	private static final String SERVER_RUNTIME_SERVICE_NAME = "weblogic.management.mbeanservers.runtime";
	private static final ObjectName serverRuntimeServiceMBean;

	static {
		try {
			serverRuntimeServiceMBean = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
		} catch (MalformedObjectNameException e) {
			throw new AssertionError(e.toString());
		}
	}	
}
