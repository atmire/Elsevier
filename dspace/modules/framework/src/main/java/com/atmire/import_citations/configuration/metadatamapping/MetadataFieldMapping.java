package com.atmire.import_citations.configuration.metadatamapping;

import org.dspace.content.Metadatum;

import java.util.Collection;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 18/09/12
 * Time: 14:41
 */

public interface  MetadataFieldMapping<RecordType,QueryType> {

        public Metadatum toDCValue(MetadataField field,String mf);

        public Collection<Metadatum> resultToDCValueMapping(RecordType record);



}
