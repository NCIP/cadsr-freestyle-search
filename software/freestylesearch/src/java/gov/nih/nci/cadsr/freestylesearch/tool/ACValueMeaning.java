/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/ACValueMeaning.java,v 1.1 2007-01-25 20:24:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.cadsr.domain.ValueMeaning;

/**
 * Define access to the Value Meaning table in the caDSR.
 * 
 * @author lhebel
 *
 */
public class ACValueMeaning extends GenericAC
{
    /**
     * Constructor
     * 
     * @param index_ The master index used to numerically identify this class type.
     */
    public ACValueMeaning(int index_)
    {
        super.setMasterIndex(index_);
    }

    /* (non-Javadoc)
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#factoryAC()
     */
    @Override
    public AdministeredComponent factoryAC()
    {
        return new ValueMeaning();
    }

    /* (non-Javadoc)
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getACClass()
     */
    @Override
    public Class getACClass()
    {
        return ValueMeaning.class;
    }

    /* (non-Javadoc)
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getTableName()
     */
    @Override
    public String getTableName()
    {
        return "sbr.value_meanings_view";
    }

    /* (non-Javadoc)
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getColumns()
     */
    @Override
    public String[] getColumns()
    {
        return _cols;
    }

    /* (non-Javadoc)
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getTypeName()
     */
    @Override
    public String getTypeName()
    {
        return "Value Meaning";
    }

    /**
     * The columns used to seed the search index table. Some of these are order
     * dependant.
     * 
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC
     */
    private static final String[] _cols = {
        "vm_idseq", "'vm'", "version", "long_name", "preferred_name", "preferred_definition", "vm_id"
        , "latest_version_ind", "created_by", "modified_by", "asl_name"
    };
}
