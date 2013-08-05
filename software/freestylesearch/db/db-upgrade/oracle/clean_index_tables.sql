/*L
  Copyright ScenPro Inc, SAIC-F

  Distributed under the OSI-approved BSD 3-Clause License.
  See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
L*/

/* Copyright ScenPro, Inc, 2006

    $Header: /share/content/gforge/freestylesearch/freestylesearch/db-sql/clean_index_tables.sql,v 1.1 2007-12-11 22:21:02 hebell Exp $
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
