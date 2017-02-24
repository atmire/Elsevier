package com.atmire.import_citations.configuration;

import com.atmire.import_citations.datamodel.*;
import java.util.Collection;
import java.util.*;
import org.apache.log4j.*;
import org.dspace.content.*;
import org.dspace.utils.*;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 17/09/12
 * Time: 14:19
 */
public class ImportService implements Destroyable {
    private HashMap<String, Imports> importSources;

    Logger log = Logger.getLogger(ImportService.class);

    public ImportService() {

    }

    protected static final String ANY = "*";

    protected Map<String, Imports> getImportSources() {
        if(importSources == null) {
			importSources = new HashMap<>();
			List<Imports> importSources = new DSpace().getServiceManager().getServicesByType(Imports.class);

			for (Imports imports : importSources) {
				this.importSources.put(imports.getImportSource(), imports);
			}
		}

    	return Collections.unmodifiableMap(importSources);
    }

    protected Collection<Imports> matchingImports(String url) {
        if (ANY.equals(url)) {
            return getImportSources().values();
        } else {
			if(getImportSources().containsKey(url))
				return Collections.singletonList(getImportSources().get(url));
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
        return getImportSources().keySet();
    }


    @Override
    public void destroy() throws Exception {
        for (Imports imports : getImportSources().values()) {
            if (imports instanceof Destroyable) ((Destroyable) imports).destroy();
        }
    }
}
