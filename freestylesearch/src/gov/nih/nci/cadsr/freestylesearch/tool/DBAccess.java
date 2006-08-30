// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/DBAccess.java,v 1.2 2006-08-30 20:31:23 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

import gov.nih.nci.cadsr.freestylesearch.util.SearchAC;
import gov.nih.nci.cadsr.freestylesearch.util.SearchResults;
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
 * Provide access to the caDSR tables.
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
        _connFlag = true;
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
    private ResultSet readTable(GenericAC ac_, ACAlternate alt_, Timestamp start_)
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
            + ", conte.name, rs.registration_status, nvl(rv.display_order, 1000) as reg_order, nvl(wfs.display_order, 1000) as wfs_order from ("
            + buildIDselect(ac_, alt_, start_) + ") xx, " + ac_.getTableName()
            +" zz, sbr.contexts_view conte, sbr.ac_registrations_view rs, sbr.reg_status_lov_view rv, sbr.ac_status_lov_view wfs "
            +  "where zz." + ac_.getIdseqName()
            + " = xx.idseq and conte.conte_idseq = zz.conte_idseq and wfs.asl_name = zz.asl_name and rs.ac_idseq(+) = xx.idseq and rv.registration_status(+) = rs.registration_status";

        // Get the result set.
        try
        {
            _pstmt = _conn.prepareStatement(select);
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
     * @param alt_ the record/table description
     * @param start_ the timestamp from which to select records
     * @return the result set from the select.
     */
    private ResultSet readAlternateTable(GenericAC ac_, ACAlternate alt_, Timestamp start_)
    {
        // Build the select column list.
        String[] cols = alt_.getColumns();
        String select = "";
        for (int i = 0; i < cols.length; ++i)
        {
            if (cols[i].charAt(0) == '\'')
                select = select + ", " + cols[i];
            else
                select = select + ", alt." + cols[i];
        }
        
        // Add the From clause
        select = "select " + select.substring(2) + ", conte.name from (" + buildIDselect(ac_, alt_, start_) + ") xx, "
            + alt_.getTableName() + " alt, sbr.contexts_view conte where alt."
            + alt_.getIdseqName() + " = xx.idseq and conte.conte_idseq = alt.conte_idseq";

        // Get the result set.
        try
        {
            _pstmt = _conn.prepareStatement(select);
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
            data += "union all select " + i + " as ac_order, '" + obj._idseq + "' as ac_idseq, " + obj._desc.getMasterIndex() + " as ac_table, " + obj._score + " as score from dual ";
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
            + "order by hits.ac_order asc";
        
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
     * Get the generic AC search results data.
     * 
     * @param list_ results from a Search.find().
     * @return The generic AC data
     */
    public Vector<SearchResults> getSearchResults(Vector<ResultsAC> list_)
    {
        Vector<SearchResults> results = new Vector<SearchResults>();
        if (list_ == null || list_.size() == 0)
            return results;
        
        ResultsAC obj;
        String data = "";
        String uall = "union all ";
        for (int i = 0; i < list_.size(); ++i)
        {
            obj = list_.get(i);
            data += "union all select " + i + " as ac_order, '" + obj._idseq + "' as ac_idseq, " + obj._desc.getMasterIndex() + " as ac_table, " + obj._score + " as score from dual ";
        }
        data = data.substring(uall.length());

        String select = "";
        select = "select hits.ac_table, ac.long_name, ac.preferred_name, ac.public_id, ac.version, ac.preferred_definition, c.name, nvl(rs.registration_status, ' ') "
            + "from (" + data + ") hits, sbr.admin_components_view ac, sbr.contexts_view c, sbr.ac_registrations_view rs "
            + "where ac.ac_idseq = hits.ac_idseq and c.conte_idseq = ac.conte_idseq and rs.ac_idseq(+) = ac.ac_idseq "
            + "order by hits.ac_order asc";
        
        try
        {
            // Set the database id for each sub-select.
            _pstmt = _conn.prepareStatement(select);
            
            // Get the display and save for later.
            _rs = _pstmt.executeQuery();
            while (_rs.next())
            {
                SearchResults display = new SearchResults(
                                SearchAC.valueOf(_rs.getInt(1)),
                                _rs.getString(2),
                                _rs.getString(3),
                                _rs.getInt(4),
                                _rs.getString(5),
                                _rs.getString(6),
                                _rs.getString(7),
                                _rs.getString(8).trim());
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
     * Parse the database and load the search index table.
     *
     * @param start_ the start date for records of interest.
     * @param indexTable_ The database object which holds the index table.
     */
    public void parseDatabase(Timestamp start_, DBAccessIndex indexTable_)
    {
        // Loop through table descriptions and read the desired records
        // from the database.
        for (int i = 0; i < _desc.length; ++i)
        {
            GenericAC ac = _desc[i];
            ACAlternate alt = new ACAlternate(ac);

            int total = updateCount(ac, alt, start_, true);
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
                String[] ids = eraseList(ac, alt, start_);
                indexTable_.erase(ids);
            }

            // Read the table.
            ResultSet rs = readTable(ac, alt, start_);
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
            total = updateCount(ac, alt, start_, false);
            _logger.info(" ");
            _logger.info("Doing... Alternates for " + alt.getTypeName() + " " + total + " possible records");
            rs = readAlternateTable(ac, alt, start_);
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

    private String buildIDselect(GenericAC ac_, ACAlternate alt_, Timestamp start_)
    {
        String time = start_.toString();
        time = time.split("[.]")[0];
        String select =
            "select pp." + ac_.getIdseqName() + " as idseq from " + ac_.getTableName() + " pp "
            + "where nvl(pp.date_modified, pp.date_created) >= to_date('" + time + "', 'yyyy-mm-dd hh24:mi:ss')"
            +"union select alt." + alt_.getIdseqName() + " as idseq from " + alt_.getTableName() + " alt, " + ac_.getTableName() + " pp "
            + "where nvl(alt.date_modified, alt.date_created) >= to_date('" + time + "', 'yyyy-mm-dd hh24:mi:ss') "
            + "and pp." + ac_.getIdseqName() + " = alt.ac_idseq";
        
        return select;
    }

    /**
     * Build the list of database ID's which have been modified on or after
     * the date specified.
     * 
     * @param ac_ the AC of interest
     * @param start_ the comparison date
     * @return the list of ID's to be updated in the freestyle index tables
     */
    private String[] eraseList(GenericAC ac_, ACAlternate alt_, Timestamp start_)
    {
        String[] ids = new String[0];
        
        // Build the delete using a sub-select to match that used to query
        // the database for records to load into the search index table.
        String select = buildIDselect(ac_, alt_, start_);

        try
        {
            _pstmt = _conn.prepareStatement(select);
            
            ResultSet rs = _pstmt.executeQuery();

            Vector<String> list = new Vector<String>();
            while (rs.next())
            {
                list.add(rs.getString(1));
            }

            ids = new String[list.size()];
            for (int cnt = 0; cnt < ids.length; ++cnt)
                ids[cnt] = list.get(cnt);
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
        
        return ids;
    }

    /**
     * Get the update count for the table changes.
     * 
     * @param ac_ the record/table type of interest
     * @param start_ the start date for records of interest
     * @return return the record count for the number of changes
     */
    private int updateCount(GenericAC ac_, ACAlternate alt_, Timestamp start_, boolean prime_)
    {
        // Build the delete using a sub-select to match that used to query
        // the database for records to load into the search index table.
        String select = "select count(*) from (" + buildIDselect(ac_, alt_, start_) + ")";
        
        if (!prime_)
            select += " xx, " + alt_.getTableName() + " alt where alt." + alt_.getIdseqName() + " = xx.idseq";

        int count = 0;
        try
        {
            _pstmt = _conn.prepareStatement(select);
            
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
    private void parseRecord(GenericAC ac_, ResultSet rs_, DBAccessIndex dbWrite_) throws SQLException
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
        int wfs_order = 0;
        int reg_order = 0;
        for (int i = 0; i < cols.length; ++i)
        {
            String temp = rs_.getString(i + 1);
            if (temp == null)
                continue;
            
            // Split the column value into multiple tokens.
            String[] tokens = DBAccessIndex.split(temp);
            switch (ReservedColumns.valueOf(i))
            {
                // This is the database id column so just remember it for all future
                // output.
                case IDSEQ:
                    idseq = temp;
                    break;
                    
                // This is the 'version' column so remember the original value
                // and append a '.0' when necessary.
                case VERSION:
                    dbWrite_.insertTerm(type, idseq, cols[i], temp);
                    if (tokens.length == 1)
                    {
                        temp = temp + ".0";
                        dbWrite_.insertTerm(type, idseq, cols[i], temp);
                    }
                    break;
                    
                // This is the 'type' column so remember the returned value
                // and expand to the full description of the type.
                case TYPE:
                    dbWrite_.insertTerm(type, idseq, cols[i], temp);
                    tokens = ac_.getTypeName().split(" ");

                    // Fall through and let the normal processing handle the expanded type name.

                default:
                    if (cols[i].equals("reg_order"))
                    {
                        reg_order = Integer.valueOf(temp);
                        break;
                    }
                
                    if (cols[i].equals("wfs_order"))
                    {
                        wfs_order = Integer.valueOf(temp);
                        break;
                    }

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
            dbWrite_.insertComposite(type, idseq, composite + " ", reg_order, wfs_order);

        // Cleanup and stuff.
        dbWrite_.commit();
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
        String schema = DBAccessIndex._schema.toUpperCase();
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
        String schema = DBAccessIndex._schema.toUpperCase();
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

    /**
     * Common AC description table. Entries MUST appear in the numerical order of the SearchAC ENUM.
     */
    public static GenericAC[] _desc = {
        new ACDataElement(SearchAC.DE.toInt()),
        new ACDataElementConcept(SearchAC.DEC.toInt()),
        new ACValueDomain(SearchAC.VD.toInt()),
        new ACObjectClass(SearchAC.OC.toInt()),
        new ACProperty(SearchAC.PROP.toInt()),
        new ACConcepts(SearchAC.CON.toInt()),
        new ACConceptualDomain(SearchAC.CD.toInt())
    };
    
    private boolean _needCommit;
    private int _errorCode;
    private String _errorMsg;
    private Connection _conn;
    private boolean _connFlag;
    private ResultSet _rs;
    private PreparedStatement _pstmt;
    
    private static final Logger _logger = Logger.getLogger(DBAccess.class.getName());
}
