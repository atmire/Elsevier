package com.atmire.import_citations;

import com.atmire.import_citations.configuration.Imports;
import com.atmire.import_citations.configuration.Source;
import com.atmire.import_citations.configuration.metadatamapping.GenerateQueryForItem;
import com.atmire.import_citations.configuration.metadatamapping.MetadataContributor;
import com.atmire.import_citations.configuration.metadatamapping.MetadataFieldMapping;
import com.atmire.import_citations.datamodel.Record;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.dspace.content.Metadatum;
import org.jaxen.JaxenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import java.io.StringReader;
import java.util.*;

/**
 * Created by: Roeland Dillen (roeland at atmire dot com)
 * Date: 29 May 2015
 */
public abstract class AbstractImportSource<RecordType> extends Source implements Imports {
    private GenerateQueryForItem generateQueryForItem = null;
    private MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> metadataFieldMapping;
    private String name;
    private Map<String, String> importFields;
    private String apiKey;
    private String idField;

    public GenerateQueryForItem getGenerateQueryForItem() {
        return generateQueryForItem;
    }


    public String getApiKey() {
        return apiKey;
    }

    @Autowired(required = false)
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Autowired(required = false)
    public void setIdField(String field) {
        this.idField = field;
    }


    @Autowired
    public void setGenerateQueryForItem(GenerateQueryForItem generateQueryForItem) {
        this.generateQueryForItem = generateQueryForItem;
    }

    public MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> getMetadataFieldMapping() {
        return metadataFieldMapping;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getImportFields() {
        return importFields;
    }

    public void setImportFields(Map<String, String> importFields) {
        this.importFields = importFields;
    }

    @Autowired
    public void setMetadataFieldMapping(
            MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> metadataFieldMapping) {
        this.metadataFieldMapping = metadataFieldMapping;
    }

    public Record transformSourceRecords(RecordType rt) {
        return new Record(new LinkedList<Metadatum>(getMetadataFieldMapping().resultToDCValueMapping(rt)));
    }

    protected String getSingleElementValue(String src, String elementName) {
        OMXMLParserWrapper records = OMXMLBuilderFactory.createOMBuilder(new StringReader(src));
        OMElement element = records.getDocumentElement();
        AXIOMXPath xpath = null;
        String value = null;
        try {
            xpath = new AXIOMXPath("//" + elementName);
            xpath.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
            xpath.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
            List<OMElement> recordsList = xpath.selectNodes(element);
            if (!recordsList.isEmpty()) {
                value = recordsList.get(0).getText();
            }
        } catch (JaxenException e) {
            value = null;
        }
        return value;
    }

    protected List<OMElement> splitToRecords(String recordsSrc) {
        OMXMLParserWrapper records = OMXMLBuilderFactory.createOMBuilder(new StringReader(recordsSrc));
        OMElement element = records.getDocumentElement();

        Iterator childElements = element.getChildElements();

        List<OMElement> recordsList = new ArrayList<>();

        while (childElements.hasNext()) {
            OMElement next = (OMElement) childElements.next();

            if (next.getLocalName().equals("entry")) {
                recordsList.add(next);
            }
        }
        return recordsList;
    }


    public String getIdField() {
        return idField;
    }
}
