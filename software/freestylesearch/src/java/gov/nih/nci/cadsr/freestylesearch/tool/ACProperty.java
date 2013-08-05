/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/ACProperty.java,v 1.1 2006-07-24 14:56:18 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.cadsr.domain.Property;

/**
 * Define access to the Property table in the caDSR.
 * 
 * @author lhebel Mar 3, 2006
 */
public class ACProperty extends GenericAC
{
    /**
     * Constructor
     * 
     * @param index_ The master index used to numerically identify this class type.
     */
    public ACProperty(int index_)
    {
        super.setMasterIndex(index_);
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#factoryAC()
     */
    @Override
    public AdministeredComponent factoryAC()
    {
        return new Property();
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getACClass()
     */
    @Override
    public Class getACClass()
    {
        return Property.class;
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getTableName()
     */
    @Override
    public String getTableName()
    {
        return "sbrext.properties_view_ext";
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getTypeName()
     */
    @Override
    public String getTypeName()
    {
        return _name;
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getColumns()
     */
    @Override
    public String[] getColumns()
    {
        return _cols;
    }

    /**
     * The columns used to seed the search index table. Some of these are order
     * dependant.
     * 
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC
     */
    private static final String[] _cols = {
        "prop_idseq", "'prop'", "version", "long_name", "preferred_name", "prop_id", "latest_version_ind", "created_by", "modified_by"
        , "asl_name"
    };

    private static final String _name = "Property";
}
