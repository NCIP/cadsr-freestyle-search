// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/test/SearchTest.java,v 1.3 2006-07-10 18:40:32 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.test;

import gov.nih.nci.cadsr.domain.AdministeredComponent;
import gov.nih.nci.cadsr.freestylesearch.util.Search;
import gov.nih.nci.cadsr.freestylesearch.util.SearchAC;
import gov.nih.nci.cadsr.freestylesearch.util.SearchMatch;
import gov.nih.nci.cadsr.freestylesearch.util.SearchResultSet;

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

        // Read the test properties.
        Properties prop = new Properties();
        try
        {
            FileInputStream ins = new FileInputStream(args_[1]);
            prop.loadFromXML(ins);
            ins.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return;
        }
        catch (InvalidPropertiesFormatException e)
        {
            e.printStackTrace();
            return;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        
        SearchMatch match = SearchMatch.valueOf(Integer.valueOf(prop.getProperty("matchType")));
        int limit = Integer.valueOf(prop.getProperty("limit"));
        int scores = Integer.valueOf(prop.getProperty("scores"));
        String indexUrl = prop.getProperty("index.DSurl");
        String indexUser = prop.getProperty("index.DSusername");
        String indexPswd = prop.getProperty("index.DSpassword");
        String dataUrl = prop.getProperty("data.DSurl");
        String dataUser = prop.getProperty("data.DSusername");
        String dataPswd = prop.getProperty("data.DSpassword");
        
        String schema = prop.getProperty("index.DSschema");
        Search.setSchema(schema);
        
        // Create the search object and set configuration options
        Search var = new Search(match, limit, scores);
        var.setIndexDescription(indexUrl, indexUser, indexPswd);
        var.setDataDescription(dataUrl, dataUser, dataPswd);
        
        String restrictWFS = prop.getProperty("restrictResultsByWorkflowNotRetired");
        if (restrictWFS != null && restrictWFS.equals("true"))
            var.restrictResultsByWorkflowNotRetired();
        else
            var.resetResultsByWorkflowNotRetired();

        String[] vals;
        String restrict = prop.getProperty("restricts");
        if (restrict != null)
        {
            vals = restrict.split("[, ]");
            for (int i = 0; i < vals.length; ++i)
            {
                if (vals[i] != null && vals[i].length() > 0)
                    var.restrictResultsByType(SearchAC.valueOf(Integer.valueOf(vals[i])));
            }
        }
        
        boolean outDef = false;
        boolean outAbbrev = false;
        boolean outAC = false;
        
        restrict = prop.getProperty("returns");
        if (restrict != null)
        {
            vals = restrict.split("[, ]");
            for (int i = 0; i < vals.length; ++i)
            {
                if (vals[i] != null && vals[i].length() > 0)
                {
                    switch (Integer.valueOf(vals[i]))
                    {
                    case 1: outAbbrev = true; break;
                    case 2: outAC = true; break;
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
        for (int i = 0; true; ++i)
        {
            // Get the search phrase.
            String phrase = prop.getProperty("terms." + i);
            if (phrase == null)
                break;
            _logger.info("Looking for \"" + phrase + "\"");

            int cnt;

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

            if (outAbbrev)
            {
                // Perform search and get Search object results..
                Vector<SearchResultSet> rs3 = var.findReturningResultSet(phrase);
                
                // Output results
                cnt = 0;
                for (SearchResultSet ac: rs3)
                {
                    _logger.info(String.valueOf(cnt + 1) + ": " + ac.getType() + ", " + ac.getIdseq() + ", " + ac.getScore());
                    ++cnt;
                }
                _logger.info(cnt + " matches found");
            }
        }
    }

    private static final Logger _logger = Logger.getLogger(Search.class.getName());
}
