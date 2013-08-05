/*L
  Copyright ScenPro Inc, SAIC-F

  Distributed under the OSI-approved BSD 3-Clause License.
  See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
L*/

/* Copyright ScenPro, Inc, 2005

   $Header: /share/content/gforge/freestylesearch/freestylesearch/conf/template.load_options.sql,v 1.9 2008-11-03 19:03:51 hebell Exp $
   $Name: not supported by cvs2svn $

   Author: Larry Hebel

   This script loads the Tool Options table with required and optional values
   for the Freestyle Search Engine.

   Each is described briefly below. A full description of each can be found in
   the Freestyle Search Engine Installation Guide (file:
   distrib/doc/Installation Guide.doc).
*/

whenever sqlerror exit sql.sqlcode rollback;

/*
    Don't normally need to delete and reset the table from scratch.

delete from sbrext.tool_options_view_ext where tool_name = 'FREESTYLE';
*/

/*
    ==============================================================================
    Required Settings (do not comment or remove)
    ==============================================================================

    The seed last update timestamp controls the amount of data to be processed during a seed process of the
    index tables. All support AC's with a date modified equal to or greater than the date are parsed and the
    index tables updated. The initial value must be 1999-01-01, do NOT change this and do NOT run this script
    again after the initial seed script.

    This script intentionally sets s.value = s.value, do NOT change it to t.value!!! That is why this part can not be
    combined with other parts in this same file.
*/

merge into sbrext.tool_options_view_ext s
using (
          select 'CADSRAPI' as tool_name, 'URL' as property, 'http://cadsrapi@TIER@.nci.nih.gov/cadsrapi40' as value, 'The caDSR API URL.' as description from dual
          union select 'CADSRAPI' as tool_name, 'ACQUERY' as property, '/GetHTML?query=gov.nih.nci.cadsr.domain.AdministeredComponent'||Chr(38)||'gov.nih.nci.cadsr.domain.AdministeredComponent[@version=$VERS$][@publicID=$PID$]' as value, 'The Administered Component Query.' as description from dual
) t
on (s.tool_name = t.tool_name and s.property = t.property)
when matched then update set s.value = s.value
when not matched then insert (tool_name, property, value, description) values (t.tool_name, t.property, t.value, t.description);

merge into sbrext.tool_options_view_ext s
using (
          select 'FREESTYLE' as tool_name, 'SEED.LASTUPDATE' as property, 'SBREXT' as ua_name, '1999-01-01 00:00:00.0' as value, 'The last execution of the seed process.' as description from dual
) t
on (s.tool_name = t.tool_name and s.property = t.property and s.ua_name = t.ua_name)
when matched then update set s.value = s.value, s.description = t.description
when not matched then insert (tool_name, property, value, ua_name, description) values (t.tool_name, t.property, t.value, t.ua_name, t.description);

/*
    The URL to the Freestyle Search Engine browser interface.

    Define the retired WFS values.

    Define the index update schedule
*/
merge into sbrext.tool_options_view_ext s
using (
          select 'FREESTYLE' as tool_name , 'URL' as property, 'http://freestyle@TIER@.nci.nih.gov' as value, '' as ua_name, 'The URL to the Freestyle Search Engine browser interface.' as description from dual
union select 'FREESTYLE' as tool_name , 'RETIRED.WFS.1' as property, 'CMTE APPROVED' as value, '' as ua_name, 'Workflow Status considered RETIRED by Search' as description from dual
union select 'FREESTYLE' as tool_name , 'RETIRED.WFS.2' as property, 'CMTE SUBMTD' as value, '' as ua_name, 'Workflow Status considered RETIRED by Search' as description from dual
union select 'FREESTYLE' as tool_name , 'RETIRED.WFS.3' as property, 'CMTE SUBMTD USED' as value, '' as ua_name, 'Workflow Status considered RETIRED by Search' as description from dual
union select 'FREESTYLE' as tool_name , 'RETIRED.WFS.4' as property, 'RETIRED ARCHIVED' as value, '' as ua_name, 'Workflow Status considered RETIRED by Search' as description from dual
union select 'FREESTYLE' as tool_name , 'RETIRED.WFS.5' as property, 'RETIRED PHASED OUT' as value, '' as ua_name, 'Workflow Status considered RETIRED by Search' as description from dual
union select 'FREESTYLE' as tool_name , 'RETIRED.WFS.6' as property, 'RETIRED WITHDRAWN' as value, '' as ua_name, 'Workflow Status considered RETIRED by Search' as description from dual
union select 'FREESTYLE' as tool_name , 'RETIRED.WFS.7' as property, 'RETIRED DELETED' as value, '' as ua_name, 'Workflow Status considered RETIRED by Search' as description from dual
union select 'FREESTYLE' as tool_name , 'INDEX.SCHEDULE.START' as property, '03:00' as value, '' as ua_name, 'The start time (24 hour clock) for the automatic index updates.' as description from dual
union select 'FREESTYLE' as tool_name , 'INDEX.SCHEDULE.END' as property, '19:00' as value, '' as ua_name, 'The end time (24 hour clock) for the automatic index updates.' as description from dual
union select 'FREESTYLE' as tool_name , 'INDEX.BLOCK.START' as property, '02:00' as value, '' as ua_name, 'The start time (24 hour clock) when no manual requests can be made.' as description from dual
union select 'FREESTYLE' as tool_name , 'INDEX.BLOCK.END' as property, '09:00' as value, '' as ua_name, 'The end time (24 hour clock) when no manual requests can be made.' as description from dual
union select 'FREESTYLE' as tool_name , 'INDEX.SCHEDULE.TZ' as property, 'Eastern' as value, '' as ua_name, 'The timezone for the automatic index updates.' as description from dual
union select 'FREESTYLE' as tool_name , 'VERSION' as property, '@appl.version@' as value, '' as ua_name, 'The version identification for the current Freestyle Search Engine.' as description from dual
) t
on (s.tool_name = t.tool_name and s.property = t.property)
when matched then update set s.value = t.value, s.ua_name = t.ua_name, s.description = t.description
when not matched then insert (tool_name, property, value, ua_name, description) values (t.tool_name, t.property, t.value, t.ua_name, t.description);

/*
    Be sure the LOV table is up to date and correct.
*/
merge into sbrext.gs_tables_lov s
using (
          select 0 as ac_table, 'Data Element' as name, 'de' as abbrev, 'DATAELEMENT' as actl_name from dual
union select 1 as ac_table, 'Data Element Concept' as name, 'dec' as abbrev, 'DE_CONCEPT' as actl_name from dual
union select 2 as ac_table, 'Value Domain' as name, 'vd' as abbrev, 'VALUEDOMAIN' as actl_name from dual
union select 3 as ac_table, 'Object Class' as name, 'oc' as abbrev, 'OBJECTCLASS' as actl_name from dual
union select 4 as ac_table, 'Property' as name, 'prop' as abbrev, 'PROPERTY' as actl_name from dual
union select 5 as ac_table, 'Concept' as name, 'con' as abbrev, 'CONCEPT' as actl_name from dual
union select 6 as ac_table, 'Conceptual Domain' as name, 'cd' as abbrev, 'CONCEPTUALDOMAIN' as actl_name from dual
union select 7 as ac_table, 'Value Meaning' as name, 'vm' as abbrev, 'VALUEMEANING' as actl_name from dual
) t
on (s.ac_table = t.ac_table)
when matched then update set s.name = t.name, s.abbrev = t.abbrev, s.actl_name = t.actl_name
when not matched then insert (ac_table, name, abbrev, actl_name) values (t.ac_table, t.name, t.abbrev, t.actl_name);

/*
   Commit Settings.
*/

commit;
