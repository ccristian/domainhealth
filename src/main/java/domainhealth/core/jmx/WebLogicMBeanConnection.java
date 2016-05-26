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
package domainhealth.core.jmx;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.ADMIN_SERVER_HOSTNAME;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.ADMIN_SERVER_PORT;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.IS_ADMIN_SERVER;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.IS_ADMIN_SERVER_PORT_SECURED;
import static domainhealth.core.jmx.WebLogicMBeanPropConstants.SERVER_RUNTIME;

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import domainhealth.core.env.AppLog;

/**
 * Encapsulates a WebLogic remote JMX connector and its associated JMX MBean 
 * Server connection to a WebLogic Administration Server's domain runtime 
 * tree. As a result of obtaining access to the domain runtime, it is then 
 * possible to access the domain runtime, domain configuration, all servers' 
 * runtimes and the edit mbean. An instance of a WebLogic connection can be 
 * obtained by explicitly providing the connection details for the admin 
 * server MBean tree (eg. host, port, username, password) or by having the 
 * admin server's connection details derived from the host server's runtime, 
 * as long as the client application accessing this connection has already
 * authenticated with the container as a WebLogic administrator.
 * 
 * Note: A search of http://forum.java.sun.com with the search string
 * "MBeanServerConnection thread safe" or "MBeanServerConnection multiple 
 * threads" yields answers which imply that the MBeanServerConnection is 
 * thread-safe, and which can be held open for a long time and can process 
 * multiple client requests concurrently.
 */
public class WebLogicMBeanConnection {
	/**
	 * BoxBurner Controller MBean empty parameters signature.
	 */
	public final static String[] EMPTY_OPERTN_PARAMTYPES = null;

	/**
	 * Creates new WebLogic JMX connection wrapper using explicitly provided 
	 * connection parameters
	 *  
	 * @param protocol The admin server connection protocol (ie. 't3' or 't3s')
	 * @param host The hostname/ip-address of the admin server
	 * @param port The listen admin port of the admin server
	 * @param username A WebLogic Administrator username to connect with 
	 * @param password A WebLogic Administrator password to connect with
	 * @param serviceName The particular MBean Tree service to lookup (eg. ServerService, DomainService, EditService) 
	 * @throws WebLogicMBeanException Indicates that a JMX connection to server could not be made
	 */
	protected WebLogicMBeanConnection(String protocol, String host, int port, String username, String password, String serviceName) throws WebLogicMBeanException {
				
		try {			
			Map<String, String> props = getJMXContextProps();
			props.put(Context.SECURITY_PRINCIPAL, username);
			props.put(Context.SECURITY_CREDENTIALS, password);
			jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(protocol, host, port, JNDI_ROOT + serviceName), props);
			conn = jmxConnector.getMBeanServerConnection();
			connectionDescription = String.format(CONN_USR_DESC_TMPLTE, protocol, host, port, username);
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Creates new WebLogic JMX connection wrapper using explicitly provided 
	 * JMX connection URL.
	 *  
	 * @param jmxServiceURL The target server JMX service URL
	 * @throws WebLogicMBeanException Indicates that a JMX connection to server could not be made
	 */
	protected WebLogicMBeanConnection(JMXServiceURL jmxServiceURL) throws WebLogicMBeanException {
				
		try {			
			jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, getJMXContextProps());
			conn = jmxConnector.getMBeanServerConnection();
			connectionDescription = String.format(CONN_URL_TMPLTE, jmxServiceURL);
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Creates new WebLogic JMX connection wrapper by determining the remote 
	 * connection protocol, hostname and port of the admin server from the 
	 * local server's runtime settings and then attempts to remotely connect 
	 * to the admin server using these obtained connection details. This 
	 * access method relies on the calling Java application code thread having 
	 * already authenticated with the underlying container (eg. using a 
	 * WebLogic servlet authentication API, WebLogic's doAs() security API or
	 * a Servlet's run-as or init-as deployment descriptor setting).
	 * 
	 * @param serviceName The particular MBean Tree service to lookup (eg. ServerService, DomainService, EditService) 
	 * @throws WebLogicMBeanException Indicates that a JMX connection to server could not be made
	 */
	protected WebLogicMBeanConnection(String serviceName) throws WebLogicMBeanException {
				
		try {
			MBeanServerConnection localConn = getCachedLocalConn();
			ObjectName serverRuntime = (ObjectName) localConn.getAttribute(serverRuntimeServiceMBean, SERVER_RUNTIME);
			boolean isSecure = ((Boolean) localConn.getAttribute(serverRuntime, IS_ADMIN_SERVER_PORT_SECURED)).booleanValue();
			String protocol = isSecure ? WEBLOGIC_SECURE_REMOTE_PROTOCOL: WEBLOGIC_INSECURE_REMOTE_PROTOCOL;
			String host = (String) localConn.getAttribute(serverRuntime, ADMIN_SERVER_HOSTNAME);
			int port = ((Integer) localConn.getAttribute(serverRuntime, ADMIN_SERVER_PORT)).intValue();
			jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(protocol, host, port, JNDI_ROOT + serviceName), getJMXContextProps());
			conn = jmxConnector.getMBeanServerConnection();
			connectionDescription = String.format(CONN_DESC_TMPLTE, protocol, host, port);
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Static method which checks the current WebLogic server's local runtime 
	 * MBean tree to determine if the current host WebLogic server is the 
	 * Admin Server for the WebLogic domain.
	 * 
	 * @return True if current server is the Admin Server; otherwise False
	 * @throws WebLogicMBeanException Indicates that a JMX connection to server could not be made
	 */
	public static boolean isThisTheAdminServer() throws WebLogicMBeanException {		
		try {
			MBeanServerConnection localConn = getCachedLocalConn();
			ObjectName serverRuntime = (ObjectName) localConn.getAttribute(serverRuntimeServiceMBean, SERVER_RUNTIME);
			return ((Boolean) localConn.getAttribute(serverRuntime, IS_ADMIN_SERVER)).booleanValue();
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Locate the server runtime mbean from the local jndi tree and cache it 
	 * for future use. Unfortunately, there are two different JNDI mappings
	 * for this mbean object, depending on what context the lookup is being 
	 * called from (and the object is not always present in both). As a 
	 * result, may need to try more than one lookup variation because first 
	 * may fail with not found exception. This is another reason why we 
	 * should cache it because each time it is required we don't want to 
	 * induce and swallow one or more exceptions while we try to look it up 
	 * from jndi, and suffer the performance overhead of these exceptions.
	 * 
	 * We use the double-check locking pattern to prevent overtly 
	 * synchronising the cache access. This didn't work in older versions of 
	 * Java but from Java version 1.5 onwards this pattern does works if the 
	 * member variable is made volatile - which we have done. Also, see:
	 * http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html 
	 * 
	 *  New in WLS 10.3.1 is the ability to connect to the local DomainRuntime 
	 *  directly from a local JNDI tree lookup: ctx.lookup(
	 *  "java:comp/env/jmx/domainRuntime"). However, because this won't work 
	 * with earlier versions of WebLogic and because we also need the option 
	 * to connect to the Edit tree, in some cases, its not worth trying to use
	 * this. 
	 * 
	 * @return The JNDI retrieved Server Runtime MBean
	 * @throws NamingException Indicates a problem accessing the JNDI tree
	 */
	private static MBeanServerConnection getCachedLocalConn() throws NamingException {
		if (cachedLocalConn == null) {		
			synchronized (WebLogicMBeanConnection.class) {
				if (cachedLocalConn == null) {
					InitialContext ctx = null;
					
					try {
						ctx = new InitialContext();
						
						for (String jndiLookup : LOCAL_SERVER_RUNTIME_MBEAN_JNDI_LOOKUPS) {
							try {
								cachedLocalConn = (MBeanServer) ctx.lookup(jndiLookup);
								
								if (cachedLocalConn == null) {
									AppLog.getLogger().debug("Unable to locate local server runtime mbean using jndi lookup of: " + jndiLookup);
								} else {
									AppLog.getLogger().debug("Successfully located local server runtime mbean using jndi lookup of: " + jndiLookup);
									break;
								}
							} catch (Exception e) {
								AppLog.getLogger().debug("Error attempting to locate local server runtime mbean using jndi lookup of: " + jndiLookup + "  (" + e + ")");
							}
						}
					} finally {
						if (ctx != null) {
							try { ctx.close(); } catch (Exception e) {}
						}
					}
				}
			}
		}
		
		return cachedLocalConn;
	}

	/**
	 * Populate a JNDI context property file with the minimum properties 
	 * required to access a WebLogic MBean server tree
	 * 
	 * @return WebLogic properties file
	 */
	protected Map<String, String> getJMXContextProps() {
		Map<String, String> props = new HashMap<String, String>();
		props.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, WEBLOGIC_PROVIDER_PACKAGES);
		return props;
	}
		
	/**
	 * Gets a named object property from the given MBean
	 * 
	 * @param mBean The MBean to query the property from
	 * @param attr The property of the MBean to retrieve the value from
	 * @return The value of the property
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public Object getObjectAttr(ObjectName mBean, String attr) throws WebLogicMBeanException {
		try {
			return conn.getAttribute(mBean, attr);		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Sets the value a named object property on the given MBean
	 * 
	 * @param mBean The MBean to set the property on
	 * @param attr The property of the MBean to set the value on
	 * @param value The value of the property to set
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public void setObjectAttr(ObjectName mBean, String attr, Object value) throws WebLogicMBeanException {
		try {
			conn.setAttribute(mBean, new Attribute(attr, value));		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Gets a named text property from the given MBean
	 * 
	 * @param mBean The MBean to query the property from
	 * @param attr The property of the MBean to retrieve the value from
	 * @return The value of the property
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public String getTextAttr(ObjectName mBean, String attr) throws WebLogicMBeanException {
		try {
			return (String) conn.getAttribute(mBean, attr);		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Sets the value a named object property on the given MBean
	 * 
	 * @param mBean The MBean to set the property on
	 * @param attr The property of the MBean to set the value on
	 * @param value The value of the property to set
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public void setTextAttr(ObjectName mBean, String attr, String value) throws WebLogicMBeanException {
		try {
			conn.setAttribute(mBean, new Attribute(attr, value));		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Gets a named number property from the given MBean
	 * 
	 * @param mBean The MBean to query the property from
	 * @param attr The property of the MBean to retrieve the value from
	 * @return The value of the property
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public double getNumberAttr(ObjectName mBean, String attr) throws WebLogicMBeanException {
		try {
			Object number = conn.getAttribute(mBean, attr);
			
			if (number instanceof Double) {
				return ((Double)number).doubleValue();				
			} else if (number instanceof Long) {
				return ((Long)number).longValue();				
			} else {
				return ((Integer)number).intValue();								
			}
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Sets the value a named object property on the given MBean
	 * 
	 * @param mBean The MBean to set the property on
	 * @param attr The property of the MBean to set the value on
	 * @param value The value of the property to set
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public void setNumberAttr(ObjectName mBean, String attr, long value) throws WebLogicMBeanException {
		try {
			conn.setAttribute(mBean, new Attribute(attr, value));		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Sets the value a named object property on the given MBean
	 * 
	 * @param mBean The MBean to set the property on
	 * @param attr The property of the MBean to set the value on
	 * @param value The value of the property to set
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public void setNumberAttr(ObjectName mBean, String attr, int value) throws WebLogicMBeanException {
		try {
			conn.setAttribute(mBean, new Attribute(attr, value));		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Gets a named number property from the given MBean
	 * 
	 * @param mBean The MBean to query the property from
	 * @param attr The property of the MBean to retrieve the value from
	 * @return The value of the property
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public boolean getBooleanAttr(ObjectName mBean, String attr) throws WebLogicMBeanException {
		try {
			return (Boolean) conn.getAttribute(mBean, attr);		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Sets the value a named object property on the given MBean
	 * 
	 * @param mBean The MBean to set the property on
	 * @param attr The property of the MBean to set the value on
	 * @param value The value of the property to set
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public void setBooleanAttr(ObjectName mBean, String attr, boolean value) throws WebLogicMBeanException {
		try {
			conn.setAttribute(mBean, new Attribute(attr, value));		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Gets a child object from the given MBean
	 * 
	 * @param mBean The MBean to query the property from
	 * @param attr The property of the MBean to retrieve the value from
	 * @return The value of the property
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public ObjectName getChild(ObjectName mBean, String attr) throws WebLogicMBeanException {
		try {
			return (ObjectName) conn.getAttribute(mBean, attr);		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Gets a list of child objects from the given MBean
	 * 
	 * @param mBean The MBean to query the property from
	 * @param attr The property of the MBean to retrieve the value from
	 * @return The value of the property
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */	
	public ObjectName[] getChildren(ObjectName mBean, String attr) throws WebLogicMBeanException {
		try {
			return (ObjectName[]) conn.getAttribute(mBean, attr);		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Invokes a named operation on a given MBean instance.
	 * 
	 * @param mBean The MBean to invoke the operation on
	 * @param operationName The name of the operation to invoke
	 * @param params The parameters to pass to the operation
	 * @param signature The signature of the parameters
	 * @return The result returned by the mbean operation
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public Object invoke(ObjectName mBean, String operationName, Object[] params, String[] signature) throws WebLogicMBeanException {
		try {
			return conn.invoke(mBean, operationName, params, signature);		
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}		
	}

	/**
	 * See if the JMX server has a MBean with a given them and if so return 
	 * its object name.
	 * 
	 * @param mBeanName The unique mbean name (eg. myapp:name=MyMgmntMBean)
	 * @return The mbean's object name, if exists, otherwise null
	 * @throws WebLogicMBeanException Indicates that a JMX connection error occurred
	 */
	public ObjectName getCustomMBean(String mBeanName) throws WebLogicMBeanException {
		try {
			ObjectName objName = new ObjectName(mBeanName);
			
			if (getConn().isRegistered(objName)) {
				return objName;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Close the JMX Connector and associated JMX MBeanServer connection
	 */
	public void close() {
		try {
			jmxConnector.close();
		} catch (Exception e) {
			AppLog.getLogger().error("Error closing " + this, e);
			e.printStackTrace();
		}
	}

	/**
	 * Returns a text description of the MBean server connection including hostname and port
	 * 
	 * @return The current MBean server connection description
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return connectionDescription;
	}

	/**
	 * Returns the current MBeanServer connection to overriding classes.
	 * 
	 * @return The current JMX Mbean server connection
	 */
	protected MBeanServerConnection getConn() {
		return conn;
	}
	
	// Members
	//private final JMXConnector jmxConnector;
	//private final MBeanServerConnection conn;
	protected final JMXConnector jmxConnector;
	protected final MBeanServerConnection conn;
	
	private final String connectionDescription;
		
	// Constants
	protected static final String JNDI_ROOT = "/jndi/";
	private static final String CONNECTION_DESC_PREFIX = "WebLogic-JMX-Connection";
	private static final String CONN_URL_TMPLTE = CONNECTION_DESC_PREFIX + "()";
	private static final String CONN_DESC_TMPLTE = CONNECTION_DESC_PREFIX + "(%s://%s:%s)";
	private static final String CONN_USR_DESC_TMPLTE = CONN_DESC_TMPLTE + ";username=%s)";
	private static final String WEBLOGIC_PROVIDER_PACKAGES = "weblogic.management.remote";
	private static final String WEBLOGIC_INSECURE_REMOTE_PROTOCOL = "t3";
	private static final String WEBLOGIC_SECURE_REMOTE_PROTOCOL = "t3s";
	private static final String[] LOCAL_SERVER_RUNTIME_MBEAN_JNDI_LOOKUPS = {"java:comp/env/jmx/runtime", "java:comp/jmx/runtime"};
	private static volatile MBeanServerConnection cachedLocalConn = null;
	private static final ObjectName serverRuntimeServiceMBean;

	static {
		try {
			serverRuntimeServiceMBean = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
		} catch (MalformedObjectNameException e) {
			throw new AssertionError(e.toString());
		}
	}
}
