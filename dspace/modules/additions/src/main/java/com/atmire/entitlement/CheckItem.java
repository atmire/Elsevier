package com.atmire.entitlement;

import com.atmire.util.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;
import org.apache.commons.lang3.*;
import org.apache.log4j.*;
import org.dspace.authority.rest.*;
import org.dspace.authority.util.XMLUtils;
import org.dspace.content.*;
import org.dspace.core.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 02 Oct 2015
 */
public abstract class CheckItem {

    /**
     * log4j logger
     */
    private static final Logger log = Logger.getLogger(CheckItem.class);
    protected Document response = null;
    protected Set<String> trueValues = Collections.singleton("true");
    protected Set<String> falseValues = Collections.singleton("false");
    protected boolean useApiKey = true;

    /**
     * Sends a rest request to the elsevier api
     *
     * @param item The item to be checked
     * @return  true    when the response element is contained in getTrueValues()
     *          false   when the response element is contained in getFalseValues()
     *          null    when none of the above
     */
    public ArticleAccess check(Item item) {
        ArticleAccess articleAccess = new ArticleAccess();

        String url = ConfigurationManager.getProperty("elsevier-sciencedirect", getUrlConfigKey());
        RESTConnector connect = new RESTConnector(url);

        String pii = MetadataUtils.getPII(item);
        String doi = MetadataUtils.getDOI(item);
        String eid = MetadataUtils.getEID(item);
        String scopus_id = MetadataUtils.getScopusID(item);
        String pubmed_ID = MetadataUtils.getPubmedID(item);
        InputStream responseStream= null;
        if (StringUtils.isNotBlank(pii)) {
            responseStream = connect.get("hostingpermission/pii/" + pii + getQueryString(), null);
        }  else if (StringUtils.isNotBlank(eid)) {
            responseStream = connect.get("hostingpermission/eid/" + eid + getQueryString(), null);
        } else if (StringUtils.isNotBlank(doi) && doi.startsWith("10.1016")) {
            responseStream = connect.get("hostingpermission/doi/" + doi + getQueryString(), null);
        } else if (publisherIsElsevier(item)) {
            if (StringUtils.isNotBlank(scopus_id)) {
                responseStream = connect.get("hostingpermission/scopus_id/" + scopus_id + getQueryString(), null);
            } else if (StringUtils.isNotBlank(pubmed_ID)) {
                responseStream = connect.get("hostingpermission/pubmed_id/" + pubmed_ID + getQueryString(), null);
            }
        }


        if (responseStream != null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                response = builder.parse(responseStream);
                Node hostingNode = XMLUtils.getNode(response, getCheckNodeXPath());
                Node hostingAllowedNode;
                if(hostingNode==null){
                    // If the hostingNode == null
                    // -> Log the response and return the initially created articelAcces object since it's no use continuing the processing
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer transformer = tf.newTransformer();
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    StringWriter writer = new StringWriter();
                    transformer.transform(new DOMSource(response), new StreamResult(writer));
                    String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
                    log.warn("Error retrieving required nodes from the response, please verify whether your ScienceDirect API key has sufficient permissions: "+output);
                    return articleAccess;
                }else{
                    hostingAllowedNode = XMLUtils.getNode(hostingNode, "hosting-platform[@type='institutional_repository']/document-version[journal_article_version/text()='AM']/hosting-allowed[@audience='Public']");
                }
                if (hostingAllowedNode != null) {
                    NamedNodeMap attributes = hostingAllowedNode.getAttributes();

                   Node audience = attributes.getNamedItem("audience");
                    Node start_date = attributes.getNamedItem("start_date");
                    if(audience != null){
                        articleAccess.setAudience(audience.getTextContent());
                    } else{
                        if(hostingAllowedNode.getNextSibling()!=null){

                        }
                    }
                    if(start_date !=null){
                       articleAccess.setStartDate(start_date.getTextContent());
                    }
                }
            } catch (XPathExpressionException e) {
                log.error("Error", e);
            } catch (TransformerConfigurationException e) {
                log.error("Error", e);
            } catch (TransformerException e) {
                log.error("Error", e);
            } catch (ParserConfigurationException e) {
                log.error("Error", e);
            } catch (IOException e) {
                log.error("Error", e);
            } catch (SAXException e) {
                log.error("Error", e);
            }
        }

        if(StringUtils.isBlank(articleAccess.getAudience())){
            if (StringUtils.isNotBlank(pii)) {
            responseStream = connect.get("pii/" + pii + getQueryString(), null);
                log.error("using the fallback access check implementation for article with pii " + pii);

            } else if (StringUtils.isNotBlank(doi)) {
                responseStream = connect.get("doi/" + doi + getQueryString(), null);
                log.error("using the fallback access check implementation for article with doi " + doi);
            }

            if (responseStream != null) {
                try {

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    response = builder.parse(responseStream);
                    Node node = XMLUtils.getNode(response, getCheckNodeXPath());

                    if (node != null) {
                        articleAccess.setAudience(node.getTextContent());
                    }
                } catch (XPathExpressionException e) {
                    log.error("Error", e);
                } catch (ParserConfigurationException e) {
                    log.error("Error", e);
                } catch (IOException e) {
                    log.error("Error", e);
                } catch (SAXException e) {
                    log.error("Error", e);
                }
            }
        }

        // if no permission can be found, set default to restricted
        if(StringUtils.isBlank(articleAccess.getAudience())){
            articleAccess.setAudience("restricted");

            if (StringUtils.isNotBlank(pii)) {
                log.error("fallback found no permissions for article with pii " + pii);

            } else if (StringUtils.isNotBlank(doi)) {
                log.error("fallback found no permissions for article with doi " + doi);
            }
        }

        return articleAccess;
    }

    private boolean publisherIsElsevier(Item item) {
        Metadatum[] publisherMetadata = item.getMetadataByMetadataString("dc.publisher");
        boolean publisherIsElsevier = false;
        for(Metadatum metadatum : publisherMetadata){
            if(metadatum.value.matches("^(?i)elsevier.*")){
                publisherIsElsevier =true;
            }
        }
        return publisherIsElsevier;
    }


    /**
     * Initializes when check(Item) is called.
     *
     * @return The link, if any, associated with the last item that has been checked.
     */
    public String getLink() {
        String link = null;
        if (response != null) {
            try {
                Node node = XMLUtils.getNode(response, getLinkNodeXPath());
                if (node != null) {
                    link = node.getTextContent();
                }
            } catch (XPathExpressionException e) {
                log.error("Error", e);
            }
        }
        return link;
    }

    private String getQueryString() {
        String key = "apiKey=" + ConfigurationManager.getProperty("elsevier-sciencedirect", "api.key");
        String accept = "httpAccept=text/xml";

        String params;
        if (useApiKey) {
            params = GeneralUtils.join("&", key, accept);
        } else {
            params = accept;
        }
        if (StringUtils.isNotBlank(params)) {
            params = "?" + params;
        }

        return params;
    }

    protected Set<String> getFalseValues() {
        return falseValues;
    }

    protected Set<String> getTrueValues(){
        return trueValues;
    }

    public void setFalseValues(Set<String> falseValues) {
        this.falseValues = falseValues;
    }

    public void setTrueValues(Set<String> trueValues) {
        this.trueValues = trueValues;
    }

    public boolean isUseApiKey() {
        return useApiKey;
    }

    public void setUseApiKey(boolean useApiKey) {
        this.useApiKey = useApiKey;
    }

    protected abstract String getLinkNodeXPath();

    protected abstract String getCheckNodeXPath();

    protected abstract String getUrlConfigKey();

}
