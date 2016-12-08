package com.atmire.import_citations.configuration.metadatamapping;

import com.atmire.import_citations.configuration.Query;
import com.atmire.import_citations.configuration.SourceException;
import org.dspace.content.Item;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 14/12/12
 * Time: 11:44
 */
public interface GenerateQueryForItem {

    public Query generateQueryForItem(Item item) throws SourceException;
}
