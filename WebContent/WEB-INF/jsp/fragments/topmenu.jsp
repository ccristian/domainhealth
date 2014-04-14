<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="domainhealth-tags" prefix="dh" %>
				<div id="top_menu_container">					
					<div id="appname">DomainHealth</div>					
					<ul>
						<li><a href='<c:out value="${contexturl}"/>/corestats?<dh:link-ctx-params/>'>Core</a></li>
						<c:if test="${showhosts}">
							<li><a href='<c:out value="${contexturl}"/>/hostmachinestats?<dh:link-ctx-params/>'>Host</a></li>
						</c:if>
						<li><a href='<c:out value="${contexturl}"/>/datasourcestats?<dh:link-ctx-params/>'>JDBC</a></li>
						<li><a href='<c:out value="${contexturl}"/>/destinationstats?<dh:link-ctx-params/>'>JMS</a></li>
						<li><a href='<c:out value="${contexturl}"/>/webappstats?<dh:link-ctx-params/>'>Web-Apps</a></li>
						<li><a href='<c:out value="${contexturl}"/>/ejbstats?<dh:link-ctx-params/>'>EJBs</a></li>
						<li><a href='<c:out value="${contexturl}"/>/workmgrstats?<dh:link-ctx-params/>'>Work Mgrs</a></li>
						<li><a href='<c:out value="${contexturl}"/>/svrchnlstats?<dh:link-ctx-params/>'>Channels</a></li>
					</ul>
				</div> 
