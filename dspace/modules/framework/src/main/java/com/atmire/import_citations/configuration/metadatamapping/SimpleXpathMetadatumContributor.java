package com.atmire.import_citations.configuration.metadatamapping;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.xpath.AXIOMXPath;
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
        List<Metadatum> values=new LinkedList<Metadatum>();
        try {
            AXIOMXPath xpath=new AXIOMXPath(query);
            for(String ns:prefixToNamespaceMapping.keySet()){
                xpath.addNamespace(prefixToNamespaceMapping.get(ns),ns);
            }
            List<Object> nodes=xpath.selectNodes(t);
            for(Object el:nodes)
                if(el instanceof OMElement)
                    values.add(metadataFieldMapping.toDCValue(field, ((OMElement) el).getText()));
                else if(el instanceof OMAttribute){
                    values.add(metadataFieldMapping.toDCValue(field, ((OMAttribute) el).getAttributeValue()));
                } else if(el instanceof String){
                    values.add(metadataFieldMapping.toDCValue(field, (String) el));
                } else if(el instanceof OMText)
                    values.add(metadataFieldMapping.toDCValue(field, ((OMText) el).getText()));
                else
                {
                    System.err.println("node of type: "+el.getClass());
                }
            return values;
        } catch (JaxenException e) {
            System.err.println(query);
            throw new RuntimeException(e);
        }

    }
}
