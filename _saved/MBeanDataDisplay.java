package domainhealth.rest;

import java.io.IOException;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

public class MBeanDataDisplay {

	final MBeanServerConnection server;
	final TextDataDisplay dataDisplay = new TextDataDisplay();

	public MBeanDataDisplay(MBeanServerConnection conn) {
		this.server = conn;
	}

	StringBuffer writeAttribute(StringBuffer buffer, String prefix, ObjectName mbean, MBeanAttributeInfo info, Object value) {
		
		buffer.append(prefix).append("# ").append(info.getDescription()).append("\n");
		return dataDisplay.write(buffer, prefix, info.getName(), value);
	}
	
	StringBuffer writeAttribute(StringBuffer buffer, String prefix, ObjectInstance mbeanInst, MBeanAttributeInfo info, Object value) {
		
		buffer.append(prefix).append("# ").append(info.getDescription()).append("\n");
		return dataDisplay.write(buffer, prefix, info.getName(), value);
	}

	public StringBuffer write(StringBuffer buffer, String prefix, ObjectName mbean) throws IOException, JMException {
		final MBeanInfo info = server.getMBeanInfo(mbean);
		buffer.append(prefix).append("MBean: ").append(mbean).append("\n");
		buffer.append(prefix).append("{\n");
		final String attrPrefix = prefix + "   ";
		final MBeanAttributeInfo[] attributes = info.getAttributes();

		for (MBeanAttributeInfo attr : attributes) {
			Object toWrite = null;
			try {
				toWrite = server.getAttribute(mbean, attr.getName());
			} catch (Exception x) {
				toWrite = x;
			}
			
			//writeAttribute(buffer, attrPrefix, mbean, attr, toWrite);
			//buffer.append("\n");
			
			// No idea why but threshold is not supported causing an exception
			if(attr.getDescription().toLowerCase().contains("threshold")) {
				
			} else {
				writeAttribute(buffer, attrPrefix, mbean, attr, toWrite);
				buffer.append("\n");
			}
		}
		buffer.append(prefix).append("}\n");
		return buffer;
	}
	
	public StringBuffer write(StringBuffer buffer, String prefix, ObjectInstance mbeanInst) throws IOException, JMException {
		
		ObjectName mbean = mbeanInst.getObjectName();
		final MBeanInfo info = server.getMBeanInfo(mbean);
		buffer.append(prefix).append("MBean: ").append(mbean).append("\n");
		buffer.append(prefix).append("{\n");
		final String attrPrefix = prefix + "   ";
		final MBeanAttributeInfo[] attributes = info.getAttributes();
	
		for (MBeanAttributeInfo attr : attributes) {
			Object toWrite = null;
			try {
				toWrite = server.getAttribute(mbean, attr.getName());
			} catch (Exception x) {
				toWrite = x;
			}
			
			//writeAttribute(buffer, attrPrefix, mbean, attr, toWrite);
			//buffer.append("\n");
			
			// No idea why but threshold is not supported causing an exception
			if(attr.getDescription().toLowerCase().contains("threshold")) {
				
			} else {
				writeAttribute(buffer, attrPrefix, mbean, attr, toWrite);
				buffer.append("\n");
			}
		}
		buffer.append(prefix).append("}\n");
		return buffer;
	}

	public String toString(String prefix, ObjectName mbean) throws IOException, JMException {
		return write(new StringBuffer(), prefix, mbean).toString();
	}
	
	public String toString(String prefix, ObjectInstance mbeanInst) throws IOException, JMException {
		return write(new StringBuffer(), prefix, mbeanInst).toString();
	}

	public String toString(ObjectName mbean) throws IOException, JMException {
		return toString("", mbean);
	}
	
	public String toString(ObjectInstance mbeanInst) throws IOException, JMException {
		return toString("", mbeanInst);
	}
}