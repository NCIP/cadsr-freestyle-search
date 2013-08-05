/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2007 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/ui/UpdateIndices.java,v 1.1 2007-02-13 19:35:17 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.ui;

import java.sql.Timestamp;
import gov.nih.nci.cadsr.freestylesearch.tool.DBAccess;
import gov.nih.nci.cadsr.freestylesearch.util.Seed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.apache.struts.Globals;

/**
 * This class executes on the request from the user to update the Freestyle index tables.
 * 
 * @author lhebel
 */
public class UpdateIndices extends Action
{
    /**
     * Allows the incremental index update to run in a background thread.
     * 
     * @author lhebel
     */
    private class SeedThread extends Thread
    {
        /**
         * Constructor
         *
         *@param request_ the HTTP request.
         */
        public SeedThread(HttpServletRequest request_)
        {
            _ds = (FreestylePlugIn) request_.getSession().getServletContext().getAttribute(
                            FreestylePlugIn._DATASOURCE);
        }

        @Override
        public void run()
        {
            // Perform the incremental index update.
            try
            {
                Seed s = new Seed();
                s.setDS(_ds.getDataSource());
                s.setCredentials(_ds.getUser(), _ds.getPswd());
                s.parseDatabase(System.currentTimeMillis());
            }
            catch (Exception ex)
            {
                // Remember no UI, this will happen in background and terminate the thread.
                _logger.error(ex.toString());
            }
            _logger.info("Incremental index update complete.");
        }

        private FreestylePlugIn _ds;
    }
    
    /**
     * 
     */
    public UpdateIndices()
    {
        super();
    }

    /**
     * Action process to Edit an Alert Definition.
     * 
     * @param mapping_
     *        The action map from the struts-config.xml.
     * @param form_
     *        The form bean for the edit.jsp page.
     * @param request_
     *        The servlet request object.
     * @param response_
     *        The servlet response object.
     * @return The action to continue processing.
     */
    @Override
    public ActionForward execute(ActionMapping mapping_, ActionForm form_,
        HttpServletRequest request_, HttpServletResponse response_)
    {
        UpdateIndicesForm form = (UpdateIndicesForm) form_;
        MessageResources msgs = (MessageResources) request_.getSession().getServletContext().getAttribute(Globals.MESSAGES_KEY);

        // Perform an incremental build - just update since the last run.
        if (form.getBuildType().equals(UpdateIndicesForm.BUILDINC))
        {
            // Check for a blackout period.
            long millis = System.currentTimeMillis();
            if (form.getIncBlockStartTS().getTime() < millis && millis < form.getIncBlockEndTS().getTime())
            {
                form.setMsg(msgs.getMessage("error.badtime"));
            }

            // The request must be confirmed to avoid unauthorized requests.
            else if (form.getConfirmBool())
            {
                // Let the process run in the background.
                new SeedThread(request_).start();
                
                // Tell the user the request has been submitted.
                form.setMsg(msgs.getMessage("msg.incBuild"));
                form.setAllowed(false);
            }
            
            // Not confirmed yet.
            else
            {
                form.setMsg(msgs.getMessage("msg.confirmBuild"));
            }
        }
        
        // Perform a full build - wipe out the index and start from scratch
        else if (form.getBuildType().equals(UpdateIndicesForm.BUILDFULL))
        {
            // Check for a blackout period.
            long millis = System.currentTimeMillis();
            if (form.getFullBlockStartTS().getTime() < millis && millis < form.getFullBlockEndTS().getTime())
            {
                form.setMsg(msgs.getMessage("error.badtime"));
            }

            // The request must be confirmed to avoid unauthorized requests.
            else if (form.getConfirmBool())
            {
                DBAccess db = new DBAccess();

                try
                {
                    // Set the last update timestamp to the magic value for a full build.
                    FreestylePlugIn ds = (FreestylePlugIn) request_.getSession().getServletContext().getAttribute(
                                    FreestylePlugIn._DATASOURCE);
                    db.open(ds.getDataSource(), ds.getUser(), ds.getPswd());
                    Timestamp ts = Timestamp.valueOf(UpdateIndicesForm.FULLFLAG);
                    db.setLastSeedTimestamp(ts.getTime());

                    // Inform the user the request has been submitted and will be run at night.
                    form.setMsg(msgs.getMessage("msg.fullBuild"));
                    form.setAllowed(false);
                }
                catch (Exception ex)
                {
                    // Tell the user if anything is amiss.
                    form.setMsg(ex.toString());
                }
                finally
                {
                    // Be sure to close the connection if made.
                    db.close();
                }
            }
            
            // Wasn't confirmed yet.
            else
            {
                form.setMsg(msgs.getMessage("msg.confirmBuild"));
            }
        }
        
        // The user is trying to decide what to do.
        else
        {
            form.setMsg(msgs.getMessage("msg.intro"));
        }
        
        form.setReOpen(true);
        return new ActionForward("/jsp/updateindices.jsp");
    }

    private static final Logger _logger = Logger.getLogger(UpdateIndices.class);
}
