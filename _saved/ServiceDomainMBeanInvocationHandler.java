package domainhealth.rest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import weblogic.management.jmx.MBeanServerInvocationHandler;

import com.bea.wli.monitoring.ServiceDomainMBean;

/**
 * Invocation handler class for ServiceDomainMBean class.
 */
public class ServiceDomainMBeanInvocationHandler implements InvocationHandler {
  
	/*
	private String jndiURL = "weblogic.management.mbeanservers.domainruntime";
	
	private String protocol = "t3";
	private String hostname = "";
	private int port = 0;
	private String jndiRoot = "/jndi/";

	private String username = "";
	private String password = "";
	*/
	
	private JMXConnector jmxConnector = null;
	private Object actualMBean = null;
	
	//
	//private MBeanServerConnection conn;
	//

	/**
	 * 
	 * @param hostName
	 * @param port
	 * @param userName
	 * @param password
	 */
/*
	public ServiceDomainMBeanInvocationHandler(String hostName, int port, String userName, String password) {
		this.hostname = hostName;
		this.port = port;
		this.username = userName;
		this.password = password;
	}
*/
	/**
	 * 
	 * @param jmxConnector
	 */
	public ServiceDomainMBeanInvocationHandler(JMXConnector jmxConnector) {
		this.jmxConnector = jmxConnector;
	}

	/**
	 * Gets JMX connection
	 * 
	 * @return JMX connection
	 * @throws IOException
	 * @throws MalformedURLException
	 */
/*
	public JMXConnector initConnection() throws IOException, MalformedURLException {
		JMXServiceURL serviceURL = new JMXServiceURL(protocol, hostname, port, jndiRoot + jndiURL);
		Hashtable<String, String> h = new Hashtable<String, String>();

		if (username != null)
			h.put(Context.SECURITY_PRINCIPAL, username);
		if (password != null)
			h.put(Context.SECURITY_CREDENTIALS, password);

		h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
		return JMXConnectorFactory.connect(serviceURL, h);
	}
*/

	/**
	  * Invokes specified method with specified params on specified
	  * object.
	  * @param proxy
	  * @param method
	  * @param args
	  * @return
	  * @throws Throwable
	  */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
     try {
/*
          if (jmxConnector == null) {
               jmxConnector = initConnection();
          }
*/        
          if (actualMBean == null) {
        	  actualMBean = findServiceDomain(jmxConnector.getMBeanServerConnection());
           }
          
          Object returnValue = method.invoke(actualMBean, args);
          return returnValue;
     }
     catch (Exception e) {
         throw e;
     }
}

	/**
	 * Finds the specified MBean object
	 *
	 * @param connection
	 *            - A connection to the MBeanServer.
	 * @param mbeanName
	 *            - The name of the MBean instance.
	 * @param mbeanType
	 *            - The type of the MBean.
	 * @param parent
	 *            - The name of the parent Service. Can be NULL.
	 * @return Object - The MBean or null if the MBean was not found.
	 */
	public Object findServiceDomain(MBeanServerConnection connection) {
		ServiceDomainMBean serviceDomainbean = null;
		try {
			ObjectName on = new ObjectName(ServiceDomainMBean.OBJECT_NAME);
			serviceDomainbean = (ServiceDomainMBean) MBeanServerInvocationHandler.newProxyInstance(connection, on);
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
			return null;
		}
		return serviceDomainbean;
	}	
}