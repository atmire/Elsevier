package com.atmire.import_citations;

import com.atmire.import_citations.configuration.SourceException;
import com.atmire.import_citations.configuration.metadatamapping.MetadataField;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;

import javax.annotation.Resource;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 17 Oct 2014
 */

public class GenerateDOIQueryForItem_Scopus implements GenerateQueryForItem_Scopus {

	public MetadataField getIdentifyingMetadataField() {
		return identifyingMetadataField;
	}
	@Resource(name="identifyingMetadataField")
	public void setIdentifyingMetadataField(MetadataField identifyingMetadataField) {
		this.identifyingMetadataField = identifyingMetadataField;
	}

	private MetadataField identifyingMetadataField;
	@Override
	public String generateQueryForItem(Item item) throws SourceException {
		Metadatum value[]=item.getMetadata(identifyingMetadataField.getSchema(), identifyingMetadataField.getElement(), identifyingMetadataField.getQualifier(), Item.ANY);
		if(value.length>0){
			String doi =  value[0].value;
			return "DOI("+ doi +")";
		} else {
			return null;
		}
	}

	@Override
	public String generateFallbackQueryForItem(Item item) throws SourceException {
		Metadatum authors[] = item.getMetadata("dc", "contributor", "author", Item.ANY);
		Metadatum titles[] = item.getMetadata("dc", "title", null, Item.ANY);

		String authorString = "";

		if (authors.length > 0 && titles.length > 0) {
			for (int i = 0; i < authors.length; i++) {
				if (i > 0) {
					authorString += " AND ";
				}

				authorString += "AUTH(" + authors[i].value + ")";
			}
			String query = "TITLE(" + titles[0].value + ") AND " + authorString;

			return query;
		}
		return null;
	}
}
