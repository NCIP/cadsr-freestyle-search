/* Copyright ScenPro, Inc, 2005

   $Header: /share/content/gforge/freestylesearch/freestylesearch/conf/common/load_options.sql,v 1.1 2006-06-30 13:46:46 hebell Exp $
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
values ('FREESTYLE', 'SEED.LASTUPDATE', '1999-01-01 00:00:00.0', 'SBREXT'
'The last execution of the seed process.');

/*
   Commit Settings.
*/

commit;
