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
        <title>${symbol_dollar}{project.name} - Acesso Negado</title>
        <%@include file="../../includes/main.jsp" %>
    </head>
    <body>
        <div id="main">
            <div style="padding:20px;">
                <h1>Acesso Negado</h1>
                O conteúdo que você está tentando acessar é restrito e você não tem permissão de acessá-lo.<br>
                Por favor, entre com outro usuário neste <a href="<c:url value="/login"/>">link</a>.<br><br>
                Caso tenha alguma dúvida, basta entrar em contato com o suporte.<br>
                Obrigado
            </div>
        </div>
    </body>
</html>
