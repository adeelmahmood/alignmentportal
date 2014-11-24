package org.jhu.metagenomics.alignmentportal.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.genomics.GenomicsScopes;

public class GenomicsUtils {

	public static List<String> getScopes() {
		List<String> scopes = new ArrayList<String>();
		scopes.add(GenomicsScopes.GENOMICS);
		return scopes;
	}
}
