// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/ACAlternate.java,v 1.3 2006-07-10 18:40:32 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import gov.nih.nci.cadsr.domain.AdministeredComponent;

/**
 * Create an Alternate instantiation of an GenericAC to use to seed from the Alternate Names
 * (designations) data.
 * 
 * @author lhebel
 *
 */
public class ACAlternate extends GenericAC
{
    /**
     * Constructor
     * 
     * @param ac_ The AC to use as the source for this alternate.
     */
    public ACAlternate(GenericAC ac_)
    {
        _root = ac_;
        super.setMasterIndex(_root.getMasterIndex());
        _cols[ReservedColumns.TYPE.toInt()] = "'" + _root.getType() + "'";
    }

    @Override
    public AdministeredComponent factoryAC()
    {
        return _root.factoryAC();
    }

    @Override
    public Class getACClass()
    {
        return _root.getACClass();
    }

    @Override
    public String getTableName()
    {
        return "sbr.designations_view";
    }

    @Override
    public String[] getColumns()
    {
        return _cols;
    }

    @Override
    public String getTypeName()
    {
        return _root.getTypeName();
    }
    
    /**
     * Get the root table name used as the source for this alternate.
     * 
     * @return The root table name.
     */
    public String getRootTableName()
    {
        return _root.getTableName();
    }
    
    /**
     * Get the root IDSEQ column name used as the source for this alternate.
     * 
     * @return The root IDSEQ column name.
     */
    public String getRootIdseqName()
    {
        return _root.getIdseqName();
    }

    private GenericAC _root;
    private String[] _cols = {
                    "ac_idseq", "'unknown'", "''", "name", "detl_name"
    };
}