/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/DBAccessIndex.java,v 1.14 2008-05-02 18:49:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

import gov.nih.nci.cadsr.freestylesearch.util.SearchException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.log4j.Logger;

/**
 * Provide access to the Freestyle Index tables.
 * 
 * @author lhebel
 *
 */
public class DBAccessIndex
{
    /**
     * Constructor
     *
     */
    public DBAccessIndex()
    {
        _limit = 100;
        _scoreLimit = 0;
        _connFlag = true;
    }
    
    /**
     * Get the setting for the maximum results.
     * 
     * @return The maximum possible result count.
     */
    public int getLimit()
    {
        return _limit;
    }
    
    /**
     * Set the maximum possible results to be returned from a search.
     * 
     * @param limit_ The desired maximum results.
     */
    public void setLimit(int limit_)
    {
        _limit = limit_;
    }

    /**
     * Exclude or include the "retired" Workflow Status.
     * 
     * @param flag_ when true the "retired" AC are not returned in a search
     */
    public void excludeWorkflowStatusRetired(boolean flag_)
    {
        _excludeWFSretired = flag_;
    }
    
    /**
     * Set the restriction for results by score. This is a relative scale so 1 returns all
     * matches with the highest score in the set, 2 returns the two highest, etc.
     * 
     * @param score_ Zero (0) to disable the restriction, otherwise the relative scale number
     *      to return.
     */
    public void restrictResultsByScore(int score_)
    {
        _scoreLimit = score_;
    }

    /**
     * Open a single simple connection to the database. No connection pooling is performed.
     * 
     * @param dburl_
     *        The  TNSNAME entry describing the database location when using OCI
     *        (thick client) or the database url when using the then client.
     * @param user_
     *        The  user id.
     * @param pswd_
     *        The password which must match 'user_'.
     * @exception SearchException
     */
    public void open(String dburl_, String user_, String pswd_) throws SearchException
    {
        // If we already have a connection, don't bother.
        if (_conn != null)
            return;

        try
        {
            // Thin client URL's contain colons (:), thick client does not.
            OracleDataSource ods = new OracleDataSource();
            if (dburl_.indexOf(':') > 0)
            {
                String parts[] = dburl_.split("[:]");
                ods.setDriverType("thin");
                ods.setServerName(parts[0]);
                ods.setPortNumber(Integer.parseInt(parts[1]));
                ods.setServiceName(parts[2]);
            }
            else
            {
                ods.setDriverType("oci8");
                ods.setTNSEntryName(dburl_);
            }
            
            // Get the connection and turn off auto commit.
            _conn = ods.getConnection(user_, pswd_);
            _conn.setAutoCommit(false);
            _needCommit = false;
            return;
        }
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + dburl_ + ", " + user_ + ", " + pswd_ + "\n" + ex.toString();
            throw new SearchException(ex);
        }
    }
    
    /**
     * Set the connection to the one provided. The connection is not closed by this class because it was not
     * created by this class.
     * 
     * @param conn_ a database connection to use.
     * 
     * @exception SearchException
     */
    public void open(Connection conn_) throws SearchException
    {
        // Since we didn't create the connection set a flag to ensure we
        // don't close the connection.
        _conn = conn_;
        try
        {
            _conn.setAutoCommit(false);
        }
        catch (SQLException ex)
        {
            throw new SearchException(ex);
        }
        finally
        {
            _needCommit = false;
            _connFlag = false;
        }
    }
    
    /**
     * Get a connection from the specified datasource.
     * 
     * @param ds_ a datasource to provide the connection.
     * @param user_ the user name/account for the connection.
     * @param pswd_ the password for the account.
     * 
     * @exception SearchException
     */
    public void open(DataSource ds_, String user_, String pswd_) throws SearchException
    {
        try
        {
            _conn = ds_.getConnection(user_, pswd_);
            _conn.setAutoCommit(false);
        }
        catch (SQLException ex)
        {
            throw new SearchException(ex);
        }
        finally
        {
            _needCommit = false;
        }
    }
    
    /**
     * Get a connection from the specified datasource.
     * 
     * @param ds_ a datasource to provide the connection.
     * 
     * @exception SearchException
     */
    public void open(DataSource ds_) throws SearchException
    {
        try
        {
            _conn = ds_.getConnection();
            _conn.setAutoCommit(false);
        }
        catch (SQLException ex)
        {
            throw new SearchException(ex);
        }
        finally
        {
            _needCommit = false;
        }
    }
    
    /**
     * Close the database connection.
     *
     */
    public void close()
    {
        try
        {
            // Clean up any open statements, result sets, etc.
            cleanup();
            if (_conn != null)
            {
                // Commit if needed.
                if (_needCommit)
                    _conn.commit();
            }
        }
        catch (SQLException e)
        {
            _logger.error(e.toString());
        }
        finally
        {
            // Close only if we opened it.
            if (_connFlag)
            {
                try
                {
                    if (_conn != null)
                        _conn.close();
                }
                catch (Exception ex)
                {
                }
            }
            
            _conn = null;
        }
    }
    
    /**
     * Clean up the open handles.
     *
     * @throws SearchException 
     */
    private void cleanup() throws SearchException
    {
        SQLException se = null;
        try
        {
            if (_rs != null)
            {
                _rs.close();
            }
        }
        catch (SQLException ex)
        {
            se = ex;
        }
        finally
        {
            _rs = null;
        }

        try
        {
            if (_pstmt != null)
            {
                _pstmt.close();
            }
        }
        catch (SQLException ex)
        {
            if (se == null)
                se = ex;
        }
        finally
        {
            _pstmt = null;
        }
        
        if (se != null)
            throw new SearchException(se);
    }

    /**
     * Call cleanup() and catch the exception.
     *
     */
    public void cleanupWithCatch()
    {
        try
        {
            cleanup();
        }
        catch (SearchException ex)
        {
            if (ex.getCause() instanceof SQLException)
            {
                SQLException et = (SQLException) ex.getCause();
                _errorCode = et.getErrorCode();
            }
            else
                _errorCode = -1;
            _errorMsg = _errorCode + ": " + ex.toString();
            _logger.error(_errorMsg);
        }
    }
    
    /**
     * Insert values into the term index table.
     * 
     * @param type_ the numeric identifier for the record type
     * @param idseq_ the unique database id for the record
     * @param col_ the column name from which the 'token_' was read
     * @param term_ the token to save in the search index table
     * @exception SearchException
     */
    public void insertTerm(int type_, String idseq_, String col_, String term_) throws SearchException
    {
        // Build the SQL insert statement and save the data. 
        String insert = "insert into " + _indexTable
            + " (ac_idseq, ac_table, ac_col, token) "
            + "values (?, ?, ?, ?)";

        try
        {
            if (_pstmt == null)
                _pstmt = _conn.prepareStatement(insert);
            _pstmt.setString(1, idseq_);
            _pstmt.setInt(2, type_);
            _pstmt.setString(3, col_);
            _pstmt.setString(4, term_);
            
            _pstmt.execute();
            _needCommit = true;
        }
        
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            // A code of '1' means a duplicate row was found in the table. We can ignore duplicates
            // but we care about everything else.
            if (_errorCode != 1)
            {
                _errorMsg = _errorCode + ": " + insert + "\n" + ex.toString();
                _logger.error(_errorMsg);
                throw new SearchException(ex);
            }
            _errorCode = 0;
        }
    }

    /**
     * Insert the composite term string into the composite index table.
     * 
     * @param type_ the numeric identifier for the record type
     * @param idseq_ the unique database id for the record
     * @param comp_ the composite term string
     * @param reg_ the registration order/weight
     * @param wfs_ the workflow status order/weight
     * @exception SearchException
     */
    public void insertComposite(int type_, String idseq_, String comp_, int reg_, int wfs_) throws SearchException
    {
        // Build the SQL insert statement and save the data. 
        String insert = "insert into " + _compositeTable
            + " (ac_idseq, ac_table, composite, reg_order, wfs_order) "
            + "values (?, ?, ?, ?, ?)";

        try
        {
            if (_pstmt == null)
                _pstmt = _conn.prepareStatement(insert);
            _pstmt.setString(1, idseq_);
            _pstmt.setInt(2, type_);
            _pstmt.setString(3, comp_);
            _pstmt.setInt(4, reg_);
            _pstmt.setInt(5, wfs_);
            
            _pstmt.execute();
            _needCommit = true;
        }
        
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            // A code of '1' means a duplicate row was found in the table. We can ignore duplicates
            // but we care about everything else.
            if (_errorCode != 1)
            {
                _errorMsg = _errorCode + ": " + insert + "\n" + ex.toString();
                _logger.error(_errorMsg);
                throw new SearchException(ex);
            }
            _errorCode = 0;
        }
        
        finally
        {
            cleanupWithCatch();
        }
    }

    /**
     * Build the SQL select to pull from the terms index.
     * 
     * @param terms_ the comma separated terms, appropriately enclosed in appostophies
     * @param types_ (optional) the comma separated type codes or null
     * @param orig_ the lower case original search phrase
     * @return the SQL select for the term index
     */
    private String buildSelectTerms(String terms_, String types_, String orig_)
    {
        String select = " select xcm.ac_idseq, xcm.ac_table, sum(xcm.weight) as cnt from "
            + "(select ac_idseq, ac_table, weight from "
            + _indexTable + " where token in ("
            + terms_
            + ") union all select xac.ac_idseq, xtl.ac_table, "
            + "5 * decode(xac.asl_name, 'RELEASED', 5, 'DRAFT NEW', 3, 'DRAFT MOD', 3, 1)"
            + " from sbr.admin_components_view xac, sbrext.gs_tables_lov xtl where lower(xac.long_name) = '"
            + orig_
            + "' and xtl.actl_name = xac.actl_name) xcm";

        if (types_ != null)
            select += " where xcm.ac_table in (" + types_ + ")";

        select += " group by xcm.ac_idseq, xcm.ac_table";

        return select;
    }
    
    /**
     * Build the SQL select to pull from the composite index.
     * 
     * @param terms_
     *            the comma separated terms, appropriately enclosed in appostophies
     * @param types_
     *            (optional) the comma separated type codes or null
     * @param pairs_
     *            the array of terms used as pairs
     * @return the SQL select for the composite index
     */
    private String buildSelectComposite(String terms_, String types_, String[] pairs_)
    {
        String select = "";
        String strongest = " " + pairs_[0];
        String weakest = "%" + pairs_[0];
        String subSelect = "";

        /*
        subSelect += "ac_idseq in (select distinct gs.ac_idseq from " + _indexTable + " gs where ";
        if (types_ != null)
            subSelect += "gs.ac_table in (" + types_ + ") and ";
        subSelect += "gs.token in (" + terms_ + ")) and";
        */

        if (types_ != null)
            subSelect += "ac_table in (" + types_ + ") and ";
        
        for (int i = 1; i < pairs_.length; ++i)
        {
            select += 
                " union all select ac_idseq, ac_table, 10 as cnt"
                + " from " + _compositeTable
                + " where " + subSelect
                + " composite like '% " + pairs_[i - 1] + " " + pairs_[i] + " %' group by ac_idseq, ac_table";
            strongest += " " + pairs_[i];
            weakest += "%" + pairs_[i];
        }

        select += 
            " union all select ac_idseq, ac_table, 10 as cnt"
            + " from " + _compositeTable
            + " where " + subSelect
            + " composite like '%" + strongest + " %' group by ac_idseq, ac_table";

        select += 
            " union all select ac_idseq, ac_table, 5 as cnt"
            + " from " + _compositeTable
            + " where " + subSelect
            + " composite like '" + weakest + "%' group by ac_idseq, ac_table";

        return select.substring(10);
    }

    /**
     * Perform an exact match search.
     * 
     * @param restrict_ the list of types to restrict the search results
     * @param phrase_ the terms of interest
     * @param orig_ the original phrase
     * @return the results list.
     * @exception SearchException
     */
    public Vector<ResultsAC> searchExact(int[] restrict_, String phrase_, String orig_) throws SearchException
    {
        Vector<ResultsAC> var = new Vector<ResultsAC>();

        // Build the IN clause for the types.
        String inClause = null;
        if (restrict_ != null)
        {
            inClause = "";
            for (int i = 0; i < restrict_.length; ++i)
            {
                if (restrict_[i] != 0)
                    inClause = inClause + ", " + i;
            }
            if (inClause.length() == 0)
                return var;
            inClause = inClause.substring(2);
        }

        String phrase = phrase_.replaceAll("[\\s]+", " ");
        String orig = orig_.replaceAll("[\\s]+", " ");
        String[] terms = phrase.split(" ");
        String inTokens = "'" + phrase.replaceAll(" ", "','") + "'";
        
        // Build the select. Note Oracle has a limit of 1000 items in an "IN" clause
        // however we don't expect anyone to every use that many search terms.
        String sqlSelect = "select hits.ac_idseq, hits.ac_table, sum(hits.cnt) as score";
        String sqlFrom = " from";
        String sqlWhere = " where";
        if (terms.length > 1)
        {
            sqlFrom += " (" + buildSelectTerms(inTokens, inClause, orig)
                + " union all " + buildSelectComposite(inTokens, inClause, terms)
                + ") hits";
        }
        else
        {
            sqlFrom += " (" + buildSelectTerms(inTokens, inClause, orig)
                + ") hits";
        }
        
        if (_excludeWFSretired)
        {
            sqlFrom += ", (select ac_idseq from " + _indexTable + " where ac_col = '" + DBAccess.COLRAWWFS + "' and token not IN ("
                + "select opt.value from sbrext.tool_options_view_ext opt where opt.tool_name = 'FREESTYLE' and opt.property LIKE 'RETIRED.WFS.%' and opt.value = token)) wfs";
            sqlWhere += " hits.ac_idseq = wfs.ac_idseq and";
        }
        
        if (_excludeContextNames != null)
        {
            sqlFrom += ", (select ac_idseq from " + _indexTable + " where ac_col = '" + DBAccess.COLOWNEDBYCONTEXT + "' and token not IN (" + _excludeContextNames + ")) ctx";
            sqlWhere += " hits.ac_idseq = ctx.ac_idseq and";
        }
        
        sqlFrom += ", " + _compositeTable + " cp";
        
        sqlWhere += " cp.ac_idseq = hits.ac_idseq and cp.reg_order > 0 group by hits.ac_idseq, hits.ac_table, cp.reg_order, cp.wfs_order  order by score desc, hits.ac_table asc, cp.reg_order asc, cp.wfs_order asc";

        String select = sqlSelect + sqlFrom + sqlWhere;
        try
        {
            // Look for matches.
            _pstmt = _conn.prepareStatement(select);
            _rs = _pstmt.executeQuery();
            copyResults(var);
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + select + "\n" + ex.toString();
            _logger.error(_errorMsg);
            throw new SearchException(ex);
        }
        
        finally
        {
            // Clean up the open statements, etc.
            cleanupWithCatch();
        }

        return var;
    }
    
    /**
     * Perform a partial search for the terms.
     * 
     * @param restrict_ the list of AC types to restrict the results
     * @param phrase_ the terms of interest
     * @return the results list.
     * @exception SearchException
     */
    public Vector<ResultsAC> searchPartial(int[] restrict_, String phrase_) throws SearchException
    {
        Vector<ResultsAC> var = new Vector<ResultsAC>();

        // Build the IN clause for the types.
        String inClause = null;
        if (restrict_ != null)
        {
            inClause = "";
            for (int i = 0; i < restrict_.length; ++i)
            {
                if (restrict_[i] != 0)
                    inClause = inClause + ", " + i;
            }
            if (inClause.length() == 0)
                return var;
            
            inClause = inClause.substring(2);
        }

        // Build the select statement. Unfortunately the "LIKE" does not
        // take a list of values.
        String select = "select hits.ac_idseq, hits.ac_table, sum(hits.weight) as score "
            + "from " + _indexTable + " hits, " + _compositeTable + " cp where";
        
        if (_excludeWFSretired)
        {
            select += " hits.ac_idseq not in (select tt.ac_idseq from " + _indexTable + " tt where tt.ac_idseq = hits.ac_idseq and tt.ac_col = 'asl_name' and tt.token = 'retired') and";
        }
        
        if (_excludeContextNames != null)
        {
            select += " hits.ac_idseq not in (select tt.ac_idseq from " + _indexTable + " tt where tt.ac_idseq = hits.ac_idseq and tt.ac_col = '" + DBAccess.COLOWNEDBYCONTEXT + "' and tt.token in (" + _excludeContextNames + ")) and";
        }
        
        if (inClause != null)
            select += " hits.ac_table in (" + inClause + ") and";

        select += " (hits.token like '%"
                        + phrase_.replaceAll("[\\s]+", "%' or hits.token like '%") + "%') and"
                        + " cp.ac_idseq = hits.ac_idseq and cp.reg_order > 0 group by hits.ac_idseq, hits.ac_table, cp.reg_order, cp.wfs_order order by score desc, hits.ac_table asc, cp.reg_order asc, cp.wfs_order asc";

        try
        {
            // Look for matches.
            _pstmt = _conn.prepareStatement(select);
            _rs = _pstmt.executeQuery();
            copyResults(var);
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + select + "\n" + ex.toString();
            _logger.error(_errorMsg);
            throw new SearchException(ex);
        }
        
        finally
        {
            // Clean up the open statements, etc.
            cleanupWithCatch();
        }

        return var;
    }

    /**
     * Copy the results from the database search for further processing.
     * 
     * @param var_ the results vector
     * @exception SearchException
     */
    private void copyResults(Vector<ResultsAC> var_) throws SearchException
    {
        try
        {
            // There is a configurable limit to the maximum results returned.
            int scoreChgs = (_scoreLimit == 0) ? 999999 : _scoreLimit;
            ++scoreChgs;
            int lastScore = -1;
            int limit = _limit;
            while (_rs.next() && limit > 0)
            {
                // Keep the results for later processing.
                String idseq = _rs.getString(1);
                int table = _rs.getInt(2);
                int score = _rs.getInt(3);
                if (0 <= table && table < DBAccess._desc.length)
                {
                    if (lastScore != score)
                    {
                        --scoreChgs;
                        if (scoreChgs == 0)
                            break;
                        lastScore = score;
                    }
                    var_.add(new ResultsAC(
                        idseq
                        ,DBAccess._desc[table]
                        ,score));
                    --limit;
                }
                else
                    _logger.warn("Extended data found in the Index Tables. Upgrade to a current release of Freestyle Search. [" + table + ", " + score + ", " + idseq + "]");
            }
        }
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + ex.toString();
            throw new SearchException(ex);
        }
    }
    
    /**
     * Truncate the index tables in preparation for a complete reload.
     *
     * @exception SearchException
     */
    public void truncateTables() throws SearchException
    {
        String sql;
        sql = "begin sbrext.freestyle_pkg.truncate_gs_tables; end;";
        CallableStatement stmt = null;
        try
        {
            stmt = _conn.prepareCall(sql);
            stmt.executeUpdate();

            _logger.info("Index Tables truncated.");
        }
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + sql + "\n" + ex.toString();
            _logger.error(_errorMsg);
            throw new SearchException(ex);
        }
        finally
        {
            if (stmt != null)
            {
                try { stmt.close(); } catch (Exception ex) { }
            }
        }
    }
    
    /**
     * Erase existing records in the freestyle index tables.
     * 
     * @param ids_ the list of database id's
     * @exception SearchException
     */
    public void erase(String[] ids_) throws SearchException
    {
        if (ids_ == null || ids_.length == 0)
            return;

        int pos = 0;
        while (true)
        {
            if (pos == ids_.length)
                break;

            String inc = "";
            for (int i = 0; pos < ids_.length && i < 1000; ++i)
            {
                inc += ", '" + ids_[pos++] + "'";
            }
            inc = inc.substring(2);
            
            // Build the delete using a sub-select to match that used to query
            // the database for records to load into the search index table.
            String delete = "delete from " + _indexTable + " where ac_idseq in (" + inc + ")";
    
            try
            {
                _pstmt = _conn.prepareStatement(delete);
                
                _pstmt.execute();
                
                int count = _pstmt.getUpdateCount();
                _logger.info("Deleted " + count + " records from " + _indexTable);
            }
            
            // We had an unexpected problem.
            catch (SQLException ex)
            {
                _errorCode = ex.getErrorCode();
                _errorMsg = _errorCode + ": " + delete + "\n" + ex.toString();
                _logger.error(_errorMsg);
                throw new SearchException(ex);
            }
            
            finally
            {
                cleanupWithCatch();
            }
            
            commit();
            
            delete = "delete from " + _compositeTable + " where ac_idseq in ( " + inc + ")";
    
            try
            {
                _pstmt = _conn.prepareStatement(delete);
                
                _pstmt.execute();
                
                int count = _pstmt.getUpdateCount();
                _logger.info("Deleted " + count + " records from " + _compositeTable);
            }
            
            // We had an unexpected problem.
            catch (SQLException ex)
            {
                _errorCode = ex.getErrorCode();
                _errorMsg = _errorCode + ": " + delete + "\n" + ex.toString();
                _logger.error(_errorMsg);
                throw new SearchException(ex);
            }
            
            finally
            {
                // Cleanup and commit changes.
                cleanupWithCatch();
            }
            
            commit();
        }
    }
    
    /**
     * Commit any changes.
     *
     *@exception SearchException
     */
    public void commit() throws SearchException
    {
        try
        {
            _conn.commit();
        }
        catch (SQLException ex)
        {
            throw new SearchException(ex);
        }
        finally
        {
            _needCommit = false;
        }
    }
    
    /**
     * Check the string provided against the list of excluded words.
     * 
     * @param txt_ a token word/term
     * @return true if the word is in the list, otherwise false
     */
    public boolean checkExcludes(String txt_)
    {
        // Use a binary search on the list. This REQUIRES any updates
        // to the list be kept alphabetically ascending.
        int min = 0;
        int max = _exclude.length;
        while (true)
        {
            int pos = (min + max) / 2;
            int rc = txt_.compareTo(_exclude[pos]);
            if (rc == 0)
            {
                return true;
            }
            else if (rc < 0)
            {
                if (max == pos)
                    break;
                max = pos;
            }
            else
            {
                if (min == pos)
                    break;
                min = pos;
            }
        }

        return false;
    }
    
    /**
     * Set the schema used for the freestyle index tables.
     * 
     * @param schema_ the schema name.
     */
    public static void setSchema(String schema_)
    {
        String[] temp = _indexTable.split("[.]");
        _indexTable = schema_ + "." + temp[1];

        temp = _compositeTable.split("[.]");
        _compositeTable = schema_ + "." + temp[1];

        _schema = schema_;
    }
    
    /**
     * Split the phrase using the reserved characters.
     * 
     * @param phrase_ the original text to be parsed
     * 
     * @return the tokens determined by the reserved characters.
     */
    public static String[] split(String phrase_)
    {
        return phrase_.split(_tokenChars);
    }
    
    /**
     * Convert all reserved characters to spaces.
     * 
     * @param phrase_ the search text
     * 
     * @return the modified text with replacements
     */
    public static String replaceAll(String phrase_)
    {
        return phrase_.replaceAll(_tokenChars, " ");
    }
    
    /**
     * Set the names of Contexts which should be excluded from the
     * search. These are "Owned By" Contexts only, not "Used By". Use
     * this method with no arguments to clear the exclusion list.
     * 
     * @param names_ the context name(s)
     */
    public void setExcludeContextNames(String ... names_)
    {
        if (names_ == null || names_.length == 0)
        {
            _excludeContextNames = null;
            return;
        }

        for (String name : names_)
        {
            if (_excludeContextNames == null)
                _excludeContextNames = "'" + name.toLowerCase() + "'";
            else
                _excludeContextNames += ",'" + name.toLowerCase() + "'";
        }
    }
    
    private String _excludeContextNames;
    private int _limit;
    private boolean _excludeWFSretired;
    private int _scoreLimit;
    private boolean _needCommit;
    private int _errorCode;
    private String _errorMsg;
    private Connection _conn;
    private boolean _connFlag;
    private ResultSet _rs;
    private PreparedStatement _pstmt;

    private static String _indexTable = "sbrext.gs_tokens";
    private static String _compositeTable = "sbrext.gs_composite";
    private static final String _tokenChars = "[ ,._?!*':;&<>(){}\"\\[\\]\\t\\r\\n]";

    /**
     * The database schema.
     */
    public static String _schema = "sbrext";

    private static final String[] _exclude = {
        "act"
        ,"all"
        ,"also"
        ,"am"
        ,"an"
        ,"and"
        ,"and/or"
        ,"any"
        ,"are"
        ,"are/have"
        ,"as"
        ,"ask"
        ,"asks"
        ,"at"
        ,"be"
        ,"became"
        ,"become"
        ,"becomes"
        ,"been"
        ,"being"
        ,"between"
        ,"but"
        ,"by"
        ,"can"
        ,"check"
        ,"could"
        ,"describe"
        ,"description"
        ,"do"
        ,"due"
        ,"during"
        ,"each"
        ,"easy"
        ,"either"
        ,"etc"
        ,"ever"
        ,"every"
        ,"everywhere"
        ,"for"
        ,"from"
        ,"give"
        ,"given"
        ,"had"
        ,"has"
        ,"has/had"
        ,"have"
        ,"have/had"
        ,"haves"
        ,"having"
        ,"her"
        ,"here"
        ,"him"
        ,"his"
        ,"how"
        ,"however"
        ,"if"
        ,"in"
        ,"include"
        ,"includes"
        ,"including"
        ,"is"
        ,"is/was"
        ,"it"
        ,"its"
        ,"itself"
        ,"kind"
        ,"less"
        ,"may"
        ,"more"
        ,"most"
        ,"must"
        ,"neither"
//        ,"no"
        ,"not"
        ,"of"
//        ,"off"
//        ,"on"
        ,"only"
        ,"or"
        ,"other"
        ,"otherwise"
        ,"please"
        ,"see"
        ,"seem"
        ,"seems"
        ,"seen"
        ,"sees"
        ,"should"
        ,"since"
        ,"specified"
        ,"specify"
        ,"taken"
        ,"test"
        ,"tests"
        ,"that"
        ,"the"
        ,"their"
        ,"them"
        ,"there"
        ,"these"
        ,"thing"
        ,"think"
        ,"this"
        ,"those"
        ,"though"
        ,"to"
        ,"try"
        ,"trying"
        ,"type"
        ,"use"
        ,"used"
        ,"uses"
        ,"using"
        ,"was"
        ,"way"
        ,"ways"
        ,"were"
        ,"what"
        ,"when"
        ,"where"
        ,"whether"
        ,"which"
        ,"who"
        ,"whom"
        ,"whose"
        ,"will"
        ,"with"
        ,"within"
        ,"would"
//        ,"yes"
//        ,"yes/no"
        ,"you"
        ,"your"
    };

    private static final Logger _logger = Logger.getLogger(DBAccessIndex.class.getName());
}
