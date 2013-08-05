/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/ACAlternate.java,v 1.1 2006-07-24 14:56:18 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

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
