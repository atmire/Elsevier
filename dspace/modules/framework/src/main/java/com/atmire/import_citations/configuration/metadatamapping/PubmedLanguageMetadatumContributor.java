package com.atmire.import_citations.configuration.metadatamapping;

import org.apache.log4j.Logger;
import org.dspace.content.Metadatum;

import java.util.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 07/07/15
 * Time: 15:08
 */
public class PubmedLanguageMetadatumContributor<T> implements MetadataContributor<T> {
    Logger log = Logger.getLogger(PubmedDateMetadatumContributor.class);

    private MetadataFieldMapping<T,MetadataContributor<T>> metadataFieldMapping;
    private HashMap<String,String> iso3toIso2;

    private MetadataField field;
    private MetadataContributor language;

    public PubmedLanguageMetadatumContributor() {
        iso3toIso2=new HashMap<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            iso3toIso2.put(locale.getISO3Language(),locale.getLanguage());
        }
    }

    public PubmedLanguageMetadatumContributor(MetadataField field, MetadataContributor language) {
        this();
        this.field = field;
        this.language = language;
    }

    @Override
    public void setMetadataFieldMapping(MetadataFieldMapping<T, MetadataContributor<T>> metadataFieldMapping) {
        this.metadataFieldMapping = metadataFieldMapping;
        language.setMetadataFieldMapping(metadataFieldMapping);
    }

    @Override
    public Collection<Metadatum> contributeMetadata(T t) {
        List<Metadatum> values=new LinkedList<Metadatum>();

        try {
            LinkedList<Metadatum> languageList = (LinkedList<Metadatum>) language.contributeMetadata(t);

            for (Metadatum metadatum : languageList) {

				values.add(metadataFieldMapping.toDCValue(field, iso3toIso2.get(metadatum.value.toLowerCase())));
			}
        } catch (Exception e) {
            log.error("Error", e);
        }

        return values;
    }

    public MetadataContributor getLanguage() {
        return language;
    }

    public void setLanguage(MetadataContributor language) {
        this.language = language;
    }

    public MetadataField getField() {
        return field;
    }

    public void setField(MetadataField field) {
        this.field = field;
    }
}