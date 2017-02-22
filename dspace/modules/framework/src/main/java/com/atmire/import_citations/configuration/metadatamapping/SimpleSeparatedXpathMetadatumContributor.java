package com.atmire.import_citations.configuration.metadatamapping;

import org.apache.axiom.om.OMElement;
import org.dspace.content.Metadatum;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jonas - jonas@atmire.com on 20/05/16.
 */
public class SimpleSeparatedXpathMetadatumContributor extends SimpleXpathMetadatumContributor implements MetadataContributor<OMElement> {

    private String separator;

    public String getSeparator() {
        return separator;
    }

    @Required
    public void setSeparator(String separator) {
        this.separator = Pattern.quote(separator);
    }


    @Override
    protected void addRetrievedValueToMetadata(List<Metadatum> values, String value) {
        String[] separatedValues= value.split(separator);
        for(String valuePart: separatedValues){
            values.add(getMetadataFieldMapping().toDCValue(getField(), valuePart));
        }
    }
}
