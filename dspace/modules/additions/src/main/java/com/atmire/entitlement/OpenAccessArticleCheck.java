package com.atmire.entitlement;


/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 29 Sep 2015
 */
public class OpenAccessArticleCheck extends CheckItem {

    public static OpenAccessArticleCheck getInstance() {
        return new OpenAccessArticleCheck();
    }

    public OpenAccessArticleCheck() {
        setUseApiKey(true);
    }

    @Override
    protected String getLinkNodeXPath() {
        return "/hosting-permission-response/document-hosting-permission/link[@rel=\"scidir\"]/@href";
    }

    @Override
    protected String getCheckNodeXPath() {
        return "/hosting-permission-response/document-hosting-permission";
    }

    protected String getUrlConfigKey() {
        return "api.article.url";
    }

}
