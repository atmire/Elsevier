package com.atmire.import_citations.configuration;

import com.atmire.import_citations.datamodel.Record;
import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 17/09/12
 * Time: 14:19
 */
public class ImportService implements Destroyable {
    private HashMap<String, Imports> importSources = new HashMap<String, Imports>();

    Logger log = Logger.getLogger(ImportService.class);

    public ImportService() {

    }

    protected static final String ANY = "*";

    @Autowired(required = true)
    public void setImportSources(List<Imports> importSources) throws SourceException {
        log.info("Loading " + importSources.size() + " import sources.");
        for (Imports imports : importSources) {
            this.importSources.put(imports.getImportSource(), imports);
        }

    }

    protected Map<String, Imports> getImportSources() {
        return Collections.unmodifiableMap(importSources);
    }

    protected Collection<Imports> matchingImports(String url) {
        if (ANY.equals(url)) {
            return importSources.values();
        } else {
			if(importSources.containsKey(url))
				return Collections.singletonList(importSources.get(url));
			else
				return Collections.emptyList();
		}
    }


    public Collection<Record> findMatchingRecords(String url, Item item) throws SourceException {
		try {
			List<Record> recordList = new LinkedList<Record>();

			for (Imports imports : matchingImports(url)) {
				recordList.addAll(imports.findMatchingRecords(item));
			}

			return recordList;
		} catch (Exception e) {
			throw new SourceException(e);
		}
	}

    public Collection<Record> findMatchingRecords(String url, Query query) throws SourceException {
		try {
			List<Record> recordList = new LinkedList<Record>();
			for (Imports imports : matchingImports(url)) {
				recordList.addAll(imports.findMatchingRecords(query));
			}

			return recordList;
		} catch (Exception e) {
			throw new SourceException(e);
		}
	}

    public int getNbRecords(String url, String query) throws SourceException {
		try {
			int total = 0;
			for (Imports Imports : matchingImports(url)) {
				total += Imports.getNbRecords(query);
			}
			return total;
		} catch (Exception e) {
			throw new SourceException(e);
		}
	}

    public int getNbRecords(String url, Query query) throws SourceException {
		try {
			int total = 0;
			for (Imports Imports : matchingImports(url)) {
				total += Imports.getNbRecords(query);
			}
			return total;
		} catch (Exception e) {
			throw new SourceException(e);
		}
	}


    public Collection<Record> getRecords(String url, String query, int start, int count) throws SourceException {
		try {
			List<Record> recordList = new LinkedList<Record>();
			for (Imports imports : matchingImports(url)) {
				recordList.addAll(imports.getRecords(query, start, count));
			}
			return recordList;
		} catch (Exception e) {
			throw new SourceException(e);
		}
	}
    public Collection<Record> getRecords(String url, Query query) throws SourceException {
		try {
			List<Record> recordList = new LinkedList<Record>();
			for (Imports imports : matchingImports(url)) {
				recordList.addAll(imports.getRecords(query));
			}
			return recordList;
		} catch (Exception e) {
			throw new SourceException(e);
		}
	}


    public Record getRecord(String url, String id) throws SourceException {
		try {
			for (Imports imports : matchingImports(url)) {
				if (imports.getRecord(id) != null) return imports.getRecord(id);
	
			}
			return null;
		} catch (Exception e) {
			throw new SourceException(e);
		}
	}

    public Record getRecord(String url, Query query) throws SourceException {
		try {
			for (Imports imports : matchingImports(url)) {
				if (imports.getRecord(query) != null) return imports.getRecord(query);
	
			}
			return null;
		} catch (Exception e) {
			throw new SourceException(e);
		}
	}

    public Collection<String> getImportUrls() {
        return importSources.keySet();
    }


    @Override
    public void destroy() throws Exception {
        for (Imports imports : importSources.values()) {
            if (imports instanceof Destroyable) ((Destroyable) imports).destroy();
        }
    }
}
