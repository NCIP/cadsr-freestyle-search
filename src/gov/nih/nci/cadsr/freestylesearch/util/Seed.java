// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/Seed.java,v 1.1 2006-06-30 13:46:47 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Seed the freestyle search index tables. The caDSR is scanned for updates using the record date_modified
 * and comparing to the last seed timestamp. Updated records are used to refresh the index tables.
 * 
 * @author lhebel Mar 3, 2006
 */
public class Seed
{
    /**
     * Constructor
     *
     */
    public Seed()
    {
    }
    
    /**
     * Main entry when started from a command line.
     * 
     * @param args_ no valid arguments at this time.
     */
    public static void main(String[] args_)
    {
        long millis = System.currentTimeMillis();

        DOMConfigurator.configure(args_[0]);
        
        Seed var = new Seed();

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
        
        var._dbIndex = prop.getProperty("index.DSurl");
        var._userIndex = prop.getProperty("index.DSusername");
        var._pswdIndex = prop.getProperty("index.DSpassword");
        var._dbData = prop.getProperty("data.DSurl");
        var._userData = prop.getProperty("data.DSusername");
        var._pswdData = prop.getProperty("data.DSpassword");
        
        String schema = prop.getProperty("index.DSschema");
        DBAccess.setSchema(schema);
        
        _logger.info(" ");
        _logger.info("---------------------------------------------------------------------------------");
        _logger.info("Seed started for schema... " + schema.toUpperCase());

        var.parseDatabase(millis);

        _logger.info("Seed ended ...");
    }
    
    /**
     * Parse the database and extract selected tokens.
     * 
     * @param millis_ the marker time for this run in milliseconds 
     *
     */
    public void parseDatabase(long millis_)
    {
        // Connect to database.
        DBAccess data = new DBAccess();
        if (data.open(_dbData, _userData, _pswdData) != 0)
            return;
        DBAccess index = new DBAccess();
        if (index.open(_dbIndex, _userIndex, _pswdIndex) != 0)
        {
            data.close();
            return;
        }

        getLastSeedTimestamp(index, millis_);

        _logger.info("Retrieving changes made since " + _start.toString());
        data.parseDatabase(_start, index);
        index.close();
        data.close();
    }
    
    /**
     * Calculate the time at midnight the previous day. This will be >24 and <48
     * hours in the past.
     * 
     * @return the timestamp for midnight yesterday
     */
    private Timestamp getYesterday()
    {
        Timestamp start;
        long ct = System.currentTimeMillis();
        ct -= 24 * 60 *60 * 1000;
        start = new Timestamp(ct);
        String yesterday = start.toString().split(" ")[0];
        start = Timestamp.valueOf(yesterday + " 00:00:00");

        start = Timestamp.valueOf("1999-12-01 00:00:00");
        return start;
    }

    /**
     * Get the last seed timestamp and set the current timestamp to the value
     * specified.
     * 
     * @param index_ the index table database access object
     * @param millis_ >0 resets the seed timestamp, ==0 does not
     */
    private void getLastSeedTimestamp(DBAccess index_, long millis_)
    {
        _start = index_.getLastSeedTimestamp();
        if (_start == null)
            _start = getYesterday();
        
        if (millis_ > 0)
            index_.setLastSeedTimestamp(millis_);
    }

    private Timestamp _start;
    private String _dbIndex;
    private String _userIndex;
    private String _pswdIndex;
    private String _dbData;
    private String _userData;
    private String _pswdData;
    
    private static final Logger _logger = Logger.getLogger(Seed.class.getName());
}
