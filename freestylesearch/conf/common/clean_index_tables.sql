/* Copyright ScenPro, Inc, 2006

    $Header: /share/content/gforge/freestylesearch/freestylesearch/conf/common/clean_index_tables.sql,v 1.2 2007-01-25 20:24:04 hebell Exp $
    $Name: not supported by cvs2svn $

    Author: Larry Hebel

    This script cleans the freestyle index tables in preparation of a complete reload. Perform the following steps:

    1. Run this script.
    2. Execute the Seed script.

    Users may continue to use the API and Browser interface. The software will not break during this process, however,
    the search results will not be guaranteed correct until the Seed script completes.
*/
begin sbrext.freestyle_pkg.truncate_gs_tables; end;
commit;
select count(*) from sbrext.gs_composite;
select count(*) from sbrext.gs_tokens;
