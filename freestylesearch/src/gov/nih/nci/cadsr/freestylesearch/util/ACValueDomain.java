// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/ACValueDomain.java,v 1.2 2006-07-05 14:53:51 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

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
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#factoryAC()
     */
    @Override
    public AdministeredComponent factoryAC()
    {
        return new ValueDomain();
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getACClass()
     */
    @Override
    public Class getACClass()
    {
        return ValueDomain.class;
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getTableName()
     */
    @Override
    public String getTableName()
    {
        // TODO Auto-generated method stub
        return "sbr.value_domains_view";
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getTypeName()
     */
    @Override
    public String getTypeName()
    {
        return _name;
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getColumns()
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
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC
     */
    private static final String[] _cols = {
        "vd_idseq", "'vd'", "version", "long_name", "preferred_name", "preferred_definition", "vd_id"
        , "latest_version_ind", "created_by", "modified_by", "asl_name"
    };

    private static final String _name = "Value Domain";
}
