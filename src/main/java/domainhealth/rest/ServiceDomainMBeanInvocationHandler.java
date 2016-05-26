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

	private JMXConnector jmxConnector = null;
	private Object actualMBean = null;

	/**
	 * 
	 * @param jmxConnector
	 */
	public ServiceDomainMBeanInvocationHandler(JMXConnector jmxConnector) {
		this.jmxConnector = jmxConnector;
	}

	/**
	 * Invokes specified method with specified params on specified object.
	 * 
	 * @param proxy
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {

			if (actualMBean == null) {
				actualMBean = findServiceDomain(jmxConnector.getMBeanServerConnection());
			}

			Object returnValue = method.invoke(actualMBean, args);
			return returnValue;
		} catch (Exception e) {
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