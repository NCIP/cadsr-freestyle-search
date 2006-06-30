// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/Search.java,v 1.1 2006-06-30 13:46:47 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

/**
 * <p>This class encapsulates the freestyle search engine execution using
 * the freestyle index tables. See the package description for example
 * code. All access to the Search engine is through this class. Except for
 * documented method returns and arguments, no other class in this package
 * must be used.</p><p>The first call immediately following a "new Search(...)"
 * must be one of the setDataDescription(...) methods. This identifies the database and
 * access to the caDSR and the Index tables.</p>
 * 
 * @author lhebel Mar 6, 2006
 *
 */
public class Search
{
    /**
     * Constructor using programmed defaults.
     *
     */
    public Search()
    {
        init(AC_MATCH_BEST, 100, 5);
    }

    /**
     * Constructor
     * 
     * @param match_ The match comparison flag,  AC_MATCH_EXACT, AC_MATCH_PARTIAL, AC_MATCH_BEST
     * @param limit_  The maximum results limit.
     * @param scores_  The maximum number of score groups.
     *
     */
    public Search(int match_, int limit_, int scores_)
    {
        init(match_, limit_, scores_);
    }

    /**
     * Common initialization during object construction.
     * 
     * @param match_ The match comparison flag,  AC_MATCH_EXACT, AC_MATCH_PARTIAL, AC_MATCH_BEST
     * @param limit_  The maximum results limit.
     * @param scores_  The maximum number of score groups.
     */
    private void init(int match_, int limit_, int scores_)
    {
        _excludeWFSretired = false;
        _matchFlag = match_;
        _limit = limit_;
        _highestScores = scores_;
        
        _restrict = new int[AC_TYPE_COUNT];
        _restrictAll = true;
    }
    
    /**
     * Find AC's using the phrase (terms) provided, results are in ASCII text form using
     * default AC attributes.
     * 
     * @param phrase_ the terms of interest
     * @return return the default display results as ASCII text
     */
    public Vector<String> findReturningDefault(String phrase_)
    {
        Vector<ResultsAC> rs0 = find(phrase_);
        Vector<String> rs = getDefaultDisplay(rs0);
        return rs;
    }
    
    /**
     * Find AC's using the phrase (terms) provided, results are in objects containing
     * the AC type, database ID and result score.
     * 
     * @param phrase_ the terms of interest
     * @return return the collection using SearchResultSet.
     */
    public Vector<SearchResultSet> findReturningResultSet(String phrase_)
    {
        Vector<ResultsAC> rs0 = find(phrase_);
        Vector<SearchResultSet> rs = new Vector<SearchResultSet>();
        
        for (ResultsAC obj : rs0)
        {
            SearchResultSet var = new SearchResultSet(obj._idseq, obj._score, obj._desc.getMasterIndex());
            rs.add(var);
        }
        return rs;
    }
    
    /**
     * Find AC's using the phrase (terms) provided, results are in objects defined by
     * the caCORE API in the client.jar.
     * 
     * @param phrase_ the terms of interest
     * @return return the collection using the base interface AdministeredComponent.
     */
    public Vector<AdministeredComponent> findReturningAdministeredComponent(String phrase_)
    {
        Vector<ResultsAC> rs0 = find(phrase_);
        Vector<AdministeredComponent> rs = new Vector<AdministeredComponent>();

        // Get the coreapi url
        DBAccess db = new DBAccess();
        try
        {
            openData(db);
        }
        catch (SQLException ex)
        {
            _logger.fatal(ex.toString());
            return new Vector<AdministeredComponent>();
        }
        String url = db.getCoreUrl();
        db.close();
        
        ApplicationService coreapi = ApplicationService.getRemoteInstance(url);
        
        for (ResultsAC obj : rs0)
        {
            Class cvar = obj._desc.getACClass();
            AdministeredComponent var = obj._desc.factoryAC();
            var.setId(obj._idseq);
            try
            {
                List ac = coreapi.search(cvar, var);
                if (ac.size() == 0)
                    _logger.fatal("Failed to find (type, id) (" + obj._desc.getMasterIndex() + ", " + obj._idseq + ")");
                else
                    rs.add(var);
            }
            catch (ApplicationException ex)
            {
                _logger.fatal(ex.toString());
            }
        }
        return rs;
    }
    
    /**
     * Search the freestyle index table for matches.
     * 
     * @param phrase_ the terms of interest
     * 
     * @return the internal results object collection.
     */
    private Vector<ResultsAC> find(String phrase_)
    {
        _logger.debug("match: " + _matchFlag + " limit: " + _limit + " score: " + _highestScores);
        _logger.debug(phrase_);

        // Open the database.
        DBAccess dbIndex = new DBAccess();
        
        // Set data settings.
        dbIndex.setLimit(_limit);
        dbIndex.restrictResultsByScore(_highestScores);
        dbIndex.excludeWorkflowStatusRetired(_excludeWFSretired);
        
        // Pre-qualify the phrase.
        String phrase = phrase_.replaceAll(DBAccess._tokenChars, " ");
        phrase = phrase.trim();
        if (phrase != null && phrase.length() > 0)
        {
            try
            {
                openIndex(dbIndex);
            }
            catch (SQLException ex)
            {
                _logger.fatal(ex.toString());
                return new Vector<ResultsAC>();
            }
            
            // We do a case sensitive search on a lower case string
            // which has the effect of a case insensitive search without
            // the extra processing overhead.
            phrase = phrase.toLowerCase();
            Vector<ResultsAC> var = null;
            switch (_matchFlag)
            {
                case AC_MATCH_EXACT: var = doMatchExact(dbIndex, phrase); break;
                case AC_MATCH_PARTIAL: var = doMatchPartial(dbIndex, phrase); break;
                case AC_MATCH_BEST:
                default: var = doMatchBest(dbIndex, phrase); break;
            }
            dbIndex.close();
            
            return var;
        }
        return new Vector<ResultsAC>();
    }
    
    /**
     * Get the default results display.
     * 
     * @param list_ the search results collection from find()
     * @return the AC default display in ASCII format.
     */
    public Vector<String> getDefaultDisplay(Vector<ResultsAC> list_)
    {
        if (list_.size() == 0)
            return new Vector<String>();

        DBAccess dbData = new DBAccess();
        try
        {
            openData(dbData);
        }
        catch (SQLException ex)
        {
            _logger.fatal(ex.toString());
            return new Vector<String>();
        }

        // Retrieve the displayable results for the user.
        Vector<String> rs = dbData.getDisplay(list_);
        dbData.close();
        return rs;
    }
    
    /**
     * Establish connections for the index tables. This
     * provides for the possibility the index tables may not be hosted by the
     * target caDSR database.
     * 
     * @param indexDB_ the access object to the freestyle index tables
     * @throws SQLException
     */
    private void openIndex(DBAccess indexDB_) throws SQLException
    {
        if (indexDB_ != null)
        {
            if (_indexDS != null)
            {
                if (_indexUser == null)
                {
                    if (indexDB_.open(_indexDS) != 0)
                        throw new SQLException("Failed to open connection from DataSource.", "Failed", -101);
                }
                if (indexDB_.open(_indexDS, _indexUser, _indexPswd) != 0)
                    throw new SQLException("Failed to open connection from DataSource.", "Failed", -101);
            }
    
            else if (_indexConn != null)
            {
                if (indexDB_.open(_indexConn) != 0)
                    throw new SQLException("Failed to use supplied Connection.", "Failed", -102);
            }
    
            else if (indexDB_.open(_indexUrl, _indexUser, _indexPswd) != 0)
                throw new SQLException("Failed to open connection to index.", "Failed", -103);
        }
    }

    /**
     * Establish connections for the data tables in the caDSR. This
     * provides for the possibility the index tables may not be hosted by the
     * target caDSR database.
     * 
     * @param dataDB_ the access object to the caDSR target data
     * @throws SQLException
     */
    private void openData(DBAccess dataDB_) throws SQLException
    {
        if (dataDB_ != null)
        {
            if (_dataDS != null)
            {
                if (_dataUser == null)
                {
                    if (dataDB_.open(_dataDS) != 0)
                        throw new SQLException("Failed to open connection from DataSource.", "Failed", -201);
                }
                if (dataDB_.open(_dataDS, _dataUser, _dataPswd) != 0)
                    throw new SQLException("Failed to open connection from DataSource.", "Failed", -201);
            }
    
            else if (_dataConn != null)
            {
                if (dataDB_.open(_dataConn) != 0)
                    throw new SQLException("Failed to use supplied Connection.", "Failed", -202);
            }
    
            else if (dataDB_.open(_dataUrl, _dataUser, _dataPswd) != 0)
                throw new SQLException("Failed to open connection to caDSR.", "Failed", -203);
        }
    }

    /**
     * Perform an exact match on all terms specified.
     * 
     * @param dbIndex_ the index tables access object
     * @param phrase_ the terms of interest
     * @return the collection of successful matches in descending order by score
     */
    private Vector<ResultsAC> doMatchExact(DBAccess dbIndex_, String phrase_)
    {
        // Remove words we don't keep.
        String[] terms = phrase_.split(" ");
        String phrase = "";
        if (terms.length > 1)
        {
            for (int i = 0; i < terms.length; ++i)
            {
                if (terms[i].length() < 2 || dbIndex_.checkExcludes(terms[i]))
                    continue;
                phrase += " " + terms[i];
            }
            phrase = phrase.substring(1);
        }
        else
            phrase = phrase_;

        if (phrase.length() == 0)
            phrase = phrase_;

        // The SQL is optomized to filter by type when not including all possible
        // AC's.
        Vector<ResultsAC> var = null;
        if (_restrictAll)
        {
            var = dbIndex_.searchExact(phrase);
        }
        else
        {
            var = dbIndex_.searchExact(_restrict, phrase);
        }
        return var;
    }

    /**
     * Perform a partial match on all terms specified.
     * 
     * @param dbIndex_ the index tables access object
     * @param phrase_ the terms of interest
     * @return the collection of successful matches in descending order by score
     */
    private Vector<ResultsAC> doMatchPartial(DBAccess dbIndex_, String phrase_)
    {
        // The SQL is optomized to filter by type when not including all possible
        // AC's. Excluded words are not removed as they may appear as part of
        // a non-excluded word, e.g. "man" is part of "chairman"
        Vector<ResultsAC> var = null;
        if (_restrictAll)
        {
            var = dbIndex_.searchPartial(phrase_);
        }
        else
        {
            var = dbIndex_.searchPartial(_restrict, phrase_);
        }
        return var;
    }

    /**
     * Perform a best match on all terms specified. First an exact match is performed and
     * if no results, a partial is automatically performed.
     * 
     * @param dbIndex_ the index tables access object
     * @param phrase_ the terms of interest
     * @return the collection of successful matches in descending order by score
     */
    private Vector<ResultsAC> doMatchBest(DBAccess dbIndex_, String phrase_)
    {
        // Remove words we don't keep.
        String[] terms = phrase_.split(" ");
        String phrase = "";
        if (terms.length > 1)
        {
            for (int i = 0; i < terms.length; ++i)
            {
                if (terms[i].length() < 2 || dbIndex_.checkExcludes(terms[i]))
                    continue;
                phrase += " " + terms[i];
            }
            phrase = phrase.substring(1);
        }
        else
            phrase = phrase_;

        if (phrase.length() == 0)
            phrase = phrase_;

        Vector<ResultsAC> var = null;
        if (_restrictAll)
        {
            var = dbIndex_.searchExact(phrase);
            if (var.size() == 0)
                var = dbIndex_.searchPartial(phrase_);
        }
        else
        {
            var = dbIndex_.searchExact(_restrict, phrase);
            if (var.size() == 0)
                var = dbIndex_.searchPartial(_restrict, phrase_);
        }
        return var;
    }
    
    /**
     * Set the match flag to control how comparisons are performed
     * during a search.
     * 
     * @param match_ AC_MATCH_EXACT, all words/tokens are compared exactly,
     *      no partial comparisons are done; AC_MATCH_PARTIAL, all words/tokens are
     *      compared as partial strings, no exact comparisons are done; AC_MATCH_BEST,
     *      all words/comparisons are compared exactly, if and only if no results are found
     *       a partial comparison search is performed.
     */
    public void setMatchFlag(int match_)
    {
        _matchFlag = match_;
    }
    
    /**
     * Get the search limit. This is the maxium returns from a find...().
     *  
     * @return the search limit
     */
    public int getLimit()
    {
        return _limit;
    }
    
    /**
     * Set the limit for maximum results returned.
     * 
     * @param limit_
     */
    public void setResultsLimit(int limit_)
    {
        _limit = limit_;
    }
    
    /**
     * Restrict the results by the type(s) of record. The Search.AC_TYPE_* constants must be used as
     * arguments to this method, e.g. AC_TYPE_DE, AC_TYPE_DEC, etc.
     * 
     * @param args The record type(s) to filter the search results.
     */
    public void restrictResultsByType(int ... args)
    {
        for (int i = 0; i < args.length; ++i)
        {
            _restrict[args[i]] = 1;
        }

        int total = 0;
        for (int i = 0; i < _restrict.length; ++i)
        {
            total += _restrict[i];
        }
        
        _restrictAll = (total == AC_TYPE_COUNT);
    }
    
    /**
     * Reset the restrict list to include All types.
     *
     */
    public void resetRestrictResultsByType()
    {
        _restrict = new int[AC_TYPE_COUNT];
        _restrictAll = true;
    }
    
    /**
     * Restrict the results by the search score groups. A score group is one or more results with the same
     * score.
     * 
     * @param score_ A zero (0) indicates no restrictions, a one (1) indicates results with the highest
     *      score are returned, a two (2) indicates results with the 2 highest scores are returned,
     *      and so forth.
     */
    public void restrictResultsByScore(int score_)
    {
        _highestScores = score_;
    }

    /**
     * Reset the restriction by score to return all results. NOT recommended unless something about the
     * search results and terms is known to produce a manageable result set.
     *
     */
    public void resetRestrictResultsByScore()
    {
        _highestScores = 0;
    }
    
    /**
     * Restrict the results to those with Workflow Status not "retired".
     * 
     */
    public void restrictResultsByWorkflowNotRetired()
    {
        _excludeWFSretired = true;
    }
    
    /**
     * Clear the Workflow Status restriction. All are returned in the results.
     * 
     */
    public void resetResultsByWorkflowNotRetired()
    {
        _excludeWFSretired = false;
    }
    
    /**
     * Get the AC type names.
     * 
     * @return the list of supported types.
     */
    public static String[] getTypes()
    {
        return DBAccess.getTypes();
    }
    
    /**
     * Get the names of the columns which may be searched for all the types. This is
     * a single cumulative list, not segregated by AC type.
     * 
     * @return the combined list of columns included in the searches.
     */
    public static String[] getColNames()
    {
        return GenericAC.getColNames();
    }
    
    /**
     * Reserved.
     * 
     * @param text_ reserved
     */
    public static void setSchema(String text_)
    {
        if (text_ != null && text_.length() > 0)
            DBAccess.setSchema(text_);
    }
    
    /**
     * Set the database description which hosts the freestyle search index tables. Use
     * setDataDescription(...) instead.
     * 
     * @param url_ the database URL
     * @param user_ the access account
     * @param pswd_ the access account password
     */
    public void setIndexDescription(String url_, String user_, String pswd_)
    {
        _indexUrl = url_;
        if (_dataUrl == null)
            _dataUrl = url_;
        _indexUser = user_;
        if (_dataUser == null)
            _dataUser = user_;
        _indexPswd = pswd_;
        if (_dataPswd == null)
            _dataPswd = pswd_;
    }
    
    /**
     * Set the database description which hosts the freestyle search index tables. Use
     * setDataDescription(...) instead.
     * 
     * @param ds_ the datasource pool
     * @param user_ the access account
     * @param pswd_ the access account password
     */
    public void setIndexDescription(DataSource ds_, String user_, String pswd_)
    {
        if (user_ == null)
        {
            setIndexDescription(ds_);
            return;
        }

        _indexDS = ds_;
        if (_dataDS == null)
            _dataDS = ds_;
        _indexUser = user_;
        if (_dataUser == null)
            _dataUser = user_;
        _indexPswd = pswd_;
        if (_dataPswd == null)
            _dataPswd = pswd_;
    }
    
    /**
     * Set the database description which hosts the freestyle search index tables. Use
     * setDataDescription(...) instead.
     * 
     * @param ds_ the datasource pool
     */
    public void setIndexDescription(DataSource ds_)
    {
        _indexDS = ds_;
        if (_dataDS == null)
            _dataDS = ds_;
        _indexUser = null;
        if (_dataUser == null)
            _dataUser = _indexUser;
        _indexPswd = null;
        if (_dataPswd == null)
            _dataPswd = _indexPswd;
    }
    
    /**
     * Set the database description which hosts the freestyle search index tables. Use
     * setDataDescription(...) instead.
     * 
     * @param conn_ an established database connection
     */
    public void setIndexDescription(Connection conn_)
    {
        _indexConn = conn_;
        if (_dataConn == null)
            _dataConn = conn_;
    }
    
    /**
     * Set the database description which hosts the freestyle index tables and caDSR.
     * 
     * @param url_ the database URL
     * @param user_ the access account
     * @param pswd_ the access account password
     */
    public void setDataDescription(String url_, String user_, String pswd_)
    {
        _dataUrl = url_;
        if (_indexUrl == null)
            _indexUrl = url_;
        _dataUser = user_;
        if (_indexUser == null)
            _indexUser = user_;
        _dataPswd = pswd_;
        if (_indexPswd == null)
            _indexPswd = pswd_;
    }
    
    /**
     * Set the database description which hosts the freestyle index tables and caDSR.
     * 
     * @param ds_ the datasource pool
     * @param user_ the access account
     * @param pswd_ the access account password
     */
    public void setDataDescription(DataSource ds_, String user_, String pswd_)
    {
        if (user_ == null)
        {
            setDataDescription(ds_);
            return;
        }

        _dataDS = ds_;
        if (_indexDS == null)
            _indexDS = ds_;
        _dataUser = user_;
        if (_indexUser == null)
            _indexUser = user_;
        _dataPswd = pswd_;
        if (_indexPswd == null)
            _indexPswd = pswd_;
    }
    
    /**
     * Set the database description which hosts the freestyle index tables and caDSR. The
     * datasource must already have a valid user and password defined.
     * 
     * @param ds_ the datasource pool
     */
    public void setDataDescription(DataSource ds_)
    {
        _dataDS = ds_;
        if (_indexDS == null)
            _indexDS = ds_;
        _dataUser = null;
        if (_indexUser == null)
            _indexUser = _dataUser;
        _dataPswd = null;
        if (_indexPswd == null)
            _indexPswd = _dataPswd;
    }
    
    /**
     * Set the database description which hosts the freestyle index tables and caDSR.
     * 
     * @param conn_ an established database connection
     */
    public void setDataDescription(Connection conn_)
    {
        _dataConn = conn_;
        if (_indexConn == null)
            _indexConn = conn_;
    }
    
    /**
     * Get the last seed timestamp as a string. This is not a compatible format for any
     * parsing method.
     * 
     * @return the timestamp as a string
     */
    public String getLastSeedTimestampString()
    {
        DBAccess db = new DBAccess();
        String seedTime = null;
        try
        {
            openIndex(db);
            seedTime = db.getLastSeedTimestamp().toString() + " (yyyy-mm-dd hh:mm:ss Eastern Time)";
            db.close();
        }
        catch (SQLException ex)
        {
            _logger.fatal(ex.toString());
        }
        return seedTime;
    }
    
    /**
     * Get the last seed timestamp.
     * 
     * @return the timestamp
     */
    public Timestamp getLastSeedTimestamp()
    {
        DBAccess db = new DBAccess();
        Timestamp seedTime = null;
        try
        {
            openIndex(db);
            seedTime = db.getLastSeedTimestamp();
            db.close();
        }
        catch (SQLException ex)
        {
            _logger.fatal(ex.toString());
        }
        return seedTime;
    }
    
    /**
     * The AC type constant for a Data Element
     */
    public static final int AC_TYPE_DE = 0;
    
    /**
     * The AC type constant for a Data Element Concept
     */
    public static final int AC_TYPE_DEC = 1;
    
    /**
     * The AC type constant for a Value Domain
     */
    public static final int AC_TYPE_VD = 2;
    
    /**
     * The AC type constant for a Object Class
     */
    public static final int AC_TYPE_OC = 3;    

    /**
     * The AC type constant for a Property
     */
    public static final int AC_TYPE_PROP = 4;

    /**
     * The AC type constant for a Property
     */
    public static final int AC_TYPE_CON = 5;

    /**
     * The AC type constant for a Property
     */
    public static final int AC_TYPE_CD = 6;
    
    /**
     * The supported AC type count
     */
    public static final int AC_TYPE_COUNT = 7;
    
    /**
     * Indicates all words/tokens are compared exactly, no partial
     * comparisons are done, e.g. "Lateral" will not match "Colateral".
     */
    public static final int AC_MATCH_EXACT = 0;
    
    /**
     * Indicates all words/tokens are compared partially, no exact
     * comparisons are done, e.g. "Lateral" will always match "Colateral".
     */
    public static final int AC_MATCH_PARTIAL = 1;
    
    /**
     * Indicates all words/tokens are compared exactly, then if no results
     * are found a partial search is performed, e.g. "Lateral" will match "Lateral"
     * and if found the search stops, if not found "Lateral" will match "Colateral".
     */
    public static final int AC_MATCH_BEST = 2;

    private boolean _excludeWFSretired;
    private String _indexUrl;
    private String _indexUser;
    private String _indexPswd;
    private int _limit;
    private String _dataUrl;
    private String _dataUser;
    private String _dataPswd;
    private int[] _restrict;
    private boolean _restrictAll;
    private int _highestScores;
    private int _matchFlag;
    private DataSource _indexDS;
    private Connection _indexConn;
    private DataSource _dataDS;
    private Connection _dataConn;

    private static final Logger _logger = Logger.getLogger(Search.class.getName());
}
