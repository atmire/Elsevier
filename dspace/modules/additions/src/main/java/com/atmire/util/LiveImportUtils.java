package com.atmire.util;

import com.atmire.import_citations.configuration.*;
import com.atmire.import_citations.datamodel.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.commons.lang.*;
import org.apache.log4j.*;
import org.dspace.core.*;
import org.dspace.utils.*;
import org.springframework.beans.factory.annotation.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 06/10/15
 * Time: 13:59
 */
public class LiveImportUtils {
    @Autowired
    private ImportService importService;

    private HashMap<String, String> liveImportFields;
    private String url;

    Logger log = Logger.getLogger(LiveImportUtils.class);

    public Collection<Record> getRecords(HashMap<String, String> fieldValues, int start, int rpp){
        Collection<Record> records = new ArrayList<>();

        try {
            records = importService.getRecords(getUrl(), getQuery(fieldValues), start, rpp);
        } catch (SourceException e) {
            log.error(e.getMessage(),e);
        }

        return records;
    }

    public int getNbRecords(HashMap<String, String> fieldValues){
        int total = 0;
        try {
            total = importService.getNbRecords(getUrl(), getQuery(fieldValues));
        } catch (SourceException e) {
            log.error(e.getMessage(),e);
        }

        return total;
    }

    public String getQuery(HashMap<String, String> fieldValues) {
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldValues.keySet()) {
            if(query.length()>0) {
                query.append(" AND ");
            }

            query.append(fieldName + "(" + fieldValues.get(fieldName) + ")");
        }

        return query.toString();
    }

    public HashMap<String, String> getFieldValues(HttpServletRequest request){
        HashMap<String,String> fieldValues = new HashMap<>();
        for (String field : getLiveImportFields().keySet()) {
            String value = request.getParameter(field);

            if(StringUtils.isNotBlank(value)){
                fieldValues.put(getLiveImportFields().get(field), value);

            }
        }
        return fieldValues;
    }

    public String getUrl() {
        if(url==null){
            url = ConfigurationManager.getProperty("elsevier-sciencedirect", "api.scidir.url");
        }
        return url;
    }

    public HashMap<String, String> getLiveImportFields() {
        if(liveImportFields==null){
            liveImportFields = new DSpace().getServiceManager().getServiceByName("LiveImportFields", HashMap.class);
        }

        return liveImportFields;
    }
}
