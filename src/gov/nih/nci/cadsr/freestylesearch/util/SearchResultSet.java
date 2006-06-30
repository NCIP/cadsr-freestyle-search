// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchResultSet.java,v 1.1 2006-06-30 13:46:47 hebell Exp $
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
     * @param type_ the AC type, Search.AC_TYPE_DE, Search.AC_TYPE_DEC, etc.
     */
    public SearchResultSet(String idseq_, int score_, int type_)
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
     * @return the AC type code, Search.AC_TYPE_DE for a Data Element, Search.AC_TYPE_DEC for a Data
     *      Element Concept, etc.
     */
    public int getType()
    {
        return _type;
    }
    
    protected String _idseq;
    protected int _score;
    protected int _type;
}
