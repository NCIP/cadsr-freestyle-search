// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/Search.java,v 1.15 2007-02-13 19:35:17 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.cadsr.freestylesearch.tool.DBAccess;
import gov.nih.nci.cadsr.freestylesearch.tool.DBAccessIndex;
import gov.nih.nci.cadsr.freestylesearch.tool.GenericAC;
import gov.nih.nci.cadsr.freestylesearch.tool.ResultsAC;
import gov.nih.nci.cadsr.freestylesearch.tool.SearchRequest;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
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
     * 
     */
    public static final String _vers ="3.2.1.20070213";
    
    /**
     * Return the version marker for the freestylesearch.jar
     * 
     * @return the version marker
     */
    public static String getVersion()
    {
        return _vers;
    }

    /**
     * Constructor using programmed defaults.
     *
     */
    public Search()
    {
        init(SearchMatch.BEST, 100, 5);
    }

    /**
     * Constructor
     * 
     * @param match_ The match comparison flag in the SearchMatch enum.
     * @param limit_  The maximum results limit.
     * @param scores_  The maximum number of score groups.
     *
     */
    public Search(SearchMatch match_, int limit_, int scores_)
    {
        init(match_, limit_, scores_);
    }

    /**
     * Common initialization during object construction.
     * 
     * @param match_ The match comparison flag in the SearchMatch enum.
     * @param limit_  The maximum results limit.
     * @param scores_  The maximum number of score groups.
     */
    private void init(SearchMatch match_, int limit_, int scores_)
    {
        _excludeWFSretired = false;
        _matchFlag = match_;
        _limit = limit_;
        _highestScores = scores_;

        _restrict = new int[SearchAC.count()];
        _restrictAll = true;
    }

    /**
     * Find AC's using the phrase (terms) provided, results are in ASCII text form using
     * default AC attributes.
     * 
     * @param phrase_ the terms of interest
     * @return return the default display results as ASCII text
     * @exception SearchException
     */
    public Vector<String> findReturningDefault(String phrase_) throws SearchException
    {
        Vector<String> rs = new Vector<String>();

        if (_serverURL == null)
        {
            Vector<ResultsAC> rs0 = find(phrase_);
            rs = getDefaultDisplay(rs0);
        }
        else
        {
            SearchRequest sr = new SearchRequest(_serverURL);
            rs = sr.findReturningDefault(this, phrase_);
        }

        return rs;
    }

    /**
     * Create the public result set from the internal result set.
     * 
     * @param rs_ the internal result set
     * @return the public result set
     */
    private Vector<SearchResultObject> findReturningResultSet(Vector<ResultsAC> rs_)
    {
        Vector<SearchResultObject> rs = new Vector<SearchResultObject>();

        for (ResultsAC obj : rs_)
        {
            SearchResultObject var = new SearchResultObject(obj._idseq, obj._score, SearchAC.valueOf(obj._desc.getMasterIndex()));
            rs.add(var);
        }
        return rs;
    }
    
    /**
     * Find AC's using the phrase (terms) provided, results are in objects containing
     * the AC type, database ID and result score.
     * 
     * @param phrase_ the terms of interest
     * @return return the collection using SearchResultObject.
     * @exception SearchException
     */
    public Vector<SearchResultObject> findReturningResultSet(String phrase_) throws SearchException
    {
        Vector<SearchResultObject> rs = new Vector<SearchResultObject>();
        
        if (_serverURL == null)
        {
            Vector<ResultsAC> rs0 = find(phrase_);
            rs = findReturningResultSet(rs0);
        }
        else
        {
            SearchRequest sr = new SearchRequest(_serverURL);
            rs = sr.findReturningResultSet(this, phrase_);
        }
        
        return rs;
    }
    
    /**
     * Find AC's using the phrase (terms) provided, results are in objects containing
     * the AC name, definition, public id, etc.
     * 
     * @param phrase_ the terms of interest
     * @return return the collection using SearchResults
     * @exception SearchException
     */
    public Vector<SearchResults> findReturningSearchResults(String phrase_) throws SearchException
    {
        Vector<SearchResults> rs = new Vector<SearchResults>();
        
        if (_serverURL == null)
        {
            Vector<ResultsAC> rs0 = find(phrase_);
            rs = findReturningSearchResults(rs0);
        }
        else
        {
            SearchRequest sr = new SearchRequest(_serverURL);
            rs = sr.findReturningSearchResults(this, phrase_);
        }
        
        return rs;
    }

    /**
     * Get the ApplicationService for the caCORE API
     * 
     * @return the ApplicationService
     * @exception SearchException
     */
    private ApplicationService getCoreUrl() throws SearchException
    {
        if (_coreApiUrl != null)
            return ApplicationService.getRemoteInstance(_coreApiUrl);

        String url = getDsrCoreUrl();
        
        return ApplicationService.getRemoteInstance(url);
    }

    /**
     * Get the caCORE url registered in the caDSR. Can not be used when the setDataDescription(URL) has been used.
     * 
     * @return the caCORE url
     * @exception SearchException
     */
    public String getDsrCoreUrl() throws SearchException
    {
        String url = null;

        if (_serverURL == null)
        {
            // Get the coreapi url
            DBAccess db = new DBAccess();
            open(db);
            url = db.getCoreUrl();
            db.close();
        }
        
        return url;
    }

    /**
     * Find AC's using the phrase (terms) provided, results are in objects containing
     * the SearchResultObject and AdministeredComponent.
     * 
     * @param phrase_ the terms of interest
     * @return return the collection using SearchResultsWithAC.
     * @exception SearchException
     */
    public Vector<SearchResultsWithAC> findReturningResultsWithAC(String phrase_) throws SearchException
    {
        Vector<SearchResultsWithAC> rs = new Vector<SearchResultsWithAC>();
        Vector<SearchResultObject> rs1 = null;
        Vector<AdministeredComponent> rs2 = null;
        
        if (_serverURL == null)
        {
            Vector<ResultsAC> rs0 = find(phrase_);
            
            rs1 = findReturningResultSet(rs0);
            rs2 = findReturningAdministeredComponent(rs0);
            
            if (rs0.size() != rs1.size() || rs0.size() != rs2.size())
            {
                throw new SearchException("Error retrieving results for method findReturningResultsWithAC (direct connection) [" + rs0.size() + ", " + rs1.size() + ", " + rs2.size() + "]");
            }
            
            rs0 = null;
        }
        else
        {
            SearchRequest sr = new SearchRequest(_serverURL);
            rs1 = sr.findReturningResultSet(this, phrase_);
            setCoreApiUrl(sr.getCaCoreUrl());

            String[] idseq = new String[rs1.size()];
            for (int i = 0; i < idseq.length; ++i)
            {
                idseq[i] = rs1.get(i).getIdseq();
            }
            rs2 = findReturningAdministeredComponent(idseq);

            if (rs1.size() != rs2.size())
            {
                throw new SearchException("Error retrieving results for method findReturningResultsWithAC (URL " + _serverURL + ") [" + rs1.size() + ", " + rs2.size() + "]");
            }
            
            idseq = null;
        }

        for (int i = 0; i < rs1.size(); ++i)
        {
            SearchResultsWithAC var = new SearchResultsWithAC(rs1.get(i), rs2.get(2));
            rs.add(var);
        }
        
        rs1 = null;
        rs2 = null;
        
        return rs;
    }
    
    private Vector<AdministeredComponent> findReturningAdministeredComponent2(Vector<String> idseq_) throws SearchException
    {
        String[] idseq = new String[idseq_.size()];
        for (int i = 0; i < idseq.length; ++i)
            idseq[i] = idseq_.get(i);
        
        return findReturningAdministeredComponent(idseq);
    }
    
    private Vector<AdministeredComponent> findReturningAdministeredComponent(Vector<ResultsAC> rs_) throws SearchException
    {
        String[] idseq = new String[rs_.size()];
        for (int i = 0; i < idseq.length; ++i)
            idseq[i] = rs_.get(i)._idseq;
        
        return findReturningAdministeredComponent(idseq);
    }

    /**
     * Create the public result set from the internal result set.
     * 
     * @param idseq_ the internal result set
     * @return the public result set
     */
    private Vector<AdministeredComponent> findReturningAdministeredComponent(String[] idseq_) throws SearchException
    {
        ApplicationService coreapi = getCoreUrl();

        HashMap<String, Integer> rsMap = new HashMap<String, Integer>();
        List<AdministeredComponent> acID = new ArrayList<AdministeredComponent>();

        for (int cnt = 0; cnt < idseq_.length; ++cnt)
        {
            AdministeredComponent var = new AdministeredComponent();
            var.setId(idseq_[cnt]);
            acID.add(var);
            rsMap.put(idseq_[cnt], Integer.valueOf(cnt));
        }
        
        Vector<AdministeredComponent> rs = new Vector<AdministeredComponent>();

        try
        {
            @SuppressWarnings("unchecked")
            List<AdministeredComponent> acs = coreapi.search(AdministeredComponent.class, acID); 
            if (acs.size() != idseq_.length)
                throw new SearchException("Invalid results from caCORE API.");

            rs.setSize(acs.size());
            for (int i = 0; i < acs.size(); ++i)
            {
                AdministeredComponent record = acs.get(i);
                Integer x = rsMap.get(record.getId());
                rs.set(x.intValue(), record);
            }
        }
        catch (ApplicationException ex)
        {
            _logger.error(ex.toString(), ex);
            throw new SearchException(ex.toString());
        }

        rsMap = null;

        return rs;
    }
    
    /**
     * Find AC's using the phrase (terms) provided, results are in objects defined by
     * the caCORE API in the client.jar.
     * 
     * @param phrase_ the terms of interest
     * @return return the collection using the base interface AdministeredComponent.
     * @exception SearchException
     */
    public Vector<AdministeredComponent> findReturningAdministeredComponent(String phrase_) throws SearchException
    {
        Vector<AdministeredComponent> rs = new Vector<AdministeredComponent>();

        if (_serverURL == null)
        {
            Vector<ResultsAC> rs0 = find(phrase_);
            rs = findReturningAdministeredComponent(rs0);
        }
        else
        {
            SearchRequest sr = new SearchRequest(_serverURL);
            Vector<String> idseq = sr.findReturningIdseq(this, phrase_);
            setCoreApiUrl(sr.getCaCoreUrl());
            rs = findReturningAdministeredComponent2(idseq);
        }

        return rs;
    }

    /**
     * Search the freestyle index table for matches.
     * 
     * @param phrase_ the terms of interest
     * 
     * @return the internal results object collection.
     * @exception SearchException
     */
    private Vector<ResultsAC> find(String phrase_) throws SearchException
    {
        _logger.debug("match: " + _matchFlag + " limit: " + _limit + " score: " + _highestScores);
        _logger.debug(phrase_);

        // Open the database.
        DBAccessIndex dbIndex = new DBAccessIndex();

        // Set data settings.
        dbIndex.setLimit(_limit);
        dbIndex.restrictResultsByScore(_highestScores);
        dbIndex.excludeWorkflowStatusRetired(_excludeWFSretired);
        if (_excludeTest == false && _excludeTraining == false)
            dbIndex.setExcludeContextNames();
        else
        {
            if (_excludeTest)
                dbIndex.setExcludeContextNames("Test");
            if (_excludeTraining)
                dbIndex.setExcludeContextNames("Training");
        }

        // Pre-qualify the phrase.
        String phrase = DBAccessIndex.replaceAll(phrase_);
        phrase = phrase.trim();
        if (phrase != null && phrase.length() > 0)
        {
            open(dbIndex);

            // We do a case sensitive search on a lower case string
            // which has the effect of a case insensitive search without
            // the extra processing overhead.
            phrase = phrase.toLowerCase();
            Vector<ResultsAC> var = null;
            switch (_matchFlag)
            {
                case EXACT: var = doMatchExact(dbIndex, phrase); break;
                case PARTIAL: var = doMatchPartial(dbIndex, phrase); break;
                case BEST:
                default: var = doMatchBest(dbIndex, phrase); break;
            }
            dbIndex.close();

            return var;
        }
        return new Vector<ResultsAC>();
    }

    /**
     * Get the search results in usable form
     * 
     * @param list_ the search results collection from find()
     * @return the AC search results
     * @exception SearchException
     */
    private Vector<SearchResults> findReturningSearchResults(Vector<ResultsAC> list_) throws SearchException
    {
        if (list_.size() == 0)
            return new Vector<SearchResults>();

        DBAccess dbData = new DBAccess();
        open(dbData);

        // Retrieve the results
        Vector<SearchResults> rs = dbData.getSearchResults(list_);
        dbData.close();
        return rs;
    }

    /**
     * Get the default results display.
     * 
     * @param list_ the search results collection from find()
     * @return the AC default display in ASCII format.
     * @exception SearchException
     */
    private Vector<String> getDefaultDisplay(Vector<ResultsAC> list_) throws SearchException
    {
        if (list_.size() == 0)
            return new Vector<String>();

        DBAccess dbData = new DBAccess();
        open(dbData);

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
     * @throws SearchException
     */
    private void open(DBAccessIndex indexDB_) throws SearchException
    {
        if (indexDB_ != null)
        {
            if (_indexDS != null)
            {
                if (_indexUser == null)
                {
                    indexDB_.open(_indexDS);
                }
                else
                {
                    indexDB_.open(_indexDS, _indexUser, _indexPswd);
                }
            }

            else if (_indexConn != null)
            {
                indexDB_.open(_indexConn);
            }

            else
            {
                indexDB_.open(_indexUrl, _indexUser, _indexPswd);
            }
        }
    }

    /**
     * Establish connections for the data tables in the caDSR. This
     * provides for the possibility the index tables may not be hosted by the
     * target caDSR database.
     * 
     * @param dataDB_ the access object to the caDSR target data
     * @throws SearchException
     */
    private void open(DBAccess dataDB_) throws SearchException
    {
        if (dataDB_ != null)
        {
            if (_dataDS != null)
            {
                if (_dataUser == null)
                {
                    dataDB_.open(_dataDS);
                }
                else
                {
                    dataDB_.open(_dataDS, _dataUser, _dataPswd);
                }
            }

            else if (_dataConn != null)
            {
                dataDB_.open(_dataConn);
            }

            else
            {
                dataDB_.open(_dataUrl, _dataUser, _dataPswd);
            }
        }
    }

    /**
     * Perform an exact match on all terms specified.
     * 
     * @param dbIndex_ the index tables access object
     * @param phrase_ the terms of interest
     * @return the collection of successful matches in descending order by score
     * @exception SearchException
     */
    private Vector<ResultsAC> doMatchExact(DBAccessIndex dbIndex_, String phrase_) throws SearchException
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
            var = dbIndex_.searchExact(null, phrase);
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
     * @exception SearchException
     */
    private Vector<ResultsAC> doMatchPartial(DBAccessIndex dbIndex_, String phrase_) throws SearchException
    {
        // The SQL is optomized to filter by type when not including all possible
        // AC's. Excluded words are not removed as they may appear as part of
        // a non-excluded word, e.g. "man" is part of "chairman"
        Vector<ResultsAC> var = null;
        if (_restrictAll)
        {
            var = dbIndex_.searchPartial(null, phrase_);
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
     * @exception SearchException
     */
    private Vector<ResultsAC> doMatchBest(DBAccessIndex dbIndex_, String phrase_) throws SearchException
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
            var = dbIndex_.searchExact(null, phrase);
            if (var.size() == 0)
                var = dbIndex_.searchPartial(null, phrase_);
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
     * @param match_ see the SearchMatch enum for values
     */
    public void setMatchFlag(SearchMatch match_)
    {
        _matchFlag = match_;
    }

    /**
     * Get the search limit. This is the maxium returns from a find...().
     *  
     * @return the search limit
     */
    public int getResultsLimit()
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
     * Restrict the results by the type(s) of record. See the SearchAC enum for values.
     * 
     * @param args The record type(s) to filter the search results.
     */
    public void restrictResultsByType(SearchAC ... args)
    {
        for (int i = 0; i < args.length; ++i)
        {
            _restrict[args[i].toInt()] = 1;
        }

        restrictCheck();
    }
    
    /**
     * Reserved.
     * 
     * @param restrict_ reserved
     */
    public void restrictResultsByType(int[] restrict_)
    {
        if (restrict_ != null)
            _restrict = restrict_;
        else
        {
            for (int i = 0; i < _restrict.length; ++i)
                _restrict[i] = 1;
        }
        
        restrictCheck();
    }
    
    private void restrictCheck()
    {
        int total = 0;
        for (int i = 0; i < _restrict.length; ++i)
        {
            total += _restrict[i];
        }

        _restrictAll = (total == SearchAC.count());
    }

    /**
     * Reset the restrict list to include All types.
     *
     */
    public void resetRestrictResultsByType()
    {
        _restrict = new int[SearchAC.count()];
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
     * @deprecated Use excludeWorkflowStatus(true)
     * 
     */
    public void restrictResultsByWorkflowNotRetired()
    {
        excludeWorkflowStatusRetired(true);
    }

    /**
     * @deprecated Use excludeWorkflowStatus(false)
     * 
     */
    public void resetResultsByWorkflowNotRetired()
    {
        excludeWorkflowStatusRetired(false);
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
            DBAccessIndex.setSchema(text_);
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
     * @exception SearchException
     */
    public String getLastSeedTimestampString() throws SearchException
    {
        DBAccess db = new DBAccess();
        String seedTime = null;

        open(db);
        Timestamp ts = db.getLastSeedTimestamp();
        if (ts == null)
            throw new SearchException("Can not retrieve the Index Table timestamp - installation incomplete or in error.");

        seedTime = ts.toString() + " (yyyy-mm-dd hh:mm:ss Eastern Time)";
        db.close();

        return seedTime;
    }

    /**
     * Get the last seed timestamp.
     * 
     * @return the timestamp
     * @exception SearchException
     */
    public Timestamp getLastSeedTimestamp() throws SearchException
    {
        DBAccess db = new DBAccess();
        Timestamp seedTime = null;

        open(db);
        seedTime = db.getLastSeedTimestamp();
        db.close();

        return seedTime;
    }
    
    /**
     * Set the caCORE API URL should the AdministeredComponent related methods be used. This is
     * ONLY required if the value stored in the caDSR is not desired. Normally this method is not
     * used unless testing a new pre-release of the client.jar or on the development environments.
     * 
     * @param url_ The caCORE URL
     */
    public void setCoreApiUrl(String url_)
    {
        _coreApiUrl = url_;
    }
    
    /**
     * Set the server URL for the data connection. When this is used all other connection information
     * is ignored in preference for the server configuration.
     * 
     * @param url_ the Freestyle server, i.e. http://freestyle.nci.nih.gov
     */
    public void setDataDescription(String url_)
    {
        _serverURL = url_;
    }
    
    /**
     * Get the Exclude WFS Retired flag
     * 
     * @return flag value
     */
    public boolean getExcludeWFS()
    {
        return _excludeWFSretired;
    }
    
    /**
     * Get the Limit value
     * 
     * @return the value
     */
    public int getLimit()
    {
        return _limit;
    }
    
    /**
     * Get the Results By Score value
     * 
     * @return the value
     */
    public int getResultsByScore()
    {
        return _highestScores;
    }
    
    /**
     * Get the Match Flag value
     * 
     * @return the value
     */
    public SearchMatch getMatch()
    {
        return _matchFlag;
    }
    
    /**
     * Get the Restrict All Flag value
     * 
     * @return the value
     */
    public boolean getRestrictAll()
    {
        return _restrictAll;
    }
    
    /**
     * Get the Restrict list
     * 
     * @return the list
     */
    public int[] getRestrict()
    {
        return _restrict;
    }
    
    /**
     * Exclude everything Owned By the Test Context from the search results.
     * 
     * @param flag_ true to exclude "Test", false to include "Test"
     */
    public void excludeTest(boolean flag_)
    {
        _excludeTest = flag_;
    }
    
    /**
     * Get the setting to exclude the "Test" Context
     * 
     * @return true to exclude "Test"
     */
    public boolean getExcludeTest()
    {
        return _excludeTest;
    }
    
    /**
     * Exclude everything Owned By the Training Context from the search results.
     * 
     * @param flag_ true to exclude "Training", false to include "Training"
     */
    public void excludeTraining(boolean flag_)
    {
        _excludeTraining = flag_;
    }
    
    /**
     * Exclude every AC with a Workflow Status "RETIRED*"
     * 
     * @param flag_ true to exclude AC's, false to include AC's
     */
    public void excludeWorkflowStatusRetired(boolean flag_)
    {
        _excludeWFSretired = flag_;
    }
    
    /**
     * Get the setting to exclude the "Training" Context
     * 
     * @return true to exclude "Training"
     */
    public boolean getExcludeTraining()
    {
        return _excludeTraining;
    }

    {
        if (_versLog)
        {
            _versLog = false;
            _logger.info("Freestyle Search Engine JAR (freestylesearch.jar)  version " + _vers);
        }
    }

    private String _indexUrl;
    private String _indexUser;
    private String _indexPswd;
    private String _dataUrl;
    private String _dataUser;
    private String _dataPswd;
    private DataSource _indexDS;
    private Connection _indexConn;
    private DataSource _dataDS;
    private Connection _dataConn;
    private String _coreApiUrl;
    private String _serverURL;

    private boolean _excludeWFSretired;
    private int _limit;
    private int[] _restrict;
    private boolean _restrictAll;
    private int _highestScores;
    private SearchMatch _matchFlag;
    private boolean _excludeTest;
    private boolean _excludeTraining;
    
    private static boolean _versLog = true;

    private static final Logger _logger = Logger.getLogger(Search.class.getName());
}
