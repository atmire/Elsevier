/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.step;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.lang.*;
import org.apache.log4j.*;
import org.dspace.app.util.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.content.factory.*;
import org.dspace.content.service.*;
import org.dspace.core.*;
import org.dspace.importer.external.datamodel.*;
import org.dspace.importer.external.exception.*;
import org.dspace.importer.external.metadatamapping.*;
import org.dspace.importer.external.service.*;
import org.dspace.submit.*;
import org.dspace.utils.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 01/10/15
 * Time: 15:33
 */
public class LiveImportStep extends AbstractProcessingStep {
    private String url = ConfigurationManager.getProperty("elsevier-sciencedirect.api.scidir.url");
    private static Logger log = Logger.getLogger(LiveImportStep.class);

    protected static ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    @Override
    public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo subInfo) throws ServletException, IOException, SQLException, AuthorizeException {
        String importId = request.getParameter("import_id");

        if(StringUtils.isBlank(importId)){
            String buttonPressed = Util.getSubmitButton(request, "");

            if(buttonPressed.startsWith("submit-import-")){
                importId = buttonPressed.substring("record-import-".length());
            }
        }

        if (StringUtils.isNotBlank(importId)) {
            ImportService importService = new DSpace().getServiceManager().getServiceByName("importService", ImportService.class);
            Item item = subInfo.getSubmissionItem().getItem();
            try {
                ImportRecord record = importService.getRecord(url, "eid(" + importId + ")");

                itemService.clearMetadata(context,item, Item.ANY, Item.ANY, Item.ANY, Item.ANY);

                for (MetadatumDTO metadatum : record.getValueList()) {
                    itemService.addMetadata(context, item, metadatum.getSchema(), metadatum.getElement(), metadatum.getQualifier(), metadatum.getLanguage(), metadatum.getValue());
                }

                itemService.update(context,item);
                context.dispatchEvents();
            } catch (MetadataSourceException e) {
                log.error(e.getMessage(), e);
            }
        }
        return 0;
    }

    @Override
    public int getNumberOfPages(HttpServletRequest request, SubmissionInfo subInfo) throws ServletException {
        return 1;
    }
}
