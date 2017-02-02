/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative.importer.external.scidir;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.avalon.framework.parameters.*;
import org.apache.cocoon.*;
import org.apache.cocoon.environment.*;
import org.apache.commons.lang3.StringUtils;
import org.dspace.app.xmlui.cocoon.*;
import org.dspace.app.xmlui.utils.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.authorize.*;
import org.dspace.importer.external.scidir.ScidirImportSourceServiceImpl;
import org.dspace.importer.external.service.AbstractImportMetadataSourceService;
import org.dspace.utils.*;
import org.xml.sax.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 30/09/15
 * Time: 17:26
 */
public class LiveImportForm extends AbstractDSpaceTransformer {

    private static final Message T_DSPACE_HOME = message("xmlui.general.dspace_home");
    private static final Message T_trail = message("xmlui.scidir.live-import.trail");
    private static final Message T_head = message("xmlui.scidir.live-import.head");
    private static final Message T_hint = message("xmlui.scidir.live-import.hint");
    private static final Message T_submit = message("xmlui.scidir.live-import.submit");
    protected static final Message T_lookup_help = message("xmlui.Submission.submit.LiveImportStep.lookup_help");
    private Map<String, AbstractImportMetadataSourceService> sources = new DSpace().getServiceManager().getServiceByName("ImportServices", HashMap.class);

    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException {
        pageMeta.addMetadata("title").addContent(T_head);

        pageMeta.addTrailLink(contextPath + "/", T_DSPACE_HOME);
        pageMeta.addTrailLink(null, T_trail);
    }

    @Override
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);

        Request request = ObjectModelHelper.getRequest(objectModel);

        request.getSession().setAttribute("selected", null);
        request.getSession().setAttribute("currentRecords", null);
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException {

        Request request = ObjectModelHelper.getRequest(objectModel);
        Division div = body.addInteractiveDivision("live-import", contextPath + "/liveimport/result", Division.METHOD_POST, "");
        div.setHead(T_head);

        div.addPara().addContent(T_hint);


        List form = div.addList("submit-liveimport", List.TYPE_FORM);

        String importSourceString = request.getParameter("source");

        if (StringUtils.isBlank(importSourceString)) {

            importSourceString = request.getSession(true).getAttribute("source").toString();

        }

        if (StringUtils.isNotBlank(importSourceString)) {

            form.addItem().addContent(T_lookup_help);
        }

        AbstractImportMetadataSourceService importSource = sources.get(importSourceString);


        if (importSource != null) {

            Map<String, String> fields = importSource.getImportFields();

            for (String field : fields.keySet()) {

                Text text = form.addItem().addText(field);

                text.setLabel(message("xmlui.scidir.live-import." + field));

                text.setHelp(message("xmlui.scidir.live-import." + field + "_hint"));


                if (StringUtils.isNotBlank(request.getParameter(field))) {

                    text.setValue(request.getParameter(field));

                }

            }

            request.getSession(true).removeAttribute("source");

            request.getSession().setAttribute("source", importSourceString);


        }

        form.addItem().addButton("submit-search").setValue(T_submit);

    }
}
