package org.jhu.metagenomics.alignmentportal.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.Dataset;
import com.google.api.services.genomics.model.SearchVariantSetsRequest;
import com.google.api.services.genomics.model.VariantSet;
import com.google.cloud.genomics.utils.Paginator;

@RestController
@RequestMapping("/variantsets")
public class VariantSetsController {

	@Value("${google.genomics.project.number}")
	private long projectNumber;

	private final Genomics genomics;

	@Autowired
	public VariantSetsController(Genomics genomics) {
		this.genomics = genomics;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<VariantSet> list() throws IOException {
		List<VariantSet> sets = new ArrayList<VariantSet>();
		// get datasets
		List<Dataset> datasets = genomics.datasets().list().setProjectNumber(projectNumber).execute().getDatasets();
		for (Dataset dataset : datasets) {
			// get variant sets
			Paginator.Variantsets variantSets = Paginator.Variantsets.create(genomics);
			for (VariantSet variantSet : variantSets.search(new SearchVariantSetsRequest().setDatasetIds(Arrays
					.asList(dataset.getId())))) {
				if (variantSet.getReferenceBounds() != null && variantSet.getReferenceBounds().size() > 0) {
					sets.add(variantSet);
				}
			}
		}
		return sets;
	}

	@RequestMapping("/delete/{variantSetId}")
	public void delete(@PathVariable String variantSetId) throws IOException {
		genomics.variantsets().delete(variantSetId).execute();
	}
}