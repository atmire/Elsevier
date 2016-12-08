package com.atmire.import_citations.configuration.metadatamapping;

import org.dspace.content.Metadatum;

import java.util.Collection;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 11/01/13
 * Time: 09:18
 */
public interface MetadataContributor<RecordType> {

    public void setMetadataFieldMapping(MetadataFieldMapping<RecordType,MetadataContributor<RecordType>> rt);

    public Collection<Metadatum> contributeMetadata(RecordType t);
}
