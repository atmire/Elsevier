/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */

package org.dspace.importer.external.service;

import java.io.StringReader;
import java.util.*;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.log4j.Logger;
import org.dspace.importer.external.datamodel.*;
import org.dspace.importer.external.metadatamapping.*;
import org.dspace.importer.external.metadatamapping.contributor.*;
import org.dspace.importer.external.metadatamapping.transform.*;
import org.dspace.importer.external.scidir.GenerateQueryForItem;
import org.dspace.importer.external.service.components.*;
import org.jaxen.JaxenException;
import org.springframework.beans.factory.annotation.Autowired;
import javax.xml.stream.XMLStreamException;

/**
 * This class is a partial implementation of {@link MetadataSource}. It provides assistance with mapping metadata from source format to DSpace format.
 * AbstractImportSourceService has a generic type set 'RecordType'.
 * In the importer implementation this type set should be the class of the records received from the remote source's response.
 *
 * @author Roeland Dillen (roeland at atmire dot com)
 *
 */
public abstract class AbstractImportMetadataSourceService<RecordType> extends AbstractRemoteMetadataSource implements MetadataSource {
    protected GenerateQueryService generateQueryForItem = null;
    private MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> metadataFieldMapping;
    private String name;
    private Map<String, String> importFields;
    private String apiKey;
    private String idField;

    Logger log = Logger.getLogger(AbstractImportMetadataSourceService.class);

    public AbstractImportMetadataSourceService(GenerateQueryService generateQueryService, MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> metadataFieldMapping) {
        this.generateQueryForItem = generateQueryService;
        this.metadataFieldMapping = metadataFieldMapping;
    }

    protected AbstractImportMetadataSourceService() {
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


    /**
     * Retrieve the MetadataFieldMapping containing the mapping between RecordType and Metadata
     * @return The configured MetadataFieldMapping
     */
    public MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> getMetadataFieldMapping() {
        return metadataFieldMapping;
    }

    /**
     * Sets the MetadataFieldMapping to base the mapping of RecordType and
     * @param metadataFieldMapping
     */
    @Autowired
    public void setMetadataFieldMapping(
            MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> metadataFieldMapping) {
        this.metadataFieldMapping = metadataFieldMapping;
    }

    /**
     *  Return an ImportRecord constructed from the results in a RecordType
     * @param recordType The recordtype to retrieve the DCValueMapping from
     * @return An {@link ImportRecord}, This is based on the results retrieved from the recordTypeMapping
     */
    public ImportRecord transformSourceRecords(RecordType recordType) {
        return new ImportRecord(new LinkedList<>(getMetadataFieldMapping().resultToDCValueMapping(recordType)));
    }

    protected String getSingleElementValue(String src, String elementName) {
        String value = null;
        try {
            OMElement element = AXIOMUtil.stringToOM(src);
            AXIOMXPath xpath = new AXIOMXPath("//" + elementName);
            xpath.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
            xpath.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
            List<OMElement> recordsList = xpath.selectNodes(element);
            if (!recordsList.isEmpty()) {
                value = recordsList.get(0).getText();
            }
        } catch (JaxenException e) {
            value = null;
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
            value = null;
        }
        return value;
    }

    protected List<OMElement> splitToRecords(String recordsSrc) {


        List<OMElement> recordsList = new ArrayList<>();
        try {

            OMElement element = AXIOMUtil.stringToOM(recordsSrc);

            Iterator childElements = element.getChildElements();
            while (childElements.hasNext()) {
                OMElement next = (OMElement) childElements.next();

                if (next.getLocalName().equals("entry")) {
                    recordsList.add(next);
                }
            }
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
        }
        return recordsList;
    }


    public String getIdField() {
        return idField;
    }
}
