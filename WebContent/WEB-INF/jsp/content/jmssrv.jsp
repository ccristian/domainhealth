<%@ page import="domainhealth.core.jmx.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="domainhealth-tags" prefix="dh" %>
	
		<td>
			<c:choose>			
				<c:when test="${empty jmsServerSummary}">
					<p>There is none Destination for this JMS server</p>
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
								<th>ConsumerCurrentCount</th>
								<th>ConsumerHighCount</th>
								<th>ConsumerTotalCount</th>
							</tr>
							
							<c:forEach var="jmsDestination" items="${jmsServerSummary}">
								<tr>
									<td class="first"><c:out value="${jmsDestination.destinationName}"/></td>
									<td><c:out value="${jmsDestination.messagesCurrentCount}"/></td>
									<td><c:out value="${jmsDestination.messagesPendingCount}"/></td>
									<td><c:out value="${jmsDestination.messagesReceivedCount}"/></td>
									<td><c:out value="${jmsDestination.messagesHighCount}"/></td>
									<td><c:out value="${jmsDestination.consumersCurrentCount}"/></td>
									<td><c:out value="${jmsDestination.consumersHighCount}"/></td>
									<td><c:out value="${jmsDestination.consumersTotalCount}"/></td>
								</tr>
							</c:forEach>
						</table>
					</div>
				</c:otherwise>
			</c:choose>
		</td>
