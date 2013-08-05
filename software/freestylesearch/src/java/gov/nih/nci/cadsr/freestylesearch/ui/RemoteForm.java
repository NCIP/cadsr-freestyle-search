/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/RemoteForm.java,v 1.2 2007-01-25 20:24:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.ui;

import javax.servlet.http.HttpServletRequest;
import gov.nih.nci.cadsr.freestylesearch.util.Search;
import gov.nih.nci.cadsr.freestylesearch.util.SearchMatch;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * This class maps the data sent by a remote request to the Freestyle server. Although the
 * results of each request may vary greatly, the data required by the request is always the
 * same.
 * 
 * @author lhebel
 *
 */
public class RemoteForm extends ActionForm
{
    /**
     * Constructor
     *
     */
    public RemoteForm()
    {
        _phrase = null;
        _ewfsr = "false";
        _limit = "5";
        _highest = "5";
        _match = "BEST";
        _rall = "true";
        _restrict = null;
        _version = "1";
        _xtest = "false";
        _xtrain = "false";
    }
    
    /**
     * Get the Exclude Test Context setting
     * 
     * @return "true" to exclude "Test"
     */
    public String getXtest()
    {
        return _xtest;
    }
    
    /**
     * Get the Exclude Test Context setting
     * 
     * @return "true" to exclude "Test"
     */
    public boolean getXtestBool()
    {
        return _xtest.equals("true");
    }
    
    /**
     * Set the Exclude Test Context setting
     * 
     * @param val_ "true" to exclude "Test"
     */
    public void setXtest(String val_)
    {
        _xtest = val_;
    }
    
    /**
     * Get the Exclude Training Context setting
     * 
     * @return "true" to exclude "Training"
     */
    public String getXtrain()
    {
        return _xtrain;
    }
    
    /**
     * Get the Exclude Training Context setting
     * 
     * @return "true" to exclude "Training"
     */
    public boolean getXtrainBool()
    {
        return _xtrain.equals("true");
    }
    
    /**
     * Set the Exclude Training Context setting
     * 
     * @param val_ "true" to exclude "Training"
     */
    public void setXtrain(String val_)
    {
        _xtrain = val_;
    }

    /**
     * Get the search phrase
     * 
     * @return search phrase
     */
    public String getPhrase()
    {
        return _phrase;
    }

    /**
     * Set the search phrase
     * 
     * @param val_ the phrase
     */
    public void setPhrase(String val_)
    {
        _phrase = val_;
    }
    
    /**
     * Get the Exclude WFS Retired value
     * 
     * @return the flag as a String
     */
    public String getEwfsr()
    {
        return _ewfsr;
    }
    
    /**
     * Set the Exclude WFS Retired value
     * 
     * @param val_ the value
     */
    public void setEwfsr(String val_)
    {
        _ewfsr = val_;
    }
    
    /**
     * Get the Exlucde WFS Retired value
     * 
     * @return boolean representation
     */
    public boolean getEwfsrBool()
    {
        return _ewfsr.equals("true");
    }
    
    /**
     * Get the Limit
     * 
     * @return the value
     */
    public String getLimit()
    {
        return _limit;
    }

    /**
     * Set the limit
     * 
     * @param val_ the limit
     */
    public void setLimit(String val_)
    {
        _limit = val_;
    }

    /**
     * Get the limit as an int
     * 
     * @return the limit
     */
    public int getLimitInt()
    {
        return Integer.valueOf(_limit);
    }
    
    /**
     * Get the Results by Score value
     * 
     * @return the value
     */
    public String getHighest()
    {
        return _highest;
    }
    
    /**
     * Set the Results by Score
     * 
     * @param val_ the value
     */
    public void setHighest(String val_)
    {
        _highest = val_;
    }
    
    /**
     * Get the Results by Score as an int
     * 
     * @return the value
     */
    public int getHighestInt()
    {
        return Integer.valueOf(_highest);
    }
    
    /**
     * Get the match flag
     * 
     * @return the match flag
     */
    public String getMatch()
    {
        return _match;
    }
    
    /**
     * Set the Match flag
     * 
     * @param val_ the value
     */
    public void setMatch(String val_)
    {
        _match = val_;
    }
    
    /**
     * Get the Match flag as an enumerator
     * 
     * @return the value
     */
    public SearchMatch getMatchEnum()
    {
        return SearchMatch.valueOf(_match);
    }
    
    /**
     * Get the Restrict All flag
     * 
     * @return the flag
     */
    public String getRall()
    {
        return _rall;
    }
    
    /**
     * Set the Restrict All flag
     * 
     * @param val_ the value
     */
    public void setRall(String val_)
    {
        _rall = val_;
    }
    
    /**
     * Get the Restrict All flag as a boolean
     * 
     * @return the value
     */
    public boolean getRallBool()
    {
        return _rall.equals("true");
    }
    
    /**
     * Get the Restrict list. The first character is always a comma ","
     * 
     * @return the list as concatenated comma separated values.
     */
    public String getRestrict()
    {
        return _restrict;
    }
    
    /**
     * Set the Restrict list.
     * 
     * @param val_ the value
     */
    public void setRestrict(String val_)
    {
        _restrict = val_;
    }

    /**
     * Get the Restrict list as an array of int
     * 
     * @return the list
     */
    public int[] getRestrictInt()
    {
        if (_restrict == null)
            return null;

        String[] list = _restrict.substring(1).split(",");
        int[] vals = new int[list.length];
        for (int i = 0; i < list.length; ++i)
        {
            vals[i] = Integer.valueOf(list[i]);
        }
        return vals;
    }
    
    /**
     * Get the interface version
     * 
     * @return the version
     */
    public String getVersion()
    {
        return _version;
    }
    
    /**
     * Set the interface version
     * 
     * @param val_ the version
     */
    public void setVersion(String val_)
    {
        _version = val_;
    }
    
    /**
     * Get the interface version
     * 
     * @return the version
     */
    public int getVersionInt()
    {
        return Integer.valueOf(_version);
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
    public ActionErrors validate(ActionMapping mapping_, HttpServletRequest request_)
    {
        ActionErrors errors = new ActionErrors();
        
        return errors;
    }
    
    /**
     * Load the search object with the values on this form
     * 
     * @param sobj_ the search object
     */
    public void loadSearch(Search sobj_)
    {
        // Set search options.
        sobj_.restrictResultsByScore(getHighestInt());
        sobj_.setResultsLimit(getLimitInt());
        sobj_.setMatchFlag(getMatchEnum());
        if (getEwfsrBool())
            sobj_.excludeWorkflowStatusRetired(true);
        if (getXtestBool())
            sobj_.excludeTest(true);
        if (getXtrainBool())
            sobj_.excludeTraining(true);
        sobj_.restrictResultsByType(getRestrictInt());
    }

    private String _phrase;
    private String _ewfsr;
    private String _limit;
    private String _highest;
    private String _match;
    private String _rall;
    private String _restrict;
    private String _version;
    private String _xtest;
    private String _xtrain;

    private static final long serialVersionUID = -3462090858792943710L;
}
