package org.dspace.importer.external.metadatamapping.processor;

import org.springframework.beans.factory.annotation.Required;

public class RegexReplaceMetadataProcessor implements MetadataProcessor{

    private String regex;
    private String replacement;

    @Required
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Required
    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    @Override
    public String processMetadataValue(String value) {

        return value.replaceAll(regex, replacement);
    }
}
