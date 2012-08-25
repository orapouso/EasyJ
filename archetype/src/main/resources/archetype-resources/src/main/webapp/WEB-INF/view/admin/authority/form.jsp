#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Authority ${symbol_dollar}{not empty data.id ? data.name : 'New'}</title>
        <%@include file="../../../includes/main.jsp" %>
    </head>
    <body>
        <h1>Authority ${symbol_dollar}{not empty data.id ? data.name : 'New'}</h1>
        <div id="main">
            <c:url var="action" value="/admin/authority${symbol_dollar}{not empty data.id ? '/' : ''}${symbol_dollar}{data.id}" />
            <form:form action="${symbol_dollar}{action}" method="POST" modelAttribute="data">
                <c:if test="${symbol_dollar}{not empty data.id}">
                <input type="hidden" name="_method" value="PUT"/>
                </c:if>
                <label for="authority">Authority:</label>
                <c:choose>
                    <c:when test="${symbol_dollar}{empty data.id}">
                        <form:input path="id" /><form:errors path="id" />
                    </c:when>
                    <c:otherwise>
                        ${symbol_dollar}{data.id}
                    </c:otherwise>
                </c:choose><br/>
                <label for="authority">Name:</label><form:input path="name" /><form:errors path="name" /><br/>
                <input type="submit" value="Send"/><br/>
            </form:form>
            <c:if test="${symbol_dollar}{not empty data.id}">
            <form action="${symbol_dollar}{action}" method="POST" modelAttribute="data" onsubmit="return confirm('Are you sure you want to delete THAT?')">
                <input type="hidden" name="_method" value="DELETE"/>
                <input type="submit" value="Delete" /><br/>
            </form>
            </c:if>
            <form action="${symbol_dollar}{action}" method="GET">
                <input type="submit" value="Cancel" /><br/>
            </form>
        </div>
    </body>
</html>
