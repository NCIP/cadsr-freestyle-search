/* Copyright ScenPro, Inc, 2005

   $Header: /share/content/gforge/freestylesearch/freestylesearch/conf/osi/load_options.sql,v 1.3 2007-02-13 19:35:16 hebell Exp $
   $Name: not supported by cvs2svn $

   Author: Larry Hebel

   This script loads the Tool Options table with required and optional values
   for the Freestyle Search Engine.

   Each is described briefly below. A full description of each can be found in
   the Freestyle Search Engine Installation Guide (file:
   distrib/doc/Installation Guide.doc).
*/

delete from sbrext.tool_options_view_ext where tool_name = 'FREESTYLE';

/*
  ==============================================================================
  Required Settings (do not comment or remove)
  ==============================================================================

  The seed last update timestamp controls the amount of data to be processed during a seed process of the
  index tables. All support AC's with a date modified equal to or greater than the date are parsed and the
  index tables updated. The initial value must be 1999-01-01, do NOT change this and do NOT run this script
  again after the initial seed script.
*/

insert into sbrext.tool_options_view_ext (tool_name, property, value, ua_name, description)
values ('FREESTYLE', 'SEED.LASTUPDATE', '1999-01-01 00:00:00.0', 'SBREXT',
'The last execution of the seed process.');

/*
    The URL to the Freestyle Search Engine browser interface.
*/

insert into sbrext.tool_options_view_ext (tool_name, property, value, description)
values ('FREESTYLE', 'URL', 'http://freestyle.nci.nih.gov',
'The URL to the Freestyle Search Engine browser interface.');

/*
    Define the retired WFS values.
*/
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'RETIRED.WFS.1', 'CMTE APPROVED', 'Workflow Status considered RETIRED by Search');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'RETIRED.WFS.2', 'CMTE SUBMTD', 'Workflow Status considered RETIRED by Search');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'RETIRED.WFS.3', 'CMTE SUBMTD USED', 'Workflow Status considered RETIRED by Search');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'RETIRED.WFS.4', 'RETIRED ARCHIVED', 'Workflow Status considered RETIRED by Search');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'RETIRED.WFS.5', 'RETIRED PHASED OUT', 'Workflow Status considered RETIRED by Search');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'RETIRED.WFS.6', 'RETIRED WITHDRAWN', 'Workflow Status considered RETIRED by Search');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'RETIRED.WFS.7', 'RETIRED DELETED', 'Workflow Status considered RETIRED by Search');

/*
    Define the index update schedule
*/
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'INDEX.SCHEDULE.START', '03:00', 'The start time (24 hour clock) for the automatic index updates.');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'INDEX.SCHEDULE.END', '19:00', 'The end time (24 hour clock) for the automatic index updates.');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'INDEX.BLOCK.START', '02:00', 'The start time (24 hour clock) when no manual requests can be made.');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'INDEX.BLOCK.END', '09:00', 'The end time (24 hour clock) when no manual requests can be made.');
insert into sbrext.tool_options_view_ext (tool_name, property, value, description) values ('FREESTYLE', 'INDEX.SCHEDULE.TZ', 'Eastern', 'The timezone for the automatic index updates.');

/*
    Be sure the LOV table is up to date and correct.
*/
merge into sbrext.gs_tables_lov s
using (
      select 0 as ac_table, 'Data Element' as name, 'de' as abbrev from dual
union select 1 as ac_table, 'Data Element Concept' as name, 'dec' as abbrev from dual
union select 2 as ac_table, 'Value Domain' as name, 'vd' as abbrev from dual
union select 3 as ac_table, 'Object Class' as name, 'oc' as abbrev from dual
union select 4 as ac_table, 'Property' as name, 'prop' as abbrev from dual
union select 5 as ac_table, 'Concept' as name, 'con' as abbrev from dual
union select 6 as ac_table, 'Conceptual Domain' as name, 'cd' as abbrev from dual
union select 7 as ac_table, 'Value Meaning' as name, 'vm' as abbrev from dual
) t
on (s.ac_table = t.ac_table)
when matched then update set s.name = t.name, s.abbrev = t.abbrev
when not matched then insert (ac_table, name, abbrev) values (t.ac_table, t.name, t.abbrev);

/*
   Commit Settings.
*/

commit;
