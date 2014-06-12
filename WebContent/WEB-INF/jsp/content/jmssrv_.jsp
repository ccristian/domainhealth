<%@ page import="domainhealth.core.jmx.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="domainhealth-tags" prefix="dh" %>
	
		<td>
		
			<h2>Destinations Summary</h2>
						
			<c:if test="${empty jmsServersSummary}">
				<p>There is none Destinations</p>
			</c:if>
			
			<c:if test="${not empty jmsServersSummary}">
				<p>Destinations summary</p>
				<table>
					<tr>				
						<th class="first"><strong>Name</strong></th>
						<th>MsgCurCnt</th>
						<th>MsgPndCnt</th>
						<th>MsgRcvCnt</th>
						<th>MsgHighCnt</th>
						<th>ConsCurCnt</th>
						<th>ConsHighCnt</th>
						<th>ConsTotCnt</th>
					</tr>
					
					<c:forEach var="jmsServerSummary" items="${jmsServersSummary}">
						<tr>
							<td class="first"><c:out value="${jmsServerSummary.destinationName}"/></td>
							<td><c:out value="${jmsServerSummary.messagesCurrentCount}"/></td>
							<td><c:out value="${jmsServerSummary.messagesPendingCount}"/></td>
							<td><c:out value="${jmsServerSummary.messagesReceivedCount}"/></td>
							<td><c:out value="${jmsServerSummary.messagesHighCount}"/></td>
							<td><c:out value="${jmsServerSummary.consumersCurrentCount}"/></td>
							<td><c:out value="${jmsServerSummary.consumersHighCount}"/></td>
							<td><c:out value="${jmsServerSummary.consumersTotalCount}"/></td>
						</tr>
					</c:forEach>
				</table>
			</c:if>
			
		</td>
