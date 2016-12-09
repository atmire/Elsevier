package org.dspace.app.xmlui.cocoon;

import java.io.*;
import java.sql.*;
import java.util.Collection;
import java.util.*;
import javax.xml.*;
import javax.xml.parsers.*;
import org.apache.avalon.framework.parameters.*;
import org.apache.cocoon.*;
import org.apache.cocoon.environment.*;
import org.apache.cocoon.generation.*;
import org.apache.cocoon.xml.dom.*;
import org.apache.commons.lang.*;
import org.apache.log4j.*;
import org.dspace.app.xmlui.utils.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.content.*;
import org.dspace.content.factory.*;
import org.dspace.content.service.*;
import org.dspace.core.*;
import org.dspace.core.Context;
import org.dspace.importer.external.datamodel.*;
import org.dspace.importer.external.metadatamapping.*;
import org.dspace.importer.external.service.*;
import org.dspace.utils.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 15/06/15
 * Time: 16:56
 */
public class JSONImportSearcher extends AbstractGenerator {

    private ImportService importService;

    private String url = ConfigurationManager.getProperty("elsevier-sciencedirect.api.scidir.url");

    private static Logger log = Logger.getLogger(JSONImportSearcher.class);

    private Request request;
    private Context context;

    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    @Override
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        request = ObjectModelHelper.getRequest(objectModel);
        try {
            context = ContextUtil.obtainContext(objectModel);
        } catch (SQLException e) {
            log.error(e.getMessage(),e);
        }
        importService = new DSpace().getServiceManager().getServiceByName("importService", ImportService.class);
    }

    @Override
    public void generate() throws IOException, SAXException, ProcessingException {
        HashMap<String, String> liveImportFields = new DSpace().getServiceManager().getServiceByName("LiveImportFields", HashMap.class);

        StringBuilder query = new StringBuilder();

        for (String field : liveImportFields.keySet()) {
            String queryString = request.getParameter(field);

            if(StringUtils.isNotBlank(queryString)){
                if(StringUtils.isNotBlank(query.toString())) {
                    query.append(" AND ");
                }

                query.append(liveImportFields.get(field) + "(" + queryString + ")");
            }
        }

        int start = 0;
        String startString = request.getParameter("start");
        if(StringUtils.isNotBlank(startString)){
            int parsedStart = Integer.parseInt(startString);
            if(parsedStart>=0){
                start = parsedStart;
            }
        }

        try {
            int total = importService.getNbRecords(url, query.toString());
            Collection<ImportRecord> records = importService.getRecords(url, query.toString(), start, 20);

            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            org.w3c.dom.Document document = docBuilder.newDocument();

            Element rootnode = document.createElement("root");
            document.appendChild(rootnode);
            rootnode.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:i18n", WingConstants.I18N.URI);

            Element totalNode = document.createElement("total");
            totalNode.setTextContent(String.valueOf(total));
            rootnode.appendChild(totalNode);

            Element startNode = document.createElement("start");
            startNode.setTextContent(String.valueOf(start));
            rootnode.appendChild(startNode);

            Element recordsNode = document.createElement("records");
            recordsNode.setAttribute("array", "true");
            rootnode.appendChild(recordsNode);
            recordsNode.setAttribute("array", "true");

            MetadataFieldConfig importIdField = new DSpace().getServiceManager().getServiceByName("importId", MetadataFieldConfig.class);

            for (ImportRecord record : records) {
                Element recordWrapperNode = document.createElement("recordWrapper");
                recordWrapperNode.setAttribute("object", "true");
                recordsNode.appendChild(recordWrapperNode);

                Element recordNode = document.createElement("record");
                recordNode.setAttribute("namedObject", "true");

                HashMap<String, Element> metadatumValueNodes = new HashMap();

                for (MetadatumDTO metadatum : record.getValueList()) {
                    if (StringUtils.isNotBlank(metadatum.getValue())) {
                        if (!metadatumValueNodes.containsKey(metadatum.getField())) {
                            Element metadatumNode = document.createElement(metadatum.getField());
                            metadatumNode.setAttribute("array", "true");
                            metadatumValueNodes.put(metadatum.getField(), metadatumNode);

                            if (metadatum.getField().equals(importIdField.getField())) {
                                Iterator<Item> iterator = itemService.findByMetadataField(context, importIdField.getSchema(), importIdField.getElement(), importIdField.getQualifier(), metadatum.getValue());

                                if (iterator.hasNext()) {
                                    Element existsInDSpaceNode = document.createElement("imported");
                                    existsInDSpaceNode.setTextContent("true");
                                    recordNode.appendChild(existsInDSpaceNode);
                                }
                            }
                        }

                        Element metadatumValueNode = document.createElement("metadatumValue");
                        String value = metadatum.getValue();
                        if(value.startsWith("0")) {
                            value = "\"" + value + "\"";
                        }
                        metadatumValueNode.setTextContent(value);

                        metadatumValueNodes.get(metadatum.getField()).appendChild(metadatumValueNode);
                    }
                }

                for (Element element : metadatumValueNodes.values()) {
                    recordNode.appendChild(element);
                }

                recordWrapperNode.appendChild(recordNode);
            }

            DOMStreamer streamer = new DOMStreamer(contentHandler, lexicalHandler);
            streamer.stream(document);

        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
