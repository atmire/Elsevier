package com.atmire.import_citations.configuration.metadatamapping.processor;

import org.apache.commons.lang.StringUtils;

/**
 * Removes the last point from an author name, this is required for the SAP lookup
 *
 * User: kevin (kevin at atmire.com)
 * Date: 23/10/12
 * Time: 09:50
 */
public class AuthorMetadataProcessor implements MetadataProcessor{

    @Override
    public String processMetadataValue(String value) {
        String ret=value;
        ret= StringUtils.strip(ret);
        ret= StringUtils.stripEnd(ret, ".");

        return ret;
    }
}
