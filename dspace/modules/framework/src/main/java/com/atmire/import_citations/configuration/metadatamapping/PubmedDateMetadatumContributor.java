package com.atmire.import_citations.configuration.metadatamapping;

import org.apache.log4j.Logger;
import org.dspace.content.DCDate;
import org.dspace.content.Metadatum;
import org.springframework.beans.factory.annotation.Required;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 06/07/15
 * Time: 13:48
 */
public class PubmedDateMetadatumContributor<T> implements MetadataContributor<T> {
	Logger log = Logger.getLogger(PubmedDateMetadatumContributor.class);

	private MetadataFieldMapping<T, MetadataContributor<T>> metadataFieldMapping;

	private List<String> dateFormatsToAttempt;


	public List<String> getDateFormatsToAttempt() {
		return dateFormatsToAttempt;
	}
	@Required
	public void setDateFormatsToAttempt(List<String> dateFormatsToAttempt) {
		this.dateFormatsToAttempt = dateFormatsToAttempt;
	}

	private MetadataField field;
	private MetadataContributor day;
	private MetadataContributor month;
	private MetadataContributor year;

	@Override
	public void setMetadataFieldMapping(MetadataFieldMapping<T, MetadataContributor<T>> metadataFieldMapping) {
		this.metadataFieldMapping = metadataFieldMapping;
		day.setMetadataFieldMapping(metadataFieldMapping);
		month.setMetadataFieldMapping(metadataFieldMapping);
		year.setMetadataFieldMapping(metadataFieldMapping);
	}

	public PubmedDateMetadatumContributor() {
	}

	public PubmedDateMetadatumContributor(MetadataField field, MetadataContributor day, MetadataContributor month, MetadataContributor year) {
		this.field = field;
		this.day = day;
		this.month = month;
		this.year = year;
	}

	@Override
	public Collection<Metadatum> contributeMetadata(T t) {
		List<Metadatum> values = new LinkedList<Metadatum>();


		try {
			LinkedList<Metadatum> yearList = (LinkedList<Metadatum>) year.contributeMetadata(t);
			LinkedList<Metadatum> monthList = (LinkedList<Metadatum>) month.contributeMetadata(t);
			LinkedList<Metadatum> dayList = (LinkedList<Metadatum>) day.contributeMetadata(t);

			for (int i = 0; i < yearList.size(); i++) {
				DCDate dcDate = null;
				String dateString = "";

				if (monthList.size() > i && dayList.size() > i) {
					dateString = yearList.get(i).value + "-" + monthList.get(i).value + "-" + dayList.get(i).value;
				} else if (monthList.size() > i) {
					dateString = yearList.get(i).value + "-" + monthList.get(i).value;
				} else {
					dateString = yearList.get(i).value;
				}


				for (String dateFormat : dateFormatsToAttempt) {
					try {
						SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
						Date date = formatter.parse(dateString);
						dcDate = new DCDate(date);
						continue;
					} catch (ParseException e) {
						log.info("Date format " + dateFormat + " was unsuccessfull");
					}
				}


				if (dcDate != null) {
					values.add(metadataFieldMapping.toDCValue(field, dcDate.toString()));
				}
			}
		} catch (Exception e) {
			log.error("Error", e);
		}
		return values;
	}

	public MetadataField getField() {
		return field;
	}

	public void setField(MetadataField field) {
		this.field = field;
	}

	public MetadataContributor getDay() {
		return day;
	}

	public void setDay(MetadataContributor day) {
		this.day = day;
	}

	public MetadataContributor getMonth() {
		return month;
	}

	public void setMonth(MetadataContributor month) {
		this.month = month;
	}

	public MetadataContributor getYear() {
		return year;
	}

	public void setYear(MetadataContributor year) {
		this.year = year;
	}

}