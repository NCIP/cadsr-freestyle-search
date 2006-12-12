// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchRequest.java,v 1.1 2006-12-12 15:24:53 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * This class manages the client side request made for remote access to the Freestyle
 * server. The methods in this class are only used when Search.setDataDescription(URL)
 * is used. The supported methods have identical names to the supported Search methods.
 * If a "find*" method exists in Search and not in this class, the Freestyle server will
 * not return results.
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
     * @param phrase_ the search phrase
     * @return the URL
     */
    private String getURL(String req_, Search sobj_, String phrase_)
    {
        boolean restrictAll = sobj_.getRestrictAll();
        String phrase = phrase_.replaceAll("[ &]", "%20");

        String url = _url + req_
            + "?version=1"
            + "&phrase=" + phrase
            + "&ewfsr=" + sobj_.getExcludeWFS()
            + "&limit=" + sobj_.getLimit()
            + "&highest=" + sobj_.getResultsByScore()
            + "&match=" + sobj_.getMatch()
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
    
    private Vector<String> findReturningString(String url_, boolean trans_)
    {
        Vector<String> rs = new Vector<String>();
        int rc = HttpURLConnection.HTTP_OK;
        try
        {
            URL rps = new URL(url_);
            HttpURLConnection http = (HttpURLConnection) rps.openConnection();
            http.setUseCaches(false);
            rc = http.getResponseCode();
            switch (rc)
            {
                case HttpURLConnection.HTTP_OK:
                    break;

                default:
                    _logger.fatal(rc + " : " + http.getResponseMessage());
                    break;
            }
            
            // Get the Alert Name returned from the create service.
            if (rc == HttpURLConnection.HTTP_OK)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                while (true)
                {
                    String line = in.readLine();
                    if (line == null)
                        break;
                    if (trans_)
                        line = line.replace("<br/>", "\n");
                    rs.add(line);
                }
            }
            http.disconnect();
        }
        catch(MalformedURLException ex)
        {
            _logger.fatal("[" + url_ + "] " + ex.toString());
        }
        catch(IOException ex)
        {
            _logger.fatal("[" + url_ + "] " + ex.toString());
        }
        
        return rs;
    }
    
    /**
     * Make a request to the server for the search.
     * 
     * @param sobj_ the search object
     * @param phrase_ the search phrase
     * @return the results
     */
    public Vector<String> findReturningDefault(Search sobj_, String phrase_)
    {
        String url = getURL("findReturningDefault", sobj_, phrase_);

        return findReturningString(url, true);
    }
    
    /**
     * Make a request to the server to get the database id's for matches
     * 
     * @param sobj_ the search object
     * @param phrase_ the search phrase
     * @return the results
     */
    public Vector<String> findReturningIdseq(Search sobj_, String phrase_)
    {
        String url = getURL("findReturningIdseq", sobj_, phrase_);

        return findReturningString(url, false);
    }
    
    /**
     * Return the Generic Search Results object
     * 
     * @param sobj_ the search object
     * @param phrase_ the search phrase
     * @return the search results
     */
    public Vector<SearchResults> findReturningSearchResults(Search sobj_, String phrase_)
    {
        String url = getURL("findReturningSearchResults", sobj_, phrase_);

        Vector<SearchResults> rs = new Vector<SearchResults>();
        int rc = HttpURLConnection.HTTP_OK;
        try
        {
            URL rps = new URL(url);
            HttpURLConnection http = (HttpURLConnection) rps.openConnection();
            http.setUseCaches(false);
            rc = http.getResponseCode();
            switch (rc)
            {
                case HttpURLConnection.HTTP_OK:
                    break;

                default:
                    _logger.fatal(rc + " : " + http.getResponseMessage());
                    break;
            }
            
            // Get the Alert Name returned from the create service.
            if (rc == HttpURLConnection.HTTP_OK)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                SearchAC rType = null;
                String rLname = null;
                String rPname = null;
                int rId = 0;
                String rVers = null;
                String rPdef = null;
                String rCname = null;
                String rReg = null;

                while (true)
                {
                    String line = in.readLine();
                    if (line == null)
                        break;
                    
                    if (line.equals(RECEND))
                    {
                        SearchResults rec = new SearchResults(rType, rLname, rPname, rId, rVers, rPdef, rCname, rReg);
                        rs.add(rec);
                        rType = null;
                        rLname = null;
                        rPname = null;
                        rId = 0;
                        rVers = null;
                        rPdef = null;
                        rCname = null;
                        rReg = null;
                    }
                    else if (line.startsWith(TYPE))
                        rType = SearchAC.valueOf(line.substring(TYPE.length()));
                    else if (line.startsWith(LNAME))
                        rLname = line.substring(LNAME.length()).replace("<br/>", "\n");
                    else if (line.startsWith(PNAME))
                        rPname = line.substring(PNAME.length()).replace("<br/>", "\n");
                    else if (line.startsWith(ID))
                        rId = Integer.valueOf(line.substring(ID.length()));
                    else if (line.startsWith(VERS))
                        rVers = line.substring(VERS.length());
                    else if (line.startsWith(PDEF))
                        rPdef = line.substring(PDEF.length()).replace("<br/>", "\n");
                    else if (line.startsWith(CNAME))
                        rCname = line.substring(CNAME.length());
                    else if (line.startsWith(REG))
                        rReg = line.substring(REG.length());
                }
            }
            http.disconnect();
        }
        catch(MalformedURLException ex)
        {
            _logger.fatal("[" + url + "] " + ex.toString());
        }
        catch(IOException ex)
        {
            _logger.fatal("[" + url + "] " + ex.toString());
        }
        
        return rs;
    }

    
    private String _url;
    
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
    public static final String RECEND = "record_end";

    private static final Logger _logger = Logger.getLogger(SearchRequest.class.getName());
}
