<!--
    Copyright 2007, ScenPro, Inc
    
    $Header: /share/content/gforge/freestylesearch/freestylesearch/ui/jsp/index.jsp,v 1.1 2007-06-07 15:07:01 hebell Exp $
    $Name: not supported by cvs2svn $
-->
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<%@ page contentType="text/html" %>

<jsp:forward page="/do/search" />

