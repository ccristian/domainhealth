<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" session="false" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<%@ include file="fragments/header.jsp" %>
	<body>	
		<div id="top_menu">
			<%@ include file="fragments/topmenu.jsp" %>
		</div>

		<div id="title_bar">
			<%@ include file="fragments/titlebar.jsp" %>
		</div>

		<%-- Toolbar is not used for dashboard --%>
		<%--
		 Updated by gregoan
		<div id="tool_bar">
			<%@ include file="fragments/toolbar.jsp" %>
		</div>
		--%>
		
		<div id="tool_bar">
			<c:if test="${resourcetype ne 'jmssrv' and resourcetype ne 'safagent'}">
			<%@ include file="fragments/toolbar.jsp" %>
			</c:if>
		</div>
			
		<div id="left_column">
			<%@ include file="fragments/leftmenu.jsp" %>
		</div>
		
		<div id="main_content">
		
			<table border="0" cellpadding="0" cellspacing="0">
				<tr><td colspan="2"><h6>&nbsp;</h6></td></tr>
				<c:if test="${resourcetype eq 'core' or not empty resourcename}">
					<jsp:include page='<%="content/" + pageContext.findAttribute("resourcetype") + ".jsp"%>'/>
				</c:if>
							
				<c:if test="${resourcetype ne 'core' and empty resourcename}">				
					<tr>
						<td colspan="2">
							<c:if test="${not empty resources}">
								<div id="choosetext">Select resource from left menu</div>
							</c:if>
							
							<%--
							<c:if test="${empty resources and resourcetype ne 'workmgr' and resourcetype ne 'svrchnl'}">
								<div id="choosetext">No resources of this type exist</div>
							</c:if>
							<c:if test="${empty resources and (resourcetype eq 'workmgr' or resourcetype eq 'svrchnl')}">
								<div id="choosetext">No resources of this type exist (only available for WLDF collected statistics)</div>
							</c:if>
							--%>
							
							<c:if test="${empty resources and resourcetype ne 'workmgr' and resourcetype ne 'svrchnl' and resourcetype ne 'jmssrv' and resourcetype ne 'safagent'}">
								<div id="choosetext">No resources of this type exist</div>
							</c:if>
							<c:if test="${empty resources and (resourcetype eq 'workmgr' or resourcetype eq 'svrchnl' or resourcetype eq 'jmssrv' or resourcetype eq 'safagent')}">
								<div id="choosetext">No resources of this type exist (only available for WLDF collected statistics)</div>
							</c:if>
												
						</td>
					</tr>
				</c:if>
			</table>
		</div>

		<div id="footer">
			<%@ include file="fragments/footer.jsp" %>				
		</div>
	</body>
</html>
