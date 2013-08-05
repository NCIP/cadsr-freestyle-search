/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchResultObject.java,v 1.2 2006-07-24 14:55:21 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

/**
 * Map the freestyle search results to the basic database ID, type and score.
 * 
 * @author lhebel
 *
 */
public class SearchResultObject
{
    /**
     * @param idseq_ the database ID
     * @param score_ the search result score
     * @param type_ see the SearchAC enum
     */
    public SearchResultObject(String idseq_, int score_, SearchAC type_)
    {
        _idseq = idseq_;
        _score = score_;
        _type = type_;
    }
    
    /**
     * @return the database id.
     */
    public String getIdseq()
    {
        return _idseq;
    }
    
    /**
     * @return the calculated match score 
     */
    public int getScore()
    {
        return _score;
    }
    
    /**
     * @return the AC type code, see the SearchAC enum for values.
     */
    public SearchAC getType()
    {
        return _type;
    }
    
    private String _idseq;
    private int _score;
    private SearchAC _type;
}
