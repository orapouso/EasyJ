#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>User ${symbol_dollar}{data.name}</title>
        <%@include file="../../../includes/main.jsp" %>
    </head>
    <body>
        <h1>User ${symbol_dollar}{data.name}</h1>
        <div id="main">
            <label for="name">Name:</label> ${symbol_dollar}{data.name}<br/>
            <label for="username">Username:</label> ${symbol_dollar}{data.username}<br/>
            <label for="enabled">Enabled?</label>${symbol_dollar}{data.enabled ? "Sim" : "Não"}<br/>
            <c:url var="action" value="/admin/user/${symbol_dollar}{data.id}/edit" />
            <form action="${symbol_dollar}{action}" method="GET">
                <input type="submit" value="Edit" /><br/>
            </form>
            <c:url var="action" value="/admin/user" />
            <form action="${symbol_dollar}{action}" method="GET">
                <input type="submit" value="Users" /><br/>
            </form>
        </div>
    </body>
</html>
