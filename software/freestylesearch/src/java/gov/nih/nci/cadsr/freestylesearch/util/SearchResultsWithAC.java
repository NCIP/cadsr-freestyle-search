/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchResultsWithAC.java,v 1.2 2006-07-24 14:55:21 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import gov.nih.nci.cadsr.domain.AdministeredComponent;

/**
 * Map the freestyle search results to a SearchResultObject and the corresponding
 * AdministeredComponent object defined by the caCORE API.
 * 
 * @author lhebel
 *
 */
public class SearchResultsWithAC
{
    /**
     * Constructor
     * 
     * @param obj_ the result object 
     * @param ac_ the caCORE API Administered Component
     *
     */
    public SearchResultsWithAC(SearchResultObject obj_, AdministeredComponent ac_)
    {
        _obj = obj_;
        _ac = ac_;
    }
    
    /**
     * Get the SearchResultObject element.
     * 
     * @return a SearchResultObject (never null)
     */
    public SearchResultObject getResultObject()
    {
        return _obj;
    }
    
    /**
     * Get the AdministeredComponent element.
     * 
     * @return an AdministeredComponent or null if an error occurred retrieving the record details through the caCORE API
     */
    public AdministeredComponent getAdministeredComponent()
    {
        return _ac;
    }

    private SearchResultObject _obj;
    private AdministeredComponent _ac;
}
