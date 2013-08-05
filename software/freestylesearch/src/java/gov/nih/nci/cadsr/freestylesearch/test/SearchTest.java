/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/test/SearchTest.java,v 1.15 2008-04-22 20:28:39 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.test;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.cadsr.freestylesearch.util.Search;
import gov.nih.nci.cadsr.freestylesearch.util.SearchAC;
import gov.nih.nci.cadsr.freestylesearch.util.SearchException;
import gov.nih.nci.cadsr.freestylesearch.util.SearchMatch;
import gov.nih.nci.cadsr.freestylesearch.util.SearchResultObject;
import gov.nih.nci.cadsr.freestylesearch.util.SearchResults;
import gov.nih.nci.cadsr.freestylesearch.util.SearchResultsWithAC;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Test the freestyle search engine
 * 
 * @author lhebel
 *
 */
public class SearchTest
{

    private static String _default = "default";
    private static final Logger _logger = Logger.getLogger(SearchTest.class.getName());

    /**
     * A description of the profile for testing.
     * 
     * @author lhebel
     *
     */
    public class Profile
    {
        /**
         */
        public String _indexDSurl;
        /**
         */
        public String _indexDSusername;
        /**
         */
        public String _indexDSpassword;
        /**
         */
        public String _indexDSschema;
        /**
         */
        public String _dataDSurl;
        /**
         */
        public String _dataDSusername;
        /**
         */
        public String _dataDSpassword;
        /**
         */
        public String _freestyleUrl;
        /**
         */
        public String _coreUrl;
        /**
         */
        public String _returns;
        /**
         */
        public String _matchType;
        /**
         */
        public String _limit;
        /**
         */
        public String _scores;
        /**
         */
        public String _restricts;
        /**
         */
        public String _restrictResultsByWorkflowNotRetired;
        /**
         */
        public String _excludeTest;
        /**
         */
        public String _excludeTraining;
        /**
         */
        public String _phrase;
        /**
         */
        public String _access;
        /**
         */
        public String _prefix;
        
        /**
         * Constructor
         * @param prefix_ the profile prefix
         *
         */
        public Profile(String prefix_)
        {
            _prefix = prefix_;
        }
        
        /**
         * Constructor
         * @param prefix_ the profile prefix
         * @param other_ the object to duplicate
         */
        public Profile(String prefix_, Profile other_)
        {
            _indexDSurl = other_._indexDSurl;
            _indexDSusername = other_._indexDSusername;
            _indexDSpassword = other_._indexDSpassword;
            _indexDSschema = other_._indexDSschema;
            _dataDSurl = other_._dataDSurl;
            _dataDSusername = other_._dataDSusername;
            _dataDSpassword = other_._dataDSpassword;
            _freestyleUrl = other_._freestyleUrl;
            _coreUrl = other_._coreUrl;
            _returns = other_._returns;
            _matchType = other_._matchType;
            _limit = other_._limit;
            _scores = other_._scores;
            _restricts = other_._restricts;
            _restrictResultsByWorkflowNotRetired = other_._restrictResultsByWorkflowNotRetired;
            _excludeTest = other_._excludeTest;
            _excludeTraining = other_._excludeTraining;
            _phrase = other_._phrase;
            _access = other_._access;

            _prefix = prefix_;
        }

        /**
         * @return name
         */
        public String nameIndexDSurl()
        {
            return _prefix + ".index.DSurl";
        }

        /**
         * @return name
         */
        public String nameIndexDSusername()
        {
            return _prefix + ".index.DSusername";
        }

        /**
         * @return name
         */
        public String nameIndexDSpassword()
        {
            return _prefix + ".index.DSpassword";
        }

        /**
         * @return name
         */
        public String nameIndexDSschema()
        {
            return _prefix + ".index.DSschema";
        }

        /**
         * @return name
         */
        public String nameDataDSurl()
        {
            return _prefix + ".data.DSurl";
        }

        /**
         * @return name
         */
        public String nameDataDSusername()
        {
            return _prefix + ".data.DSusername";
        }

        /**
         * @return name
         */
        public String nameDataDSpassword()
        {
            return _prefix + ".data.DSpassword";
        }

        /**
         * @return name
         */
        public String nameFreestyleUrl()
        {
            return _prefix + ".freestyle.url";
        }

        /**
         * @return name
         */
        public String nameCoreUrl()
        {
            return _prefix + ".core.url";
        }

        /**
         * @return name
         */
        public String nameReturns()
        {
            return _prefix + ".returns";
        }

        /**
         * @return name
         */
        public String nameMatchType()
        {
            return _prefix + ".matchType";
        }

        /**
         * @return name
         */
        public String nameLimit()
        {
            return _prefix + ".limit";
        }

        /**
         * @return name
         */
        public String nameScores()
        {
            return _prefix + ".scores";
        }

        /**
         * @return name
         */
        public String nameRestricts()
        {
            return _prefix + ".restricts";
        }

        /**
         * @return name
         */
        public String nameRestrictResultsByWorkflowNotRetired()
        {
            return _prefix + ".restrictResultsByWorkflowNotRetired";
        }

        /**
         * @return name
         */
        public String nameExcludeTest()
        {
            return _prefix + ".excludeTest";
        }

        /**
         * @return name
         */
        public String nameExcludeTraining()
        {
            return _prefix + ".excludeTraining";
        }

        /**
         * @return name
         */
        public String namePhrase()
        {
            return _prefix + ".phrase";
        }

        /**
         * @return name
         */
        public String nameAccess()
        {
            return _prefix + ".access";
        }
        
        /**
         * Load the properties for this profile
         * 
         * @param prop_ the properties object
         */
        public void load(Properties prop_)
        {
            String indexDSurl = prop_.getProperty(nameIndexDSurl());
            String indexDSusername = prop_.getProperty(nameIndexDSusername());
            String indexDSpassword = prop_.getProperty(nameIndexDSpassword());
            String indexDSschema = prop_.getProperty(nameIndexDSschema());
            String dataDSurl = prop_.getProperty(nameDataDSurl());
            String dataDSusername = prop_.getProperty(nameDataDSusername());
            String dataDSpassword = prop_.getProperty(nameDataDSpassword());
            String freestyleUrl = prop_.getProperty(nameFreestyleUrl());
            String coreUrl = prop_.getProperty(nameCoreUrl());
            String returns = prop_.getProperty(nameReturns());
            String matchType = prop_.getProperty(nameMatchType());
            String limit = prop_.getProperty(nameLimit());
            String scores = prop_.getProperty(nameScores());
            String restricts = prop_.getProperty(nameRestricts());
            String restrictResultsByWorkflowNotRetired = prop_.getProperty(nameRestrictResultsByWorkflowNotRetired());
            String excludeTest = prop_.getProperty(nameExcludeTest());
            String excludeTraining = prop_.getProperty(nameExcludeTraining());
            String phrase = prop_.getProperty(namePhrase());
            String access = prop_.getProperty(nameAccess());

            if (indexDSurl != null)
                _indexDSurl = indexDSurl;
            if (indexDSusername != null)
                _indexDSusername = indexDSusername;
            if (indexDSpassword != null)
                _indexDSpassword = indexDSpassword;
            if (indexDSschema != null)
                _indexDSschema = indexDSschema;
            if (dataDSurl != null)
                _dataDSurl = dataDSurl;
            if (dataDSusername != null)
                _dataDSusername = dataDSusername;
            if (dataDSpassword != null)
                _dataDSpassword = dataDSpassword;
            if (freestyleUrl != null)
                _freestyleUrl = freestyleUrl;
            if (coreUrl != null)
                _coreUrl = coreUrl;
            if (returns != null)
                _returns = returns;
            if (matchType != null)
                _matchType = matchType;
            if (limit != null)
                _limit = limit;
            if (scores != null)
                _scores = scores;
            if (restricts != null)
                _restricts = restricts;
            if (restrictResultsByWorkflowNotRetired != null)
                _restrictResultsByWorkflowNotRetired = restrictResultsByWorkflowNotRetired;
            if (excludeTest != null)
                _excludeTest = excludeTest;
            if (excludeTraining != null)
                _excludeTraining = excludeTraining;
            if (phrase != null)
                _phrase = phrase;
            if (access != null)
                _access = access;
        }

        /**
         * @return value
         */
        public String getIndexDSurl()
        {
            return _indexDSurl;
        }

        /**
         * @return value
         */
        public String getIndexDSusername()
        {
            return _indexDSusername;
        }

        /**
         * @return value
         */
        public String getIndexDSpassword()
        {
            return _indexDSpassword;
        }

        /**
         * @return value
         */
        public String getIndexDSschema()
        {
            return _indexDSschema;
        }

        /**
         * @return value
         */
        public String getDataDSurl()
        {
            return _dataDSurl;
        }

        /**
         * @return value
         */
        public String getDataDSusername()
        {
            return _dataDSusername;
        }

        /**
         * @return value
         */
        public String getDataDSpassword()
        {
            return _dataDSpassword;
        }

        /**
         * @return value
         */
        public String getFreestyleUrl()
        {
            return _freestyleUrl;
        }

        /**
         * @return value
         */
        public String getCoreUrl()
        {
            return _coreUrl;
        }

        /**
         * @return value
         */
        public String getReturns()
        {
            return _returns;
        }

        /**
         * @return value
         */
        public SearchMatch getMatchType()
        {
            return SearchMatch.valueOf(Integer.valueOf(_matchType));
        }

        /**
         * @return value
         */
        public int getLimit()
        {
            return Integer.valueOf(_limit);
        }

        /**
         * @return value
         */
        public int getScores()
        {
            return Integer.valueOf(_scores);
        }

        /**
         * @return value
         */
        public SearchAC[] getRestricts()
        {
            SearchAC[] restricts = null;

            if (_restricts != null)
            {
                String[] vals = _restricts.split("[, ]");
                restricts = new SearchAC[vals.length];

                for (int i = 0; i < vals.length; ++i)
                {
                    if (vals[i] != null && vals[i].length() > 0)
                        restricts[i] = SearchAC.valueOf(Integer.valueOf(vals[i]));
                }
            }
            
            return restricts;
        }

        /**
         * @return value
         */
        public boolean getRestrictResultsByWorkflowNotRetired()
        {
            return "true".equals(_restrictResultsByWorkflowNotRetired);
        }

        /**
         * @return value
         */
        public boolean getExcludeTest()
        {
            return "true".equals(_excludeTest);
        }

        /**
         * @return value
         */
        public boolean getExcludeTraining()
        {
            return "true".equals(_excludeTraining);
        }

        /**
         * @return value
         */
        public String getPhrase()
        {
            return _phrase;
        }

        /**
         * @return value
         */
        public boolean getAccess()
        {
            return "remote".equals(_access);
        }
    }
    
    /**
     * Load the profiles
     * 
     * @param file_ the configuration XML
     * @return the array of profiles, [0] is the default
     * @throws Exception
     */
    private Profile[] loadProfiles(String file_) throws Exception
    {
        // Read the test properties.
        Properties prop = new Properties();
        FileInputStream ins = null;
        try
        {
            ins = new FileInputStream(file_);
            prop.loadFromXML(ins);
        }
        catch (FileNotFoundException e)
        {
            throw e;
        }
        catch (InvalidPropertiesFormatException e)
        {
            throw e;
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (ins != null)
            {
                try
                {
                    ins.close();
                }
                catch (Exception ex)
                {
                }
            }
        }
        
        // Determine the number of profiles and their names/prefices
        String profiles = prop.getProperty("profiles");
        String[] prefixes = null;
        Profile[] list = null;
        if (profiles == null)
            list = new Profile[1];
        else
        {
            prefixes = profiles.split("[, ]");
            list = new Profile[1 + prefixes.length];
        }

        // The default is always used and if it doesn't exist then everything uses
        // programmed defaults.
        list[0] = new Profile(_default);
        int lip = 0;
        list[lip].load(prop);
        ++lip;
        
        // Load a profile for each name/prefix in the list.
        for (int pre = 0; pre < prefixes.length; ++pre)
        {
            if (prefixes[pre] != null && prefixes[pre].length() > 0 && !_default.equals(prefixes[pre]))
            {
                list[lip] = new Profile(prefixes[pre], list[0]);
                list[lip].load(prop);
                ++lip;
            }
        }
        
        if (lip < list.length)
        {
            Profile[] temp = new Profile[lip];
            System.arraycopy(list, 0, temp, 0, lip);
            list = temp;
        }

        return list;
    }
    
    /**
     * Main entry when started from a command line.
     * 
     * @param args_ [0] is the log4j.xml configuration file name, [1] is the SearchTest.xml configuration file name.
     *      See the sample files provided in the source package for further information on content.
     */
    public static void main(String[] args_)
    {
        // Verify the program arguments.
        if (args_.length != 2)
        {
            System.out.println("<program> <log4j.xml> <SearchTest.xml>");
            return;
        }

        // Configure Log4j
        DOMConfigurator.configure(args_[0]);
        
        SearchTest test = new SearchTest();
        
        try
        {
            test.run(args_[1]);
        }
        catch (Exception ex)
        {
            _logger.error(ex.toString(), ex);
        }
    }
    
    private void run(String file_) throws Exception
    {
        Profile[] list = loadProfiles(file_);

        Search.setSchema(list[0].getIndexDSschema());
        
        boolean skipDef = (list.length > 1);
        
        for (Profile prof : list)
        {
            if (prof == null)
                continue;
            if (skipDef && prof._prefix.equals(_default))
                continue;
            skipDef = false;

            _logger.info("");
            _logger.info("Profile: " + prof._prefix);

            if (prof.getCoreUrl() != null)
                _logger.info("Using caCORE API URL: " + prof.getCoreUrl());

            SearchMatch match = prof.getMatchType();
            int limit = prof.getLimit();
            int scores = prof.getScores();
            Search var = new Search(match, limit, scores);

            String remoteURL = prof.getFreestyleUrl();
            boolean remote = prof.getAccess();
            if (remote && remoteURL == null)
            {
               _logger.error("Access is set to \"remote\" but the freestyle.url is missing.");
               continue;
            }

            if (remote)
            {
                var.setDataDescription(remoteURL);
            }
            else
            {
                if (prof.getCoreUrl() != null)
                    var.setCaDsrApiUrl(prof.getCoreUrl(), null);
                var.setIndexDescription(prof.getIndexDSurl(), prof.getIndexDSusername(), prof.getIndexDSpassword());
                var.setDataDescription(prof.getDataDSurl(), prof.getDataDSusername(), prof.getDataDSpassword());
            }

            var.excludeWorkflowStatusRetired(prof.getRestrictResultsByWorkflowNotRetired());
            var.excludeTest(prof.getExcludeTest());
            var.excludeTraining(prof.getExcludeTraining());
            SearchAC[] rlist = prof.getRestricts();
            for (SearchAC temp : rlist)
            {
                if (temp != null)
                    var.restrictResultsByType(temp);
            }
        
            boolean outDef = false;
            boolean outAbbrev = false;
            boolean outAC = false;
            boolean outRS = false;
            boolean outWAC = false;
            
            String restrict = prof.getReturns();
            if (restrict != null)
            {
                String[] vals = restrict.split("[, ]");
                for (int i = 0; i < vals.length; ++i)
                {
                    if (vals[i] != null && vals[i].length() > 0)
                    {
                        switch (Integer.valueOf(vals[i]))
                        {
                        case 1: outAbbrev = true; break;
                        case 2: outAC = true; break;
                        case 3: outRS = true; break;
                        case 4: outWAC = true; break;
                        case 0:
                        default: outDef = true; break;
                        }
                    }
                }
            }
            else
            {
                outDef = true;
            }
    
            // Perform a search on each phrase.
            try
            {
                // Get the search phrase.
                String phrase = prof.getPhrase();
                if (phrase == null)
                {
                    _logger.error("No search phrase provided.");
                    continue;
                }
                _logger.info("Looking for \"" + phrase + "\"");
    
                int cnt;
    
                if (outAbbrev)
                {
                    // Perform search and get Search object results..
                    Vector<SearchResultObject> rs3 = var.findReturningResultSet(phrase);
                    
                    // Output results
                    cnt = 0;
                    for (SearchResultObject ac: rs3)
                    {
                        _logger.info(String.valueOf(cnt + 1) + ": " + ac.getType() + ", " + ac.getIdseq() + ", " + ac.getScore());
                        ++cnt;
                    }
                    _logger.info(cnt + " matches found");
                }
    
                if (outDef)
                {
                    // Perform search and get default results.
                    Vector<String> rs = var.findReturningDefault(phrase);
            
                    // Output results
                    for (cnt = 0; cnt < rs.size(); ++cnt)
                    {
                        _logger.info(String.valueOf(cnt + 1) + ": " + rs.get(cnt));
                    }
                    _logger.info(cnt + " matches found");
                }
    
                if (outRS)
                {
                    // Perform search and get default results.
                    Vector<SearchResults> rs = var.findReturningSearchResults(phrase);
            
                    // Output results
                    for (cnt = 0; cnt < rs.size(); ++cnt)
                    {
                        SearchResults obj = rs.get(cnt);
                        _logger.info(String.valueOf(cnt + 1) + ": ["
                                        + obj.getType() + "] ["
                                        + obj.getLongName() + "] ["
                                        + obj.getPreferredName() + "] ["
                                        + obj.getPublicID() + "] ["
                                        + obj.getVersion() + "] ["
                                        + obj.getPreferredDefinition() + "] ["
                                        + obj.getContextName() + "] ["
                                        + obj.getRegistrationStatus() + "] ["
                                        + obj.getWorkflowStatus() + "] ["
                                        + obj.getObjectClassID() + "] ["
                                        + obj.getObjectClassVersion() + "] ["
                                        + obj.getPropertyID() + "] ["
                                        + obj.getPropertyVersion() + "]");
                    }
                    _logger.info(cnt + " matches found");
                }
    
                if (outAC)
                {
                    // Perform search and get caCORE object results.
                    Vector<AdministeredComponent> rs2 = var.findReturningAdministeredComponent(phrase);
                    
                    // Output results
                    cnt = 0;
                    for (AdministeredComponent ac: rs2)
                    {
                        _logger.info(String.valueOf(cnt + 1) + ": " + ac.getLongName() + "\n" + ac.getPublicID() + " / " + ac.getVersion());
                        ++cnt;
                    }
                    _logger.info(cnt + " matches found");
                }
    
                if (outWAC)
                {
                    // Perform search and get caCORE object results.
                    Vector<SearchResultsWithAC> rs2 = var.findReturningResultsWithAC(phrase);
                    
                    // Output results
                    cnt = 0;
                    for (SearchResultsWithAC ac: rs2)
                    {
                        _logger.info(String.valueOf(cnt + 1) + ": " + ac.getResultObject().getScore() + " : " + ac.getAdministeredComponent().getLongName() + "\n" + ac.getAdministeredComponent().getPublicID() + " / " + ac.getAdministeredComponent().getVersion());
                        ++cnt;
                    }
                    _logger.info(cnt + " matches found");
                }
            }
            catch (SearchException ex)
            {
                _logger.error(ex.toString(), ex);
            }
        }
    }
}
