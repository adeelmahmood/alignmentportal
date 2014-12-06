package org.jhu.metagenomics.alignmentportal.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.genomics.GenomicsScopes;

public class GenomicsUtils {

	public static List<String> getScopes() {
		List<String> scopes = new ArrayList<String>();
		scopes.add(GenomicsScopes.GENOMICS);
		scopes.add(GenomicsScopes.DEVSTORAGE_READ_WRITE);
		scopes.add(GenomicsScopes.BIGQUERY);
		scopes.add(GenomicsScopes.GENOMICS_READONLY);
//		scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);
//		scopes.add(StorageScopes.DEVSTORAGE_READ_ONLY);
//		scopes.add(StorageScopes.DEVSTORAGE_READ_WRITE);
		return scopes;
	}
}
