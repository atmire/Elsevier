package com.atmire.import_citations;

import com.atmire.import_citations.configuration.Query;
import com.atmire.import_citations.configuration.metadatamapping.GenerateQueryForItem;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.Metadatum;
import org.dspace.content.service.ItemService;
import org.dspace.statistics.content.StatisticsDataVisits;

import java.util.List;

/**
 * Created by jonas - jonas@atmire.com on 06/11/15.
 */
public class GeneratePubmedQueryService {
    public Query generateQueryForItem(Item item) {
        Query query = new Query();

        Metadatum[] dois = item.getMetadata("dc", "identifier", "doi", Item.ANY);

        if(dois.length>0){
            query.addParameter("term", dois[0].value);
            query.addParameter("field","ELocationID");
            return query;
        }

        Metadatum[] titles = item.getMetadata("dc", "title", null, Item.ANY);

        if(titles.length>0) {
            query.addParameter("term", titles[0].value);
            query.addParameter("field","title");
            return query;
        }
        return null;
    }
}