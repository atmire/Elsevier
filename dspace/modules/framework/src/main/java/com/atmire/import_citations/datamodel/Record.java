package com.atmire.import_citations.datamodel;

import org.dspace.content.Metadatum;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 17/09/12
 * Time: 14:03
 */
public class Record {
    private List<Metadatum> valueList = null;

    public List<Metadatum> getValueList() {
        return Collections.unmodifiableList(valueList);
    }

    public Record(List<Metadatum> valueList) {
        //don't want to alter the original list. Also now I can control the type of list
        this.valueList = new LinkedList<Metadatum>(valueList);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Record");
        sb.append("{valueList=");
        for(Metadatum val:valueList){
            sb.append("{");
            sb.append(val.schema);
            sb.append("; ");
            sb.append(val.element);
            sb.append("; ");

            sb.append(val.qualifier);
            sb.append("; ");

            sb.append(val.value);
            sb.append("; ");
            sb.append("}\n");

        }
        sb.append("}\n");
        return sb.toString();
    }

    public Collection<Metadatum> getValue(String schema, String element,String qualifier){
        List<Metadatum> values=new LinkedList<Metadatum>();
        for(Metadatum value:valueList){
            if(value.schema.equals(schema)&&value.element.equals(element)){
               if(qualifier==null&&value.qualifier==null){
                   values.add(value);
               } else if (value.qualifier!=null&&value.qualifier.equals(qualifier)) {
                   values.add(value);
                }
            }
        }
        return values;
    }

    public void addValue(Metadatum value){
        this.valueList.add(value);
    }

    public Collection<Metadatum> getValue(String field) {
        String[] split = field.split("\\.");

        if(split.length==2){
            return getValue(split[0],split[1],null);
        }
        else {
            return getValue(split[0],split[1],split[2]);
        }
    }
}
