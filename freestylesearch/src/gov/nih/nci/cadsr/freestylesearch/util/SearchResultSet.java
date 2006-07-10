// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchResultSet.java,v 1.2 2006-07-10 18:40:32 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

/**
 * Map the freestyle search results to the basic database ID, type and score.
 * 
 * @author lhebel
 *
 */
public class SearchResultSet
{
    /**
     * @param idseq_ the database ID
     * @param score_ the search result score
     * @param type_ see the SearchAC enum
     */
    public SearchResultSet(String idseq_, int score_, SearchAC type_)
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
    
    protected String _idseq;
    protected int _score;
    protected SearchAC _type;
}
