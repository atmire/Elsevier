package com.atmire.entitlement;

import java.util.*;
import org.apache.log4j.*;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 29 Sep 2015
 */
public class EntitlementCheckItem extends CheckItem {

    /**
     * log4j logger
     */
    private static final Logger log = Logger.getLogger(EntitlementCheckItem.class);

    public static EntitlementCheckItem getInstance() {
        return new EntitlementCheckItem();
    }

    private EntitlementCheckItem() {
        HashSet<String> trueValues = new HashSet<String>();
        trueValues.add("true");
        trueValues.add("open_access");
        setTrueValues(trueValues);
    }

    protected String getUrlConfigKey() {
        return "api.entitlement.url";
    }

    protected String getCheckNodeXPath() {
        return "/entitlement-response/document-entitlement/entitled";
    }

    protected String getLinkNodeXPath() {
        return "/entitlement-response/document-entitlement/link[@rel=\"scidir\"]/@href";
    }

}
