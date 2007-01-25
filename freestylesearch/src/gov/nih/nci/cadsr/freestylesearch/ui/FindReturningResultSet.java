// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/FindReturningResultSet.java,v 1.1 2007-01-25 20:24:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.ui;

import gov.nih.nci.cadsr.freestylesearch.tool.SearchRequest;
import gov.nih.nci.cadsr.freestylesearch.util.Search;
import gov.nih.nci.cadsr.freestylesearch.util.SearchException;
import gov.nih.nci.cadsr.freestylesearch.util.SearchResultObject;
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
 * @author lhebel
 *
 */
public class FindReturningResultSet extends Action
{
    /**
     * Constructor
     *
     */
    public FindReturningResultSet()
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
        
        Vector<SearchResultObject> results = null;
        try
        {
            // Perform the search.
            results = var.findReturningResultSet(form.getPhrase());
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
            if (form.getVersionInt() == 1)
            {
                try
                {
                    String url = var.getDsrCoreUrl();
                    response_.setStatus(HttpURLConnection.HTTP_OK);
                    PrintWriter out = response_.getWriter();
                    
                    out.println(SearchRequest.CACOREURL + url);
                    for (SearchResultObject line : results)
                    {
                        out.println(SearchRequest.TYPE + line.getType());
                        out.println(SearchRequest.IDSEQ + line.getIdseq());
                        out.println(SearchRequest.SCORE + line.getScore());
                        out.println(SearchRequest.RECEND);
                    }
                    out.close();
                }
                catch (SearchException ex)
                {
                    response_.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
                    PrintWriter out = response_.getWriter();
                    out.println(ex.toString());
                    out.close();
                }
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

    private static final Logger _logger = Logger.getLogger(FindReturningResultSet.class.getName());
}
