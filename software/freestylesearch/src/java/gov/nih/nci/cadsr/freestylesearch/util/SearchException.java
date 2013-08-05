/*L
 * Copyright ScenPro Inc, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-freestyle-search/LICENSE.txt for details.
 */

// Copyright (c) 2007 ScenPro, Inc.

// $Header: /share/content/gforge/freestylesearch/freestylesearch/src/gov/nih/nci/cadsr/freestylesearch/util/SearchException.java,v 1.1 2007-01-25 20:24:07 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.freestylesearch.util;

/**
 * The package specific exception thrown by Freestyle
 * 
 * @author lhebel
 *
 */
public class SearchException extends RuntimeException
{
    /**
     * Constructor 
     */
    public SearchException()
    {
        super();
    }

    /**
     * Constructor 
     * 
     * @param message the exception text
     */
    public SearchException(String message)
    {
        super(message);
    }

    /**
     * Constructor 
     * 
     * @param message the exception text
     * @param cause the root cause
     */
    public SearchException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor 
     * 
     * @param cause the root cause
     */
    public SearchException(Throwable cause)
    {
        super(cause);
    }
    
    /**
     * Pass to super class.
     */
    @Override
    public String toString()
    {
        return super.toString();
    }

    private static final long serialVersionUID = -3328117770407419890L;
}
