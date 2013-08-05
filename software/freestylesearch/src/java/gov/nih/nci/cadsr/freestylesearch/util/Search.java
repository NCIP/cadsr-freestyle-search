/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/Search.java,v 1.24 2008-04-22 20:28:39 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.cadsr.freestylesearch.tool.DBAccess;
import gov.nih.nci.cadsr.freestylesearch.tool.DBAccessIndex;
import gov.nih.nci.cadsr.freestylesearch.tool.GenericAC;
import gov.nih.nci.cadsr.freestylesearch.tool.ResultsAC;
import gov.nih.nci.cadsr.freestylesearch.tool.SearchRequest;
import gov.nih.nci.system.client.*;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
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
     * The published Freestyle Search Engine API version stamp.
     */
    public static final String _vers ="Appl.version";

    /*
     * Index table connection credentials
     */
    private String _indexUrl;
    private String _indexUser;
    private String _indexPswd;
    private DataSource _indexDS;
    private Connection _indexConn;
    
    /*
     * Data connection credentials, i.e. caDSR
     */
    private String _dataUrl;
    private String _dataUser;
    private String _dataPswd;
    private DataSource _dataDS;
    private Connection _dataConn;

    /*
     * The caDSR API URL
     */
    private String _caDsrApiUrl;
    
    /*
     * The caDSR API ServiceInfo bean id
     */
    private String _caDsrApiBeanId;
    
    /*
     * The Freestyle Search Server URL
     */
    private String _serverURL;

    /*
     * Flag to exclude all "retired" Workflow Statuses
     */
    private boolean _excludeWFSretired;
    
    /*
     * The maximum result limit for any type of search.
     */
    private int _limit;
    
    /*
     * Each Administered Component (AC) may or may not be included in the search. Each AC has a
     * numerical type value, zero based, used to index this array.
     */
    private int[] _restrict;
    private boolean _restrictAll;
    
    /*
     * The number of highest scores to return.
     */
    private int _highestScores;
    
    /*
     * The type of match, e.g. partial, exact, ...
     */
    private SearchMatch _matchFlag;
    
    /*
     * The flags to exclude special Contexts from the results.
     */
    private boolean _excludeTest;
    private boolean _excludeTraining;
    
    private static boolean _versLog = true;

    private static final Logger _logger = Logger.getLogger(Search.class.getName());

    static
    {
        if (_versLog)
        {
            // This is an API so give the developer/user a log note about the release/version
            // of the software being used.
            _versLog = false;
            _logger.info("Freestyle Search Engine JAR (freestylesearch.jar)  version " + getVersion());
        }
    }
    
    /**
     * Return the version marker for the freestylesearch.jar
     * 
     * @return the version marker
     */
    public static String getVersion()
    {
        ResourceBundle props = PropertyResourceBundle.getBundle("gov.nih.nci.cadsr.freestylesearch.Freestyle");
        String temp = (props != null) ? props.getString(_vers) : "Error loading Property file.";
        return temp;
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

        // If the API server is not used, make the search locally.
        if (_serverURL == null)
        {
            Vector<ResultsAC> rs0 = find(phrase_);
            rs = getDefaultDisplay(rs0);
        }
        
        // Send the request to the API server.
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
        
        // If the API server is not used, make the search locally.
        if (_serverURL == null)
        {
            Vector<ResultsAC> rs0 = find(phrase_);
            rs = findReturningResultSet(rs0);
        }

        // Send the request to the API server.
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
        
        // If the API server is not used, make the search locally.
        if (_serverURL == null)
        {
            Vector<ResultsAC> rs0 = find(phrase_);
            rs = findReturningSearchResults(rs0);
        }

        // Send the request to the API server.
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
    private ApplicationService getCaDsrService() throws SearchException
    {
        try
        {
            if (_caDsrApiUrl != null)
                return ApplicationServiceProvider.getApplicationServiceFromUrl(_caDsrApiUrl, _caDsrApiBeanId);
    
            // String url = getDsrCoreUrl();
            
            return ApplicationServiceProvider.getApplicationServiceFromUrl(_caDsrApiUrl, _caDsrApiBeanId);
        }
        catch (Exception ex)
        {
            throw new SearchException(ex);
        }
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

        if (_serverURL != null)
            return url;

        // Get the coreapi url
        DBAccess db = new DBAccess();
        try
        {
            open(db);
            url = db.getCoreUrl();
        }
        catch (SearchException ex)
        {
            throw ex;
        }
        finally
        {
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
        
        // If the API server is not used, make the search locally.
        if (_serverURL == null)
        {
            // Find the phrase in the index tables.
            Vector<ResultsAC> rs0 = find(phrase_);
            
            // Get the basic AC details and scores from the caDSR.
            rs1 = findReturningResultSet(rs0);
            
            // Get the AC details from the caCORE API
            rs2 = findReturningAdministeredComponent(rs0);
            
            // There have been problems with the caCORE API so double check it's working.
            if (rs0.size() != rs1.size() || rs0.size() != rs2.size())
            {
                throw new SearchException("Error retrieving results for method findReturningResultsWithAC (direct connection) [" + rs0.size() + ", " + rs1.size() + ", " + rs2.size() + "]");
            }

            // Don't need the internal result set any more.
            rs0 = null;
        }

        // Send the request to the API server.
        else
        {
            // Get the basic AC details from the Freestyle API Server. This also returns the caCORE API to use.
            SearchRequest sr = new SearchRequest(_serverURL);
            rs1 = sr.findReturningResultSet(this, phrase_);
            setCaDsrApiUrl(sr.getCaCoreUrl(), null);

            // Build the request list for the caCORE API and retrieve the AC details.
            String[] idseq = new String[rs1.size()];
            for (int i = 0; i < idseq.length; ++i)
            {
                idseq[i] = rs1.get(i).getIdseq();
            }
            rs2 = findReturningAdministeredComponent(idseq);

            // There have been problems with the caCORE API so double check it's working.
            if (rs1.size() != rs2.size())
            {
                throw new SearchException("Error retrieving results for method findReturningResultsWithAC (URL " + _serverURL + ") [" + rs1.size() + ", " + rs2.size() + "]");
            }
            
            idseq = null;
        }

        // Whether the calls are made locally or through the Freestyle API server, they must be combined into a single
        // result for the caller.
        for (int i = 0; i < rs1.size(); ++i)
        {
            SearchResultsWithAC var = new SearchResultsWithAC(rs1.get(i), rs2.get(2));
            rs.add(var);
        }

        // Help the garbage collector, the internal structures are not needed.
        rs1 = null;
        rs2 = null;
        
        return rs;
    }

    /**
     * Collect the Vector AC database id and get the AC details from the caCORE API.
     * 
     * @param idseq_ the AC database id's
     * @return the caCORE API AC objects
     * @throws SearchException
     */
    private Vector<AdministeredComponent> findReturningAdministeredComponent2(Vector<String> idseq_) throws SearchException
    {
        String[] idseq = new String[idseq_.size()];
        for (int i = 0; i < idseq.length; ++i)
            idseq[i] = idseq_.get(i);
        
        return findReturningAdministeredComponent(idseq);
    }
    
    /**
     * Collect the AC database id's from the ResultsAC Vector and get the AC details from the
     * caCORE API.
     * 
     * @param rs_ the internal results
     * @return the caCORE API AC objects
     * @throws SearchException
     */
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
        // Get the caCORE API URL.
        ApplicationService cadsrApi = getCaDsrService();

        // The results from the caCORE are not sorted and the order is not maintained or consistent with
        // the original search vector. This makes it necessary to "sort" the results from the caCORE API
        // to match the original internal results vector.
        
        // Create a map using the database id (IDSEQ) and its original position in the internal results array. 
        HashMap<String, Integer> rsMap = new HashMap<String, Integer>();
        List<AdministeredComponent> acID = new ArrayList<AdministeredComponent>();

        // Create the required caCORE Search request object and because a HashMap doesn't work with
        // primitive types, the array index must be converted to an Integer object.
        for (int cnt = 0; cnt < idseq_.length; ++cnt)
        {
            AdministeredComponent var = new AdministeredComponent();
            var.setId(idseq_[cnt]);
            acID.add(var);
            rsMap.put(idseq_[cnt], Integer.valueOf(cnt));
        }
        
        Vector<AdministeredComponent> rs = new Vector<AdministeredComponent>();

        // Call the caCORE API to get the AC details.
        try
        {
            List<Object> acs = cadsrApi.search(AdministeredComponent.class, acID); 
            if (acs.size() != idseq_.length)
                throw new SearchException("Invalid results from caCORE API.");

            // Using the HashMap put the results in the original internal order.
            rs.setSize(acs.size());
            for (int i = 0; i < acs.size(); ++i)
            {
                AdministeredComponent record = (AdministeredComponent) acs.get(i);
                Integer x = rsMap.get(record.getId());
                rs.set(x.intValue(), record);
            }
        }
        catch (ApplicationException ex)
        {
            _logger.error(ex.toString(), ex);
            throw new SearchException(ex.toString());
        }

        // Helping the garbage collector, just in case.
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

        // If the API server is not used, make the search locally.
        if (_serverURL == null)
        {
            Vector<ResultsAC> rs0 = find(phrase_);
            rs = findReturningAdministeredComponent(rs0);
        }

        // Send the request to the API server.
        else
        {
            SearchRequest sr = new SearchRequest(_serverURL);
            Vector<String> idseq = sr.findReturningIdseq(this, phrase_);
            setCaDsrApiUrl(sr.getCaCoreUrl(), null);
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

        DBAccessIndex dbIndex = new DBAccessIndex();
        Vector<ResultsAC> var = new Vector<ResultsAC>();

        try
        {
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
                // Need to open the index table connection and do a search.
                open(dbIndex);
    
                // We do a case sensitive search on a lower case string
                // which has the effect of a case insensitive search without
                // the extra processing overhead.
                phrase = phrase.toLowerCase();
                switch (_matchFlag)
                {
                    case EXACT: var = doMatchExact(dbIndex, phrase); break;
                    case PARTIAL: var = doMatchPartial(dbIndex, phrase); break;
                    case BEST:
                    default: var = doMatchBest(dbIndex, phrase); break;
                }
            }
        }
        catch (SearchException ex)
        {
            throw ex;
        }
        finally
        {
            dbIndex.close();
        }
        return var;
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
        // Don't bother if the internal results is empty.
        if (list_.size() == 0)
            return new Vector<SearchResults>();

        // Open the data connection.
        DBAccess dbData = new DBAccess();
        Vector<SearchResults> rs = new Vector<SearchResults>();
        try
        {
            open(dbData);
    
            // Retrieve the AC details.
            rs = dbData.getSearchResults(list_);
        }
        catch (SearchException ex)
        {
            throw ex;
        }
        finally
        {
            dbData.close();
        }
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
        // Don't bother if the internal results is empty.
        if (list_.size() == 0)
            return new Vector<String>();

        // Open the data connection.
        DBAccess dbData = new DBAccess();
        Vector<String> rs = new Vector<String>();
        try
        {
            open(dbData);
    
            // Retrieve the displayable AC results for the user.
            rs = dbData.getDisplay(list_);
        }
        catch (SearchException ex)
        {
            throw ex;
        }
        finally
        {
            dbData.close();
        }
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
        // Of course the method argument can not be null.
        if (indexDB_ != null)
        {
            // Do we have a data source?
            if (_indexDS != null)
            {
                // If there is no user (account) make an anonymous connection.
                if (_indexUser == null)
                {
                    indexDB_.open(_indexDS);
                }
                
                // Use a specific user/password for the connection.
                else
                {
                    indexDB_.open(_indexDS, _indexUser, _indexPswd);
                }
            }

            // Do we have an existing database connection?
            else if (_indexConn != null)
            {
                indexDB_.open(_indexConn);
            }

            // If all else fails use the database URL and user credentials to create the connection.
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
        // Of course the method argument can not be null.
        if (dataDB_ != null)
        {
            // Do we have a data source?
            if (_dataDS != null)
            {
                // If there is no user (account) make an anonymous connection.
                if (_dataUser == null)
                {
                    dataDB_.open(_dataDS);
                }

                // Use a specific user/password for the connection.
                else
                {
                    dataDB_.open(_dataDS, _dataUser, _dataPswd);
                }
            }

            // Do we have an existing database connection?
            else if (_dataConn != null)
            {
                dataDB_.open(_dataConn);
            }

            // If all else fails use the database URL and user credentials to create the connection.
            else
            {
                dataDB_.open(_dataUrl, _dataUser, _dataPswd);
            }
        }
    }
    
    /**
     * Scrub the user phrase for undesirable/excluded words.
     * 
     * @param dbIndex_ the index tables database connection
     * @param phrase_ the user search phrase
     * @return the scrubbed phrase
     */
    private String scrubPhrase(DBAccessIndex dbIndex_, String phrase_)
    {
        // How many terms are in this phrase?
        String[] terms = phrase_.split(" ");
        
        // Not enough, return the original, no scrubbing needed.
        if (terms.length < 2)
            return phrase_;
        
        // Remove words we don't keep.
        String phrase = "";
        for (int i = 0; i < terms.length; ++i)
        {
            if (terms[i].length() < 2 || dbIndex_.checkExcludes(terms[i]))
                continue;
            phrase += " " + terms[i];
        }

        // If the user has a bunch of single letter values or excluded terms and nothing else, we'll try a search
        // using the original phrase.
        if (phrase.length() == 0)
            phrase = phrase_;
        
        // There was something more meaningful in the search phrase so remove the leading space added in the
        // previous loop.
        else
            phrase = phrase.substring(1);
        
        return phrase;
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
        // Scrub the phrase for undesirable words.
        String phrase = scrubPhrase(dbIndex_, phrase_);

        // The SQL is optimized to filter by type when not including all possible
        // AC's.
        Vector<ResultsAC> var = null;
        if (_restrictAll)
        {
            // No restrictions on the AC types so search for everything.
            var = dbIndex_.searchExact(null, phrase, phrase_);
        }
        else
        {
            // Some AC types are not included in this search.
            var = dbIndex_.searchExact(_restrict, phrase, phrase_);
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
        // The SQL is optimized to filter by type when not including all possible
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
        // Scrub the user search phrase
        String phrase = scrubPhrase(dbIndex_, phrase_);

        Vector<ResultsAC> var = null;

        // No restrictions on the AC type, search them all
        if (_restrictAll)
        {
            // If no results are found from an exact search then automatically perform a partial.
            var = dbIndex_.searchExact(null, phrase, phrase_);
            if (var.size() == 0)
                var = dbIndex_.searchPartial(null, phrase_);
        }

        // Some AC types are not to be searched
        else
        {
            // If no results are found from an exact search then automatically perform a partial.
            var = dbIndex_.searchExact(_restrict, phrase, phrase_);
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
     * Reserved. This method is reserved for use within the Freestyle Search Engine API.
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

    /**
     * Check the restricted AC array and reset the boolean that tracks the AC selections.
     * It's quicker and easier to check the boolean during processing than constantly scanning
     * the array for restricted AC's.
     */
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
     * Clear all the Index Connection Description variables.
     *
     */
    private void clearIndexConnectionDescriptions()
    {
        _indexConn = null;
        _indexDS = null;
        _indexPswd = null;
        _indexUrl = null;
        _indexUser = null;
    }

    /**
     * Clear all the Data Connection Description variables.
     *
     */
    private void clearDataConnectionDescriptions()
    {
        _dataConn = null;
        _dataDS = null;
        _dataPswd = null;
        _dataUrl = null;
        _dataUser = null;
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
        // Set the index tables access
        clearIndexConnectionDescriptions();
        _indexUrl = url_;
        _indexUser = user_;
        _indexPswd = pswd_;

        // If the data access isn't set then make them the same
        if (_dataUrl == null)
        {
            setDataDescription(url_, user_, pswd_);
        }
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
        // Set the index tables access
        clearIndexConnectionDescriptions();
        _indexDS = ds_;
        _indexUser = user_;
        _indexPswd = pswd_;

        // If the data access isn't set then make them the same
        if (_dataDS == null)
        {
            setDataDescription(ds_, user_, pswd_);
        }
    }

    /**
     * Set the database description which hosts the freestyle search index tables. Use
     * setDataDescription(...) instead.
     * 
     * @param ds_ the datasource pool
     */
    public void setIndexDescription(DataSource ds_)
    {
        // Set the index tables access
        clearIndexConnectionDescriptions();
        _indexDS = ds_;

        // If the data access isn't set then make them the same
        if (_dataDS == null)
        {
            setDataDescription(ds_);
        }
    }

    /**
     * Set the database description which hosts the freestyle search index tables. Use
     * setDataDescription(...) instead.
     * 
     * @param conn_ an established database connection
     */
    public void setIndexDescription(Connection conn_)
    {
        // Set the index tables access
        clearIndexConnectionDescriptions();
        _indexConn = conn_;

        // If the data access isn't set then make them the same
        if (_dataConn == null)
        {
            setDataDescription(conn_);
        }
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
        // Set the data tables access
        clearDataConnectionDescriptions();
        _dataUrl = url_;
        _dataUser = user_;
        _dataPswd = pswd_;

        // If the index access isn't set then make them the same
        if (_indexUrl == null)
        {
            setIndexDescription(url_, user_, pswd_);
        }
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
        // Set the data tables access
        clearDataConnectionDescriptions();
        _dataDS = ds_;
        _dataUser = user_;
        _dataPswd = pswd_;

        // If the index access isn't set then make them the same
        if (_indexDS == null)
        {
            setIndexDescription(ds_, user_, pswd_);
        }
    }

    /**
     * Set the database description which hosts the freestyle index tables and caDSR. The
     * datasource must already have a valid user and password defined.
     * 
     * @param ds_ the datasource pool
     */
    public void setDataDescription(DataSource ds_)
    {
        // Set the data tables access
        clearDataConnectionDescriptions();
        _dataDS = ds_;

        // If the index access isn't set then make them the same
        if (_indexDS == null)
            setIndexDescription(ds_);
    }

    /**
     * Set the database description which hosts the freestyle index tables and caDSR.
     * 
     * @param conn_ an established database connection
     */
    public void setDataDescription(Connection conn_)
    {
        // Set the data tables access
        clearDataConnectionDescriptions();
        _dataConn = conn_;

        // If the index access isn't set then make them the same
        if (_indexConn == null)
            setIndexDescription(conn_);
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

        try
        {
            open(db);
            Timestamp ts = db.getLastSeedTimestamp();
            if (ts == null)
                throw new SearchException("Can not retrieve the Index Table timestamp - installation incomplete or in error.");
    
            seedTime = ts.toString() + " (yyyy-mm-dd hh:mm:ss Eastern Time)";
        }
        catch (SearchException ex)
        {
            throw ex;
        }
        finally
        {
            db.close();
        }

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

        try
        {
            open(db);
            seedTime = db.getLastSeedTimestamp();
        }
        catch (SearchException ex)
        {
            throw ex;
        }
        finally
        {
            db.close();
        }

        return seedTime;
    }
    
    /**
     * Set the caDSR API URL should the AdministeredComponent related methods be used. This is
     * ONLY required if the value stored in the caDSR is not desired. Normally this method is not
     * used unless testing a new pre-release of the client.jar or on the development environments.
     * 
     * @param url_ The caDSR API URL
     * @param bean_ The API ServiceInfo bean name as entered in the application-config-client.xml file. Use null for the default "CaDsrServiceInfo".
     */
    public void setCaDsrApiUrl(String url_, String bean_)
    {
        _caDsrApiUrl = url_;
        _caDsrApiBeanId = (bean_ == null) ? "CaDsrServiceInfo" : bean_;
    }

    /**
     * This is a convenience method for setCaDsrApiUrl().
     * @see gov.nih.nci.cadsr.freestylesearch.util.Search#setCaDsrApiUrl(String, String)
     * 
     * @param url_ the caDSR API URL
     */
    public void setCoreApiUrl(String url_)
    {
        setCaDsrApiUrl(url_, null);
    }
    
    /**
     * Set the server URL for the data connection. When this is used all other connection information
     * is ignored in preference for the server configuration.
     * 
     * @param url_ the Freestyle server, i.e. http://freestyle.nci.nih.gov
     */
    public void setDataDescription(String url_)
    {
        clearDataConnectionDescriptions();
        clearIndexConnectionDescriptions();
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
}
