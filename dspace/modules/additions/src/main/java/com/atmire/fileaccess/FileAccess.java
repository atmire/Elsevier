package com.atmire.fileaccess;

import com.atmire.entitlement.*;
import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.commons.lang3.*;
import org.apache.log4j.*;
import org.dspace.app.util.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.eperson.*;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 02 Oct 2015
 */
public class FileAccess {

    /**
     * log4j logger
     */
    private static final Logger log = Logger.getLogger(FileAccess.class);

    public static void setFileAccess(Context context, Bitstream bitstream, String fileAccess, String startDate) throws SQLException, AuthorizeException {
        DCDate date = new DCDate(startDate);
        setFileAccess(context,bitstream,fileAccess,date);
    }

    public static void setFileAccess(Context context, Bitstream bitstream, String fileAccess, DCDate startDate) throws SQLException, AuthorizeException {
        Group group = getGroupAnonymous(context);

        if ("public".equals(fileAccess)) {
            if (group != null) {
                AuthorizationUtils.addPolicyOnce(context, bitstream, Constants.READ, group);
            }
        }

        if ("embargo".equals(fileAccess)) {
            if(startDate.toDate()!=null) {
                if (group != null) {
                    AuthorizeManager.removeGroupPolicies(context,bitstream,group);
                    AuthorizationUtils.addPolicyOnce(context, bitstream, Constants.READ, group, startDate.toDate());
                }
            }
            else {
                AuthorizationUtils.addPolicyOnce(context, bitstream, Constants.READ, group);
            }
        }

        if ("restricted".equals(fileAccess)) {
            AuthorizeManager.removeAllPolicies(context, bitstream);
        }
    }

    private static Group getGroupAnonymous(Context context) throws SQLException {
        String groupName = "Anonymous";
        Group group = Group.findByName(context, groupName);
        if (group == null) {
            log.error("Group not found: " + groupName);
        }
        return group;
    }

    public static ArticleAccess getFileAccess(Context context, Bitstream bitstream) throws SQLException {
        ArticleAccess articleAccess = new ArticleAccess();

        String metadata = bitstream.getMetadata("workflow.fileaccess");
        String embargoDate = bitstream.getMetadata("workflow.fileaccess.date");
        if (StringUtils.isNotBlank(metadata)) {
            articleAccess.setAudience(metadata);
            articleAccess.setStartDate(embargoDate);
        } else {
            articleAccess.setAudience("restricted");
            List<ResourcePolicy> policies = AuthorizeManager.getPoliciesActionFilter(context, bitstream, Constants.READ);

            Group anonymous = getGroupAnonymous(context);

            for (ResourcePolicy policy : policies) {
                if (anonymous.equals(policy.getGroup())){
                    articleAccess.setAudience("public");
                    articleAccess.setStartDate((new DCDate(policy.getStartDate())).toString());
                }
            }

            boolean readAccess = AuthorizationUtils.groupActionCheck(context, bitstream, Constants.READ, getGroupAnonymous(context));
            if (readAccess) {
                articleAccess.setAudience("public");
            }
        }

        return articleAccess;
    }

    public static DCDate getEmbargoDate(HttpServletRequest request){
        int year = Util.getIntParameter(request, "file-access-date_year");
        int month = Util.getIntParameter(request, "file-access-date_month");
        int day = Util.getIntParameter(request, "file-access-date_day");

        return new DCDate(year, month, day, -1, -1, -1);
    }

    // check if the file access for the artile in Elsevier is identical to the file access for this bitstream
    public static boolean fileAccessIdentical(Context context, Bitstream bitstream) throws SQLException {
        OpenAccessArticleCheck openAccessArticleCheck = OpenAccessArticleCheck.getInstance();

        DSpaceObject parent = bitstream.getParentObject();

        while (parent.getType()!= Constants.ITEM){
            parent = parent.getParentObject();
        }

        ArticleAccess originalFileAccess = openAccessArticleCheck.check((Item) parent);
        ArticleAccess fileAccess = getFileAccess(context, bitstream);

        if(StringUtils.equals(originalFileAccess.getStartDate(),fileAccess.getStartDate())){
            return true;
        }
        else if(StringUtils.isNotBlank(originalFileAccess.getAudience()) && StringUtils.equals(originalFileAccess.getAudience(),fileAccess.getAudience())){
            return true;
        }

        return false;
    }


}
