package com.atmire.import_citations.configuration.metadatamapping;


import com.atmire.import_citations.configuration.metadatamapping.processor.MetadataProcessor;
import org.apache.log4j.Logger;
import org.dspace.content.Metadatum;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 19/09/12
 * Time: 10:09
 */
public abstract class AbstractMetadataFieldMapping<RecordType> implements MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> {

    private Map<MetadataField, MetadataContributor<RecordType>> metadataFieldMap;

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(AbstractMetadataFieldMapping.class);
//    public Map<MetadataContributor<RecordType>, MetadataField> getMetadataFieldReverseMap() {
//        return metadataFieldReverseMap;
//    }

    private Map<MetadataField, MetadataProcessor> metadataProcessorMap;

    public void setMetadataProcessorMap(Map<MetadataField, MetadataProcessor> metadataProcessorMap)
    {
        this.metadataProcessorMap = metadataProcessorMap;
    }

    public MetadataProcessor getMetadataProcessor(MetadataField metadataField)
    {
        if(metadataProcessorMap != null)
        {
            return metadataProcessorMap.get(metadataField);
        }else{
            return null;
        }
    }

    public Metadatum toDCValue(MetadataField field, String value) {
        Metadatum dcValue = new Metadatum();



        if (field == null) return null;
        MetadataProcessor metadataProcessor = getMetadataProcessor(field);
        if(metadataProcessor != null)
        {
            value = metadataProcessor.processMetadataValue(value);
        }
        dcValue.value = value;
        dcValue.element = field.getElement();
        dcValue.qualifier = field.getQualifier();
        dcValue.schema = field.getSchema();
        return dcValue;
    }

    //private Map<MetadataContributor<RecordType>, MetadataField> metadataFieldReverseMap = new HashMap<MetadataContributor<RecordType>, MetadataField>();

//    public void setMetadataFieldReverseMap(Map<String, MetadataField> metadataFieldReverseMap) {
//        this.metadataFieldReverseMap = metadataFieldReverseMap;
//    }

    private boolean reverseDifferent = false;

    private String AND = "AND";
    private String OR = "OR";
    private String NOT = "NOT";

    public String getAND() {
        return AND;
    }

    public void setAND(String AND) {
        this.AND = AND;
    }

    public String getOR() {
        return OR;
    }

    public void setOR(String OR) {
        this.OR = OR;
    }

    public String getNOT() {
        return NOT;
    }

    public void setNOT(String NOT) {
        this.NOT = NOT;
    }

    public Map<MetadataField, MetadataContributor<RecordType>> getMetadataFieldMap() {
        return metadataFieldMap;
    }

    public void setMetadataFieldMap(Map<MetadataField, MetadataContributor<RecordType>> metadataFieldMap) {
        this.metadataFieldMap = metadataFieldMap;
        for(MetadataContributor<RecordType> mc:metadataFieldMap.values()){
            mc.setMetadataFieldMapping(this);
        }

    }


    @Override
    public Collection<Metadatum> resultToDCValueMapping(RecordType record) {
        List<Metadatum> values=new LinkedList<Metadatum>();


        for(MetadataContributor<RecordType> query:getMetadataFieldMap().values()){
            try {
                values.addAll(query.contributeMetadata(record));
            } catch (Exception e) {
                log.error("Error", e);
            }

        }
        return values;

    }
}
