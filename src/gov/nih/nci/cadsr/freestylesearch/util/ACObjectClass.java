// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/ACObjectClass.java,v 1.2 2006-07-05 14:53:51 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.cadsr.domain.ObjectClass;

/**
 * Define access to the Object Class table in the caDSR.
 * 
 * @author lhebel Mar 3, 2006
 */
public class ACObjectClass extends GenericAC
{
    /**
     * Constructor
     * 
     * @param index_ The master index used to numerically identify this class type.
     */
    public ACObjectClass(int index_)
    {
        super.setMasterIndex(index_);
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#factoryAC()
     */
    @Override
    public AdministeredComponent factoryAC()
    {
        return new ObjectClass();
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getACClass()
     */
    @Override
    public Class getACClass()
    {
        return ObjectClass.class;
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getTableName()
     */
    @Override
    public String getTableName()
    {
        return "sbrext.object_classes_view_ext";
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
        "oc_idseq", "'oc'", "version", "long_name", "preferred_name", "oc_id", "latest_version_ind", "created_by", "modified_by"
        , "asl_name"
    };

    private static final String _name = "Object Class";
}
