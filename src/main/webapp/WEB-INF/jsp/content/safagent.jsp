<%@ page import="domainhealth.core.jmx.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="domainhealth-tags" prefix="dh" %>
	
		<td>
			<c:choose>			
				<c:when test="${empty safAgentSummary}">
					<p>There is none Destination for this SAF Agent</p>
				</c:when>
			
				<c:otherwise>
				
					<div id="dashboard">
					
						<table>
							<tr>
								<th><strong>Name</strong></th>
								
								<th>MsgCurrentCount</th>
								<th>MsgPendingCount</th>
								<th>MsgReceiveCount</th>
								<th>MsgHighCount</th>
								
								<th>DowntimeHigh</th>
								<th>DowntimeTotal</th>
								<th>UptimeHigh</th>
								<th>UptimeTotal</th>
								
								<th>FailedMessagesTotal</th>
								
							</tr>
							
							<c:forEach var="safAgentDestination" items="${safAgentSummary}">
								<tr>
									<td class="first"><c:out value="${safAgentDestination.destinationName}"/></td>
									
									<td><c:out value="${safAgentDestination.messagesCurrentCount}"/></td>
									<td><c:out value="${safAgentDestination.messagesPendingCount}"/></td>
									<td><c:out value="${safAgentDestination.messagesReceivedCount}"/></td>
									<td><c:out value="${safAgentDestination.messagesHighCount}"/></td>
									
									<td><c:out value="${safAgentDestination.downtimeHigh}"/></td>
									<td><c:out value="${safAgentDestination.downtimeTotal}"/></td>
									<td><c:out value="${safAgentDestination.uptimeHigh}"/></td>
									<td><c:out value="${safAgentDestination.uptimeTotal}"/></td>
									
									<td><c:out value="${safAgentDestination.failedMessagesTotal}"/></td>
								</tr>
							</c:forEach>
						</table>
					</div>
				</c:otherwise>
			</c:choose>
		</td>
