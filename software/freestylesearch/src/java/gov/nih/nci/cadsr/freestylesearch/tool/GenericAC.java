/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/GenericAC.java,v 1.3 2008-04-16 21:15:31 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

import gov.nih.nci.cadsr.domain.AdministeredComponent;

/**
 * @author lhebel Mar 3, 2006
 */

/**
 * This class is the parent to the specific classes which describe
 * the various records/tables of interest.
 * 
 * @author lhebel
 *
 */
public abstract class GenericAC
{
    /**
     * Constructor
     * 
     * @param index_ The master index used to numerically identify this class type.
     */
    public GenericAC(int index_)
    {
        _masterIndex = index_;
    }

    /**
     * Constructor
     *
     */
    public GenericAC()
    {
    }
    
    /**
     * Create the caCORE API equivalent object for the AC
     * 
     * @return the AC for the sub-class type
     */
    public abstract AdministeredComponent factoryAC();
    
    /**
     * Create the caCORE API class equivalent object for the AC
     * 
     * @return the AC class for the sub-class type
     */
    public abstract Class getACClass();
    
    /**
     * Get the database table name for this class.
     * 
     * @return the database table name including schema
     */
    public abstract String getTableName();
    
    /**
     * Get the list of columns of interest.
     * 
     * @return the table column names
     */
    public abstract String[] getColumns();
    
    /**
     * Get the type name for the derived class.
     * 
     * @return the expanded type name
     */
    public abstract String getTypeName();
    
    /**
     * Get the name of the idseq column.
     * 
     * @return the expanded type name
     */
    public String getIdseqName()
    {
        String[] cols = getColumns();
        return cols[ReservedColumns.IDSEQ.toInt()];
    }
    
    /**
     * Get the string representation of the class type.
     * 
     * @return the expanded type name
     */
    public String getType()
    {
        String[] cols = getColumns();
        return cols[ReservedColumns.TYPE.toInt()].replaceAll("'", "");
    }
    
    /**
     * Get the name of the public id column.
     * 
     * @return the expanded type name
     */
    public String getPublicIdName()
    {
        String type = getType();
        return ((type.compareTo("de") == 0) ? "cde_id" : type + "_id");
    }
    
    /**
     * Set the master index used to uniquely describe the class type as
     * an integer.
     * 
     * @param index_ the unique class type identifier
     */
    public void setMasterIndex(int index_)
    {
        _masterIndex = index_;
    }
    
    /**
     * 
     * Get the master index used to uniquely describe the class type as
     * an integer.
     * 
     * @return the unique class type identifier
     */
    public int getMasterIndex()
    {
        return _masterIndex;
    }
    
    /**
     * Get the names of the columns which may be searched for all the types. This is
     * a single cumulative list and not segregated by the AC type.
     * 
     * @return the combined list of columns included in the searches.
     */
    public static String[] getColNames()
    {
        return _colNames;
    }
    
    private int _masterIndex;
    
    private static final String[] _colNames = {
        "Version", "Long Name", "Preferred Name", "Preferred Definition", "Question", "Public ID"
        , "Latest Version Indicator", "Created By", "Modified By", "Workflow Status", "Registration Status"
        , "Context", "Alternate Name", "Definition Source", "Origin"
    };
}
