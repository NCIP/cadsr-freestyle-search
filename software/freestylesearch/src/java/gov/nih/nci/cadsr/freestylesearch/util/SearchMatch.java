/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchMatch.java,v 1.1 2006-07-10 18:40:32 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

/**
 * This class defines the values for the Search Matching option.
 *  
 * @author lhebel
 */
public enum SearchMatch
{
    /**
     * Indicates all words/tokens are compared exactly, no partial
     * comparisons are done, e.g. "Lateral" will not match "Colateral".
     */
    EXACT ( 0 ),
    
    /**
     * Indicates all words/tokens are compared partially, no exact
     * comparisons are done, e.g. "Lateral" will always match "Colateral".
     */
    PARTIAL ( 1 ),
    
    /**
     * Indicates all words/tokens are compared exactly, then if no results
     * are found a partial search is performed, e.g. "Lateral" will match "Lateral"
     * and if found the search stops, if not found "Lateral" will match "Colateral".
     */
    BEST ( 2 );
    
    /**
     * Return the integer value of the enum
     * 
     * @return the integer value
     */
    public int toInt()
    {
        return _val;
    }
    
    /**
     * Convert an integer value to the enum
     * 
     * @param val_ an integer
     * @return the enum type
     */
    public static SearchMatch valueOf(int val_)
    {
        if (val_ < 0 || values().length < val_)
            return BEST;
        return values()[val_];
    }
    
    private SearchMatch(int val_)
    {
        _val = val_;
    }
    
    private int _val;
}
