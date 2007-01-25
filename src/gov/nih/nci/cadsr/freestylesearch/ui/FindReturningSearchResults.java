// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/FindReturningSearchResults.java,v 1.2 2007-01-25 20:24:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.ui;

import gov.nih.nci.cadsr.freestylesearch.tool.SearchRequest;
import gov.nih.nci.cadsr.freestylesearch.util.Search;
import gov.nih.nci.cadsr.freestylesearch.util.SearchException;
import gov.nih.nci.cadsr.freestylesearch.util.SearchResults;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * This class supports the remote request made through the Search.findReturningSearchResults() method
 * when Search.setDataDescription(URL) has been used.
 * 
 * @author lhebel
 *
 */
public class FindReturningSearchResults extends Action
{
    /**
     * Constructor
     *
     */
    public FindReturningSearchResults()
    {
    }

    /**
     * Action process to Edit an Alert Definition.
     * 
     * @param mapping_
     *        The action map from the struts-config.xml.
     * @param form_
     *        The form bean for the edit.jsp page.
     * @param request_
     *        The servlet request object.
     * @param response_
     *        The servlet response object.
     * @return The action to continue processing.
     */
    @Override
    public ActionForward execute(ActionMapping mapping_, ActionForm form_,
        HttpServletRequest request_, HttpServletResponse response_)
    {
        Search var = FindReturningDefault.common(request_);
        
        // Copy settings from the form
        RemoteForm form = (RemoteForm) form_;
        form.loadSearch(var);
        
        Vector<SearchResults> results = null;
        try
        {
            // Perform the search.
            results = var.findReturningSearchResults(form.getPhrase());
        }
        catch (SearchException ex)
        {
            response_.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            try
            {
                PrintWriter out = response_.getWriter();
                out.println(ex.toString());
                out.close();
            }
            catch (java.io.IOException ex2)
            {
                _logger.error(ex2.toString());
            }
            return null;
        }

        try
        {
            // Give a response the caller can understand.
            int vers = form.getVersionInt();
            if (vers == 1 || vers == 2)
            {
                response_.setStatus(HttpURLConnection.HTTP_OK);
                PrintWriter out = response_.getWriter();
                for (SearchResults line : results)
                {
                    out.println(SearchRequest.TYPE + line.getType());
                    out.println(SearchRequest.LNAME + line.getLongName().replace("\n", "<br/>"));
                    out.println(SearchRequest.PNAME + line.getPreferredName().replace("\n", "<br/>"));
                    out.println(SearchRequest.ID + line.getPublicID());
                    out.println(SearchRequest.VERS + line.getVersion());
                    out.println(SearchRequest.PDEF + line.getPreferredDefinition().replace("\n", "<br/>"));
                    out.println(SearchRequest.CNAME + line.getContextName());
                    out.println(SearchRequest.REG + line.getRegistrationStatus());
                    out.println(SearchRequest.WFS + line.getWorkflowStatus());
                    out.println(SearchRequest.RECEND);
                }
                out.close();
            }
            else
            {
                response_.setStatus(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
            }
        }
        catch (java.io.IOException ex)
        {
            _logger.error(ex.toString());
        }
        
        return null;
    }

    private static final Logger _logger = Logger.getLogger(FindReturningSearchResults.class.getName());
}
