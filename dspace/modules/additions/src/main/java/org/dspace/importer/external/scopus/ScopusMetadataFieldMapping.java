package org.dspace.importer.external.scopus;


import org.apache.axiom.om.OMElement;
import org.dspace.importer.external.metadatamapping.AbstractMetadataFieldMapping;
import org.dspace.importer.external.metadatamapping.MetadataFieldConfig;
import org.dspace.importer.external.metadatamapping.contributor.MetadataContributor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 14/12/12
 * Time: 11:02
 */
@Component
public class ScopusMetadataFieldMapping extends AbstractMetadataFieldMapping<OMElement> {

    @Override
    @Resource(name="scopusMetadataFieldMap")
    public void setMetadataFieldMap(Map<MetadataFieldConfig, MetadataContributor<OMElement>> metadataFieldMap) {
        super.setMetadataFieldMap(metadataFieldMap);
    }


}
