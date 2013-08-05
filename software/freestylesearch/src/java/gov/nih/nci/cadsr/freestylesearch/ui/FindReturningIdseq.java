/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/FindReturningIdseq.java,v 1.3 2007-05-14 15:25:47 hebell Exp $
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
 * This class supports the remote request made through the Search.findReturningAdministeredComponent() method
 * when Search.setDataDescription(URL) has been used. Unlike the other remote services this service returns
 * only the database id's of the matching records. The client then calls the caCORE API using those id's to retrieve
 * the record details.
 * 
 * @author lhebel
 *
 */
public class FindReturningIdseq extends Action
{
    /**
     * Constructor
     *
     */
    public FindReturningIdseq()
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
            PrintWriter out = null;
            try
            {
                out = response_.getWriter();
                out.println(ex.toString());
            }
            catch (java.io.IOException ex2)
            {
                _logger.error(ex2.toString());
            }
            finally
            {
                if (out != null)
                    out.close();
            }
            return null;
        }

        PrintWriter out = null;
        try
        {
            // Give a response the caller can understand.
            out = response_.getWriter();
            int vers = form.getVersionInt(); 
            switch (vers)
            {
                case 1:
                {
                    response_.setStatus(HttpURLConnection.HTTP_OK);
                    for (SearchResultObject line : results)
                    {
                        out.println(line.getIdseq());
                    }
                    break;
                }
                case 2:
                {
                    try
                    {
                        String url = var.getDsrCoreUrl();
                        response_.setStatus(HttpURLConnection.HTTP_OK);
                        out.println(SearchRequest.CACOREURL + url);
                        for (SearchResultObject line : results)
                        {
                            out.println(SearchRequest.IDSEQ + line.getIdseq());
                        }
                    }
                    catch (SearchException ex)
                    {
                        response_.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
                        out.println(ex.toString());
                    }
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
            response_.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            _logger.error(ex.toString());
        }
        finally
        {
            if (out != null)
                out.close();
        }
        
        return null;
    }

    private static final Logger _logger = Logger.getLogger(FindReturningIdseq.class.getName());
}
