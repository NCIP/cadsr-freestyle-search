<!-- Copyright ScenPro, Inc. 2005
     $Header: /share/content/gforge/freestylesearch/freestylesearch/ui/jsp/updateindices.jsp,v 1.1 2007-02-13 19:35:17 hebell Exp $
     $Name: not supported by cvs2svn $
-->
<%@ taglib uri="/tags/struts-bean" prefix="bean"%>
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ page import="java.util.Vector"%>
<%@ page import="gov.nih.nci.cadsr.freestylesearch.ui.UpdateIndicesForm"%>

<html>
    <head>
        <title><bean:message key="update.title" /></title>
        <html:base />
        <meta http-equiv="Content-Language" content="en-us">
        <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=WINDOWS-1252">
        <LINK href="freestyle.css" rel="stylesheet" type="text/css">
        <script type="text/javascript">
            function loaded()
            {
                if (updateForm.allowed.value == "Y")
                    <%=UpdateIndicesForm.FORMNAME%>.submitBtn.disabled = false;
                else
                    <%=UpdateIndicesForm.FORMNAME%>.submitBtn.disabled = true;
            }
        </script>
    </head>

    <body onload="loaded();">
        <table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor="#A90101">
            <tr bgcolor="#A90101">
                <td valign="center" align="left"><a href="http://www.cancer.gov" target="_blank" alt="NCI Logo"> <img src="brandtype.gif" border="0"></a></td>
                <td valign="center" align="right"><a href="http://www.cancer.gov" target="_blank" alt="NCI Logo"> <img src="tagline_nologo.gif" border="0"></a></td>
            </tr>
        </table>
        <table class="secttable">
            <colgroup>
                <col />
            </colgroup>
            <tbody class="secttbody" />
                <tr>
                    <td><a target="_blank" href="http://ncicb.nci.nih.gov/NCICB/infrastructure/cacore_overview/cadsr"><img style="border: 0px solid black" title="NCICB caDSR" src="freestyle_banner.gif"></a></td>
                </tr>
                <tr>
                    <td align="center"><p class="ttl18"><bean:message key="update.title" /></p></td>
                </tr>
        </table>
        <html:form method="post" action="/updateindices">
            <hr />
            <html:hidden property="reOpen" />
            <html:hidden property="blockedFRange" />
            <html:hidden property="blockedIRange" />
            <html:hidden property="fullBlockStart" />
            <html:hidden property="fullBlockEnd" />
            <html:hidden property="incBlockStart" />
            <html:hidden property="incBlockEnd" />
            <html:hidden property="lastUpdate" />
            <html:hidden property="allowed" />
            <html:errors />
            <p class="bstd12" style="text-align: center">
                <bean:write name="updateForm" property="msg" />
            </p>
            <table style="margin: 0.3in 0in 0.3in 0in">
                <tr>
                    <td><label><bean:message key="input.user"/></label></td><td><html:text property="user"/></td>
                </tr>
                <tr>
                    <td><label><bean:message key="input.pswd"/></label></td><td><html:password property="pswd"/></td>
                </tr>
            </table>
            <p>
                <label>
                    <bean:message key="build.type"/>
                </label>
            </p>
            <dl>
                <dt>
                    <html:radio property="<%=UpdateIndicesForm.BUILDTYPE%>" value="<%=UpdateIndicesForm.BUILDFULL%>" />
                    <bean:message key="build.full"/>
                <dd>
                    <bean:message key="build.blackout"/>
                    <bean:write name="<%=UpdateIndicesForm.FORMNAME%>" property="blockedFRange" />
                <dt>
                    <html:radio property="<%=UpdateIndicesForm.BUILDTYPE%>" value="<%=UpdateIndicesForm.BUILDINC%>" />
                    <bean:message key="build.inc"/>
                    <bean:write name="<%=UpdateIndicesForm.FORMNAME%>" property="lastUpdate" />
                    <bean:message key="build.timeformat"/>
                <dd>
                    <bean:message key="build.blackout"/>
                    <bean:write name="<%=UpdateIndicesForm.FORMNAME%>" property="blockedIRange" />
            </dl>
            <p style="font-weight: bold; color: #cc0000">
                <html:checkbox property="confirm" value="Y"/>&nbsp;<bean:message key="build.confirm"/>
            </p>
            <p>
                <html:submit property="submitBtn"/>
            </p>
        </html:form>
    </body>
</html>
