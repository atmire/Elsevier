package com.atmire.app.xmlui.aspect.submission.submit;

import com.atmire.import_citations.AbstractImportSource;
import org.dspace.app.xmlui.aspect.submission.AbstractSubmissionStep;
import org.dspace.app.xmlui.aspect.submission.StepAndPage;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.utils.DSpace;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lotte.hofstede at atmire.com
 */
public class SourceChoiceStep extends AbstractSubmissionStep {
    private static final Message T_title =
            message("xmlui.Submission.submit.SourceChoiceStep.title");
    private static final Message T_choose_source =
            message("xmlui.Submission.submit.SourceChoiceStep.choose_source");
    private Map<String, AbstractImportSource> sources = new DSpace().getServiceManager().getServiceByName("ImportServices", HashMap.class);
    protected static final Message T_skip =
            message("xmlui.Submission.general.submission.skip");

    public List addReviewSection(List reviewList) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
        return null;
    }

    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
        Collection collection = submission.getCollection();
        String actionURL = contextPath + "/handle/"+collection.getHandle() + "/submit/" + knot.getId() + ".continue";
        Division div = body.addInteractiveDivision("SourceChoiceStep", actionURL, Division.METHOD_POST, "primary submission");
        div.setHead(T_submission_head);
        addSubmissionProgressList(div);

        List form = div.addList("submit-lookup", List.TYPE_FORM);

        form.setHead(T_title);

        Select select = form.addItem().addSelect("source", "ImportSourceSelect");
        select.addOption("", T_choose_source);
        for (Map.Entry<String, AbstractImportSource> source : sources.entrySet()) {
            select.addOption(source.getKey(), source.getValue().getName());
        }

        String lastSource = submission.getItem().getMetadata("workflow.import.source");
        select.setOptionSelected(lastSource);

        Division statusDivision = div.addDivision("statusDivision");
        List statusList = statusDivision.addList("statusList", List.TYPE_FORM);
        addControlButtons(statusList);
    }

    /**
     * Adds the "<-Previous", "Save/Cancel" and "Next->" buttons
     * to a given form.  This method ensures that the same
     * default control/paging buttons appear on each submission page.
     * <p>
     * Note: A given step may define its own buttons as necessary,
     * and not call this method (since it must be explicitly invoked by
     * the step's addBody() method)
     *
     * @param controls The List which will contain all control buttons
     */
    public void addControlButtons(List controls)
            throws WingException {
        org.dspace.app.xmlui.wing.element.Item actions = controls.addItem();

        // only have "<-Previous" button if not first step
        if (!isFirstStep()) {
            actions.addButton(AbstractProcessingStep.PREVIOUS_BUTTON).setValue(T_previous);
        }

        // always show "Save/Cancel"
        actions.addButton(AbstractProcessingStep.CANCEL_BUTTON).setValue(T_save);

        // If last step, show "Complete Submission"
        if (isLastStep()) {
            actions.addButton(AbstractProcessingStep.NEXT_BUTTON).setValue(T_complete);
        } else // otherwise, show "Next->"
        {
            actions.addButton(com.atmire.submit.step.SourceChoiceStep.CONDITIONAL_NEXT_IMPORT).setValue(T_next);
        }


        actions.addButton(AbstractProcessingStep.PROGRESS_BAR_PREFIX + new StepAndPage(getStep() + 2, 1)).setValue(T_skip);



    }

}
