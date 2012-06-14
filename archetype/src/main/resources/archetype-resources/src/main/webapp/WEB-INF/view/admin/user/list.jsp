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
        <title>${symbol_dollar}{project.name} - Users</title>
        <%@include file="../../../includes/main.jsp" %>
    </head>
    <body>
        <h1>Users</h1>
        <div id="main">
            <table>
                <tr>
                    <th>Name</th>
                    <th><a href="<c:url value="/admin/user/create" />">Add new</a></th>
                </tr>
                <c:forEach items="${symbol_dollar}{data}" var="user">
                <tr>
                    <td><a href="<c:url value="/admin/user/${symbol_dollar}{user.id}" />">${symbol_dollar}{user.username}</a></td>
                    <td><a href="<c:url value="/admin/user/${symbol_dollar}{user.id}/edit" />">Edit</a></td>
                </tr>
                </c:forEach>
            </table>
        </div>
    </body>
</html>
