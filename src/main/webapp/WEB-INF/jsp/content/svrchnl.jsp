<%@ page import="domainhealth.core.jmx.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="domainhealth-tags" prefix="dh" %>
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<c:out value="${resourcename}"/>/<%=WebLogicMBeanPropConstants.ACCEPT_COUNT%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<c:out value="${resourcename}"/>/<%=WebLogicMBeanPropConstants.CONNECTIONS_COUNT%>.png?<dh:link-ctx-params/>'/></td>
	</tr>
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<c:out value="${resourcename}"/>/<%=WebLogicMBeanPropConstants.CHNL_MESSAGES_RECEIVED_COUNT%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<c:out value="${resourcename}"/>/<%=WebLogicMBeanPropConstants.CHNL_MESSAGES_SENT_COUNT%>.png?<dh:link-ctx-params/>'/></td>
	</tr>