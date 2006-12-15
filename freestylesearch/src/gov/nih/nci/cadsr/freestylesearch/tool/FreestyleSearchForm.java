// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/FreestyleSearchForm.java,v 1.2 2006-07-10 18:40:32 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

import gov.nih.nci.cadsr.freestylesearch.util.Search;
import gov.nih.nci.cadsr.freestylesearch.util.SearchAC;
import gov.nih.nci.cadsr.freestylesearch.util.SearchMatch;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/**
 * The ActionForm mapping the JSP used by this example user interface.
 * 
 * @author lhebel Mar 7, 2006
 */
public class FreestyleSearchForm extends ActionForm
{
    /**
     * Constructor
     *
     */
    public FreestyleSearchForm()
    {
        // Set defaults for all data.
        _excludeRetired = false;
        _firstTime = "Y";
        _displayOptions = "N";
        _phrase = "";
        _limit = 100;
        _score = 3;
        _matching = SearchMatch.BEST;
        _types = new boolean[SearchAC.count()];
        for (int i = 0; i < _types.length; ++i)
            _types[i] = true;
    }
    
    /**
     * Set the search phrase input field.
     * 
     * @param val_ the search phrase, i.e. one or more terms
     */
    public void setPhrase(String val_)
    {
        _phrase = val_;
    }

    /**
     * Get the input search phrase.
     * 
     * @return return the user search phrase
     */
    public String getPhrase()
    {
        return _phrase;
    }
    
    /**
     * Get the maximum results limit.
     * 
     * @return the current maximum return limit.
     */
    public String getLimit()
    {
        return String.valueOf(_limit);
    }
    
    /**
     * Get the maximum results limit.
     * 
     * @return the current maximum return limit.
     */
    public int getLimitInt()
    {
        return _limit;
    }

    /**
     * Set the maximum results limit.
     * 
     * @param limit_ the limit for the result set.
     */
    public void setLimit(String limit_)
    {
        _limit = Integer.parseInt(limit_);
    }
    
    /**
     * Get the term comparison mode.
     * 
     * @return the term comparison mode, i.e. exact, partial or best.
     */
    public String getMatching()
    {
        return String.valueOf(_matching.toInt());
    }
    
    /**
     * Get the term comparison mode.
     * 
     * @return the term comparison mode, i.e. exact, partial or best.
     */
    public SearchMatch getMatchingEnum()
    {
        return _matching;
    }

    /**
     * Set the term comparison mode.
     * 
     * @param matching_ the integer term comparison as defined in SearchMatch.toInt().
     */
    public void setMatching(String matching_)
    {
        _matching = SearchMatch.valueOf(Integer.parseInt(matching_));
    }
    
    /**
     * Get the score group count limit.
     * 
     * @return the score group count limit
     */
    public String getScore()
    {
        return String.valueOf(_score);
    }
    
    /**
     * Get the score group count limit.
     * 
     * @return the score group count limit.
     */
    public int getScoreInt()
    {
        return _score;
    }

    /**
     * Set the score group count limit.
     * 
     * @param score_ the score group count limit.
     */
    public void setScore(String score_)
    {
        _score = Integer.parseInt(score_);
    }
    
    /**
     * Get the display options flag, used to control the "Options" on the user interface.
     * 
     * @return the display options flag
     */
    public String getDisplayOptions()
    {
        return _displayOptions;
    }
    
    /**
     * Set the display options flag, used to control the "Options" on the user interface.
     * 
     * @param opt_ the display options flag
     */
    public void setDisplayOptions(String opt_)
    {
        _displayOptions = opt_;
    }
    
    /**
     * Get the AC types selections.
     * 
     * @return the restriction settings by type
     */
    public boolean[] getTypes()
    {
        return _types;
    }
    
    /**
     * Get the first time flag, i.e. is this the first use of freestyle in the current browser session.
     * 
     * @return return the first time flag
     */
    public String getFirstTime()
    {
        return _firstTime;
    }
    
    /**
     * Set the browser session first time flag.
     * 
     * @param flag_ 'Y' for the first time, otherwise 'N'
     */
    public void setFirstTime(String flag_)
    {
        _firstTime = flag_;
    }
    
    /**
     * Set the exclude retired AC flag
     * 
     * @param flag_ "Y" to exclude reitred AC's, otherwise don't exclude them.
     */
    public void setExcludeRetired(String flag_)
    {
        if (flag_ != null && flag_.equals("Y"))
            _excludeRetired = true;
        else
            _excludeRetired = false;
    }

    /**
     * Get the exlude retired AC flag.
     * 
     * @return "Y" to exclude retired AC's.
     */
    public String getExcludeRetired()
    {
        return (_excludeRetired) ? "Y" : "N";
    }

    /**
     * Get the exlude retired AC flag.
     * 
     * @return "Y" to exclude retired AC's.
     */
    public boolean getExcludeRetiredBool()
    {
        return _excludeRetired;
    }

    /**
     * Validate the content of the Edit Screen.
     * 
     * @param mapping_
     *        The action map defined for Edit.
     * @param request_
     *        The servlet request object.
     * @return Any errors found.
     */
    public ActionErrors validate(ActionMapping mapping_,
        HttpServletRequest request_)
    {
        ActionErrors errors = new ActionErrors();
        FreestylePlugIn ds = (FreestylePlugIn) request_.getSession().getServletContext().getAttribute(FreestylePlugIn._DATASOURCE);

        Search var = new Search();
        var.setDataDescription(ds.getDataSource(), ds.getUser(), ds.getPswd());
        String seedTime = var.getLastSeedTimestampString();
        request_.setAttribute("seedTime", seedTime);

        // The absence of a search phrase is not really an error but we don't want
        // to proceed to the Action Class
        if (_phrase == null || _phrase.length() == 0)
        {
            errors.add("error", new ActionMessage("error.nosearch"));
        }

        // If this is not the first time so update the AC type selections.
        if (_firstTime.charAt(0) == 'N')
        {
            for(int i = 0; i < _types.length; ++i)
            {
                _types[i] =  (request_.getParameter("restrict" + i) != null);
            }
        }
        else
        {
            _firstTime = "N";
        }

        // Set the attributes for proper display on the UI Options.
        for( int i = 0; i < _types.length; ++i)
        {
            if (_types[i])
                request_.setAttribute("restrict" + i, "Y");
        }

        // Return
        return errors;
    }

    private static final long serialVersionUID = 88840366374682878L;

    private boolean _excludeRetired;
    private String _phrase;
    private int _limit;
    private SearchMatch _matching;
    private int _score;
    private String _displayOptions;
    private boolean[] _types;
    private String _firstTime;
}
