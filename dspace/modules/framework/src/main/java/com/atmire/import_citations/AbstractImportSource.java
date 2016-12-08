package com.atmire.import_citations;

import com.atmire.import_citations.configuration.Imports;
import com.atmire.import_citations.configuration.Source;
import com.atmire.import_citations.configuration.metadatamapping.GenerateQueryForItem;
import com.atmire.import_citations.configuration.metadatamapping.MetadataContributor;
import com.atmire.import_citations.configuration.metadatamapping.MetadataFieldMapping;
import com.atmire.import_citations.datamodel.Record;
import org.dspace.content.Metadatum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import java.util.LinkedList;

/**
 * Created by: Roeland Dillen (roeland at atmire dot com)
 * Date: 29 May 2015
 */
public abstract class AbstractImportSource<RecordType> extends Source implements Imports{
	private GenerateQueryForItem generateQueryForItem = null;
	private MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> metadataFieldMapping;

	public GenerateQueryForItem getGenerateQueryForItem() {
		return generateQueryForItem;
	}

	@Autowired
	public void setGenerateQueryForItem(GenerateQueryForItem generateQueryForItem) {
		this.generateQueryForItem = generateQueryForItem;
	}

	public MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> getMetadataFieldMapping() {
		return metadataFieldMapping;
	}

	@Required
	public void setMetadataFieldMapping(
			MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> metadataFieldMapping) {
		this.metadataFieldMapping = metadataFieldMapping;
	}

	public Record transformSourceRecords(RecordType rt){
		 return new Record(new LinkedList<Metadatum>(getMetadataFieldMapping().resultToDCValueMapping(rt)));
	}
}
