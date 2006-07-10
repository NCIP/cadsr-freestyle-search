// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchAC.java,v 1.1 2006-07-10 18:40:32 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

/**
 * This class defines the types of Administered Components supported by Freestyle.
 * 
 * @author lhebel
 */
public enum SearchAC
{
    
    /**
     * Data Element
     */
    DE ( 0 ),
    
    /**
     * Data Element Concept
     */
    DEC ( 1 ),
    
    /**
     * Value Domain
     */
    VD ( 2 ),
    
    /**
     * Object Class
     */
    OC ( 3 ),

    /**
     * Property
     */
    PROP ( 4 ),

    /**
     * Concept
     */
    CON ( 5 ),

    /**
     * Conceptual Domain
     */
    CD ( 6 );

    /**
     * Return the number of AC's
     * 
     * @return the number of defined AC's
     */
    public static int count()
    {
        return values().length;
    }
    
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
     * Convert an integer to the enum value.
     * 
     * @param val_ the integer
     * @return the enum value
     */
    public static SearchAC valueOf(int val_)
    {
        if (val_ < 0 || values().length < val_)
            return null;
        return values()[val_];
    }
    
    private SearchAC(int val_)
    {
        _val = val_;
    }
    
    private int _val;
}
