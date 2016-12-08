package com.atmire.util;

import java.sql.*;
import java.util.*;
import org.dspace.content.*;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 02 Oct 2015
 */
public class ItemUtils {

    public static Set<Bitstream> getBitstreams(Item item) throws SQLException {
        Set<Bitstream> bitstreams = new HashSet<Bitstream>();
        for (Bundle bundle : item.getBundles()) {
            Collections.addAll(bitstreams, bundle.getBitstreams());
        }
        return bitstreams;
    }
}
