/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchAC.java,v 1.3 2008-01-28 23:00:13 hebell Exp $
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
    DE ( 0, "Data Element" ),
    
    /**
     * Data Element Concept
     */
    DEC ( 1, "Data Element Concept" ),
    
    /**
     * Value Domain
     */
    VD ( 2, "Value Domain" ),
    
    /**
     * Object Class
     */
    OC ( 3, "Object Class" ),

    /**
     * Property
     */
    PROP ( 4, "Property" ),

    /**
     * Concept
     */
    CON ( 5, "Concept" ),

    /**
     * Conceptual Domain
     */
    CD ( 6, "Conceptual Domain" ),

    /**
     * Value Meaning
     */
    VM ( 7, "Value Meaning" );

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

    /**
     * Get the official name of the type.
     * 
     * @return the display ready AC type name.
     */
    public String getName()
    {
        return _name;
    }
    
    private SearchAC(int val_, String name_)
    {
        _val = val_;
        _name = name_;
    }
    
    private int _val;
    private String _name;
}
