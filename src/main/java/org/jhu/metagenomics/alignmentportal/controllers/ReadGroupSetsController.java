package org.jhu.metagenomics.alignmentportal.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.Dataset;
import com.google.api.services.genomics.model.ReadGroupSet;
import com.google.api.services.genomics.model.SearchReadGroupSetsRequest;
import com.google.cloud.genomics.utils.Paginator;

@RestController
@RequestMapping("/readgroupsets")
public class ReadGroupSetsController {

	@Value("${google.genomics.project.number}")
	private long projectNumber;

	private final Genomics genomics;

	@Autowired
	public ReadGroupSetsController(Genomics genomics) {
		this.genomics = genomics;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<ReadGroupSet> list() throws IOException {
		// get datasets
		List<Dataset> datasets = genomics.datasets().list().setProjectNumber(projectNumber).execute().getDatasets();
		List<String> datasetIds = new ArrayList<String>();
		for (Dataset dataset : datasets) {
			datasetIds.add(dataset.getId());
		}

		List<ReadGroupSet> sets = new ArrayList<ReadGroupSet>();
		// get read group sets
		Paginator.ReadGroupSets readGroupSets = Paginator.ReadGroupSets.create(genomics);
		for (ReadGroupSet readGroupSet : readGroupSets.search(new SearchReadGroupSetsRequest()
				.setDatasetIds(datasetIds))) {
			sets.add(readGroupSet);
		}
		return sets;
	}

	@RequestMapping("/delete/{readGroupSetId}")
	public void delete(@PathVariable String readGroupSetId) throws IOException {
		genomics.readgroupsets().delete(readGroupSetId).execute();
	}
	
	@RequestMapping("/get/{readGroupSetId}")
	public void get(@PathVariable String readGroupSetId) throws IOException {
		genomics.readgroupsets().get(readGroupSetId).execute();
	}
}
