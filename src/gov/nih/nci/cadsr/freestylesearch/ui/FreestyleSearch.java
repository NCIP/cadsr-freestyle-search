// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/FreestyleSearch.java,v 1.2 2007-01-25 20:24:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.ui;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import gov.nih.nci.cadsr.freestylesearch.util.Search;
import gov.nih.nci.cadsr.freestylesearch.util.SearchAC;
import gov.nih.nci.cadsr.freestylesearch.util.SearchException;


/**
 * The Struts Action class for the independant UI to Freestyle.
 * 
 * @author lhebel Mar 7, 2006
 */
public class FreestyleSearch extends Action
{
    /**
     * Constructor
     *
     */
    public FreestyleSearch()
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
        // Initialize local data.
        FreestyleSearchForm form = (FreestyleSearchForm) form_;
        FreestylePlugIn ds = (FreestylePlugIn) request_.getSession().getServletContext().getAttribute(FreestylePlugIn._DATASOURCE);

        // Create a search object.
        Search var = new Search();
        
        // Set database connectivity.
        var.setDataDescription(ds.getDataSource(), ds.getUser(), ds.getPswd());
        
        // Set search options.
        var.restrictResultsByScore(form.getScoreInt());
        var.setResultsLimit(form.getLimitInt());
        var.setMatchFlag(form.getMatchingEnum());
        if (form.getExcludeRetiredBool())
            var.excludeWorkflowStatusRetired(true);
        if (form.getExcludeTestBool())
            var.excludeTest(true);
        if (form.getExcludeTrainBool())
            var.excludeTraining(true);

        // Set the type restrictions for the search.
        String restricts = "";
        boolean[] types = form.getTypes();
        for (int i = 0; i < types.length; ++i)
        {
            if (types[i])
            {
                SearchAC temp = SearchAC.valueOf(i);
                var.restrictResultsByType(temp);
                restricts += ", " + temp.toString();
            }
        }
        if (restricts.length() == 0)
            _logger.debug("No AC types selected.");
        else
            _logger.debug("AC types selected " + restricts.substring(2));

        // Perform the search.
        Vector<String> results = null;
        try
        {
            results = var.findReturningDefault(form.getPhrase());
        }
        catch (SearchException ex)
        {
            results = new Vector<String>();
            results.add(ex.toString());
        }
        
        // Make the results available to the JSP.
        request_.setAttribute(_results, results);
        return new ActionForward("/jsp/freestylesearch.jsp");
    }
    
    /**
     * Get the supported list of AC types.
     * 
     * @return the names of the supported types
     */
    public static String[] getTypes()
    {
        return Search.getTypes();
    }
    
    /**
     * Get the supported columns for all supported AC types.
     * 
     * @return the column names
     */
    public static String[] getColNames()
    {
        return Search.getColNames();
    }
    
    /**
     * Define a common attribute name for the search results.
     */
    public static final String _results = "searchResults";
    
    private static final Logger _logger = Logger.getLogger(FreestyleSearch.class.getName());
}
