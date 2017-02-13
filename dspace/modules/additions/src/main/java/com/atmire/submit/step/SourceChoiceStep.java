package com.atmire.submit.step;

import com.atmire.import_citations.AbstractImportSource;
import org.apache.commons.lang.StringUtils;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.utils.DSpace;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lotte.hofstede at atmire.com
 */
public class SourceChoiceStep extends AbstractProcessingStep {
    private Map<String, AbstractImportSource> sources = new DSpace().getServiceManager().getServiceByName("ImportServices", HashMap.class);
    public static final String CONDITIONAL_NEXT_IMPORT = "submit_condition_next_import";

    public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo subInfo) throws ServletException, IOException, SQLException, AuthorizeException {
        String source = request.getParameter("source");
        Item item = subInfo.getSubmissionItem().getItem();
        item.clearMetadata("workflow", "import", "source", Item.ANY);
        if (StringUtils.isNotBlank(source) && sources.keySet().contains(source)) {
            item.addMetadata("workflow", "import", "source", null, source);
            item.update();
            context.commit();
        }
        return 0;
    }

    public int getNumberOfPages(HttpServletRequest request, SubmissionInfo subInfo) throws ServletException {
        return 1;
    }
}
