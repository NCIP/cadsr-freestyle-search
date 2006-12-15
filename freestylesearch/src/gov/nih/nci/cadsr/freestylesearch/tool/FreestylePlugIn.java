// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/tool/FreestylePlugIn.java,v 1.1 2006-06-30 13:46:47 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.tool;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

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
    public void init(ActionServlet arg0, ModuleConfig arg1) throws ServletException
    {
        // Add an audit trail to the log. This will also be important to verify when the
        // Systems team deploys on Stage and Production and when the server is
        // restarted.
        _logger.info(" ");
        _logger.info("Freestyle started ..................................................................");

        // Get the init parameters for accessing the database.
        String stDataSource = arg0.getInitParameter("jbossDataSource");
        _user = arg0.getInitParameter(_DSUSER);
        _pswd = arg0.getInitParameter(_DSPSWD);
        _schema = arg0.getInitParameter(_DSSCHEMA);
 
        // Create database pool
        Context envContext = null;
        try 
        {
            envContext = new InitialContext();
            _ds = (DataSource)envContext.lookup("java:/" + stDataSource);
            if (_ds != null)
            {
                // Only set the context attribute if we can successfully retrieve the datasource
                // from JBoss.
                arg0.getServletContext().setAttribute(_DATASOURCE, this);
                _logger.info("Using JBoss datasource configuration.");
            }
        }
        catch (Exception ex) 
        {
            String stErr = "Error retrieving datasource from JBoss [" + ex.getMessage() + "].";
            _logger.fatal(stErr, ex);
        }
    }

    /**
     * Get the Servlet datasource.
     * 
     * @return the datasource.
     */
    public DataSource getDataSource()
    {
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
    
    private DataSource _ds;
    private String _user;
    private String _pswd;
    private String _schema;

    private static final Logger _logger = Logger.getLogger(FreestylePlugIn.class.getName());
}
