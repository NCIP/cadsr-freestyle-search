/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/SearchRequest.java,v 1.2 2007-07-13 16:25:06 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

import gov.nih.nci.cadsr.freestylesearch.util.Search;
import gov.nih.nci.cadsr.freestylesearch.util.SearchAC;
import gov.nih.nci.cadsr.freestylesearch.util.SearchException;
import gov.nih.nci.cadsr.freestylesearch.util.SearchResultObject;
import gov.nih.nci.cadsr.freestylesearch.util.SearchResults;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * Reserved for internal use only.
 * 
 * This class manages the client side request made for remote access to the Freestyle
 * server. The methods in this class are only used when Search.setDataDescription(URL)
 * is used. The supported methods have identical names to the supported Search methods.
 * If a "find*" method exists in Search and not in this class, the Freestyle server will
 * not return results.
 * 
 * Anyone using the Freestyle API should only instantiate and reference methods in the Search
 * class.
 * 
 * @author lhebel
 *
 */
public class SearchRequest
{
    /**
     * Make the request through the URL
     * @param url_ the URL to the Freestyle server
     * @throws IllegalArgumentException
     *
     */
    public SearchRequest(String url_) throws IllegalArgumentException
    {
        if (url_ == null || url_.length() == 0)
            throw new IllegalArgumentException("invalid URL");

        if (url_.lastIndexOf('/') < (url_.length() - 1))
            _url = url_ + "/";
        else
            _url = url_;
        
        _url += "freestyle/do/";
    }
    
    /**
     * Build the URL to make the request.
     * 
     * @param sobj_ the search object
     * @param vers_ the method signature version number
     * @param phrase_ the search phrase
     * @return the URL
     */
    private String formURL(String req_, int vers_, Search sobj_, String phrase_)
    {
        boolean restrictAll = sobj_.getRestrictAll();
        String phrase = phrase_.replaceAll("[ &]", "%20");

        String url = _url + req_
            + "?version=" + vers_
            + "&phrase=" + phrase
            + "&ewfsr=" + sobj_.getExcludeWFS()
            + "&limit=" + sobj_.getLimit()
            + "&highest=" + sobj_.getResultsByScore()
            + "&match=" + sobj_.getMatch()
            + "&xtest=" + sobj_.getExcludeTest()
            + "&xtrain=" + sobj_.getExcludeTraining()
            + "&rall=" + restrictAll;

        if (!restrictAll)
        {
            url += "&restrict=";
            int[] restrict = sobj_.getRestrict();
            for (int i = 0; i < restrict.length; ++i)
            {
                url += "," + restrict[i];
            }
        }
        
        return url;
    }
    
    /**
     * Process simple string responses, no specialized class is used.
     * 
     * @param url_ the Freestyle Server URL
     * 
     * @return the results in a String Vector
     * @exception SearchException
     */
    private Vector<String> findReturningString(String url_) throws SearchException
    {
        Vector<String> rs = new Vector<String>();
        int rc = HttpURLConnection.HTTP_OK;
        HttpURLConnection http = null;
        try
        {
            URL rps = new URL(url_);
            http = (HttpURLConnection) rps.openConnection();
            http.setUseCaches(false);
            rc = http.getResponseCode();
            switch (rc)
            {
                case HttpURLConnection.HTTP_OK:
                    break;

                default:
                    _logger.error(rc + " : " + http.getResponseMessage());
                    break;
            }
            
            // Collect the data records.
            if (rc == HttpURLConnection.HTTP_OK)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                while (true)
                {
                    String line = in.readLine();
                    if (line == null)
                        break;

                    // Remember the caCORE URL
                    if (line.startsWith(CACOREURL))
                        _cacoreURL = line.substring(CACOREURL.length());

                    // IDSEQ values are not translated.
                    else if (line.startsWith(IDSEQ))
                        rs.add(line.substring(IDSEQ.length()));

                    // Translate the text results
                    else if (line.startsWith(TEXT))
                    {
                        line = line.substring(TEXT.length()).replace("<br/>", "\n");
                        rs.add(line);
                    }
                }
            }
            else if (rc == HttpURLConnection.HTTP_INTERNAL_ERROR)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                String line = in.readLine();
                if (line == null)
                    line = "Unknown HttpURLConnection.HTTP_INTERNAL_ERROR";
                throw new SearchException(line);
            }
            else
            {
                throw new SearchException("Server Error " + rc);
            }
        }
        catch(MalformedURLException ex)
        {
            throw new SearchException("[" + url_ + "] " + ex.toString());
        }
        catch(IOException ex)
        {
            throw new SearchException("[" + url_ + "] " + ex.toString());
        }
        catch (SearchException ex)
        {
            throw ex;
        }
        finally
        {
            if (http != null)
                http.disconnect();
        }
        
        return rs;
    }
    
    /**
     * Make a request to the server for the search.
     * 
     * @param sobj_ the search object
     * @param phrase_ the search phrase
     * @return the results
     * @exception SearchException
     */
    public Vector<String> findReturningDefault(Search sobj_, String phrase_) throws SearchException
    {
        String url = formURL("findReturningDefault", 2, sobj_, phrase_);

        return findReturningString(url);
    }
    
    /**
     * Make a request to the server to get the database id's for matches. The first entry
     * in the Vector.get(0) is the caCORE API URL stored in the Tool Options table of
     * the caDSR configured for the Freestyle server. The caller must determine what to do
     * with it and must <b>NOT</b> use it as a database id.
     * 
     * @param sobj_ the search object
     * @param phrase_ the search phrase
     * @return the results
     * @exception SearchException
     */
    public Vector<String> findReturningIdseq(Search sobj_, String phrase_) throws SearchException
    {
        String url = formURL("findReturningIdseq", 2, sobj_, phrase_);

        return findReturningString(url);
    }
    
    /**
     * Return the Generic Search Results object
     * 
     * @param sobj_ the search object
     * @param phrase_ the search phrase
     * @return the search results
     * @exception SearchException
     */
    public Vector<SearchResults> findReturningSearchResults(Search sobj_, String phrase_) throws SearchException
    {
        String url = formURL("findReturningSearchResults", 3, sobj_, phrase_);

        Vector<SearchResults> rs = new Vector<SearchResults>();
        int rc = HttpURLConnection.HTTP_OK;
        HttpURLConnection http = null;
        try
        {
            URL rps = new URL(url);
            http = (HttpURLConnection) rps.openConnection();
            http.setUseCaches(false);
            rc = http.getResponseCode();
            switch (rc)
            {
                case HttpURLConnection.HTTP_OK:
                    break;

                default:
                    _logger.error(rc + " : " + http.getResponseMessage());
                    break;
            }
            
            // Collect the data records
            if (rc == HttpURLConnection.HTTP_OK)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                SearchAC rType = null;
                String rLname = null;
                String rPname = null;
                int rAcid = 0;
                String rAcvers = null;
                String rPdef = null;
                String rCname = null;
                String rReg = null;
                String rWfs = null;
                int rOcid = -1;
                String rOcver = null;
                int rPropid = -1;
                String rPropver = null;

                while (true)
                {
                    String line = in.readLine();
                    if (line == null)
                        break;
                    
                    if (line.equals(RECEND))
                    {
                        SearchResults rec = new SearchResults(rType, rLname, rPname, rAcid, rAcvers, rPdef, rCname, rReg, rWfs, rOcid, rOcver, rPropid, rPropver);
                        rs.add(rec);
                        rType = null;
                        rLname = null;
                        rPname = null;
                        rAcid = 0;
                        rAcvers = null;
                        rPdef = null;
                        rCname = null;
                        rReg = null;
                        rWfs = null;
                        rOcid = -1;
                        rOcver = null;
                        rPropid = -1;
                        rPropver = null;
                    }
                    else if (line.startsWith(TYPE))
                        rType = SearchAC.valueOf(line.substring(TYPE.length()));
                    else if (line.startsWith(LNAME))
                        rLname = line.substring(LNAME.length()).replace("<br/>", "\n");
                    else if (line.startsWith(PNAME))
                        rPname = line.substring(PNAME.length()).replace("<br/>", "\n");
                    else if (line.startsWith(ID))
                        rAcid = Integer.valueOf(line.substring(ID.length()));
                    else if (line.startsWith(VERS))
                        rAcvers = line.substring(VERS.length());
                    else if (line.startsWith(PDEF))
                        rPdef = line.substring(PDEF.length()).replace("<br/>", "\n");
                    else if (line.startsWith(CNAME))
                        rCname = line.substring(CNAME.length());
                    else if (line.startsWith(REG))
                        rReg = line.substring(REG.length());
                    else if (line.startsWith(WFS))
                        rWfs = line.substring(WFS.length());
                    else if (line.startsWith(OCID))
                        rOcid = Integer.valueOf(line.substring(OCID.length()));
                    else if (line.startsWith(OCVER))
                        rOcver = line.substring(OCVER.length());
                    else if (line.startsWith(PROPID))
                        rPropid = Integer.valueOf(line.substring(PROPID.length()));
                    else if (line.startsWith(PROPVER))
                        rPropver = line.substring(PROPVER.length());
                }
            }
            else if (rc == HttpURLConnection.HTTP_INTERNAL_ERROR)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                String line = in.readLine();
                if (line == null)
                    line = "Unknown HttpURLConnection.HTTP_INTERNAL_ERROR";
                throw new SearchException(line);
            }
            else
            {
                throw new SearchException("Server Error " + rc);
            }
        }
        catch(MalformedURLException ex)
        {
            throw new SearchException("[" + url + "] " + ex.toString());
        }
        catch(IOException ex)
        {
            throw new SearchException("[" + url + "] " + ex.toString());
        }
        catch (SearchException ex)
        {
            throw ex;
        }
        finally
        {
            if (http != null)
                http.disconnect();
        }
        
        return rs;
    }
    
    /**
     * Find AC's using the phrase (terms) provided, results are in objects containing
     * the AC type, database ID and result score.
     * 
     * @param sobj_ the search object
     * @param phrase_ the terms of interest
     * @return return the collection using SearchResultObject.
     * @exception SearchException
     */
    public Vector<SearchResultObject> findReturningResultSet(Search sobj_, String phrase_) throws SearchException
    {
        String url = formURL("findReturningResultSet", 1, sobj_, phrase_);

        Vector<SearchResultObject> rs = new Vector<SearchResultObject>();
        int rc = HttpURLConnection.HTTP_OK;
        HttpURLConnection http = null;
        try
        {
            URL rps = new URL(url);
            http = (HttpURLConnection) rps.openConnection();
            http.setUseCaches(false);
            rc = http.getResponseCode();
            switch (rc)
            {
                case HttpURLConnection.HTTP_OK:
                    break;

                default:
                    _logger.error(rc + " : " + http.getResponseMessage());
                    break;
            }
            
            // Collect the data records.
            if (rc == HttpURLConnection.HTTP_OK)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                SearchAC rType = null;
                String rIdseq = null;
                int rScore = 0;

                while (true)
                {
                    String line = in.readLine();
                    if (line == null)
                        break;
                    
                    if (line.equals(RECEND))
                    {
                        SearchResultObject rec = new SearchResultObject(rIdseq, rScore, rType);
                        rs.add(rec);
                        rType = null;
                        rIdseq = null;
                        rScore = 0;
                    }
                    else if (line.startsWith(TYPE))
                        rType = SearchAC.valueOf(line.substring(TYPE.length()));
                    else if (line.startsWith(IDSEQ))
                        rIdseq = line.substring(IDSEQ.length());
                    else if (line.startsWith(SCORE))
                        rScore = Integer.valueOf(line.substring(SCORE.length()));
                    else if (line.startsWith(CACOREURL))
                        _cacoreURL = line.substring(CACOREURL.length());
                }
            }
            else if (rc == HttpURLConnection.HTTP_INTERNAL_ERROR)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                String line = in.readLine();
                if (line == null)
                    line = "Unknown HttpURLConnection.HTTP_INTERNAL_ERROR";
                throw new SearchException(line);
            }
            else
            {
                throw new SearchException("Server Error " + rc);
            }
        }
        catch(MalformedURLException ex)
        {
            throw new SearchException("[" + url + "] " + ex.toString());
        }
        catch(IOException ex)
        {
            throw new SearchException("[" + url + "] " + ex.toString());
        }
        catch (SearchException ex)
        {
            throw ex;
        }
        finally
        {
            if (http != null)
                http.disconnect();
        }
        
        return rs;
    }
    
    /**
     * Get the caCORE URL returned from the server.
     * 
     * @return the url
     */
    public String getCaCoreUrl()
    {
        return _cacoreURL;
    }

    private String _url;
    private String _cacoreURL;
    
    /**
     */
    public static final String TYPE = "type ";
    
    /**
     */
    public static final String LNAME= "lname ";
    
    /**
     */
    public static final String PNAME = "pname ";
    
    /**
     */
    public static final String ID = "id ";
    
    /**
     */
    public static final String VERS = "vers ";
    
    /**
     */
    public static final String PDEF = "pdef ";
    
    /**
     */
    public static final String CNAME = "cname ";
    
    /**
     */
    public static final String REG = "reg ";
    
    /**
     */
    public static final String WFS = "wfs ";
    
    /**
     */
    public static final String OCID = "ocid ";
    
    /**
     */
    public static final String OCVER = "ocver ";
    
    /**
     */
    public static final String PROPID = "propid ";
    
    /**
     */
    public static final String PROPVER = "propver ";
    
    /**
     */
    public static final String IDSEQ = "idseq ";
    
    /**
     */
    public static final String SCORE = "score ";
    
    /**
     */
    public static final String CACOREURL = "cacoreurl ";
    
    /**
     */
    public static final String TEXT = "text ";
    
    /**
     */
    public static final String RECEND = "record_end";

    private static final Logger _logger = Logger.getLogger(SearchRequest.class.getName());
}
