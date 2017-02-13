package com.atmire.import_citations;


import com.atmire.import_citations.configuration.metadatamapping.AbstractMetadataFieldMapping;
import com.atmire.import_citations.configuration.metadatamapping.MetadataContributor;
import com.atmire.import_citations.configuration.metadatamapping.MetadataField;
import org.apache.axiom.om.OMElement;
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
    public void setMetadataFieldMap(Map<MetadataField, MetadataContributor<OMElement>> metadataFieldMap) {
        super.setMetadataFieldMap(metadataFieldMap);
    }


}
