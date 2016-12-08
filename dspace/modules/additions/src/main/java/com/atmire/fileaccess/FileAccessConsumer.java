package com.atmire.fileaccess;

import com.atmire.util.*;
import java.util.*;
import org.apache.log4j.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.event.*;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 02 Oct 2015
 */
public class FileAccessConsumer implements Consumer {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(FileAccessConsumer.class);

    private List<Integer> itemIDs = new LinkedList<>();

    public void initialize() throws Exception {

    }

    /**
     * Gather the DspaceObject IDs here.
     * DO NOT COMMIT THE CONTEXT
     */
    public void consume(Context context, Event event) throws Exception {


        int subjectType = event.getSubjectType();
        int eventType = event.getEventType();
        int subjectID = event.getSubjectID();

        switch (subjectType) {
            case Constants.ITEM:
                if (eventType == Event.INSTALL) {
                    itemIDs.add(subjectID);
                }
                break;
            default:
                log.warn("consume() got unrecognized event: " + event.toString());
        }

    }

    /**
     * Find the objects based on the IDS.
     * Process them here.
     * commit and clear the IDs
     */
    public void end(Context context) throws Exception {

        // update objects
        if (!itemIDs.isEmpty()) {
            for (Integer itemID : itemIDs) {
                Item item = Item.find(context, itemID);
                Set<Bitstream> bitstreams = ItemUtils.getBitstreams(item);
                for (Bitstream bitstream : bitstreams) {
                    String metadata = bitstream.getMetadata("workflow.fileaccess");
                    String endDate = bitstream.getMetadata("workflow.fileaccess.date");

                    MetadataUtils.clearMetadata(bitstream, "workflow.fileaccess");
                    MetadataUtils.clearMetadata(bitstream, "workflow.fileaccess.date");

                    FileAccess.setFileAccess(context, bitstream, metadata,endDate);
                    bitstream.update();
                }
            }
        }
        itemIDs.clear();

        // commit context
        context.getDBConnection().commit();
    }

    public void finish(Context ctx) throws Exception {

    }
}
