/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative.plugins;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.avalon.framework.parameters.*;
import org.apache.cocoon.*;
import org.apache.cocoon.environment.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.Context;
import org.xml.sax.*;


/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 26/10/15
 * Time: 16:53
 */
public abstract class EditBitstreamFormAddition {

    protected int dsoType;
    protected String key;

    protected Parameters parameters;
    protected HashMap<String,String> errorMap ;


    public void setupHook(SourceResolver resolver, Map objectModel, String src,
                          Parameters parameters) throws ProcessingException, SAXException,
            IOException
    {
        this.parameters = parameters;
        String errorString = parameters.getParameter("errors", "");
        errorMap = new HashMap<String,String>();
        String[] errors = errorString.split(",");
        for (String error : errors) {
            //System.out.println(errorString);
            String[] errorPieces = error.split(":",2);

            if (errorPieces.length > 1)
            {
                errorMap.put(errorPieces[0], errorPieces[1]);
            }
            else
            {
                errorMap.put(errorPieces[0], errorPieces[0]);
            }
        }
    }

    public abstract void addBodyHook(Context context, DSpaceObject dSpaceObject, List main) throws WingException, SQLException, AuthorizeException;

    public int getDsoType() {
        return dsoType;
    }

    public void setDsoType(int dsoType) {
        this.dsoType = dsoType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
