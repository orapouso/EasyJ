#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>${symbol_dollar}{project.name} - Authorities</title>
        <%@include file="../../../includes/main.jsp" %>
    </head>
    <body>
        <h1>Authorities</h1>
        <div id="main">
            <table>
                <tr>
                    <th>Authority</th>
                    <th><a href="<c:url value="/admin/authority/create" />">Add new</a></th>
                </tr>
                <c:forEach items="${symbol_dollar}{data}" var="authority">
                <tr>
                    <td><a href="<c:url value="/admin/authority/${symbol_dollar}{authority.id}" />">${symbol_dollar}{authority.name}</a></td>
                    <td><a href="<c:url value="/admin/authority/${symbol_dollar}{authority.id}/edit" />">Edit</a></td>
                </tr>
                </c:forEach>
            </table>
        </div>
    </body>
</html>
