// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchResults.java,v 1.4 2007-01-25 20:24:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

/**
 * Map the search results to the generic AC attributes.
 * 
 * @author lhebel
 */
public class SearchResults
{
    /**
     * Constructor
     * 
     * @param type_ the AC type
     * @param lname_ the long name
     * @param pname_ the preferred name
     * @param id_ the public id
     * @param vers_ the version
     * @param pdef_ the preferred definition
     * @param cname_ the context name
     * @param reg_ the registration status
     * @param wfs_ the workflow status
     *
     */
    public SearchResults(SearchAC type_, String lname_, String pname_, int id_, String vers_, String pdef_, String cname_, String reg_, String wfs_)
    {
        _type = type_;
        _longName = lname_;
        _preferredName = pname_;
        _publicID = id_;
        _version = vers_;
        _preferredDefinition = pdef_;
        _contextName = cname_;
        _registrationStatus = reg_;
        _workflowStatus = wfs_;
    }

    /**
     * The AC type
     * 
     * @return the AC type
     */
    public SearchAC getType()
    {
        return _type;
    }
    
    /**
     * The AC Long Name
     * 
     * @return the long name
     */
    public String getLongName()
    {
        return _longName;
    }
    
    /**
     * The AC Preferred Name
     * 
     * @return the preferred name
     */
    public String getPreferredName()
    {
        return _preferredName;
    }
    
    /**
     * The AC Public ID
     * 
     * @return the public id
     */
    public int getPublicID()
    {
        return _publicID;
    }
    
    /**
     * The AC Version
     * 
     * @return the version
     */
    public String getVersion()
    {
        return _version;
    }
    
    /**
     * The AC Preferred Definition
     * 
     * @return the preferred definition
     */
    public String getPreferredDefinition()
    {
        return _preferredDefinition;
    }
    
    /**
     * The AC Context Name
     * 
     * @return the context name
     */
    public String getContextName()
    {
        return _contextName;
    }
    
    /**
     * The AC Registration Status
     * 
     * @return the registration status
     */
    public String getRegistrationStatus()
    {
        return _registrationStatus;
    }
    
    /**
     * The AC Workflow Status
     * 
     * @return the workflow status
     */
    public String getWorkflowStatus()
    {
        return _workflowStatus;
    }

    private SearchAC _type;
    private String _longName;
    private String _preferredName;
    private int _publicID;
    private String _version;
    private String _preferredDefinition;
    private String _contextName;
    private String _registrationStatus;
    private String _workflowStatus;
}
