package com.atmire.import_citations;

import com.atmire.import_citations.configuration.SourceException;
import org.dspace.content.Item;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 28 Oct 2014
 */
public interface GenerateQueryForItem_Scopus {
    public String generateQueryForItem(Item item) throws SourceException;
    public String generateFallbackQueryForItem(Item item) throws SourceException;
}
