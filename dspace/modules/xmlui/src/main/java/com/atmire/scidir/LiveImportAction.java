package com.atmire.scidir;

import com.atmire.import_citations.configuration.*;
import com.atmire.import_citations.datamodel.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.avalon.framework.parameters.*;
import org.apache.cocoon.acting.*;
import org.apache.cocoon.environment.*;
import org.apache.cocoon.environment.http.*;
import org.apache.log4j.*;
import org.dspace.app.util.*;
import org.dspace.app.xmlui.utils.*;
import org.dspace.content.Collection;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.handle.*;
import org.dspace.utils.*;
import org.dspace.workflow.*;
import org.dspace.xmlworkflow.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 06/10/15
 * Time: 10:49
 */
public class LiveImportAction extends AbstractAction {
    private String url = ConfigurationManager.getProperty("elsevier-sciencedirect", "api.scidir.url");
    ImportService importService = new DSpace().getServiceManager().getServiceByName(null, ImportService.class);
    private static Logger log = Logger.getLogger(LiveImportAction.class);

    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception {
        Map<String, String> returnValues = new HashMap<String, String>();

        Request request = ObjectModelHelper.getRequest(objectModel);
        org.dspace.core.Context context = ContextUtil.obtainContext(objectModel);
        String buttonPressed = Util.getSubmitButton(request, "");

        if(buttonPressed.equals(LiveImportSelected.CANCEL_BUTTON)){
            ((HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT)).sendRedirect(request.getContextPath() + "/liveimport");
        }

        HashMap<String,SessionRecord> selected = (HashMap<String,SessionRecord>) request.getSession().getAttribute("selected");
        String action = request.getParameter("import-action");
        String collectionHandle = request.getParameter("import-collection");

        Collection collection = (Collection) HandleManager.resolveToObject(context, collectionHandle);

        for (String eid : selected.keySet()) {
            try {
                Record record = importService.getRecord(url, eid);

                if (record != null) {

                    WorkspaceItem wi = WorkspaceItem.create(context, collection, false);
                    Item item = wi.getItem();

                    for (Metadatum metadatum : record.getValueList()) {
                        item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language, metadatum.value);
                    }

                    if (action.equals("workflow")) {
                        if (ConfigurationManager.getProperty("workflow", "workflow.framework").equals("xmlworkflow")) {
                            XmlWorkflowManager.startWithoutNotify(context, wi);
                        } else {
                            WorkflowManager.startWithoutNotify(context, wi);
                        }
                    } else if (action.equals("archive")) {
                        try {
                            InstallItem.installItem(context, wi);
                        } catch (Exception e) {
                            wi.deleteAll();
                            log.error("Exception after install item, try to revert...", e);
                            throw e;
                        }
                    }

                    item.update();
                    context.commit();
                }
            }
            catch (Exception e){
                log.error(e.getMessage(), e);

                returnValues.put("outcome", "failure");
                returnValues.put("message", "xmlui.scidir.live-import-action.failure");
            }
        }

        if(returnValues.size() == 0){
            returnValues.put("outcome", "success");
            returnValues.put("message", "xmlui.scidir.live-import-action.success");
        }

        return returnValues;
    }
}
