/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchResults.java,v 1.5 2007-07-13 16:25:06 hebell Exp $
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
     * @param acID_ the public id
     * @param acVers_ the version
     * @param pdef_ the preferred definition
     * @param cname_ the context name
     * @param reg_ the registration status
     * @param wfs_ the workflow status
     *
     */
    public SearchResults(SearchAC type_, String lname_, String pname_, int acID_, String acVers_, String pdef_, String cname_, String reg_, String wfs_)
    {
        _type = type_;
        _longName = lname_;
        _preferredName = pname_;
        _acID = acID_;
        _acVersion = acVers_;
        _preferredDefinition = pdef_;
        _contextName = cname_;
        _registrationStatus = reg_;
        _workflowStatus = wfs_;
        _ocID = -1;
        _ocVersion = null;
        _propID = -1;
        _propVersion = null;
    }

    /**
     * Constructor
     * 
     * @param type_ the AC type
     * @param lname_ the long name
     * @param pname_ the preferred name
     * @param acID_ the public id
     * @param acVers_ the version
     * @param pdef_ the preferred definition
     * @param cname_ the context name
     * @param reg_ the registration status
     * @param wfs_ the workflow status
     * @param ocID_ the object class public id
     * @param ocVers_ the object class version
     * @param propID_ the property public id
     * @param propVers_ the property version
     *
     */
    public SearchResults(SearchAC type_, String lname_, String pname_, int acID_, String acVers_, String pdef_, String cname_, String reg_, String wfs_, int ocID_, String ocVers_, int propID_, String propVers_)
    {
        _type = type_;
        _longName = lname_;
        _preferredName = pname_;
        _acID = acID_;
        _acVersion = acVers_;
        _preferredDefinition = pdef_;
        _contextName = cname_;
        _registrationStatus = reg_;
        _workflowStatus = wfs_;
        _ocID = ocID_;
        _ocVersion = ocVers_;
        _propID = propID_;
        _propVersion = propVers_;
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
        return _acID;
    }
    
    /**
     * The AC Version
     * 
     * @return the version
     */
    public String getVersion()
    {
        return _acVersion;
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
    
    /**
     * The Object Class Public ID. This is only valid if the getType() == SearchAC.DE.
     * 
     * @return the Object Class Public ID
     */
    public int getObjectClassID()
    {
        return _ocID;
    }
    
    /**
     * The Object Class Version. This is only valid if the getType() == SearchAC.DE.
     * 
     * @return the Object Class Version
     */
    public String getObjectClassVersion()
    {
        return _ocVersion;
    }
    
    /**
     * The Property Public ID. This is only valid if the getType() == SearchAC.DE.
     * 
     * @return the Property Public ID
     */
    public int getPropertyID()
    {
        return _propID;
    }
    
    /**
     * The Property Version. This is only valid if the getType() == SearchAC.DE.
     * 
     * @return the Property Version
     */
    public String getPropertyVersion()
    {
        return _propVersion;
    }

    private SearchAC _type;
    private String _longName;
    private String _preferredName;
    private int _acID;
    private String _acVersion;
    private String _preferredDefinition;
    private String _contextName;
    private String _registrationStatus;
    private String _workflowStatus;
    private int _ocID;
    private String _ocVersion;
    private int _propID;
    private String _propVersion;
}
