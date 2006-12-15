// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/ReservedColumns.java,v 1.1 2006-07-10 18:40:32 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

/**
 * Internal use only. This class defines the values of the Reserved columns used by Freestyle.
 * 
 * @author lhebel
 */
public enum ReservedColumns
{
    /**
     * The index in the column names for the database id.
     */
    IDSEQ ( 0 ),

    /**
     * The index in the column names for the record type.
     */
    TYPE ( 1 ),

    /**
     * The index in the column names for the record version.
     */
    VERSION ( 2 ),

    /**
     * The index for all other columns.
     */
    OTHER ( 3 );
    
    /**
     * Return the value of the enum
     * 
     * @return the enum value
     */
    public int toInt()
    {
        return _val;
    }
    
    /**
     * Convert a value to the enum type
     * 
     * @param val_ an interger value
     * @return the matching enum type
     */
    public static ReservedColumns valueOf(int val_)
    {
        if (val_ > 2 || 0 > val_)
            return OTHER;
        return values()[val_];
    }

    private ReservedColumns(int val_)
    {
        _val = val_;
    }

    private int _val;
}
