/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/GenericColumn.java,v 1.1 2007-01-25 20:24:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

/**
 * A generic representation of a database column as needed by a GenericAC.
 * 
 * Incomplete - In Work
 * 
 * @author lhebel
 *
 */
public class GenericColumn
{
    /**
     * Constructor
     * 
     * @param name_ the column name
     * @param label_ the column label
     */
    public GenericColumn(String name_, String label_)
    {
        _name = name_;
        _label = label_;
    }
    
    /**
     * Get the column name.
     * 
     * @return the column name
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * Get the column label. If a specific label has not been set
     * the column name is returned.
     * 
     * @return the column label
     */
    public String getLabel()
    {
        if (_label == null || _label.length() == 0)
            return _name;
        return _label;
    }

    private String _name;
    private String _label;
}
