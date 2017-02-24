package com.atmire.import_citations;

import com.atmire.import_citations.configuration.Query;
import com.atmire.import_citations.configuration.SourceException;
import com.atmire.import_citations.datamodel.Record;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.log4j.*;
import org.dspace.content.Item;
import org.dspace.core.*;
import org.jaxen.JaxenException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 09/06/15
 * Time: 10:23
 */
public class PubmedSource extends AbstractImportSource<OMElement> {
    private WebTarget pubmedWebTarget;
    private String baseAddress;

    private static Logger log = Logger.getLogger(PubmedSource.class);

    private class GetNbRecords implements Callable<Integer> {

        private GetNbRecords(String queryString) {
            query = new Query();
            query.addParameter("query",queryString);
        }

        private Query query;

        public GetNbRecords(Query query) {
            this.query = query;
        }

        @Override
        public Integer call() throws Exception {
            WebTarget getRecordIdsTarget = pubmedWebTarget.queryParam("term", query.getParameterAsClass("query", String.class));

            getRecordIdsTarget = getRecordIdsTarget.path("esearch.fcgi");

            Invocation.Builder invocationBuilder = getRecordIdsTarget.request(MediaType.TEXT_PLAIN_TYPE);

            Response response = invocationBuilder.get();

            String responseString = response.readEntity(String.class);

            String count = getSingleElementValue(responseString, "Count");

            try {
                return Integer.parseInt(count);
            } catch (NumberFormatException e) {
                log.error("PubmedSource: failed to parse number of results, server response: " + responseString);
                return 0;
            }
        }
    }

    @Override
    public int getNbRecords(String queryString) throws SourceException {
        return retry(new GetNbRecords(queryString));
    }

    @Override
    public int getNbRecords(Query query) throws SourceException {
        return retry(new GetNbRecords(query));
    }


    private class GetRecords implements Callable<Collection<Record>> {

        private Query query;

        private GetRecords(String queryString, int start, int count) {
            query = new Query();
            query.addParameter("query",queryString);
            query.addParameter("start",start);
            query.addParameter("count",count);
        }

        private GetRecords(Query q) {
            this.query = q;
        }

        @Override
        public Collection<Record> call() throws Exception {
            String queryString = query.getParameterAsClass("query",String.class);
            Integer start = query.getParameterAsClass("start",Integer.class);
            Integer count = query.getParameterAsClass("count",Integer.class);

            if(count==null || count < 0){
                count = 10;
            }

            if(start==null || start < 0){
                start = 0;
            }

            List<Record> records = new LinkedList<Record>();

            WebTarget getRecordIdsTarget = pubmedWebTarget.queryParam("term", queryString);
            getRecordIdsTarget = getRecordIdsTarget.queryParam("retstart", start);
            getRecordIdsTarget = getRecordIdsTarget.queryParam("retmax", count);
            getRecordIdsTarget = getRecordIdsTarget.queryParam("usehistory", "y");
            getRecordIdsTarget = getRecordIdsTarget.path("esearch.fcgi");

            Invocation.Builder invocationBuilder = getRecordIdsTarget.request(MediaType.TEXT_PLAIN_TYPE);

            Response response = invocationBuilder.get();
            String responseString = response.readEntity(String.class);

            String queryKey = getSingleElementValue(responseString, "QueryKey");
            String webEnv = getSingleElementValue(responseString, "WebEnv");

            WebTarget getRecordsTarget = pubmedWebTarget.queryParam("WebEnv", webEnv);
            getRecordsTarget = getRecordsTarget.queryParam("query_key", queryKey);
            getRecordsTarget = getRecordsTarget.queryParam("retmode", "xml");
            getRecordsTarget = getRecordsTarget.path("efetch.fcgi");
            getRecordsTarget = getRecordsTarget.queryParam("retmax", count);
            getRecordsTarget = getRecordsTarget.queryParam("retstart", start);

            invocationBuilder = getRecordsTarget.request(MediaType.TEXT_PLAIN_TYPE);
            response = invocationBuilder.get();

            List<OMElement> omElements = splitToRecords(response.readEntity(String.class));

            for (OMElement record : omElements) {
                records.add(transformSourceRecords(record));
            }

            return records;
        }
    }

    @Override
    public Collection<Record> getRecords(String query, int start, int count) throws SourceException {
        return retry(new GetRecords(query, start, count));
    }

    @Override
    public Collection<Record> getRecords(Query q) throws SourceException {
        return retry(new GetRecords(q));
    }

    private class GetRecord implements Callable<Record> {

        private Query query;

        private GetRecord(String id) {
            query = new Query();
            query.addParameter("id",id);
        }

        public GetRecord(Query q) {
            query = q;
        }

        @Override
        public Record call() throws Exception {
            String id = query.getParameterAsClass("id", String.class);

            WebTarget getRecordTarget = pubmedWebTarget.queryParam("id", id);
            getRecordTarget = getRecordTarget.queryParam("retmode", "xml");
            getRecordTarget = getRecordTarget.path("efetch.fcgi");

            Invocation.Builder invocationBuilder = getRecordTarget.request(MediaType.TEXT_PLAIN_TYPE);

            Response response = invocationBuilder.get();

            List<OMElement> omElements = splitToRecords(response.readEntity(String.class));

            if(omElements.size()==0) {
                return null;
            }

            return transformSourceRecords(omElements.get(0));
        }
    }

    @Override
    public Record getRecord(String id) throws SourceException {
        return retry(new GetRecord(id));
    }

    @Override
    public Record getRecord(Query q) throws SourceException {
        return retry(new GetRecord(q));
    }

    @Override
    public String getImportSource() {
        return getBaseAddress();
    }

    private class FindMatchingRecords implements Callable<Collection<Record>> {

        private Query query;

        private FindMatchingRecords(Item item) throws SourceException {
            query = getGenerateQueryForItem().generateQueryForItem(item);
        }

        public FindMatchingRecords(Query q) {
            query = q;
        }

        @Override
        public Collection<Record> call() throws Exception {
            List<Record> records = new LinkedList<Record>();

            WebTarget getRecordIdsTarget = pubmedWebTarget.queryParam("term", query.getParameterAsClass("term", String.class));
            getRecordIdsTarget = getRecordIdsTarget.queryParam("field", query.getParameterAsClass("field",String.class));
            getRecordIdsTarget = getRecordIdsTarget.queryParam("usehistory", "y");
            getRecordIdsTarget = getRecordIdsTarget.path("esearch.fcgi");

            Invocation.Builder invocationBuilder = getRecordIdsTarget.request(MediaType.TEXT_PLAIN_TYPE);

            Response response = invocationBuilder.get();
            String responseString = response.readEntity(String.class);

            String queryKey = getSingleElementValue(responseString, "QueryKey");
            String webEnv = getSingleElementValue(responseString, "WebEnv");

            WebTarget getRecordsTarget = pubmedWebTarget.queryParam("WebEnv", webEnv);
            getRecordsTarget = getRecordsTarget.queryParam("query_key", queryKey);
            getRecordsTarget = getRecordsTarget.queryParam("retmode", "xml");
            getRecordsTarget = getRecordsTarget.path("efetch.fcgi");

            invocationBuilder = getRecordsTarget.request(MediaType.TEXT_PLAIN_TYPE);
            response = invocationBuilder.get();

            List<OMElement> omElements = splitToRecords(response.readEntity(String.class));

            for (OMElement record : omElements) {
                records.add(transformSourceRecords(record));
            }

            return records;
        }
    }


    @Override
    public Collection<Record> findMatchingRecords(Item item) throws SourceException {
        return retry(new FindMatchingRecords(item));
    }

    @Override
    public Collection<Record> findMatchingRecords(Query q) throws SourceException {
        return retry(new FindMatchingRecords(q));
    }

    @Override
    public void init() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(getBaseAddress());
        pubmedWebTarget = webTarget.queryParam("db", "pubmed");

//        invocationBuilder = pubmedWebTarget.request(MediaType.TEXT_PLAIN_TYPE);
    }

    @Override
    public List<OMElement> splitToRecords(String recordsSrc) {
        OMXMLParserWrapper records = OMXMLBuilderFactory.createOMBuilder(new StringReader(recordsSrc));
        OMElement element = records.getDocumentElement();
        AXIOMXPath xpath = null;
        try {
            xpath = new AXIOMXPath("//PubmedArticle");
            List<OMElement> recordsList = xpath.selectNodes(element);
            return recordsList;
        } catch (JaxenException e) {
            return null;
        }
    }

    public String getBaseAddress() {
        if(baseAddress == null){
            baseAddress = ConfigurationManager.getProperty("elsevier-sciencedirect","api.pubmed.url");
        }

        return baseAddress;
    }
}