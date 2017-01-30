package com.atmire.import_citations;

import com.atmire.import_citations.configuration.Query;
import com.atmire.import_citations.configuration.metadatamapping.MetadataContributor;
import com.atmire.import_citations.configuration.metadatamapping.MetadataField;
import com.atmire.import_citations.configuration.metadatamapping.SimpleXpathMetadatumContributor;
import com.atmire.import_citations.datamodel.Record;
import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 28/09/15
 * Time: 16:34
 */
public class ScopusSourceTest {
    private static ScopusSource scopusSource;

    @BeforeClass
    public static void setUp() throws Exception {
        scopusSource = new ScopusSource();
        ScopusMetadataFieldMapping mapping=new ScopusMetadataFieldMapping();
        scopusSource.setMetadataFieldMapping(mapping);
        scopusSource.setBaseAddress("http://api.elsevier.com/content/search/index:scopus");
        scopusSource.setApiKey("28b63cae9ae0db3ba914c778ec53f0fb");

        HashMap<String,String> ns=new HashMap<String, String>();
        ns.put("http://purl.org/dc/elements/1.1/", "dc");

        Map<MetadataField,MetadataContributor<OMElement>> metadataFieldMap = new HashMap<>();
        metadataFieldMap.put(new MetadataField("dc", "title"), new SimpleXpathMetadatumContributor("//dc:title", ns, new MetadataField("dc", "title")));

        mapping.setMetadataFieldMap(metadataFieldMap);
    }

    @Test
    public void testGetNbRecords() throws Exception {
        assertTrue("The query should return at least one result", 1 <= scopusSource.getNbRecords("broad institute"));
    }

    @Test
    public void testGetNbRecordsQuery() throws Exception {
        Query query = new Query();
        query.addParameter("query","broad institute");
        assertTrue("The query should return at least one result", 1 <= scopusSource.getNbRecords(query));
    }

    @Test
    public void testGetRecordsQuery() throws Exception {
        Query query = new Query();
        query.addParameter("query","broad institute");
        query.addParameter("start", 0);
        query.addParameter("count", 10);

        Collection<Record> record = scopusSource.getRecords(query);
        assertEquals("The number of returned records should be 10", 10, record.size());
    }

    @Test
    public void testGetRecord() throws Exception {
        Record record = scopusSource.getRecord("84988931377");
        assertTrue(record.getValue("dc", "title", null).size() > 0);
        System.out.println(record);
    }

    @Test
    public void testGetRecordQuery() throws Exception {
        Query query = new Query();
        query.addParameter("id", "84988931377");
        Record record = scopusSource.getRecord(query);
        assertTrue(record.getValue("dc","title",null).size()>0);
        System.out.println(record);
    }

    @Test
    public void testGetImportSource() throws Exception {
        assertEquals("Import sourceUrl not valid", "http://api.elsevier.com/content/search/index:scopus", scopusSource.getImportSource());
    }

    // TODO test FindMatchingRecords
}
