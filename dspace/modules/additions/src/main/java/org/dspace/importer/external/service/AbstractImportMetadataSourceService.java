/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.importer.external.service;

import java.util.*;
import org.dspace.importer.external.datamodel.*;
import org.dspace.importer.external.metadatamapping.*;
import org.dspace.importer.external.metadatamapping.contributor.*;
import org.dspace.importer.external.metadatamapping.transform.*;
import org.dspace.importer.external.service.components.*;

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

	public AbstractImportMetadataSourceService(GenerateQueryService generateQueryService, MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> metadataFieldMapping){
		this.generateQueryForItem=generateQueryService;
		this.metadataFieldMapping = metadataFieldMapping;
	}

	protected AbstractImportMetadataSourceService() {
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
	public void setMetadataFieldMapping(
			MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> metadataFieldMapping) {
		this.metadataFieldMapping = metadataFieldMapping;
	}

    /**
     *  Return an ImportRecord constructed from the results in a RecordType
     * @param recordType The recordtype to retrieve the DCValueMapping from
     * @return An {@link ImportRecord}, This is based on the results retrieved from the recordTypeMapping
     */
	public ImportRecord transformSourceRecords(RecordType recordType){
		 return new ImportRecord(new LinkedList<>(getMetadataFieldMapping().resultToDCValueMapping(recordType)));
	}
}
