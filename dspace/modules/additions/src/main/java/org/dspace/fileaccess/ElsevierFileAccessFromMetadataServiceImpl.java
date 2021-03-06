/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.fileaccess;

import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.commons.lang3.*;
import org.apache.log4j.*;
import org.dspace.app.util.*;
import org.dspace.authorize.*;
import org.dspace.authorize.service.*;
import org.dspace.content.*;
import org.dspace.content.factory.*;
import org.dspace.content.service.*;
import org.dspace.core.*;
import org.dspace.eperson.*;
import org.dspace.eperson.service.*;
import org.dspace.fileaccess.service.*;
import org.dspace.importer.external.scidir.entitlement.*;
import org.springframework.beans.factory.annotation.*;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 02 Oct 2015
 */
public class ElsevierFileAccessFromMetadataServiceImpl implements FileAccessFromMetadataService {

    @Autowired(required = true)
    protected ContentServiceFactory serviceFactory;

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    @Autowired(required = true)
    protected GroupService groupService;

    @Autowired(required = true)
    protected BitstreamService bitstreamService;

    /**
     * log4j logger
     */
    private static final Logger log = Logger.getLogger(ElsevierFileAccessFromMetadataServiceImpl.class);

    public void setFileAccess(Context context, Bitstream bitstream, String fileAccess, String startDate) throws SQLException, AuthorizeException {
        DCDate date = new DCDate(startDate);
        setFileAccess(context,bitstream,fileAccess,date);
    }

    public void setFileAccess(Context context, Bitstream bitstream, String fileAccess, DCDate startDate) throws SQLException, AuthorizeException {
        Group group = getGroupAnonymous(context);

        if ("public".equals(fileAccess)) {
            if (group != null) {
                authorizeService.addPolicyOnce(context, bitstream, Constants.READ, group);
            }
        }

        if ("embargo".equals(fileAccess)) {
            if(startDate.toDate()!=null) {
                if (group != null) {
                    authorizeService.removeGroupPolicies(context,bitstream,group);
                    authorizeService.addPolicyOnce(context, bitstream, Constants.READ, group, startDate.toDate());
                }
            }
            else {
                authorizeService.addPolicyOnce(context, bitstream, Constants.READ, group);
            }
        }

        if ("restricted".equals(fileAccess)) {
            authorizeService.removeAllPolicies(context, bitstream);
        }
    }

    private Group getGroupAnonymous(Context context) throws SQLException {
        String groupName = "Anonymous";
        Group group = groupService.findByName(context, groupName);
        if (group == null) {
            log.error("Group not found: " + groupName);
        }
        return group;
    }

    public ArticleAccess getFileAccess(Context context, Bitstream bitstream) throws SQLException {
        ArticleAccess articleAccess = new ArticleAccess();

        String metadata = bitstreamService.getMetadata(bitstream, "workflow.fileaccess");
        String embargoDate = bitstreamService.getMetadata(bitstream, "workflow.fileaccess.date");
        if (StringUtils.isNotBlank(metadata)) {
            articleAccess.setAudience(metadata);
            articleAccess.setStartDate(embargoDate);
        } else {
            articleAccess.setAudience("restricted");
            List<ResourcePolicy> policies = authorizeService.getPoliciesActionFilter(context, bitstream, Constants.READ);

            Group anonymous = getGroupAnonymous(context);

            for (ResourcePolicy policy : policies) {
                if (anonymous.equals(policy.getGroup())){
                    articleAccess.setAudience("public");
                    articleAccess.setStartDate((new DCDate(policy.getStartDate())).toString());
                }
            }

            boolean readAccess = authorizeService.groupActionCheck(context, bitstream, Constants.READ, getGroupAnonymous(context));
            if (readAccess) {
                articleAccess.setAudience("public");
            }
        }

        return articleAccess;
    }

    @Override
    public DCDate getEmbargoDate(HttpServletRequest request) {
        int year = Util.getIntParameter(request, "file-access-date_year");
        int month = Util.getIntParameter(request, "file-access-date_month");
        int day = Util.getIntParameter(request, "file-access-date_day");

        return new DCDate(year, month, day, -1, -1, -1);
    }

    @Override
    public boolean fileAccessIdentical(Context context, Bitstream bitstream) throws SQLException {
        OpenAccessArticleCheck openAccessArticleCheck = OpenAccessArticleCheck.getInstance();

        DSpaceObject parent = bitstreamService.getParentObject(context,bitstream);

        while (parent.getType()!= Constants.ITEM){
            parent = serviceFactory.getDSpaceObjectService(parent).getParentObject(context,parent);
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
