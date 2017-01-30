package com.atmire.submit.step;

import com.atmire.import_citations.AbstractImportSource;
import com.atmire.import_citations.configuration.*;
import com.atmire.import_citations.datamodel.*;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.lang.*;
import org.apache.log4j.*;
import org.dspace.app.util.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.submit.*;
import org.dspace.utils.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 01/10/15
 * Time: 15:33
 */
public class LiveImportStep extends AbstractProcessingStep {
    private static Logger log = Logger.getLogger(LiveImportStep.class);
    private Map<String, AbstractImportSource> sources = new DSpace().getServiceManager().getServiceByName("ImportServices", HashMap.class);


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
            ImportService importService = new DSpace().getServiceManager().getServiceByName(null, ImportService.class);
            Item item = subInfo.getSubmissionItem().getItem();

            String importSourceString = item.getMetadata("workflow.import.source");
            AbstractImportSource importSource = sources.get(importSourceString);

            try {
                Record record = importService.getRecord(importSource.getImportSource(), "eid(" + importId + ")");

                for (Metadatum metadatum : item.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY)) {
                    if (!metadatum.getField().equals("workflow.import.source")) {
                        item.clearMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language);
                    }
                }

                for (Metadatum metadatum : record.getValueList()) {
                    item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language, metadatum.value);
                }

                item.update();
                context.commit();
            } catch (SourceException e) {
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
