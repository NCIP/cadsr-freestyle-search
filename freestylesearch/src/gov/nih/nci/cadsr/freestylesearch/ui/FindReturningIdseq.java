// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/FindReturningIdseq.java,v 1.1 2006-12-12 15:24:53 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.ui;

import gov.nih.nci.cadsr.freestylesearch.util.Search;
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
        
        // Perform the search.
        Vector<SearchResultObject> results = var.findReturningResultSet(form.getPhrase());

        try
        {
            // Give a response the caller can understand.
            if (form.getVersionInt() == 1)
            {
                response_.setStatus(HttpURLConnection.HTTP_OK);
                PrintWriter out = response_.getWriter();
                for (SearchResultObject line : results)
                {
                    out.println(line.getIdseq());
                }
                out.close();
            }
        }
        catch (java.io.IOException ex)
        {
            _logger.fatal(ex.toString());
        }
        
        return null;
    }

    private static final Logger _logger = Logger.getLogger(FindReturningIdseq.class.getName());
}
