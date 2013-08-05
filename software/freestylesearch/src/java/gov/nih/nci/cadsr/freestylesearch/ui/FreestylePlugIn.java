/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/FreestylePlugIn.java,v 1.4 2007-12-12 22:58:08 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.ui;

import gov.nih.nci.cadsr.freestylesearch.util.Search;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.MessageResources;

/**
 * This plug-in class provides a Struts application access to the init-param values
 * in the web.xml.
 * 
 * @author lhebel
 *
 */
public class FreestylePlugIn implements PlugIn
{
    /**
     * The plug-in method called when the Struts application is shutdown. This will
     * happen during a deploy to the application server and when the application
     * server is shutdown.
     */
    public void destroy()
    {
        // Want to track the even in the log.
        _logger.info(" ");
        _logger.info("Freestyle stopped ..................................................................");
    }

    /**
     * The plug-in method called when the Struts application is started.
     */
    public void init(ActionServlet servlet_, ModuleConfig module_) throws ServletException
    {
        // Add an audit trail to the log. This will also be important to verify when the
        // Systems team deploys on Stage and Production and when the server is
        // restarted.
        MessageResources msgs = (MessageResources) servlet_.getServletContext().getAttribute(Globals.MESSAGES_KEY);
        String temp = msgs.getMessage(Search._vers);
        _logger.info(" ");
        _logger.info("Freestyle " + temp + " started ..................................................................");

        // Get the init parameters for accessing the database.
        _dataSource = servlet_.getInitParameter("jbossDataSource");
        _user = servlet_.getInitParameter(_DSUSER);
        _pswd = servlet_.getInitParameter(_DSPSWD);
        _schema = servlet_.getInitParameter(_DSSCHEMA);
        servlet_.getServletContext().setAttribute(_DATASOURCE, this);
        _logger.info("Using JBoss datasource configuration. " + _dataSource);
    }

    /**
     * Get the Servlet datasource.
     * 
     * @return the datasource.
     */
    public DataSource getDataSource()
    {
        // Get pool from Application Manager
        Context envContext = null;
        try 
        {
            envContext = new InitialContext();
            _ds = (DataSource)envContext.lookup("java:/" + _dataSource);
            if (_ds == null)
            {
                _logger.error("Context lookup failed for DataSource. " + _dataSource);
            }
        }
        catch (Exception ex) 
        {
            String stErr = "Error retrieving datasource [" + _dataSource + "] from JBoss [" + ex.getMessage() + "].";
            _logger.error(stErr, ex);
            _ds = null;
        }

        return _ds;
    }
    
    /**
     * Get the application default user.
     * 
     * @return the user id.
     */
    public String getUser()
    {
        return _user;
    }
    
    /**
     * Get the application default user password.
     * 
     * @return the password.
     */
    public String getPswd()
    {
        return _pswd;
    }
    
    /**
     * Get the database schema for the index tables.
     * 
     * @return the schema.
     */
    public String getSchema()
    {
        return _schema;
    }
    
    /**
     * The common attribute name for the database access values.
     */
    public static final String _DATASOURCE = "freestyleDataSource";
    
    private static final String _DSUSER = "DSusername";
    private static final String _DSPSWD = "DSpassword";
    private static final String _DSSCHEMA = "DSschema";
    
    private String _dataSource;
    private DataSource _ds;
    private String _user;
    private String _pswd;
    private String _schema;

    private static final Logger _logger = Logger.getLogger(FreestylePlugIn.class.getName());
}
