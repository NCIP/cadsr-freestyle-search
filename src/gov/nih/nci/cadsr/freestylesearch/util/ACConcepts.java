// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/ACConcepts.java,v 1.1 2006-06-30 13:46:47 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.cadsr.domain.Concept;

/**
 * Define access to the Concepts table in the caDSR.
 * 
 * @author lhebel Mar 10, 2006
 */
public class ACConcepts extends GenericAC
{
    /**
     * Constructor
     * 
     * @param index_ The master index used to numerically identify this class type.
     */
    public ACConcepts(int index_)
    {
        super.setMasterIndex(index_);
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#factoryAC()
     */
    @Override
    public AdministeredComponent factoryAC()
    {
        return new Concept();
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getACClass()
     */
    @Override
    public Class getACClass()
    {
        return Concept.class;
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getTableName()
     */
    @Override
    public String getTableName()
    {
        return "sbrext.concepts_view_ext";
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
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getTypeName()
     */
    @Override
    public String getTypeName()
    {
        return _name;
    }

    /**
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC#getDisplay(int)
     */
    @Override
    public String getDisplay(int score_)
    {
        return "select zz.long_name || '\n\t" + _name + "\n\tPublic ID ' || zz.con_id || "
            +"'\n\tVersion ' || zz.version || '\n\tContext ' || cc.name || "
            + "'\n\tWorkflow Status ' || zz.asl_name || "
            + "'\n\tRegistration Status ' || nvl(rs.registration_status, ' ') || "
            + "'\n\tScore " + score_
            + "' from sbrext.concepts_view_ext zz, sbr.ac_registrations_view rs, sbr.contexts_view cc "
            + "where zz.con_idseq = ? and cc.conte_idseq = zz.conte_idseq and rs.ac_idseq(+) = zz.con_idseq";
    }

    /**
     * The columns used to seed the search index table. Some of these are order
     * dependant.
     * 
     * @see gov.nih.nci.cadsr.freestylesearch.util.GenericAC
     */
    private static final String[] _cols = {
        "con_idseq", "'con'", "version", "long_name", "preferred_definition", "con_id"
        , "preferred_name", "definition_source", "origin", "created_by", "modified_by", "asl_name"
    };
    
    private static final String _name = "Concept";
}
