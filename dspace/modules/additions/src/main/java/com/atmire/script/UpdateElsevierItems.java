package com.atmire.script;

import com.atmire.entitlement.*;
import com.atmire.fileaccess.*;
import com.atmire.import_citations.configuration.*;
import com.atmire.import_citations.datamodel.*;
import com.atmire.util.*;
import java.sql.*;
import java.util.*;
import java.util.Collection;
import org.apache.commons.cli.*;
import org.apache.commons.lang.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.handle.*;
import org.dspace.utils.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 09/11/15
 * Time: 15:09
 */
public class UpdateElsevierItems {

    private static boolean test = false;
    private static boolean force = false;

    private static ImportService importService;
    private static String url;

    public static void main(String[] args) {
        Context context = null;

        try {
            CommandLineParser parser = new PosixParser();

            Options options = CreateCommandLineOptions();
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("t")) {
                test = true;
            }

            if (line.hasOption("f")) {
                force = true;
            }

            context = new Context();
            context.turnOffAuthorisationSystem();

            ItemIterator itemIterator = null;

            if (line.hasOption("i")) {
                DSpaceObject dSpaceObject = HandleManager.resolveToObject(context, line.getOptionValue("i"));

                if (dSpaceObject.getType() == Constants.ITEM) {
                    ArrayList<Integer> ids = new ArrayList<Integer>();
                    ids.add(dSpaceObject.getID());
                    itemIterator = new ItemIterator(context, ids);
                }
            } else {
                itemIterator = Item.findAll(context);
            }

            if (itemIterator != null && itemIterator.hasNext()) {
                while (itemIterator.hasNext()) {
                    Item item = itemIterator.next();

                    if (line.hasOption("p")) {
                        updatePermissions(context, item);
                    }

                    if (line.hasOption("a")) {
                        assignPii(item);
                    }

                    if (line.hasOption("m")) {
                        importMetadata(item);
                    }

                    item.update();
                    context.commit();
                    item.decache();
                }

                context.restoreAuthSystemState();
                context.complete();
            }
            else {
                System.out.println("no items found.");
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }finally{
            if (context != null) {
                context.abort();
            }
        }
    }

    private static Options CreateCommandLineOptions() {
        Options options = new Options();
        Option testOption = OptionBuilder.withArgName("test").withDescription("output changes without applying them").create('t');
        Option permissionOption = OptionBuilder.withArgName("permissions").withDescription("Update the item permissions").create('p');
        Option piiOption = OptionBuilder.withArgName("assignpii").withDescription("Lookups uo the pii for items with a doi (but no pii) and add it to the metadata").create('a');
        Option metadataOption = OptionBuilder.withArgName("metadata").withDescription("import the item metadata").create('m');
        Option forceOption = OptionBuilder.withArgName("force").withDescription("force update changes from elsevier").create('f');
        Option identifierOption = OptionBuilder.withArgName("i").hasArg().withDescription("specify a handle to update a single item").create('i');

        options.addOption(testOption);
        options.addOption(permissionOption);
        options.addOption(piiOption);
        options.addOption(metadataOption);
        options.addOption(forceOption);
        options.addOption(identifierOption);
        return options;
    }

    private static void updatePermissions(Context context, Item item) throws SQLException, AuthorizeException {
        Bundle[] bundles = item.getBundles();

        for (Bundle bundle : bundles) {
            Bitstream[] bitstreams = bundle.getBitstreams();

            for (Bitstream bitstream : bitstreams) {
                boolean identical = FileAccess.fileAccessIdentical(context, bitstream);

                if(!identical){
                    boolean overruled = Boolean.parseBoolean(bitstream.getMetadata("workflow.fileaccess.overruled"));

                    if(force || !overruled) {
                        OpenAccessArticleCheck openAccessArticleCheck = OpenAccessArticleCheck.getInstance();
                        ArticleAccess itemFileAccess = openAccessArticleCheck.check(item);

                        if (test) {
                            System.out.println("permission of bitstream with id " + bitstream.getID() + " would be updated to " + itemFileAccess.toString());
                        } else {
                            FileAccess.setFileAccess(context, bitstream, itemFileAccess.getAudience(), itemFileAccess.getStartDate());
                        }
                    }
                }
            }
        }
    }

    private static void assignPii(Item item) throws SourceException {
        String pii = MetadataUtils.getPII(item);
        String doi = MetadataUtils.getDOI(item);

        String piiMdField = ConfigurationManager.getProperty("elsevier-sciencedirect", "metadata.field.pii");

        String[] split = piiMdField.split("\\.");

        String schema = split[0];
        String element = split[1];
        String qualifier = null;

        if(split.length>2) {
            qualifier = split[2];
        }

        if(force){
            Record record = getRecord("doi",doi);

            if(record!=null) {
                Collection<Metadatum> values = record.getValue(piiMdField);

                if(values.size()==0){
                    if(test){
                        System.out.println("pii for item with id " + item.getID() + " would be removed");
                    }
                    else{
                        item.clearMetadata(schema,element,qualifier, Item.ANY);
                    }
                }
                else {
                    Metadatum newPii = values.iterator().next();

                    if(!newPii.value.equals(pii)){
                        if(test){
                            System.out.println("pii for item with id " + item.getID() + " would be updated to " + newPii.value);
                        }
                        else{
                            item.clearMetadata(schema, element, qualifier, Item.ANY);
                            item.addMetadata(schema,element,qualifier,null,newPii.value);
                        }
                    }
                }
            }
        }
        else if(StringUtils.isNotBlank(doi) && StringUtils.isBlank(pii)){
            Record record = getRecord("doi",doi);

            if (record!=null) {
                Collection<Metadatum> values = record.getValue(piiMdField);

                Metadatum newPii = values.iterator().next();

                if(test){
                    System.out.println("pii " + newPii.value + " would be added to item with id " + item.getID());
                }
                else{
                    item.addMetadata(schema, element, qualifier,null,newPii.value);
                }
            }
        }
    }

    private static void importMetadata(Item item) throws SourceException {
        String pii = MetadataUtils.getPII(item);
        String doi = MetadataUtils.getDOI(item);

        if(force){
            Record record = null;
            if(StringUtils.isNotBlank(pii)){
                record = getRecord("pii",pii);
            }
            else if (StringUtils.isNotBlank(doi)){
                record = getRecord("doi",doi);
            }

            if(record!=null){
                for (Metadatum recordMetadatum : record.getValueList()) {
                    Metadatum[] metadata = item.getMetadata(recordMetadatum.schema, recordMetadatum.element, recordMetadatum.qualifier, Item.ANY);

                    boolean addMetadata = true;
                    for (Metadatum itemMetadata : metadata) {
                        if(itemMetadata.value.equals(recordMetadatum.value)){
                            addMetadata = false;
                        }
                    }

                    if(addMetadata){
                        if(test){
                            System.out.println("metadata " + recordMetadatum.getField() + " would be updated with value " + recordMetadatum.value + " for item with id " + item.getID());
                        }
                        else {
                            item.addMetadata(recordMetadatum.schema, recordMetadatum.element, recordMetadatum.qualifier,recordMetadatum.language,recordMetadatum.value);
                        }
                    }
                }
            }
        }
    }

    private static Record getRecord(String field, String value) throws SourceException {
        if(importService==null){
            importService = new DSpace().getServiceManager().getServiceByName(null, ImportService.class);
        }

        if(StringUtils.isBlank(url)) {
            url = ConfigurationManager.getProperty("elsevier-sciencedirect", "api.scidir.url");
        }

        Collection<Record> records = importService.getRecords(url,  field + "(\"" + value + "\")", 0, 1);

        if(records.size()>0) {
            return records.iterator().next();
        }

        return null;
    }
}
