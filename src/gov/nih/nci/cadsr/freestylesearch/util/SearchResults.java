// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchResults.java,v 1.1 2006-08-30 20:31:23 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

/**
 * @author lhebel
 *
 */
public class SearchResults
{
    /**
     * Constructor
     * @param type_ 
     * @param lname_ 
     * @param pname_ 
     * @param id_ 
     * @param vers_ 
     * @param pdef_ 
     * @param cname_ 
     * @param reg_ 
     *
     */
    public SearchResults(SearchAC type_, String lname_, String pname_, int id_, String vers_, String pdef_, String cname_, String reg_)
    {
        _type = type_;
        _longName = lname_;
        _preferredName = pname_;
        _publicID = id_;
        _version = vers_;
        _preferredDefinition = pdef_;
        _contextName = cname_;
        _registrationStatus = reg_;
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

    private SearchAC _type;
    private String _longName;
    private String _preferredName;
    private int _publicID;
    private String _version;
    private String _preferredDefinition;
    private String _contextName;
    private String _registrationStatus;
}
