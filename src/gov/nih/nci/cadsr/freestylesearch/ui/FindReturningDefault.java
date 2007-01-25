// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/FindReturningDefault.java,v 1.2 2007-01-25 20:24:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.ui;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Vector;
import gov.nih.nci.cadsr.freestylesearch.tool.SearchRequest;
import gov.nih.nci.cadsr.freestylesearch.util.Search;
import gov.nih.nci.cadsr.freestylesearch.util.SearchException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * This class supports the remote request made through the Search.findReturningDefault() method
 * when Search.setDataDescription(URL) has been used.
 * 
 * @author lhebel
 *
 */
public class FindReturningDefault extends Action
{
    /**
     * Constructor
     *
     */
    public FindReturningDefault()
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
        Search var = common(request_);
        
        // Copy settings from the form
        RemoteForm form = (RemoteForm) form_;
        form.loadSearch(var);

        Vector<String> results = null;
        try
        {
            // Perform the search.
            results = var.findReturningDefault(form.getPhrase());
        }
        catch(SearchException ex)
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
            switch (vers)
            {
                case 1:
                {
                    response_.setStatus(HttpURLConnection.HTTP_OK);
                    PrintWriter out = response_.getWriter();
                    for (String line : results)
                    {
                        String temp = line.replace("\n", "<br/>");
                        out.println(temp);
                    }
                    out.close();
                    break;
                }
                case 2:
                {
                    response_.setStatus(HttpURLConnection.HTTP_OK);
                    PrintWriter out = response_.getWriter();
                    for (String line : results)
                    {
                        String temp = line.replace("\n", "<br/>");
                        out.println(SearchRequest.TEXT + temp);
                    }
                    out.close();
                    break;
                }
                default:
                {
                    response_.setStatus(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
                    break;
                }
            }
        }
        catch (java.io.IOException ex)
        {
            _logger.error(ex.toString());
        }
        
        return null;
    }
    
    /**
     * Common setup for remote requests
     * 
     * @param request_ the request
     * @return the Search object for processing
     */
    public static Search common(HttpServletRequest request_)
    {
        // Initialize local data.
        FreestylePlugIn ds = (FreestylePlugIn) request_.getSession().getServletContext().getAttribute(FreestylePlugIn._DATASOURCE);

        // Create a search object.
        Search var = new Search();
        
        // Set database connectivity.
        var.setDataDescription(ds.getDataSource(), ds.getUser(), ds.getPswd());
        
        return var;
    }

    private static final Logger _logger = Logger.getLogger(FindReturningDefault.class.getName());
}
