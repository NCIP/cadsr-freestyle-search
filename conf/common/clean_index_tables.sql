/* Copyright ScenPro, Inc, 2006

    $Header: /share/content/gforge/freestylesearch/freestylesearch/conf/common/clean_index_tables.sql,v 1.1 2006-07-12 21:55:25 hebell Exp $
    $Name: not supported by cvs2svn $

    Author: Larry Hebel

    This script cleans the freestyle index tables in preparation of a complete reload. Perform the following steps:

    1. Run this script.
    2. Execute the Seed script.

    Users may continue to use the API and Browser interface. The software will not break during this process, however,
    the search results will not be guaranteed correct until the Seed script completes.
*/
update sbrext.tool_options_view_ext set value = '1999-12-01 04:00:00.0' where tool_name = 'FREESTYLE' and ua_name = 'SBREXT';
commit;
delete from sbrext.gs_composite where ac_table = 0;
commit;
delete from sbrext.gs_composite where ac_table = 1;
commit;
delete from sbrext.gs_composite where ac_table = 2;
commit;
delete from sbrext.gs_composite where ac_table = 3;
commit;
delete from sbrext.gs_composite where ac_table = 4;
commit;
delete from sbrext.gs_composite where ac_table = 5;
commit;
delete from sbrext.gs_composite where ac_table = 6;
commit;
delete from sbrext.gs_tokens where ac_table = 0;
commit;
delete from sbrext.gs_tokens where ac_table = 1;
commit;
delete from sbrext.gs_tokens where ac_table = 2;
commit;
delete from sbrext.gs_tokens where ac_table = 3;
commit;
delete from sbrext.gs_tokens where ac_table = 4;
commit;
delete from sbrext.gs_tokens where ac_table = 5;
commit;
delete from sbrext.gs_tokens where ac_table = 6;
commit;
select count(*) from sbrext.gs_composite;
select count(*) from sbrext.gs_tokens;
