<%@ page import="domainhealth.core.jmx.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="domainhealth-tags" prefix="dh" %>
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.PROCESSOR_USAGE_PERCENT%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.PROCESSOR_LAST_MINUTE_WORKLOAD_AVERAGE%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NATIVE_PROCESSES_COUNT%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.ROOT_FILESYSTEM_USED_PERCENT%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.PHYSICAL_MEMORY_USED_PERCENT%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.PHYSICAL_SWAP_USED_PERCENT%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.JVM_INSTANCE_CORES_USED%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.JVM_INSTANCE_PHYSICAL_MEMORY_USED_MEGABYTES%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																			
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.TCP_LISTEN_COUNT%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.TCP_ESTABLISHED_COUNT%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.TCP_CLOSE_WAIT_COUNT%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.TCP_TIME_WAIT_COUNT%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_RX_MEGABYTES%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_MILLIONS_RX_PACKETS%>.png?<dh:link-ctx-params/>'/></td>		
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_RX_ERRORS%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_RX_DROPPED%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_RX_OVERRUNS%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_RX_FRAME%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_TX_MEGABYTES%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_MILLIONS_TX_PACKETS%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_TX_ERRORS%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_TX_DROPPED%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_TX_OVERRUNS%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_TX_CARRIER%>.png?<dh:link-ctx-params/>'/></td>
	</tr>																		
	<tr>
		<td class="datarow" align="left" width="50%"><img src='<c:out value="${contexturl}"/>/charts/<c:out value="${resourcetype}"/>/<%=WebLogicMBeanPropConstants.NETWORK_TX_COLLISIONS%>.png?<dh:link-ctx-params/>'/></td>
		<td class="datarow" align="right" width="50%">&nbsp;</td>
	</tr>																		
