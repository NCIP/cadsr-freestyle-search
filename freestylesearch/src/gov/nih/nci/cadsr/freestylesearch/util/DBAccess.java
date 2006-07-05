// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/DBAccess.java,v 1.3 2006-07-05 14:53:51 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import oracle.jdbc.pool.OracleDataSource;

/**
 * Provide access to the caDSR and Freestyle Index tables.
 * 
 * @author lhebel Mar 3, 2006
 */
public class DBAccess
{
    /**
     * Constructor
     * 
     * Set defaults.
     *
     */
    public DBAccess()
    {
        _errorCode = 0;
        _errorMsg = "";
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
     * Get the last error message occurring during a database access.
     * 
     * @return the last error message.
     */
    public String getErrorMsg()
    {
        return _errorMsg;
    }
    
    /**
     * Get the last error code occurring during a database access.
     * 
     * @return the last error code.
     */
    public int getErrorCode()
    {
        return _errorCode;
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
     * @return The database error code.
     */
    public int open(String dburl_, String user_, String pswd_)
    {
        // If we already have a connection, don't bother.
        if (_conn != null)
            return 0;

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
            return 0;
        }
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": "
                + dburl_ + ", " + user_ + ", " + pswd_ + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
            return _errorCode;
        }
    }
    
    /**
     * Set the connection to the one provided. The connection is not closed by this class because it was not
     * created by this class.
     * 
     * @param conn_ a database connection to use.
     * 
     * @return 0 if successful, otherwise not 0.
     */
    public int open(Connection conn_)
    {
        // Since we didn't create the connection set a flag to ensure we
        // don't close the connection.
        try
        {
            _conn = conn_;
            _conn.setAutoCommit(false);
            _needCommit = false;
            _connFlag = false;
            return 0;
        }
        catch (SQLException ex)
        {
            _logger.fatal(ex.toString());
            return ex.getErrorCode();
        }
    }
    
    /**
     * Get a connection from the specified datasource.
     * 
     * @param ds_ a datasource to provide the connection.
     * @param user_ the user name/account for the connection.
     * @param pswd_ the password for the account.
     * 
     * @return 0 if successful, otherwise not 0.
     */
    public int open(DataSource ds_, String user_, String pswd_)
    {
        try
        {
            _conn = ds_.getConnection(user_, pswd_);
            _conn.setAutoCommit(false);
            _needCommit = false;
            return 0;
        }
        catch (SQLException ex)
        {
            _logger.fatal(ex.toString());
            return ex.getErrorCode();
        }
    }
    
    /**
     * Get a connection from the specified datasource.
     * 
     * @param ds_ a datasource to provide the connection.
     * 
     * @return 0 if successful, otherwise not 0.
     */
    public int open(DataSource ds_)
    {
        try
        {
            _conn = ds_.getConnection();
            _conn.setAutoCommit(false);
            _needCommit = false;
            return 0;
        }
        catch (SQLException ex)
        {
            _logger.fatal(ex.toString());
            return ex.getErrorCode();
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
                
                // Close only if we opened it.
                if (_connFlag)
                    _conn.close();
                
                _conn = null;
            }
        }
        catch (SQLException e)
        {
            _logger.fatal(e.toString());
        }
    }
    
    /**
     * Clean up the open handles.
     *
     * @throws SQLException 
     */
    public void cleanup() throws SQLException
    {
        if (_rs != null)
            _rs.close();
        if (_pstmt != null)
            _pstmt.close();
        
        _rs = null;
        _pstmt = null;
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
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " 
                + ex.toString();
            _logger.fatal(_errorMsg);
        }
    }
    
    /**
     * Read a table.
     * 
     * @param ac_ the record/table description
     * @param start_ the timestamp from which to select records
     * @return the result set from the select.
     */
    public ResultSet readTable(GenericAC ac_, Timestamp start_)
    {
        // Build the select column list.
        String[] cols = ac_.getColumns();
        String select = "";
        for (int i = 0; i < cols.length; ++i)
        {
            if (cols[i].charAt(0) == '\'')
                select = select + ", " + cols[i];
            else
                select = select + ", zz." + cols[i];
        }
        
        // Add the From clause
        select = "select " + select.substring(2)
            + ", conte.name, rs.registration_status from " + ac_.getTableName() +" zz, sbr.contexts_view conte, sbr.ac_registrations_view rs "
            +  _qualWhere + " and conte.conte_idseq = zz.conte_idseq and rs.ac_idseq(+) = zz." + ac_.getIdseqName();

        // Get the result set.
        try
        {
            _pstmt = _conn.prepareStatement(select);
            _pstmt.setTimestamp(1, start_);
            _rs = _pstmt.executeQuery();
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }

        return _rs;
    }
    
    /**
     * Read a table.
     * 
     * @param ac_ the record/table description
     * @param start_ the timestamp from which to select records
     * @return the result set from the select.
     */
    public ResultSet readAlternateTable(ACAlternate ac_, Timestamp start_)
    {
        // Build the select column list.
        String[] cols = ac_.getColumns();
        String select = "";
        for (int i = 0; i < cols.length; ++i)
        {
            if (cols[i].charAt(0) == '\'')
                select = select + ", " + cols[i];
            else
                select = select + ", alt." + cols[i];
        }
        
        // Add the From clause
        select = "select " + select.substring(2) + ", conte.name from " + ac_.getTableName() + " alt, "
            + ac_.getRootTableName()
            + " zz, sbr.contexts_view conte where alt." + ac_.getIdseqName() + " = zz." + ac_.getRootIdseqName()
            + " and (nvl(zz.date_modified, zz.date_created) >= ? or nvl(alt.date_modified, alt.date_created) >= ?)"
            + " and conte.conte_idseq = alt.conte_idseq";

        // Get the result set.
        try
        {
            _pstmt = _conn.prepareStatement(select);
            _pstmt.setTimestamp(1, start_);
            _pstmt.setTimestamp(2, start_);
            _rs = _pstmt.executeQuery();
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }

        return _rs;
    }
    
    /**
     * Insert values into the term index table.
     * 
     * @param type_ the numeric identifier for the record type
     * @param idseq_ the unique database id for the record
     * @param col_ the column name from which the 'token_' was read
     * @param term_ the token to save in the search index table
     */
    private void insertTerm(int type_, String idseq_, String col_, String term_)
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
                _errorMsg = _errorCode + ": " + insert
                    + "\n" + ex.toString();
                _logger.fatal(_errorMsg);
            }
            else
                _errorCode = 0;
        }
    }

    /**
     * Insert the composite term string into the composite index table.
     * 
     * @param type_ the numeric identifier for the record type
     * @param idseq_ the unique database id for the record
     * @param comp_ the composite term string
     */
    private void insertComposite(int type_, String idseq_, String comp_)
    {
        // Build the SQL insert statement and save the data. 
        String insert = "insert into " + _compositeTable
            + " (ac_idseq, ac_table, composite) "
            + "values (?, ?, ?)";

        try
        {
            if (_pstmt == null)
                _pstmt = _conn.prepareStatement(insert);
            _pstmt.setString(1, idseq_);
            _pstmt.setInt(2, type_);
            _pstmt.setString(3, comp_);
            
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
                _errorMsg = _errorCode + ": " + insert
                    + "\n" + ex.toString();
                _logger.fatal(_errorMsg);
            }
            else
                _errorCode = 0;
        }

        cleanupWithCatch();
    }
    
    /**
     * Commit any changes.
     *
     */
    public void commit()
    {
        try
        {
            _conn.commit();
            _needCommit = false;
        }
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " 
                + ex.toString();
            _logger.fatal(_errorMsg);
        }
    }

    /**
     * Copy the results from the database search for further processing.
     * 
     * @param var_ the results vector
     */
    private void copyResults(Vector<ResultsAC> var_)
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
                if (0 <= table && table < _desc.length)
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
                        ,_desc[table]
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
            _logger.fatal(_errorMsg);
        }
    }

    /**
     * Build the SQL select to pull from the terms index.
     * 
     * @param terms_ the comma separated terms, appropriately enclosed in appostophies
     * @param types_ (optional) the comma separated type codes or null
     * @return the SQL select for the term index
     */
    private String buildSelectWeightedTerms(String terms_, String types_)
    {
        String select =
            " select ac_idseq, ac_table, sum(weight) as cnt"
            + " from " + _indexTable;
        if (types_ == null)
            select += " where token in (" + terms_ + ")";
        else
            select += " where ac_table in (" + types_ + ") and token in (" + terms_ + ")";
            
        return select + " group by ac_idseq, ac_table";
    }

    /**
     * Build the SQL select to pull from the terms index.
     * 
     * @param terms_ the comma separated terms, appropriately enclosed in appostophies
     * @param types_ (optional) the comma separated type codes or null
     * @return the SQL select for the term index
     */
    private String buildSelectTerms(String terms_, String types_)
    {
        String select = " select ac_idseq, ac_table, sum(1) as cnt from " + _indexTable + " where ";

        if (types_ != null)
            select += "ac_table in (" + types_ + ") and ";

        select += "token in (" + terms_ + ") group by ac_idseq, ac_table";

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
                " union all select ac_idseq, ac_table, sum(10) as cnt"
                + " from " + _compositeTable
                + " where " + subSelect
                + " composite like '% " + pairs_[i - 1] + " " + pairs_[i] + " %' group by ac_idseq, ac_table";
            strongest += " " + pairs_[i];
            weakest += "%" + pairs_[i];
        }

        select += 
            " union all select ac_idseq, ac_table, sum(10) as cnt"
            + " from " + _compositeTable
            + " where " + subSelect
            + " composite like '%" + strongest + " %' group by ac_idseq, ac_table";

        select += 
            " union all select ac_idseq, ac_table, sum(5) as cnt"
            + " from " + _compositeTable
            + " where " + subSelect
            + " composite like '" + weakest + "%' group by ac_idseq, ac_table";

        return select.substring(10);
    }
    
    /**
     * Perform an exact match search.
     * 
     * @param phrase_ the tokens of interest
     * @return the results list.
     */
    public Vector<ResultsAC> searchExact(String phrase_)
    {
        Vector<ResultsAC> var = new Vector<ResultsAC>();
        String phrase = phrase_.replaceAll("[\\s]+", " ");
        String[] terms = phrase.split(" ");
        String inTokens = "'" + phrase.replaceAll(" ", "','") + "'"; 

        // Build the select. Note Oracle has a limit of 1000 items in an "IN" clause
        // however we don't expect anyone to every use that many search terms.
        String select;
        if (terms.length > 1)
            select = "select hits.ac_idseq, hits.ac_table, sum(hits.cnt) as score from " 
                + "(" + buildSelectTerms(inTokens, null)
                + " union all " + buildSelectComposite(inTokens, null, terms)
                + ") hits";
        else
            select = "select hits.ac_idseq, hits.ac_table, sum(cnt) as score from " 
                + "(" + buildSelectTerms(inTokens, null)
                + ") hits";
        
        if (_excludeWFSretired)
        {
            select += " where hits.ac_idseq not in (select tt.ac_idseq from " + _indexTable + " tt where tt.ac_idseq = hits.ac_idseq and tt.ac_col = 'asl_name' and tt.token = 'retired')";
        }
        
        select += " group by hits.ac_idseq, hits.ac_table  order by score desc, hits.ac_table asc";

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
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }

        // Clean up the open statements, etc.
        cleanupWithCatch();

        return var;
    }
    
    /**
     * Perform an exact match search.
     * 
     * @param restrict_ the list of types to restrict the search results
     * @param phrase_ the terms of interest
     * @return the results list.
     */
    public Vector<ResultsAC> searchExact(int[] restrict_, String phrase_)
    {
        Vector<ResultsAC> var = new Vector<ResultsAC>();

        // Build the IN clause for the types.
        String inClause = "";
        for (int i = 0; i < restrict_.length; ++i)
        {
            if (restrict_[i] != 0)
                inClause = inClause + ", " + i;
        }
        if (inClause.length() == 0)
            return var;
        inClause = inClause.substring(2);

        String phrase = phrase_.replaceAll("[\\s]+", " ");
        String[] terms = phrase.split(" ");
        String inTokens = "'" + phrase.replaceAll(" ", "','") + "'";
        
        // Build the select. Note Oracle has a limit of 1000 items in an "IN" clause
        // however we don't expect anyone to every use that many search terms.
        String select;
        if (terms.length > 1)
            select = "select hits.ac_idseq, hits.ac_table, sum(hits.cnt) as score from " 
                + "(" + buildSelectTerms(inTokens, inClause)
                + " union all " + buildSelectComposite(inTokens, inClause, terms)
                + ") hits";
        else
            select = "select ac_idseq, ac_table, sum(cnt) as score from " 
                + "(" + buildSelectTerms(inTokens, inClause)
                + ") hits";
        
        if (_excludeWFSretired)
        {
            select += " where hits.ac_idseq not in (select tt.ac_idseq from " + _indexTable + " tt where tt.ac_idseq = hits.ac_idseq and tt.ac_col = 'asl_name' and tt.token = 'retired')";
        }
        
        select += " group by hits.ac_idseq, hits.ac_table  order by score desc, hits.ac_table asc";

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
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }

        // Clean up the open statements, etc.
        cleanupWithCatch();

        return var;
    }
    
    /**
     * Perform a partial search for the terms.
     * 
     * @param phrase_ the terms of interest
     * @return the results list.
     */
    public Vector<ResultsAC> searchPartial(String phrase_)
    {
        Vector<ResultsAC> var = new Vector<ResultsAC>();

        // Build the select statement. Unfortunately the "LIKE" does not
        // take a list of values.
        String select = "select hits.ac_idseq, hits.ac_table, sum(hits.weight) as score "
            + "from " + _indexTable + " hits where";
        
        if (_excludeWFSretired)
        {
            select += " hits.ac_idseq not in (select tt.ac_idseq from " + _indexTable + " tt where tt.ac_idseq = hits.ac_idseq and tt.ac_col = 'asl_name' and tt.token = 'retired') and";
        }
        
        select += " hits.token like '%"
            + phrase_.replaceAll("[\\s]+", "%' or hits.token like '%") + "%' group by hits.ac_idseq, hits.ac_table  order by score desc, hits.ac_table asc";

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
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }
        
        // Clean up the open statements, etc.
        cleanupWithCatch();

        return var;
    }
    
    /**
     * Perform a partial search for the terms.
     * 
     * @param restrict_ the list of AC types to restrict the results
     * @param phrase_ the terms of interest
     * @return the results list.
     */
    public Vector<ResultsAC> searchPartial(int[] restrict_, String phrase_)
    {
        Vector<ResultsAC> var = new Vector<ResultsAC>();

        // Build the IN clause for the types.
        String inClause = "";
        for (int i = 0; i < restrict_.length; ++i)
        {
            if (restrict_[i] != 0)
                inClause = inClause + ", " + i;
        }
        if (inClause.length() == 0)
            return var;

        // Build the select statement. Unfortunately the "LIKE" does not
        // take a list of values.
        String select = "select hits.ac_idseq, hits.ac_table, sum(hits.weight) as score "
            + "from " + _indexTable + " hits where";
        
        if (_excludeWFSretired)
        {
            select += " hits.ac_idseq not in (select tt.ac_idseq from " + _indexTable + " tt where tt.ac_idseq = hits.ac_idseq and tt.ac_col = 'asl_name' and tt.token = 'retired') and";
        }

        select += " hits.ac_table in (" + inClause.substring(2) + ") and (hits.token like '%"
                        + phrase_.replaceAll("[\\s]+", "%' or hits.token like '%") + "%') "
                        + "group by hits.ac_idseq, hits.ac_table order by score desc, hits.ac_table asc";
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
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }
        
        // Clean up the open statements, etc.
        cleanupWithCatch();

        return var;
    }
    
    /**
     * Get the default display information from the results list provided.
     * 
     * @param list_ results from a Search.find().
     * @return The default display information.
     */
    public Vector<String> getDisplay(Vector<ResultsAC> list_)
    {
        Vector<String> results = new Vector<String>();
        if (list_ == null || list_.size() == 0)
            return results;

        String decode = "decode(hits.ac_table";
        for (int i = 0; i < _desc.length; ++i)
        {
            decode += "," + String.valueOf(i) + ",'" + _desc[i].getTypeName() + "'";
        }
        decode += ") ";
        
        ResultsAC obj;
        String data = "";
        String uall = "union all ";
        for (int i = 0; i < list_.size(); ++i)
        {
            obj = list_.get(i);
            data += "union all select '" + obj._idseq + "' as ac_idseq, " + obj._desc.getMasterIndex() + " as ac_table, " + obj._score + " as score from dual ";
        }
        data = data.substring(uall.length());

        String select = "";
        select = "select ac.long_name "
            + "|| '\n\t' || " + decode
            + "|| '\n\tPublic ID: ' || ac.public_id "
            + "|| '\n\tVersion: ' || ac.version "
            + "|| '\n\tContext: ' || c.name "
            + "|| '\n\tWorkflow Status: ' || ac.asl_name "
            + "|| '\n\tRegistration Status: ' || nvl(rs.registration_status, ' ') "
            + "|| '\n\tScore: ' || hits.score "
            + "from (" + data + ") hits, sbr.admin_components_view ac, sbr.contexts_view c, sbr.ac_registrations_view rs "
            + "where ac.ac_idseq = hits.ac_idseq and c.conte_idseq = ac.conte_idseq and rs.ac_idseq(+) = ac.ac_idseq "
            + "order by hits.score desc, hits.ac_table asc, upper(ac.long_name) asc";
        
        try
        {
            // Set the database id for each sub-select.
            _pstmt = _conn.prepareStatement(select);
            
            // Get the display and save for later.
            _rs = _pstmt.executeQuery();
            while (_rs.next())
            {
                String display = _rs.getString(1);
                results.add(display);
            }
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + select.substring(0, 80) + " ...\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }

        // Clean up any open statements, etc.
        cleanupWithCatch();
        
        return results;
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
     * Parse the database and load the search index table.
     *
     * @param start_ the start date for records of interest.
     * @param indexTable_ The database object which holds the index table.
     */
    public void parseDatabase(Timestamp start_, DBAccess indexTable_)
    {
        // Loop through table descriptions and read the desired records
        // from the database.
        for (int i = 0; i < _desc.length; ++i)
        {
            GenericAC ac = _desc[i];
            int total = updateCount(ac, start_);
            _logger.info(" ");
            _logger.info("Doing... " + ac.getTypeName() + " " + total + " records");

            // Clean anything we will be replacing.
            if (start_.getTime() < Timestamp.valueOf("2000-01-01 00:00:00.0").getTime())
                ;
/*
            else if (_dbData.equals(_dbIndex))
            {
                _logger.info("... deleting changed records ...");
                indexTable_.erase(ac, start_);
            }
*/
            else
            {
                _logger.info("... deleting changed records ...");
                String[] ids = new String[total];
                eraseList(ac, start_, ids);
                indexTable_.erase(ids);
            }

            // Read the table.
            ResultSet rs = readTable(ac, start_);
            if (rs != null)
            {
                int updcnt = 0;
                int count = 0;
                try
                {
                    // Read form the database.
                    while (rs.next())
                    {
                        ++count;
                        if (updcnt == 0)
                        {
                            updcnt = (total + 9) / 10;
                            _logger.info(count);
                        }
                        --updcnt;
                        parseRecord(ac, rs, indexTable_);
                    }
                    cleanup();
                }
                catch (SQLException e)
                {
                    _logger.fatal(e.toString());
                }
            }

            // Read the alternate table.
            ACAlternate alt = new ACAlternate(ac);
            total = updateCount(alt, start_);
            _logger.info(" ");
            _logger.info("Doing... Alternates for " + alt.getTypeName() + " " + total + " possible records");
            rs = readAlternateTable(alt, start_);
            if (rs != null)
            {
                int updcnt = 0;
                int count = 0;
                try
                {
                    // Read form the database.
                    while (rs.next())
                    {
                        ++count;
                        if (updcnt == 0)
                        {
                            updcnt = (total + 9) / 10;
                            _logger.info(count);
                        }
                        --updcnt;
                        parseRecord(alt, rs, indexTable_);
                    }
                    cleanup();
                }
                catch (SQLException e)
                {
                    _logger.fatal(e.toString());
                }
            }
        }
    }

    /**
     * Build the list of database ID's which have been modified on or after
     * the date specified.
     * 
     * @param ac_ the AC of interest
     * @param start_ the comparison date
     * @param ids_ the list of ID's to be updated in the freestyle index tables
     */
    private void eraseList(GenericAC ac_, Timestamp start_, String[] ids_)
    {
        // Build the delete using a sub-select to match that used to query
        // the database for records to load into the search index table.
        String select = "select zz." + ac_.getIdseqName() + " from " + ac_.getTableName() + " zz"
            + _qualWhere;

        try
        {
            _pstmt = _conn.prepareStatement(select);
            _pstmt.setTimestamp(1, start_);
            
            ResultSet rs = _pstmt.executeQuery();
            int cnt = 0;
            while (rs.next())
            {
                ids_[cnt++] = rs.getString(1);
            }
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }
        
        cleanupWithCatch();
    }
    
    /**
     * Erase existing records in the freestyle index tables.
     * 
     */
    private void erase(String[] ids_)
    {
        if (ids_ == null || ids_.length == 0)
            return;

        String inc = "";
        for (int i = 0; i < ids_.length; ++i)
        {
            inc += ", '" + ids_[i] + "'";
        }
        inc = inc.substring(2);
        
        // Build the delete using a sub-select to match that used to query
        // the database for records to load into the search index table.
        String delete = "delete from " + _indexTable + " gst where gst.ac_idseq in (" + inc + ")";

        try
        {
            _pstmt = _conn.prepareStatement(delete);
            
            _pstmt.execute();
            
            _pstmt.close();
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + delete
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }
        cleanupWithCatch();
        
        delete = "delete from " + _compositeTable + " gst where gst.ac_idseq in ( " + inc + ")";

        try
        {
            _pstmt = _conn.prepareStatement(delete);
            
            _pstmt.execute();
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + delete
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }
        
        // Cleanup and commit changes.
        cleanupWithCatch();
        commit();
    }
    
    /**
     * Erase existing records in the search index table.
     * 
     * @param ac_ the record/table type of interest
     * @param start_ the start date for records of interest

    private void erase(GenericAC ac_, Timestamp start_)
    {
        // Build the delete using a sub-select to match that used to query
        // the database for records to load into the search index table.
        String delete = "delete from " + _indexTable + " gst where gst.ac_table = ? and gst.ac_idseq in ( "
            + "select zz." + ac_.getIdseqName() + " from " + ac_.getTableName() + " zz"
            + _qualWhere + ")";

        try
        {
            _pstmt = _conn.prepareStatement(delete);
            _pstmt.setInt(1, ac_.getMasterIndex());
            _pstmt.setTimestamp(2, start_);
            
            _pstmt.execute();
            
            _pstmt.close();
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + delete
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }
        cleanupWithCatch();
        
        delete = "delete from " + _compositeTable + " gst where gst.ac_table = ? and gst.ac_idseq in ( "
        + "select zz." + ac_.getIdseqName() + " from " + ac_.getTableName() + " zz"
        + _qualWhere + ")";

        try
        {
            _pstmt = _conn.prepareStatement(delete);
            _pstmt.setInt(1, ac_.getMasterIndex());
            _pstmt.setTimestamp(2, start_);
            
            _pstmt.execute();
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + delete
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }
        
        // Cleanup and commit changes.
        cleanupWithCatch();
        commit();
    }
*/

    /**
     * Get the update count for the table changes.
     * 
     * @param ac_ the record/table type of interest
     * @param start_ the start date for records of interest
     * @return return the record count for the number of changes
     */
    private int updateCount(GenericAC ac_, Timestamp start_)
    {
        // Build the delete using a sub-select to match that used to query
        // the database for records to load into the search index table.
        String select = "select count(*) from " + ac_.getTableName() + " zz"
            + _qualWhere;

        int count = 0;
        try
        {
            _pstmt = _conn.prepareStatement(select);
            _pstmt.setTimestamp(1, start_);
            
            _rs = _pstmt.executeQuery();
            if (_rs.next())
                count = _rs.getInt(1);
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }

        // Cleanup and commit changes.
        cleanupWithCatch();
        return count;
    }

    /**
     * Parse a record read from the database.
     * 
     * @param ac_ the record/table type description
     * @param rs_ the current record to process
     * @param dbWrite_ the connection to update the search index table
     * 
     * @throws SQLException
     */
    private void parseRecord(GenericAC ac_, ResultSet rs_, DBAccess dbWrite_) throws SQLException
    {
        // Get the list of columns for this record/table type.
        int type = ac_.getMasterIndex();
        String idseq = "(none)";
        int rsLen = rs_.getMetaData().getColumnCount();
        String[] cols = new String[rsLen];
        for (int i = 0; i < cols.length; ++i)
        {
                cols[i] = rs_.getMetaData().getColumnName(i + 1).toLowerCase();
        }
        
        // The results of the read will be in the same order and have the same number of columns.
        String composite = "";
        for (int i = 0; i < cols.length; ++i)
        {
            String temp = rs_.getString(i + 1);
            if (temp == null)
                continue;
            
            // Split the column value into multiple tokens.
            String[] tokens = temp.split(_tokenChars);
            switch (i)
            {
                // This is the database id column so just remember it for all future
                // output.
                case GenericAC.AC_IDSEQ:
                    idseq = temp;
                    break;
                    
                // This is the 'version' column so remember the original value
                // and append a '.0' when necessary.
                case GenericAC.AC_VERSION:
                    dbWrite_.insertTerm(type, idseq, cols[i], temp);
                    if (tokens.length == 1)
                    {
                        temp = temp + ".0";
                        dbWrite_.insertTerm(type, idseq, cols[i], temp);
                    }
                    break;
                    
                // This is the 'type' column so remember the returned value
                // and expand to the full description of the type.
                case GenericAC.AC_TYPE:
                    dbWrite_.insertTerm(type, idseq, cols[i], temp);
                    tokens = ac_.getTypeName().split(" ");

                    // Fall through and let the normal processing handle the expanded type name.

                default:
                    // Write every token to the search index table.
                    for (int j = 0; j < tokens.length; ++j)
                    {
                        // Of course the token has to be longer than 1 character, we aren't
                        // going to save words like "I" or "A".
                        if (tokens[j] != null && tokens[j].length() > 1)
                        {
                            // All tokens will be saved in lower case.
                            String lower = tokens[j].toLowerCase();
                            
                            // If there is only one token, then save it, if there are
                            // multiple tokens then qualify the word using an exclude
                            // list.
                            if (tokens.length > 1 && dbWrite_.checkExcludes(lower))
                                continue;
                            
                            // Write the token.
                            dbWrite_.insertTerm(type, idseq, cols[i], lower);
                            
                            // Some words are different because they are hyphenated or
                            // combinations. Now that we've saved the original we need
                            // to save the individual pieces of a compound word.
                            String[] tokens2 = lower.split("[-/]");
                            if (tokens2.length > 1)
                            {
                                for (int k = 0; k < tokens2.length; ++k)
                                {
                                    // Because this is a compound word we save all parts
                                    // and do not worry about the excluded words list.
                                    if (tokens2[k] != null && tokens2[k].length() > 0)
                                    {
                                        dbWrite_.insertTerm(type, idseq, cols[i], tokens2[k]);
                                        composite += " " + tokens2[k];
                                    }
                                }
                            }
                            else
                            {
                                composite += " " + lower;
                            }
                        }
                    }
                    break;
            }
        }
        dbWrite_.cleanupWithCatch();
        
        // Add the composite string.
        if (composite.length() > 0)
            dbWrite_.insertComposite(type, idseq, composite + " ");

        // Cleanup and stuff.
        dbWrite_.commit();
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
     * Return the type names for each of the record/table types used by the search.
     * 
     * @return the list of supported types
     */
    public static String[] getTypes()
    {
        if (_desc == null)
            return new String[0];

        String[] list = new String[_desc.length];
        for (int i = 0; i < list.length; ++i)
            list[i] = _desc[i].getTypeName();
        return list;
    }
    
    /**
     * Get the last seed operation timestamp.
     * 
     * @return the last seed operation timestamp.
     */
    public Timestamp getLastSeedTimestamp()
    {
        String schema = _schema.toUpperCase();
        String select = "select value from sbrext.tool_options_view_ext where tool_name = 'FREESTYLE' and property = 'SEED.LASTUPDATE' and ua_name = '" + schema + "'";
        Timestamp rc = null;
        try
        {
            _pstmt = _conn.prepareStatement(select);
            
            _rs = _pstmt.executeQuery();
            if (_rs.next())
                rc = Timestamp.valueOf(_rs.getString(1));
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }

        // Cleanup and commit changes.
        cleanupWithCatch();
        return rc;
    }
    
    /**
     * Set the last seed operation timestamp.
     * 
     * @param millis_ the time in milliseconds. 
     * 
     */
    public void setLastSeedTimestamp(long millis_)
    {
        // Round back to the last second - make the milliseconds fraction a zero (.0).
        Timestamp ts = new Timestamp((millis_ / 1000) * 1000);
        String tss = ts.toString();

        // Try to update the existing record.
        String schema = _schema.toUpperCase();
        String update = "update sbrext.tool_options_view_ext set value = '" + tss + "'"
            + " where tool_name = 'FREESTYLE' and property = 'SEED.LASTUPDATE' and ua_name = '" + schema + "'";
        try
        {
            _pstmt = _conn.prepareStatement(update);
            
            _pstmt.execute();
            
            // No existing record so save a new one.
            if (_pstmt.getUpdateCount() < 1)
            {
                String insert = "insert into sbrext.tool_options_view_ext(tool_name, property, value, ua_name) values ('FREESTYLE', 'SEED.LASTUPDATE', '"
                        + tss + "', '" + schema + "')";
                _pstmt.close();
                _pstmt = _conn.prepareStatement(insert);
                _pstmt.execute();
            }
            
            _needCommit = true;
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + update
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }

        // Cleanup and commit changes.
        cleanupWithCatch();
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
     * Get the URL to access the caCORE API.
     * 
     * @return the URL.
     */
    public String getCoreUrl()
    {
        String select = "select value from sbrext.tool_options_view_ext where tool_name = 'EVS' and property = 'URL'";
        String url = null;
        try
        {
            _pstmt = _conn.prepareStatement(select);
            
            _rs = _pstmt.executeQuery();
            if (_rs.next())
                url = _rs.getString(1);
        }
        
        // We had an unexpected problem.
        catch (SQLException ex)
        {
            _errorCode = ex.getErrorCode();
            _errorMsg = _errorCode + ": " + select
                + "\n" + ex.toString();
            _logger.fatal(_errorMsg);
        }

        // Cleanup and commit changes.
        cleanupWithCatch();
        return url;
    }
    
    private boolean _excludeWFSretired;
    private int _scoreLimit;
    private boolean _needCommit;
    private int _errorCode;
    private String _errorMsg;
    private Connection _conn;
    private boolean _connFlag;
    private ResultSet _rs;
    private PreparedStatement _pstmt;
    private static GenericAC[] _desc = {
        new ACDataElement(0),
        new ACDataElementConcept(1),
        new ACValueDomain(2),
        new ACObjectClass(3),
        new ACProperty(4),
        new ACConcepts(5),
        new ACConceptualDomain(6)
    };

    private int _limit;
    
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
    
    private static final String _qualWhere =
        " where nvl(zz.date_modified, zz.date_created) >= ?";
    
    private static String _indexTable = "sbrext.gs_tokens";
    
    private static String _compositeTable = "sbrext.gs_composite";
    
    private static String _schema = "sbrext";
    
    /**
     * These are the characters used to separate any text into tokens, consequently
     * they are not saved or stored in the search index.
     */
    public static final String _tokenChars = "[ ,._?!*':;&<>(){}\"\\[\\]\\t\\r\\n]";
    
    private static final Logger _logger = Logger.getLogger(DBAccess.class.getName());
}
