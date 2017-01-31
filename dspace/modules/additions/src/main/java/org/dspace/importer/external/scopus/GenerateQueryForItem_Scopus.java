package org.dspace.importer.external.scopus;

import org.dspace.content.Item;
import org.dspace.importer.external.exception.MetadataSourceException;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 28 Oct 2014
 */
public interface GenerateQueryForItem_Scopus {
    public String generateQueryForItem(Item item) throws MetadataSourceException;
    public String generateFallbackQueryForItem(Item item) throws MetadataSourceException;
}
