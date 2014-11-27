<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="domainhealth-tags" prefix="dh" %>
<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<td align="left" valign="middle" width="30%">		
			<table border="0" cellpadding="1" cellspacing="0">
				<tr>
					<%-- First Day Button --%>
					<td align="center" valign="middle">
						<a href='<c:out value="${pageurl}"/>?<dh:link-ctx-params direction="firstdate"/>' onmouseover="firstday.src='<c:out value="${contexturl}"/>/images/buttons/first_hover.png'" onmouseout="firstday.src='<c:out value="${contexturl}"/>/images/buttons/first.png'"><img name="firstday" border="0" height='40' width='40' alt="Earliest Day" title="Earliest Day" src='<c:out value="${contexturl}"/>/images/buttons/first.png'/></a>			
					</td>
					
					<%-- Previous Day Button --%>
					<td align="center" valign="middle">
						<a href='<c:out value="${pageurl}"/>?<dh:link-ctx-params direction="previousdate"/>' onmouseover="previousday.src='<c:out value="${contexturl}"/>/images/buttons/previous_hover.png'" onmouseout="previousday.src='<c:out value="${contexturl}"/>/images/buttons/previous.png'"><img name="previousday" border="0" height='40' width='40' alt="Previous Day" title="Previous Day" src='<c:out value="${contexturl}"/>/images/buttons/previous.png'/></a>
					</td>
					
					<%-- Current Day Status Info --%>
					<td align="center" valign="middle">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td align="center" valign="top">
									<b><c:out value="${displayday}"/>&nbsp;</b>
								</td>
							</tr>
							<tr>
								<td align="center" valign="bottom">
									<b><c:out value="${displaydate}"/></b>
								</td>
							</tr>
						</table>
					</td>
					
					<%-- Next Day Button --%>
					<td align="center" valign="middle">
						<a href='<c:out value="${pageurl}"/>?<dh:link-ctx-params direction="nextdate"/>' onmouseover="nextday.src='<c:out value="${contexturl}"/>/images/buttons/next_hover.png'" onmouseout="nextday.src='<c:out value="${contexturl}"/>/images/buttons/next.png'"><img name="nextday" border="0" height='40' width='40' alt="Next Day" title="Next Day" src='<c:out value="${contexturl}"/>/images/buttons/next.png'/></a>
					</td>

					<%-- Last Day Button --%>
					<td align="center" valign="middle">
						<a href='<c:out value="${pageurl}"/>?<dh:link-ctx-params direction="lastdate"/>' onmouseover="lastday.src='<c:out value="${contexturl}"/>/images/buttons/last_hover.png'" onmouseout="lastday.src='<c:out value="${contexturl}"/>/images/buttons/last.png'"><img name="lastday" border="0" height='40' width='40' alt="Today" title="Today" src='<c:out value="${contexturl}"/>/images/buttons/last.png'/></a>
					</td>
				</tr>
			</table>
		</td>
		
		<%-- Server Scope Picker Drop Down List --%>
		<td align="center" valign="middle" width="40%">
			<form name='changescopeform'>
				<input type="hidden" name="datetime" value="${datetime}"/>
				<input type="hidden" name="direction" value="current"/>
				<input type="hidden" name="duration" value="${duration}"/>
				<select name='scope' onchange='this.form.submit()'>
					<option value="ALL" <c:if test="${scope == 'ALL'}">selected</c:if>>ALL</option>
					<c:forEach var="servername" items="${servernames}">   
					<option value='<c:out value="${servername}"/>' <c:if test="${scope == servername}">selected</c:if>><c:out value="${servername}"/></option>
					</c:forEach>								
				</select>
			</form>
		</td>
				
		<td align="right" valign="middle" width="30%">
			<table border="0" cellpadding="1" cellspacing="0">		
				<tr>
					<%-- Earliest Time This Day Button --%>				
					<td align="center" valign="middle">
						<a href='<c:out value="${pageurl}"/>?<dh:link-ctx-params direction="firsttime"/>' onmouseover="firsttime.src='<c:out value="${contexturl}"/>/images/buttons/first_hover.png'" onmouseout="firsttime.src='<c:out value="${contexturl}"/>/images/buttons/first.png'"><img name="firsttime" border="0" height='40' width='40' alt="Earliest this Day" title="Earliest Time this Day" src='<c:out value="${contexturl}"/>/images/buttons/first.png'/></a>
					</td>
					
					<%-- Previous Time Period Button --%>
					<td align="center" valign="middle">
						<a href='<c:out value="${pageurl}"/>?<dh:link-ctx-params direction="previoustime"/>' onmouseover="previoustime.src='<c:out value="${contexturl}"/>/images/buttons/previous_hover.png'" onmouseout="previoustime.src='<c:out value="${contexturl}"/>/images/buttons/previous.png'"><img name="previoustime" border="0" height='40' width='40' alt="Previous Hour" title="Previous Time Period" src='<c:out value="${contexturl}"/>/images/buttons/previous.png'/></a>
					</td>
					
					<td align="center" valign="middle">
						<table border="0" cellpadding="1" cellspacing="0">
							<tr>
								<%-- Current Time Status Info --%>							
								<td align="center" valign="top">
									<b><c:out value="${displaytime}"/>&nbsp;</b>
								</td>
							</tr>
							<tr>
							
								<%-- Minutes/Hours Duration Picker Drop-Down List --%>							
								<td align="center" valign="bottom">
									<form name='changedurationform'>
										<input type="hidden" name="datetime" value="${datetime}"/>
										<input type="hidden" name="direction" value="current"/>
										<input type="hidden" name="scope" value="${scope}"/>
										<select name='duration' onchange='this.form.submit()'>
											<option value="5" <c:if test="${duration == 5}">selected</c:if>>5 mins</option>
											<option value="15" <c:if test="${duration == 15}">selected</c:if>>15 mins</option>
											<option value="30" <c:if test="${duration == 30}">selected</c:if>>30 mins</option>
											<option value="60" <c:if test="${duration == 60}">selected</c:if>>1 hour</option>
											<option value="180" <c:if test="${duration == 180}">selected</c:if>>3 hours</option>
											<option value="360" <c:if test="${duration == 360}">selected</c:if>>6 hours</option>
											<option value="720" <c:if test="${duration == 720}">selected</c:if>>12 hours</option>
											<option value="1440" <c:if test="${duration == 1440}">selected</c:if>>24 hours</option>
										</select>
									</form>
								</td>
							</tr>
						</table>
					</td>		
								
					<%-- Next Time Period Button --%>
					<td align="center" valign="middle">
						<a href='<c:out value="${pageurl}"/>?<dh:link-ctx-params direction="nexttime"/>' onmouseover="nexttime.src='<c:out value="${contexturl}"/>/images/buttons/next_hover.png'" onmouseout="nexttime.src='<c:out value="${contexturl}"/>/images/buttons/next.png'"><img name="nexttime" border="0" height='40' width='40' alt="Next Hour" title="Next Time Period" src='<c:out value="${contexturl}"/>/images/buttons/next.png'/></a>
					</td>
					
					<%-- Latest Time This Day Button --%>					
					<td align="center" valign="middle">
						<a href='<c:out value="${pageurl}"/>?<dh:link-ctx-params direction="lasttime"/>' onmouseover="lasttime.src='<c:out value="${contexturl}"/>/images/buttons/last_hover.png'" onmouseout="lasttime.src='<c:out value="${contexturl}"/>/images/buttons/last.png'"><img name="lasttime" border="0" height='40' width='40' alt="Latest Time this Day" title="Latest Time this Day" src='<c:out value="${contexturl}"/>/images/buttons/last.png'/></a>
					</td>					
				</tr>
			</table>
		</td>		
	</tr>
</table>
