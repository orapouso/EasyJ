#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>${symbol_dollar}{project.name} - Login</title>
        <%@include file="../../includes/main.jsp" %>
        <script type="text/javascript" src="<c:url value="/static/js/scriptaculous/scriptaculous.js?effect" />"></script>
        <script type="text/javascript" src="<c:url value="/static/js/login.js?" />"></script>
    </head>
    <body>
        <div id="main">
            <h1>${symbol_dollar}{project.name} - Login</h1>
            <div id="login">
                <div id="loginError" style="display:none;">
                <c:choose>
                    <c:when test="${symbol_dollar}{not empty param.error}">
                        Usuário não autorizado. Username ou senha inválidos. Tente novamente.<br>
                    </c:when>
                    <c:when test="${symbol_dollar}{not empty param.timeout}">
                        Infelizmente sua sessão expirou ou você não se logou anteriormente. Por favor logue-se novamente.
                    </c:when>
                </c:choose>
                </div>
                <form id="loginForm" name="login" action="<c:url value="/authenticate" />" method="post">
                    <div class="username">
                        <div><label for="<%= UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY%>">Login</label></div>
                        <input autofocus type="text" name="<%= UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY%>" id="username" value="<c:if test="${symbol_dollar}{not empty param.login_error}"><%= session.getAttribute(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY) %></c:if>" />
                    </div>
                    <div class="password">
                        <div><label for="<%= UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY%>">Senha</label></div>
                        <input type="password" name="<%= UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY%>" id="password" />
                    </div>
                    <div class="button">
                        <input type="submit" value="Send"/>
                    </div>
                </form>
            </div>
        </div>
    </body>
</html>