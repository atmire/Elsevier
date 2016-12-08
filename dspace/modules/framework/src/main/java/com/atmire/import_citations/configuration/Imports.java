package com.atmire.import_citations.configuration;

import com.atmire.import_citations.datamodel.Record;
import org.dspace.content.Item;

import java.util.Collection;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 17/09/12
 * Time: 14:08
 */
public interface Imports {
    public int getNbRecords(String query) throws SourceException;
    public int getNbRecords(Query query) throws SourceException;
    public Collection<Record> getRecords(String query,int start, int count)throws SourceException;
    public Collection<Record> getRecords(Query q)throws SourceException;
    public Record getRecord(String id)throws SourceException;
    public Record getRecord(Query q)throws SourceException;
    public String getImportSource();

    public Collection<Record> findMatchingRecords(Item item) throws SourceException;

    public Collection<Record> findMatchingRecords(Query q) throws SourceException;
}
