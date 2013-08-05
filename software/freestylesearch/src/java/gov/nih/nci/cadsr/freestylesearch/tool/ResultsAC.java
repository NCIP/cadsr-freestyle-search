/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/ResultsAC.java,v 1.1 2006-07-24 14:56:18 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;


/**
 * 
 * @author lhebel Mar 6, 2006
 */

/**
 * This class is used to map the search results for processing.
 * 
 * @author lhebel
 *
 */
public class ResultsAC
{
    /**
     * Constructor
     * 
     * @param idseq_ the unique database id for the record 
     * @param desc_  the class description for the record
     * @param score_ the score given the result
     */
    public ResultsAC(String idseq_, GenericAC desc_, int score_)
    {
        _idseq = idseq_;
        _desc = desc_;
        _score = score_;
    }
    
    /**
     *  The unique database id for the record.
     */
    public String _idseq;
    
    /**
     * The class description for the record.
     */
    public GenericAC _desc;
    
    /**
     * The score given the result. 
     */
    public int _score;
}
