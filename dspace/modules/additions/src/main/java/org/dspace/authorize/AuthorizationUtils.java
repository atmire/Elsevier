//package com.atmire.util;
package org.dspace.authorize;

import java.sql.*;
import java.util.*;
import java.util.Date;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.eperson.*;
import org.dspace.storage.rdbms.*;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 03 Oct 2014
 */
public class AuthorizationUtils {

    public static void addPolicyOnce(Context context, DSpaceObject dSpaceObject, int action, EPerson eperson) throws SQLException, AuthorizeException {
        boolean present = ePersonActionCheck(context, dSpaceObject, action, eperson);
        if (!present) {
            AuthorizeManager.addPolicy(context, dSpaceObject, action, eperson);
        }
    }

    public static void addPolicyOnce(Context context, DSpaceObject dSpaceObject, int action, Group group) throws SQLException, AuthorizeException {
        boolean present = groupActionCheck(context, dSpaceObject, action, group);
        if (!present) {
            AuthorizeManager.addPolicy(context, dSpaceObject, action, group);
        }
    }

    public static boolean ePersonActionCheck(Context context, DSpaceObject dSpaceObject, int action, EPerson eperson) throws SQLException {
        List<ResourcePolicy> ePersonPolicies = getEPersonPolicies(context, eperson, action);
        return resourceAndActionCheck(dSpaceObject, action, ePersonPolicies);
    }

    public static boolean groupActionCheck(Context context, DSpaceObject dSpaceObject, int action, Group group) throws SQLException {
        List<ResourcePolicy> policiesForGroup = AuthorizeManager.getPoliciesForGroup(context, group);
        return resourceAndActionCheck(dSpaceObject, action, policiesForGroup);
    }

    public static boolean resourceAndActionCheck(DSpaceObject dSpaceObject, int action, List<ResourcePolicy> policies) {
        boolean present = false;
        for (ResourcePolicy resourcePolicy : policies) {
            if (dSpaceObject.getType() == resourcePolicy.getResourceType()
                    && resourcePolicy.getResourceID() == dSpaceObject.getID()
                    && resourcePolicy.getAction() == action) {
                present = true;
            }
        }
        return present;
    }

    public static void addPolicyOnce(Context context, DSpaceObject dSpaceObject, int action, Group group, Date startDate) throws SQLException, AuthorizeException {
        boolean present = false;
        List<ResourcePolicy> policiesForGroup = AuthorizeManager.getPoliciesForGroup(context, group);
        for (ResourcePolicy resourcePolicy : policiesForGroup) {
            if (dSpaceObject!=null && dSpaceObject.getType() == resourcePolicy.getResourceType()
                    && resourcePolicy.getResourceID() == dSpaceObject.getID()
                    && resourcePolicy.getAction() == action
                    && ((resourcePolicy.getStartDate()==null && startDate==null)
                    || (resourcePolicy.getStartDate()!=null && resourcePolicy.getStartDate().equals(startDate)))) {
                present = true;
            }
        }
        if (!present) {
            ResourcePolicy resourcePolicy = ResourcePolicy.create(context);
            resourcePolicy.setResource(dSpaceObject);
            resourcePolicy.setAction(action);
            resourcePolicy.setGroup(group);
            resourcePolicy.setRpType(null);
            resourcePolicy.setStartDate(startDate);
            resourcePolicy.update();
            dSpaceObject.updateLastModified();
        }
    }

    /**
     * This method will return all policies an EPerson has on all available items
     */
    public static List<ResourcePolicy> getEPersonPolicies(Context context, EPerson ePerson, int actionId) throws SQLException {

        TableRowIterator tri = DatabaseManager.queryTable(context, "resourcepolicy",
                "SELECT * FROM resourcepolicy WHERE resource_type_id= ? " +
                        "AND eperson_id= ? AND action_id= ? ",
                Constants.ITEM, ePerson.getID(), actionId);

        List<ResourcePolicy> policies = new ArrayList<ResourcePolicy>();

        try {
            while (tri.hasNext()) {
                TableRow row = tri.next();

                // first check the cache (FIXME: is this right?)
                ResourcePolicy cachepolicy = (ResourcePolicy) context.fromCache(
                        ResourcePolicy.class, row.getIntColumn("policy_id"));

                if (cachepolicy != null) {
                    policies.add(cachepolicy);
                } else {
                    policies.add(new ResourcePolicy(context, row));
                }
            }
        } finally {
            if (tri != null) {
                tri.close();
            }
        }

        return policies;
    }
}
