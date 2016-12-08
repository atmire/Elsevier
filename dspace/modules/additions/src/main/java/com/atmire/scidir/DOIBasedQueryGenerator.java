package com.atmire.scidir;

import com.atmire.import_citations.configuration.*;
import com.atmire.import_citations.configuration.metadatamapping.*;
import org.dspace.content.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 28/09/15
 * Time: 17:31
 */
public class DOIBasedQueryGenerator implements GenerateQueryForItem {
    @Override
    public Query generateQueryForItem(Item item) throws SourceException {
        Metadatum value[]=item.getMetadata("dc", "identifier", "other", Item.ANY);
        if(value.length>0){
            Query query = new Query();
            query.addParameter("query","doi("+ value[0].value +")");
            return query;
        } else {
            return null;
        }
    }
}
