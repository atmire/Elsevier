package com.atmire.import_citations.configuration.metadatamapping;

import org.dspace.content.Metadatum;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 17/06/15
 * Time: 11:02
 */
public class CombinedMetadatumContributor<T> implements MetadataContributor<T> {
    private MetadataField field;

    private LinkedList<MetadataContributor> metadatumContributors;

    private String separator;

    private MetadataFieldMapping<T,MetadataContributor<T>> metadataFieldMapping;

    public CombinedMetadatumContributor() {
    }

    public CombinedMetadatumContributor(MetadataField field, List<MetadataContributor> metadatumContributors, String separator) {
        this.field = field;
        this.metadatumContributors = (LinkedList<MetadataContributor>) metadatumContributors;
        this.separator = separator;
    }

    @Override
    public void setMetadataFieldMapping(MetadataFieldMapping<T, MetadataContributor<T>> metadataFieldMapping) {
        this.metadataFieldMapping = metadataFieldMapping;

        for (MetadataContributor metadatumContributor : metadatumContributors) {
            metadatumContributor.setMetadataFieldMapping(metadataFieldMapping);
        }
    }



    /**
     * a separate Metadatum object is created for each index of Metadatum returned from the calls to
     * MetadatumContributor.contributeMetadata(t) for each MetadatumContributor in the metadatumContributors list.
     * We assume that each contributor returns the same amount of Metadatum objects
     * @param t the object we are trying to translate
     * @return
     */
    @Override
    public Collection<Metadatum> contributeMetadata(T t) {
        List<Metadatum> values=new LinkedList<Metadatum>();

        LinkedList<LinkedList<Metadatum>> metadatumLists = new LinkedList<>();

        for (MetadataContributor metadatumContributor : metadatumContributors) {
            LinkedList<Metadatum> metadatums = (LinkedList<Metadatum>) metadatumContributor.contributeMetadata(t);
            metadatumLists.add(metadatums);
        }

        for (int i = 0; i<metadatumLists.getFirst().size();i++) {

            StringBuilder value = new StringBuilder();

            for (LinkedList<Metadatum> metadatums : metadatumLists) {
                value.append(metadatums.get(i).value);

                if(!metadatums.equals(metadatumLists.getLast())) {
                    value.append(separator);
                }
            }
            values.add(metadataFieldMapping.toDCValue(field, value.toString()));
        }

        return values;
    }

    public MetadataField getField() {
        return field;
    }

    public void setField(MetadataField field) {
        this.field = field;
    }

    public LinkedList<MetadataContributor> getMetadatumContributors() {
        return metadatumContributors;
    }

    public void setMetadatumContributors(LinkedList<MetadataContributor> metadatumContributors) {
        this.metadatumContributors = metadatumContributors;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
