package com.atmire.scidir;

import com.atmire.import_citations.AbstractImportSource;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.utils.DSpace;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lotte.hofstede at atmire.com
 */
public class ImportSourceForm extends AbstractDSpaceTransformer {
    private static final Message T_DSPACE_HOME                  = message("xmlui.general.dspace_home");
    private static final Message T_trail = message("xmlui.scidir.import-source.trail");
    private static final Message T_head = message("xmlui.scidir.import-source.head");
    private static final Message T_title = message("xmlui.scidir.import-source.title");
    private static final Message T_submit = message("xmlui.scidir.import-source.submit");

    protected static final Message T_select_help =
            message("xmlui.Submission.submit.LiveImportStep.select_help");

    private Map<String, AbstractImportSource> sources = new DSpace().getServiceManager().getServiceByName("ImportServices", HashMap.class);

    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
        pageMeta.addMetadata("title").addContent(T_title);

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

        Division div = body.addInteractiveDivision("import-source", contextPath + "/liveimport/search", Division.METHOD_POST, "import-source-division");
        div.setHead(T_head);

        List form = div.addList("submit-lookup", List.TYPE_FORM);

        form.addItem().addContent(T_select_help);

        Select select = form.addItem().addSelect("source", "ImportSourceSelect");
        for (Map.Entry<String, AbstractImportSource> source : sources.entrySet()) {
            select.addOption(source.getKey(), source.getValue().getName());
        }


        form.addItem().addButton("submit").setValue(T_submit);
    }
}
