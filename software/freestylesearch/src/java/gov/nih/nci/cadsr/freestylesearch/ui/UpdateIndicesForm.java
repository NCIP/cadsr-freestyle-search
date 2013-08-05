/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2007 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/UpdateIndicesForm.java,v 1.1 2007-02-13 19:35:17 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.ui;

import java.sql.Timestamp;
import gov.nih.nci.cadsr.freestylesearch.tool.DBAccess;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/**
 * @author lhebel
 *
 */
public class UpdateIndicesForm extends ActionForm
{
    /**
     * Constructor
     */
    public UpdateIndicesForm()
    {
        super();

        _buildType = BUILDUNK;
    }
    
    /**
     * Set the last update stamp
     * 
     * @param val_ the last update timestamp
     */
    public void setLastUpdate(String val_)
    {
        _lastUpdate = val_;
    }
    
    /**
     * Get the last update timestamp
     * 
     * @return the last update timestamp
     */
    public String getLastUpdate()
    {
        return _lastUpdate;
    }
    
    /**
     * Set the flag indicating the index update request is possible and allowed at this time.
     * 
     * @param val_ "Y" if the update is allowed, otherwise it is not.
     */
    public void setAllowed(String val_)
    {
        _allowed = (val_ != null && val_.equals("Y"));
    }
    
    /**
     * Get the flag indicating if the update request is possible and allowed at this time
     * 
     * @return "Y" if the update is allowed, otherwise it is not.
     */
    public String getAllowed()
    {
        return (_allowed) ? "Y" : "N";
    }
    
    /**
     * Set the flag indicating the index update request is possible and allowed at this time.
     * 
     * @param flag_ true if the update is allowed, otherwise it is not.
     */
    public void setAllowed(boolean flag_)
    {
        _allowed = flag_;
    }
    
    /**
     * Get the flag indicating if the update request is possible and allowed at this time
     * 
     * @return true if the update is allowed, otherwise it is not.
     */
    public boolean getAllowedBool()
    {
        return _allowed;
    }
    
    /**
     * Set the on page message to the user.
     * 
     * @param val_ the text for the on page message.
     */
    public void setMsg(String val_)
    {
        _msg = val_;
    }
    
    /**
     * Get the on page message text.
     * 
     * @return the on page message text.
     */
    public String getMsg()
    {
        return _msg;
    }
    
    /**
     * Set the type of index build to perform.
     * 
     * @param val_ the index build type
     */
    public void setBuildType(String val_)
    {
        _buildType = val_;
    }
    
    /**
     * Get the type of index to build.
     * 
     * @return the index build type.
     */
    public String getBuildType()
    {
        return _buildType;
    }
    
    /**
     * Set the blocked full build time range.
     * 
     * @param val_ the time range as text
     */
    public void setBlockedFRange(String val_)
    {
        _blockedFRange = val_;
    }
    
    /**
     * Get the blocked full build time range.
     * 
     * @return the blocked time range
     */
    public String getBlockedFRange()
    {
        return _blockedFRange;
    }
    
    /**
     * Set the blocked incremental build time range.
     * 
     * @param val_ the time range as text
     */
    public void setBlockedIRange(String val_)
    {
        _blockedIRange = val_;
    }
    
    /**
     * Get the blocked incremental build time range.
     * 
     * @return the blocked time range
     */
    public String getBlockedIRange()
    {
        return _blockedIRange;
    }
    
    /**
     * Set the re-open flag, i.e. we are opening the page a second time.
     * 
     * @param val_ "Y" if this is a revisit to the page.
     */
    public void setReOpen(String val_)
    {
        _reOpen = (val_ != null && val_.equals("Y"));
    }
    
    /**
     * Get the re-open flag
     * 
     * @return "Y" if this is a revisit to the page, otherwise "N"
     */
    public String getReOpen()
    {
        return (_reOpen) ? "Y" : "N";
    }
    
    /**
     * Set the revisit flag
     * 
     * @param flag_ true if this is a revisit to the page
     */
    public void setReOpen(boolean flag_)
    {
        _reOpen = flag_;
    }

    /**
     * Get the re-open flag
     * 
     * @return true if this is a revisit to the page
     */
    public boolean getReOpenBool()
    {
        return _reOpen;
    }
    
    /**
     * Set the submit confirmation
     * 
     * @param val_ "Y" to confirm the request, otherwise it is ignored.
     */
    public void setConfirm(String val_)
    {
        _confirm = (val_ != null && val_.equals("Y"));
    }
    
    /**
     * Get the confirm value
     * 
     * @return the confirmation value
     */
    public String getConfirm()
    {
        return (_confirm) ? "Y" : "N";
    }
    
    /**
     * Get the confirm flag
     * 
     * @return true if the submit is confirmed.
     */
    public boolean getConfirmBool()
    {
        return _confirm;
    }
    
    /**
     * Set the user name
     * 
     * @param val_ the user name
     */
    public void setUser(String val_)
    {
        _user = val_;
    }
    
    /**
     * Get the user name
     * 
     * @return the user name
     */
    public String getUser()
    {
        return _user;
    }
    
    /**
     * Set the password
     * 
     * @param val_ the password
     */
    public void setPswd(String val_)
    {
        _pswd = val_;
    }
    
    /**
     * Get the password
     * 
     * @return the password
     */
    public String getPswd()
    {
        return _pswd;
    }

    /**
     * Set the full build blocked start time
     * 
     * @param val_ the time
     */
    public void setFullBlockStart(String val_)
    {
        _fullBlockStart = Timestamp.valueOf(val_);
    }

    /**
     * Get the full build block start time
     * 
     * @return the time
     */
    public String getFullBlockStart()
    {
        return _fullBlockStart.toString();
    }
    
    /**
     * Get the full build block start time
     * 
     * @return the time
     */
    public Timestamp getFullBlockStartTS()
    {
        return _fullBlockStart;
    }

    /**
     * Set the full build blocked end time
     * 
     * @param val_ the time
     */
    public void setFullBlockEnd(String val_)
    {
        _fullBlockEnd = Timestamp.valueOf(val_);
    }
    
    /**
     * Get the full build block end time
     * 
     * @return the time
     */
    public String getFullBlockEnd()
    {
        return _fullBlockEnd.toString();
    }
    
    /**
     * Get the full build block end time
     * 
     * @return the time
     */
    public Timestamp getFullBlockEndTS()
    {
        return _fullBlockEnd;
    }

    /**
     * Set the incremental build blocked start time
     * 
     * @param val_ the time
     */
    public void setIncBlockStart(String val_)
    {
        _incBlockStart = Timestamp.valueOf(val_);
    }
    
    /**
     * Get the incremental build block start time
     * 
     * @return the time
     */
    public String getIncBlockStart()
    {
        return _incBlockStart.toString();
    }
    
    /**
     * Get the incremental build block start time
     * 
     * @return the time
     */
    public Timestamp getIncBlockStartTS()
    {
        return _incBlockStart;
    }

    /**
     * Set the incremental build blocked end time
     * 
     * @param val_ the time
     */
    public void setIncBlockEnd(String val_)
    {
        _incBlockEnd = Timestamp.valueOf(val_);
    }
    
    /**
     * Get the incremental build block end time
     * 
     * @return the time
     */
    public String getIncBlockEnd()
    {
        return _incBlockEnd.toString();
    }
    
    /**
     * Get the incremental build block end time
     * 
     * @return the time
     */
    public Timestamp getIncBlockEndTS()
    {
        return _incBlockEnd;
    }
    
    /**
     * Validate the content of the Edit Screen.
     * 
     * @param mapping_
     *        The action map defined for Edit.
     * @param request_
     *        The servlet request object.
     * @return Any errors found.
     */
    public ActionErrors validate(ActionMapping mapping_,
        HttpServletRequest request_)
    {
        ActionErrors errors = new ActionErrors();
        
        FreestylePlugIn ds = (FreestylePlugIn) request_.getSession().getServletContext().getAttribute(
                        FreestylePlugIn._DATASOURCE);
        String user = ds.getUser();
        String pswd = ds.getPswd();

        // Our first visit to the page.
        if (!_reOpen)
        {
            // Load everything needed from the database.
            DBAccess db = new DBAccess();
            try
            {
                db.open(ds.getDataSource(), user, pswd);
                _lastUpdate = db.getLastSeedTimestamp().toString();
                String tz = db.getIndexTZ();
                _fullBlockStart = db.getIndexScheduledStart();
                _fullBlockEnd = db.getIndexScheduledEnd();
                _incBlockStart = db.getIndexBlockedStart();
                _incBlockEnd = db.getIndexBlockedEnd();
                _blockedFRange = _fullBlockStart.toString().substring(11, 16) + " to " + _fullBlockEnd.toString().substring(11, 16) + " " + tz + " (24 hour time format)";
                _blockedIRange = _incBlockStart.toString().substring(11, 16) + " to " + _incBlockEnd.toString().substring(11, 16) + " " + tz + " (24 hour time format)";
                _allowed = !_lastUpdate.equals(FULLFLAG);

                // We can't take requests right now.
                if (!_allowed)
                {
                    _msg = "";
                    errors.add("msg.fullBuildWaiting", new ActionMessage("msg.fullBuildWaiting"));
                }
            }
            catch (Exception ex)
            {
                _msg = ex.toString();
                errors.add("error.exception", new ActionMessage("error.exception"));
            }
            finally
            {
                db.close();
            }
        }
        else
        {
            // You must know the user and password for freestyle to make a request.
            if (!_user.equals(user) || !_pswd.equals(pswd))
            {
                _msg = "";
                errors.add("error.baduser", new ActionMessage("error.baduser"));
            }
        }
        
        // Always clear the password.
        _pswd = "";

        return errors;
    }
    
    private String _lastUpdate;
    private boolean _allowed;
    private String _msg;
    private String _buildType;
    private String _blockedFRange;
    private String _blockedIRange;
    private boolean _reOpen;
    private boolean _confirm;
    private String _user;
    private String _pswd;
    private Timestamp _fullBlockStart;
    private Timestamp _fullBlockEnd;
    private Timestamp _incBlockStart;
    private Timestamp _incBlockEnd;

    private static final long serialVersionUID = 28107324057052880L;
    
    /**
     * 
     */
    public static final String BUILDUNK = "U";

    /**
     * 
     */
    public static final String BUILDFULL = "F";

    /**
     * 
     */
    public static final String BUILDINC = "I";
    
    /**
     * 
     */
    public static final String BUILDTYPE = "buildType";
    
    /**
     * 
     */
    public static final String FORMNAME = "updateForm";

    /**
     * 
     */
    public static final String FULLFLAG = "1999-01-01 00:00:00.0";

}
