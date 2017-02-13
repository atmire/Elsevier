/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import com.atmire.util.MetadataUtils;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.util.HashUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.SourceValidity;
import org.dspace.app.sfx.SFXFileReader;
import org.dspace.app.util.GoogleMetadata;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.content.crosswalk.DisseminationCrosswalk;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.PluginManager;
import org.dspace.utils.DSpace;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Display a single item.
 *
 * @author Scott Phillips
 */
public class ItemViewer extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    /** Language strings */
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");

    private static final Message T_trail =
        message("xmlui.ArtifactBrowser.ItemViewer.trail");

    private static final Message T_show_simple =
        message("xmlui.ArtifactBrowser.ItemViewer.show_simple");

    private static final Message T_show_full =
        message("xmlui.ArtifactBrowser.ItemViewer.show_full");

    private static final Message T_head_parent_collections =
        message("xmlui.ArtifactBrowser.ItemViewer.head_parent_collections");

    private static final Message T_withdrawn = message("xmlui.ArtifactBrowser.ItemViewer.withdrawn");
    
    private static final Message T_elsevier_embed = message("xmlui.ArtifactBrowser.ItemViewer.elsevier_embed");

	/** Cached validity object */
	private SourceValidity validity = null;

	/** XHTML crosswalk instance */
	private DisseminationCrosswalk xHTMLHeadCrosswalk = null;

	private final String sfxFile = ConfigurationManager.getProperty("dspace.dir")
            + File.separator + "config" + File.separator + "sfx.xml";

    private static final Logger log = LoggerFactory.getLogger(ItemViewer.class);

    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    @Override
    public Serializable getKey() {
        try {
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

            if (dso == null)
            {
                return "0"; // no item, something is wrong.
            }

            return HashUtil.hash(dso.getHandle() + "full:" + showFullItem(objectModel));
        }
        catch (SQLException sqle)
        {
            // Ignore all errors and just return that the component is not cachable.
            return "0";
        }
    }

    /**
     * Generate the cache validity object.
     *
     * The validity object will include the item being viewed,
     * along with all bundles & bitstreams.
     */
    @Override
    public SourceValidity getValidity()
    {
        DSpaceObject dso = null;
        if (this.validity == null)
    	{
	        try {
	            dso = HandleUtil.obtainHandle(objectModel);

	            DSpaceValidity validity = new DSpaceValidity();
	            validity.add(dso);
	            this.validity =  validity.complete();
	        }
	        catch (Exception e)
	        {
	            // Ignore all errors and just invalidate the cache.
	        }

    	}
    	return this.validity;
    }

    /** Matches Handle System URIs. */
    private static final Pattern handlePattern = Pattern.compile(
            "hdl:|https?://hdl\\.handle\\.net/", Pattern.CASE_INSENSITIVE);

    /** Matches DOI URIs. */
    private static final Pattern doiPattern = Pattern.compile(
            "doi:|https?://(dx\\.)?doi\\.org/", Pattern.CASE_INSENSITIVE);

    /**
     * Add the item's title and trail links to the page's metadata.
     */
    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item))
        {
            return;
        }

        Item item = (Item) dso;

        // Set the page title
        String title = getItemTitle(item);

        if (title != null)
        {
            pageMeta.addMetadata("title").addContent(title);
        }
        else
        {
            pageMeta.addMetadata("title").addContent(item.getHandle());
        }

        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        HandleUtil.buildHandleTrail(item,pageMeta,contextPath);
        pageMeta.addTrail().addContent(T_trail);

        // Add SFX link
        String sfxserverUrl = ConfigurationManager.getProperty("sfx.server.url");
        if (sfxserverUrl != null && sfxserverUrl.length() > 0)
        {
            String sfxQuery = "";

            // parse XML file -> XML document will be build
            sfxQuery = SFXFileReader.loadSFXFile(sfxFile, item);

            // Remove initial &, if any
            if (sfxQuery.startsWith("&"))
            {
                sfxQuery = sfxQuery.substring(1);
            }
            sfxserverUrl = sfxserverUrl.trim() +"&" + sfxQuery.trim();
            pageMeta.addMetadata("sfx","server").addContent(sfxserverUrl);
        }
        
        // Add persistent identifiers
        /* Temporarily switch to using metadata directly.
         * FIXME Proper fix is to have IdentifierService handle all durable
         * identifiers, whether minted here or elsewhere.
        List<IdentifierProvider> idPs = new DSpace().getServiceManager()
                .getServicesByType(IdentifierProvider.class);
        for (IdentifierProvider idP : idPs)
        {
            log.debug("Looking up Item {} by IdentifierProvider {}",
                    dso.getID(), idP.getClass().getName());
            try {
                String id = idP.lookup(context, dso);
                log.debug("Found identifier {}", id);
                String idType;
                String providerName = idP.getClass().getSimpleName().toLowerCase();
                if (providerName.contains("handle"))
                    idType = "handle";
                else if (providerName.contains("doi"))
                    idType = "doi";
                else
                {
                    log.info("Unhandled provider {}", idP.getClass().getName());
                    continue;
                }
                log.debug("Adding identifier of type {}", idType);
                Metadata md = pageMeta.addMetadata("identifier", idType);
                md.addContent(id);
            } catch (IdentifierNotFoundException | IdentifierNotResolvableException ex) {
                continue;
            }
        }
        */
        String identifierField = new DSpace().getConfigurationService()
                .getPropertyAsType("altmetrics.field", "dc.identifier.uri");
        for (Metadatum uri : dso.getMetadataByMetadataString(identifierField))
        {
            String idType, idValue;
            Matcher handleMatcher = handlePattern.matcher(uri.value);
            Matcher doiMatcher = doiPattern.matcher(uri.value);
            if (handleMatcher.lookingAt())
            {
                idType = "handle";
                idValue = uri.value.substring(handleMatcher.end());
            }
            else if (doiMatcher.lookingAt())
            {
                idType = "doi";
                idValue = uri.value.substring(doiMatcher.end());
            }
            else
            {
                log.info("Unhandled identifier URI {}", uri.value);
                continue;
            }
            log.debug("Adding identifier of type {}", idType);
            Metadata md = pageMeta.addMetadata("identifier", idType);
            md.addContent(idValue);
        }

        String sfxserverImg = ConfigurationManager.getProperty("sfx.server.image_url");
        if (sfxserverImg != null && sfxserverImg.length() > 0)
        {
            pageMeta.addMetadata("sfx","image_url").addContent(sfxserverImg);
        }

        boolean googleEnabled = ConfigurationManager.getBooleanProperty(
            "google-metadata.enable", false);

        if (googleEnabled)
        {
            // Add Google metadata field names & values to DRI
            GoogleMetadata gmd = new GoogleMetadata(context, item);

            for (Entry<String, String> m : gmd.getMappings())
            {
                pageMeta.addMetadata(m.getKey()).addContent(m.getValue());
            }
        }

        // Metadata for <head> element
        if (xHTMLHeadCrosswalk == null)
        {
            xHTMLHeadCrosswalk = (DisseminationCrosswalk) PluginManager.getNamedPlugin(
              DisseminationCrosswalk.class, "XHTML_HEAD_ITEM");
        }

        // Produce <meta> elements for header from crosswalk
        try
        {
            List l = xHTMLHeadCrosswalk.disseminateList(item);
            StringWriter sw = new StringWriter();

            XMLOutputter xmlo = new XMLOutputter();
            xmlo.output(new Text("\n"), sw);
            for (int i = 0; i < l.size(); i++)
            {
                Element e = (Element) l.get(i);
                // FIXME: we unset the Namespace so it's not printed.
                // This is fairly yucky, but means the same crosswalk should
                // work for Manakin as well as the JSP-based UI.
                e.setNamespace(null);
                xmlo.output(e, sw);
                xmlo.output(new Text("\n"), sw);
            }
            pageMeta.addMetadata("xhtml_head_item").addContent(sw.toString());
        }
        catch (CrosswalkException ce)
        {
            // TODO: Is this the right exception class?
            throw new WingException(ce);
        }

        pageMeta.addMetadata("javascript", "static", null, true).addContent("static/js/entitlement.js");
        boolean entitlementCheck = ConfigurationManager.getBooleanProperty("elsevier-sciencedirect", "entitlement.check.enabled", false);
        if(entitlementCheck) {
            pageMeta.addMetadata("window.DSpace", "item_pii").addContent(MetadataUtils.getPII(item));
            pageMeta.addMetadata("window.DSpace", "item_eid").addContent(MetadataUtils.getEID(item));
            String doi = MetadataUtils.getDOI(item);
            if (StringUtils.isNotBlank(doi) && doi.startsWith("10.1016")) {
                pageMeta.addMetadata("window.DSpace", "item_doi").addContent(doi);
            }
            if (publisherIsElsevier(item)) {
                pageMeta.addMetadata("window.DSpace", "item_pubmed_id").addContent(MetadataUtils.getPubmedID(item));
                pageMeta.addMetadata("window.DSpace", "item_scopus_id").addContent(MetadataUtils.getScopusID(item));
            }
            pageMeta.addMetadata("window.DSpace", "elsevier_apikey").addContent(ConfigurationManager.getProperty("elsevier-sciencedirect", "api.key"));
            pageMeta.addMetadata("window.DSpace", "elsevier_entitlement_url").addContent(ConfigurationManager.getProperty("elsevier-sciencedirect", "api.entitlement.url"));
        }
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
     * Display a single item
     */
    @Override
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item))
        {
            return;
        }

        Item item = (Item) dso;

        // Build the item viewer division.
        Division division = body.addDivision("item-view", "primary");
        String title = getItemTitle(item);
        if (title != null)
        {
            division.setHead(title);
        }
        else
        {
            division.setHead(item.getHandle());
        }

        // Add Withdrawn Message if it is
        if(item.isWithdrawn()){
            Division div = division.addDivision("notice", "notice");
            Para p = div.addPara();
            p.addContent(T_withdrawn);
            //Set proper response. Return "404 Not Found"
            HttpServletResponse response = (HttpServletResponse)objectModel
                    .get(HttpEnvironment.HTTP_RESPONSE_OBJECT);   
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Para showfullPara = division.addPara(null, "item-view-toggle item-view-toggle-top");

        if (showFullItem(objectModel))
        {
            String link = contextPath + "/handle/" + item.getHandle();
            showfullPara.addXref(link).addContent(T_show_simple);
        }
        else
        {
            String link = contextPath + "/handle/" + item.getHandle()
                    + "?show=full";
            showfullPara.addXref(link).addContent(T_show_full);
        }

        ReferenceSet referenceSet;
        if (showFullItem(objectModel))
        {
            referenceSet = division.addReferenceSet("collection-viewer",
                    ReferenceSet.TYPE_DETAIL_VIEW);
        }
        else
        {
            referenceSet = division.addReferenceSet("collection-viewer",
                    ReferenceSet.TYPE_SUMMARY_VIEW);
        }

        // Reference the actual Item
        ReferenceSet appearsInclude = referenceSet.addReference(item).addReferenceSet(ReferenceSet.TYPE_DETAIL_LIST, null, "hierarchy");
        appearsInclude.setHead(T_head_parent_collections);

        // Reference all collections the item appears in.
        for (Collection collection : item.getCollections())
        {
            appearsInclude.addReference(collection);
        }

        division.addPara("entitlement", "entitlement-wrapper hidden").addXref("", "entitlement", "entitlement-link");


        addEmbeddedDisplayLink(item, division);


        showfullPara = division.addPara(null,"item-view-toggle item-view-toggle-bottom");

        if (showFullItem(objectModel))
        {
            String link = contextPath + "/handle/" + item.getHandle();
            showfullPara.addXref(link).addContent(T_show_simple);
        }
        else
        {
            String link = contextPath + "/handle/" + item.getHandle()
                    + "?show=full";
            showfullPara.addXref(link).addContent(T_show_full);
        }
    }

    private void addEmbeddedDisplayLink(Item item, Division division) throws WingException {
        boolean embedDisplay = ConfigurationManager.getBooleanProperty("elsevier-sciencedirect", "embed.display");

        if (embedDisplay) {
            String pii = MetadataUtils.getPII(item);
            String doi = MetadataUtils.getDOI(item);
            String eid = MetadataUtils.getEID(item);
            String scopus_id = MetadataUtils.getScopusID(item);
            String pubmed_ID= MetadataUtils.getPubmedID(item);
            String link = null;
            String embeddedLink = null;
            String baseLink = contextPath + "/handle/" + item.getHandle() + "/elsevier-embed/";
            String embedURLBase = ConfigurationManager.getProperty("elsevier-sciencedirect", "ui.article.url");
            String doiURLBase = "http://dx.doi.org/";
            if (StringUtils.isNotBlank(pii)) {
                link = baseLink + pii+"?embeddedType=pii";
                embeddedLink=embedURLBase+"/pii/"+pii;
            }  else if (StringUtils.isNotBlank(eid)) {
                link = baseLink + eid+"?embeddedType=eid";
                embeddedLink=embedURLBase+"/eid/"+eid;
            } else if (StringUtils.isNotBlank(doi) && doi.startsWith("10.1016")) {
                link = baseLink + doi+"?embeddedType=doi";
                embeddedLink=doiURLBase+doi;
            } else if (publisherIsElsevier(item)) {
                if (StringUtils.isNotBlank(scopus_id)) {
                    link = baseLink + scopus_id+"?embeddedType=scopus_id";
                    embeddedLink=embedURLBase+"/scopus_id/"+scopus_id;
                } else if (StringUtils.isNotBlank(pubmed_ID)) {
                    link = baseLink + doi+"?embeddedType=pubmed_id";
                    embeddedLink=embedURLBase+"/pubmed_ID/"+pubmed_ID;
                }
            }
            if (StringUtils.isNotBlank(link)) {
                Para para = division.addPara("elsevier-embed-page", "elsevier-embed-page");
                para.addXref(link, T_elsevier_embed);
                para.addHidden("embeddedLink").setValue(embeddedLink);
            }
        }
    }

    /**
     * Determine if the full item should be referenced or just a summary.
     */
    public static boolean showFullItem(Map objectModel)
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String show = request.getParameter("show");

        if (show != null && show.length() > 0)
        {
            return true;
        }

        return false;
    }

    /**
     * Obtain the item's title.
     */
    public static String getItemTitle(Item item)
    {
        Metadatum[] titles = item.getDC("title", Item.ANY, Item.ANY);

        String title;
        if (titles != null && titles.length > 0)
        {
            title = titles[0].value;
        }
        else
        {
            title = null;
        }
        return title;
    }

    /**
     * Recycle
     */
    @Override
    public void recycle() {
    	this.validity = null;
    	super.recycle();
    }
}
