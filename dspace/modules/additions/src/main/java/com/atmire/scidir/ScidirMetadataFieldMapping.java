package com.atmire.scidir;


import com.atmire.import_citations.configuration.metadatamapping.*;
import java.util.*;
import javax.annotation.*;
import org.apache.axiom.om.*;
import org.springframework.stereotype.*;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 14/12/12
 * Time: 11:02
 */
@Component
public class ScidirMetadataFieldMapping extends AbstractMetadataFieldMapping<OMElement> {

    @Override
    @Resource(name="scidirMetadataFieldMap")
    public void setMetadataFieldMap(Map<MetadataField, MetadataContributor<OMElement>> metadataFieldMap) {
        super.setMetadataFieldMap(metadataFieldMap);
    }


}
