<!-- Copyright ScenPro, Inc. 2005
     $Header: /share/content/gforge/freestylesearch/freestylesearch/WebRoot/jsp/freestylesearch.jsp,v 1.12 2009-04-01 12:22:05 hebell Exp $
     $Name: not supported by cvs2svn $
-->
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ page import="java.util.Vector" %>
<%@ page import="gov.nih.nci.cadsr.freestylesearch.ui.FreestyleSearch" %>
<%@ page import="gov.nih.nci.cadsr.freestylesearch.util.SearchResults" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title><bean:message key="search.title" /></title>
        <html:base />
        <meta http-equiv="Content-Language" content="en-us">
        <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=WINDOWS-1252">
        <LINK href="/freestyle/css/freestyle.css" rel="stylesheet" type="text/css">
        <style>
            TD { vertical-align: top }
        </style>
        <script type="text/javascript">
            <!--
            function doSearch()
            {
                var obj = document.getElementsByName("limit");
                var limit = obj[0];
                if (limit.value == null || limit.value.length == 0 || isNaN(limit.value))
                {
                    alert("The maximum number of possible results is not valid, please correct.");
                    limit.focus();
                    return;
                }
                obj = document.getElementsByName("score");
                var score = obj[0];
                if (score.value == null || score.value.length == 0 || isNaN(score.value))
                {
                    alert("The number of top score groups is not valid, please correct.");
                    score.focus();
                    return;
                }
                var msg = document.getElementById("workingmsg");
                msg.innerHTML = "Searching, please wait...";
                
                obj = document.getElementsByName("search");
                obj[0].disabled = true;
                obj = document.getElementsByName("freestyleForm");
                obj[0].submit();
            }
            function toggleOptions()
            {
                var opts = document.getElementById("opts");
                var obj = document.getElementsByName("displayOptions");
                var dis = obj[0];
                if (opts.style.display == "none")
                {
                    opts.style.display = "block";
                    dis.value = "Y";
                }
                else
                {
                    opts.style.display = "none";
                    dis.value = "N";
                }
            }
            function loaded()
            {
                var dis = document.getElementsByName("displayOptions");
                if (dis[0].value == "Y")
                    toggleOptions();
            }
            function checkEnter(evnt)
            {
                if (evnt.keyCode == 13)
                    doSearch();
            }
            // -->
        </script>
    </head>

<body onload="loaded();" onkeyup="checkEnter(event);">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor="#A90101">
              <tr bgcolor="#A90101">
              <td valign="center" align="left"><a href="http://www.cancer.gov" target="_blank" alt="NCI Logo">
              <img src="/freestyle/images/brandtype.gif" border="0"></a></td>
              <td valign="center" align="right"><a href="http://www.cancer.gov" target="_blank" alt="NCI Logo">
              <img src="/freestyle/images/tagline_nologo.gif" border="0"></a></td></tr>
              </table>
              <table class="secttable"><colgroup><col /></colgroup><tbody class="secttbody" />
              <tr><td><a target="_blank" href="http://ncicb.nci.nih.gov/NCICB/infrastructure/cacore_overview/cadsr"><img style="border: 0px solid black" title="NCICB caDSR" src="/freestyle/images/freestyle_banner.gif"></a></td></tr>
              <tr><td align="center"><p class="ttl18"><bean:message key="search.title"/></p></td></tr>
              </table>
    <html:form method="post" action="/search" focus="phrase">
        <html:hidden property="displayOptions"/>
        <html:hidden property="firstTime"/>
        <p><bean:message key="input.intro0"/></p>
        <p><bean:message key="input.intro1"/>
<%
    String[] types = FreestyleSearch.getTypes();
    %><b><%=types[0]%><%
    for (int i = 1; i < types.length; ++i)
    {
        %>, <%=types[i]%><%
    }
%></b><bean:message key="input.intro2"/>
<%
    String[] colNames = FreestyleSearch.getColNames();
    %><b><%=colNames[0]%><%
    for (int i = 1; i < colNames.length; ++i)
    {
        %>, <%=colNames[i]%><%
    }
%></b><bean:message key="input.intro3"/> <%
        String seedTime = (String) pageContext.getRequest().getAttribute("seedTime");
        %><%=seedTime%></p>
        <html:button property="optbutn" onclick="toggleOptions();"><bean:message key="option.btn"/></html:button>
        <div id="opts" style="display: none"><hr>
        <table style="border-collapse: collapse"><tr>
        <td style="vertical-align: top">
        <p style="margin: 0.1in 0in 0in 0.2in"><bean:message key="option.matching"/><br>
        <html:radio property="matching" value="0"/>&nbsp;<bean:message key="option.matching0"/><br>
        <html:radio property="matching" value="1"/>&nbsp;<bean:message key="option.matching1"/><br>
        <html:radio property="matching" value="2"/>&nbsp;<bean:message key="option.matching2"/>
        </p>
        <p style="margin: 0.2in 0in 0in 0.2in"><bean:message key="option.results"/> <html:text property="limit" styleClass="std" style="width: 0.5in" maxlength="4"/>
        </p>
        <p style="margin: 0.2in 0in 0in 0.2in"><bean:message key="option.scores"/> <html:text property="score" styleClass="std" style="width: 0.5in" maxlength="3"/>
        </p>
        <p style="margin: 0.2in 0in 0in 0.2in"><html:checkbox property="excludeRetired" value="Y"/>&nbsp;<bean:message key="option.exclude1"/>
        </p>
        <p style="margin: 0.2in 0in 0in 0.2in"><html:checkbox property="excludeTest" value="Y"/>&nbsp;<bean:message key="option.exclude2"/>
        </p>
        <p style="margin: 0.2in 0in 0in 0.2in"><html:checkbox property="excludeTrain" value="Y"/>&nbsp;<bean:message key="option.exclude3"/>
        </p>
        </td>
        <td style="vertical-align: top; padding-left: 0.2in">
        <p style="margin: 0.1in 0in 0in 0.2in"><bean:message key="option.restrict"/><br>
<%
    for (int i = 0; i < types.length; ++i)
    {
        String restrict = "restrict" + i;
        String rtype = (String) pageContext.getRequest().getAttribute(restrict);
        String checked = (rtype != null) ? " checked " : "";
        %><input type="checkbox" name="<%=restrict%>" value="Y"<%=checked%>/>&nbsp;<%=types[i]%><br><%
    }
%></p>
        </td>
        </tr></table>
        <hr></div>
        <p style="text-align: center"><html:text property="phrase" styleClass="std" style="width: 5in" />&nbsp;&nbsp;
        <html:button property="search" styleClass="but2" onclick="doSearch();"><bean:message key="search.btn"/></html:button><br/><span id="workingmsg" style="color: #0000ff; font-weight: bold">&nbsp;</span></p><hr/>
        <html:errors />
<%
    Vector results = (Vector) pageContext.getRequest().getAttribute(FreestyleSearch._results);
    String detailsLink = (String) pageContext.getRequest().getAttribute(FreestyleSearch._detailsLink);
    String pattern = "<tr$STRIPE$>"
        + "<td><a href=\"" + detailsLink + "\" target=\"_blank\">Details</a></td>"
        + "<td>$NAME$</td>"
        + "<td>$TYPE$</td>"
        + "<td>$PID$</td>"
        + "<td>$VERS$</td>"
        + "<td>$CONT$</td>"
        + "<td>$WFS$</td>"
        + "<td>$REG$</td>"
        + "</tr>";
    if (results != null)
    {
        %><p style="margin-left: 0.2in"><b><%=results.size()%>
        Results</b></p>
        <table>
        <colgroup>
        <col style="padding: 0.05in 0.05in 0.05in 0.05in"/>
        <col style="padding: 0.05in 0.05in 0.05in 0.05in"/>
        <col style="padding: 0.05in 0.05in 0.05in 0.05in"/>
        <col style="padding: 0.05in 0.05in 0.05in 0.05in"/>
        <col style="padding: 0.05in 0.05in 0.05in 0.05in"/>
        <col style="padding: 0.05in 0.05in 0.05in 0.05in"/>
        <col style="padding: 0.05in 0.05in 0.05in 0.05in"/>
        <col style="padding: 0.05in 0.05in 0.05in 0.05in"/>
        </colgroup>
        <tr><th>Link</th><th>Long Name</th><th>Type</th><th>Public ID</th><th>Version</th><th>Context</th><th>Workflow Status</th><th>Registration Status</th></tr>
        <%
        for (int i = 0; i < results.size();)
        {
            SearchResults rec = (SearchResults)results.get(i++);
            String text = pattern.replace("$NAME$", StringEscapeUtils.escapeHtml(rec.getLongName()));
            text = text.replace("$TYPE$", StringEscapeUtils.escapeHtml(rec.getType().getName()));
            text = text.replace("$PID$", StringEscapeUtils.escapeHtml(String.valueOf(rec.getPublicID())));
            text = text.replace("$VERS$", StringEscapeUtils.escapeHtml(rec.getVersion()));
            text = text.replace("$CONT$", StringEscapeUtils.escapeHtml(rec.getContextName()));
            text = text.replace("$WFS$", StringEscapeUtils.escapeHtml(rec.getWorkflowStatus()));
            text = text.replace("$REG$", StringEscapeUtils.escapeHtml(rec.getRegistrationStatus()));
            text = text.replace("$STRIPE$", ((i % 2) == 1) ? " style=\"background-color: #cccccc\"" : "");
            // text = text.replace("$SCORE$", rec.getScore());
            // text = "<b>" + i + ")</b> " + text.replaceFirst("\n\t", "<dd>");
            // text = text.replace("\n\tScore: ", "\n\t<span style=\"color: #aaaaaa\">Score: ") + "</span>";
            // text = text.replace("\n\t", "<br/>");
            %><%=text%>
            <%
        }
        %></table><%
    }
%>
    <bean:write name="freestyleForm" property="footer" filter="false" />
    </html:form>

</body>
</html>
