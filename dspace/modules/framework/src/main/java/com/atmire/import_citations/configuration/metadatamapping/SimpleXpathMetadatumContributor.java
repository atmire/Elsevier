package com.atmire.import_citations.configuration.metadatamapping;

import com.atmire.import_citations.configuration.metadatamapping.processor.MetadataProcessor;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Metadatum;
import org.jaxen.JaxenException;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 11/01/13
 * Time: 09:21
 */
public class SimpleXpathMetadatumContributor implements MetadataContributor<OMElement> {
    private MetadataField field;

    public Map<String, String> getPrefixToNamespaceMapping() {
        return prefixToNamespaceMapping;
    }

    private MetadataFieldMapping<OMElement,MetadataContributor<OMElement>> metadataFieldMapping;

    public MetadataFieldMapping<OMElement,MetadataContributor<OMElement>> getMetadataFieldMapping() {
        return metadataFieldMapping;
    }

    public void setMetadataFieldMapping(MetadataFieldMapping<OMElement,MetadataContributor<OMElement>> metadataFieldMapping) {
        this.metadataFieldMapping = metadataFieldMapping;
    }

    private List<MetadataProcessor> metadataProcessors = new ArrayList<>();

    public List<MetadataProcessor> getMetadataProcessors() {
        return metadataProcessors;
    }

    public void setMetadataProcessors(List<MetadataProcessor> metadataProcessors) {
        this.metadataProcessors = metadataProcessors;
    }

    @Resource(name="FullprefixMapping")
    public void setPrefixToNamespaceMapping(Map<String, String> prefixToNamespaceMapping) {
        this.prefixToNamespaceMapping = prefixToNamespaceMapping;
    }

    private Map<String,String> prefixToNamespaceMapping;

    public SimpleXpathMetadatumContributor(String query, Map<String, String> prefixToNamespaceMapping, MetadataField field) {
        this.query = query;
        this.prefixToNamespaceMapping = prefixToNamespaceMapping;
        this.field = field;
    }

    public SimpleXpathMetadatumContributor() {

    }

    private String query;

    public MetadataField getField() {
        return field;
    }
    @Required
    public void setField(MetadataField field) {
        this.field = field;
    }

    public String getQuery() {
        return query;
    }
    @Required
    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public Collection<Metadatum> contributeMetadata(OMElement t) {
        List<Metadatum> values=new LinkedList<>();
        try {
            AXIOMXPath xpath=new AXIOMXPath(query);
            for(String ns:prefixToNamespaceMapping.keySet()){
                xpath.addNamespace(prefixToNamespaceMapping.get(ns),ns);
            }
            List<Object> nodes=xpath.selectNodes(t);
            for(Object el:nodes){
                String value=null;
                if(el instanceof OMElement)
                    value= ((OMElement) el).getText();
                else if(el instanceof OMAttribute){
                    value= ((OMAttribute) el).getAttributeValue();
                } else if(el instanceof String){
                    value= (String) el;
                } else if(el instanceof OMText)
                    value= ((OMText) el).getText();
                else
                {
                    System.err.println("node of type: "+el.getClass());
                }
                if(StringUtils.isNotBlank(value)){
                    addRetrievedValueToMetadata(values, value);
                }
            }
            return values;
        } catch (JaxenException e) {
            System.err.println(query);
            throw new RuntimeException(e);
        }

    }

    protected void addRetrievedValueToMetadata(List<Metadatum> values, String value) {

        for (MetadataProcessor processor : getMetadataProcessors()) {
            value = processor.processMetadataValue(value);
        }

        values.add(metadataFieldMapping.toDCValue(field, value));
    }
}
