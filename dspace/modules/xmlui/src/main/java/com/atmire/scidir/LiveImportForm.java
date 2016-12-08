package com.atmire.scidir;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.avalon.framework.parameters.*;
import org.apache.cocoon.*;
import org.apache.cocoon.environment.*;
import org.dspace.app.xmlui.cocoon.*;
import org.dspace.app.xmlui.utils.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.authorize.*;
import org.dspace.utils.*;
import org.xml.sax.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 30/09/15
 * Time: 17:26
 */
public class LiveImportForm extends AbstractDSpaceTransformer {

    private static final Message T_DSPACE_HOME                  = message("xmlui.general.dspace_home");
    private static final Message T_trail = message("xmlui.scidir.live-import.trail");
    private static final Message T_head = message("xmlui.scidir.live-import.head");
    private static final Message T_hint = message("xmlui.scidir.live-import.hint");
    private static final Message T_affiliation = message("xmlui.scidir.live-import.affiliation");
    private static final Message T_title = message("xmlui.scidir.live-import.title");
    private static final Message T_author = message("xmlui.scidir.live-import.author");
    private static final Message T_doi = message("xmlui.scidir.live-import.doi");
    private static final Message T_submit = message("xmlui.scidir.live-import.submit");

    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
        pageMeta.addMetadata("title").addContent(T_head);

        pageMeta.addTrailLink(contextPath + "/", T_DSPACE_HOME);
        pageMeta.addTrailLink(null, T_trail);
    }

    @Override
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);

        Request request = ObjectModelHelper.getRequest(objectModel);

        request.getSession().setAttribute("selected",null);
        request.getSession().setAttribute("currentRecords",null);
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException {

        Division div = body.addInteractiveDivision("live-import", contextPath + "/liveimport/result", Division.METHOD_POST, "");
        div.setHead(T_head);

        div.addPara().addContent(T_hint);

        HashMap<String, String> liveImportFields = new DSpace().getServiceManager().getServiceByName("LiveImportFields", HashMap.class);

        List form = div.addList("submit-liveimport", List.TYPE_FORM);

        for (String field : liveImportFields.keySet()) {
            Text text = form.addItem().addText(field);
            text.setLabel(message("xmlui.scidir.live-import." + field));
            text.setHelp(message("xmlui.scidir.live-import." + field + "_hint"));
        }

        form.addItem().addButton("submit").setValue(T_submit);
    }
}
