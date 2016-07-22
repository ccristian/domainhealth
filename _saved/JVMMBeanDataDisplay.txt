package domainhealth.rest;

import java.lang.management.ManagementFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class JVMMBeanDataDisplay extends MBeanDataDisplay {

	private final static ObjectName THREAD_MXBEAN_NAME;
	static {
		try {
			THREAD_MXBEAN_NAME = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
		} catch (Exception x) {
			throw new ExceptionInInitializerError(x);
		}
	}

	public JVMMBeanDataDisplay(MBeanServerConnection conn) {
		super(conn);
	}

	@Override
	StringBuffer writeAttribute(StringBuffer buffer, String prefix, ObjectName mbean, MBeanAttributeInfo info, Object value) {

		if (THREAD_MXBEAN_NAME.equals(mbean) && info.getName().equals("AllThreadIds")) {

			// Instead of displaying only the thread ids, we will display the thread infos.
			final Object threadInfos;

			try {
				threadInfos = server.invoke(mbean, "getThreadInfo", new Object[] { value, 1 }, new String[] { long[].class.getName(), int.class.getName() });
			} catch (Exception ex) {
				throw new IllegalArgumentException(mbean.toString(), ex);
			}
			buffer.append(prefix).append("# ").append("AllThreadInfo").append("\n");
			return dataDisplay.write(buffer, prefix, "AllThreadInfo", threadInfos);
		} else {
			// Default display.
			return super.writeAttribute(buffer, prefix, mbean, info, value);
		}
	}
}