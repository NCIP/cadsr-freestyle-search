/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/ACValueDomain.java,v 1.2 2006-08-15 20:42:02 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.cadsr.domain.ValueDomain;

/**
 * Define access to the Value Domain table in the caDSR.
 * 
 * @author lhebel Mar 3, 2006
 */
public class ACValueDomain extends GenericAC
{
    /**
     * Constructor
     * 
     * @param index_ The master index used to numerically identify this class type.
     */
    public ACValueDomain(int index_)
    {
        super.setMasterIndex(index_);
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#factoryAC()
     */
    @Override
    public AdministeredComponent factoryAC()
    {
        return new ValueDomain();
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getACClass()
     */
    @Override
    public Class getACClass()
    {
        return ValueDomain.class;
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.tool.GenericAC#getTableName()
     */
    @Override
    public String getTableName()
    {
        return "sbr.value_domains_view";
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
        "vd_idseq", "'vd'", "version", "long_name", "preferred_name", "preferred_definition", "vd_id"
        , "latest_version_ind", "created_by", "modified_by", "asl_name"
    };

    private static final String _name = "Value Domain";
}
