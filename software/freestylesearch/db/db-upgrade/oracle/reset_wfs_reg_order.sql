/*L
  Copyright ScenPro Inc, SAIC-F

  Distributed under the OSI-approved BSD 3-Clause License.
  See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
L*/

/* Copyright ScenPro, Inc, 2006

    $Header: /share/content/gforge/freestylesearch/freestylesearch/db-sql/reset_wfs_reg_order.sql,v 1.1 2007-12-11 22:21:02 hebell Exp $
    $Name: not supported by cvs2svn $

    Author: Larry Hebel

    This script will update the Workflow Status and Registration Status weights (order) in the gs_composite table.
    This must be done if the display_order is changed in the sbr.ac_status_lov_view or sbr.reg_status_lov_view.
    Using this script avoids the need to perform a clean on the index tables.

    Note the value "1000" in the update statements must be a value greater than the largest value in the
    status display order columns.
*/
update sbrext.gs_composite gc
set gc.wfs_order = (select nvl(wfs.display_order, 1000)
    from sbr.ac_status_lov_view wfs, sbr.admin_components_view ac
    where ac.ac_idseq = gc.ac_idseq and wfs.asl_name = ac.asl_name);
commit;
update sbrext.gs_composite gc
set gc.reg_order = (select nvl(reg.display_order, 1000)
    from sbr.ac_registrations_view rv, sbr.reg_status_lov_view reg, sbr.admin_components_view ac
    where ac.ac_idseq = gc.ac_idseq 
        and rv.ac_idseq(+) = ac.ac_idseq 
        and reg.registration_status(+) = rv.registration_status);
commit;
