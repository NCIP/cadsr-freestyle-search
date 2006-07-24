<!-- Copyright ScenPro, Inc. 2005
     $Header: /share/content/gforge/freestylesearch/freestylesearch/ui/jsp/freestylesearch.jsp,v 1.4 2006-07-24 14:55:21 hebell Exp $
     $Name: not supported by cvs2svn $
-->
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ page import="java.util.Vector" %>
<%@ page import="gov.nih.nci.cadsr.freestylesearch.ui.FreestyleSearch" %>

<html>
    <head>
        <title><bean:message key="search.title" /></title>
        <html:base />
        <meta http-equiv="Content-Language" content="en-us">
        <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=WINDOWS-1252">
        <LINK href="freestyle.css" rel="stylesheet" type="text/css">
        <script type="text/javascript">
            function doSearch()
            {
                workingmsg.innerHTML = "Searching, please wait...";
                freestyleForm.search.disabled = true;
            }
            function toggleOptions()
            {
                if (opts.style.display == "none")
                {
                    opts.style.display = "block";
                    freestyleForm.displayOptions.value = "Y";
                }
                else
                {
                    opts.style.display = "none";
                    freestyleForm.displayOptions.value = "N";
                }
            }
            function loaded()
            {
                if (freestyleForm.displayOptions.value == "Y")
                    toggleOptions();
            }
        </script>
    </head>

<body onload="loaded();">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor="#A90101">
              <tr bgcolor="#A90101">
              <td valign="center" align="left"><a href="http://www.cancer.gov" target="_blank" alt="NCI Logo">
              <img src="brandtype.gif" border="0"></a></td>
              <td valign="center" align="right"><a href="http://www.cancer.gov" target="_blank" alt="NCI Logo">
              <img src="tagline_nologo.gif" border="0"></a></td></tr>
              </table>
              <table class="secttable"><colgroup><col /></colgroup><tbody class="secttbody" />
              <tr><td><a target="_blank" href="http://ncicb.nci.nih.gov/NCICB/infrastructure/cacore_overview/cadsr"><img style="border: 0px solid black" title="NCICB caDSR" src="freestyle_banner.gif"></a></td></tr>
              <tr><td align="center"><p class="ttl18"><bean:message key="search.title"/></p></td></tr>
              </table>
    <html:form method="post" action="/search" focus="phrase" onsubmit="doSearch();">
        <p>This page is provided to exercise the freestyle search engine and offer a simple search into the NCICB Cancer
        Data Standards Repository (caDSR).</p><p>Multiple terms should be separated by spaces. All searches are case insensitive. Wild card characters are not used. Select
        the <b>Options...</b> button to configure specific search features.
        The supported record types are
<%
    String[] types = FreestyleSearch.getTypes();
    %><b><%=types[0]%><%
    for (int i = 1; i < types.length; ++i)
    {
        %>, <%=types[i]%><%
    }
%></b>. The attributes searched include the following where applicable
<%
    String[] colNames = FreestyleSearch.getColNames();
    %><b><%=colNames[0]%><%
    for (int i = 1; i < colNames.length; ++i)
    {
        %>, <%=colNames[i]%><%
    }
%></b>.</p>
        <p>The search index is current as of <%
        String seedTime = (String) pageContext.getRequest().getAttribute("seedTime");
        %><%=seedTime%></p>
        <html:hidden property="displayOptions"/>
        <html:hidden property="firstTime"/>
        <html:button property="optbutn" onclick="toggleOptions();" value="Options..." />
        <div id="opts" style="display: none"><hr>
        <table style="border-collapse: collapse"><tr>
        <td style="vertical-align: top">
        <p style="margin: 0.1in 0in 0in 0.2in">When matching words/tokens ...<br>
        <html:radio property="matching" value="0"/>&nbsp;Comparisons must be for the whole word.<br>
        <html:radio property="matching" value="1"/>&nbsp;Comparisons must be for any partial content.<br>
        <html:radio property="matching" value="2"/>&nbsp;Comparisons are first for the whole word and when no matches are found a comparison is done for any partial content.
        </p>
        <p style="margin: 0.2in 0in 0in 0.2in">Maximum number of possible results is <html:text property="limit" styleClass="std" style="width: 0.5in" maxlength="4"/>
        </p>
        <p style="margin: 0.2in 0in 0in 0.2in">Number of top score groups is <html:text property="score" styleClass="std" style="width: 0.5in" maxlength="3"/>
        </p>
        <p style="margin: 0.2in 0in 0in 0.2in"><html:checkbox property="excludeRetired" value="Y"/>&nbsp;Exclude the AC's with a "RETIRED" Workflow Status
        </p>
        </td>
        <td style="vertical-align: top; padding-left: 0.2in">
        <p style="margin: 0.1in 0in 0in 0.2in">Restrict the results to the type(s) ...<br>
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
        <html:submit property="search" styleClass="but2">Search</html:submit><br/><span id="workingmsg" style="color: #0000ff; font-weight: bold">&nbsp;</span></p><hr/>
        <html:errors />
<%
    Vector results = (Vector) pageContext.getRequest().getAttribute(FreestyleSearch._results);
    if (results != null)
    {
        %><p style="margin-left: 0.2in"><b><%=results.size()%>
        Results</b></p>
        <dl style="margin-left: 0.2in"><%
        for (int i = 0; i < results.size();)
        {
            String text = (String)results.get(i++);
            text = "<b>" + i + ")</b> " + text.replaceFirst("\n\t", "<dd>");
            text = text.replace("\n\tScore: ", "\n\t<span style=\"color: #aaaaaa\">Score: ") + "</span>";
            text = text.replace("\n\t", "<br/>");
            %><dt><%=text%>
            <%
        }
        %></dl><%
    }
%>
    </html:form>

</body>
</html>
