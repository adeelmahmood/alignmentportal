package org.jhu.metagenomics.alignmentportal.controllers;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.Dataset;
import com.google.api.services.genomics.model.ListDatasetsResponse;

@RestController
@RequestMapping("/datasets")
public class DataSetsController {

	private static final Logger log = LoggerFactory.getLogger(DataSetsController.class);

	@Value("${google.genomics.project.number}")
	private long projectNumber;

	private final Genomics genomics;

	@Autowired
	public DataSetsController(Genomics genomics) {
		this.genomics = genomics;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<Dataset> list() {
		log.debug("listing dataset for project " + projectNumber);
		ListDatasetsResponse datasets = null;
		try {
			// list all datasets
			datasets = genomics.datasets().list()
					.setProjectNumber(projectNumber)
					.execute();
		} catch (IOException e) {
			log.error("error in listing datasets", e);
		}
		return datasets.getDatasets();
	}
	
	@RequestMapping("/get/{dataset}")
	public Dataset get(@PathVariable String dataset) {
		log.debug("getting dataset " + dataset);
		//get dataset
		try {
			return genomics.datasets().get(dataset).execute();
		} catch (IOException e) {
			log.error("error in getting dataset", e);
		}
		return null;
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public Dataset create(@RequestParam String dataset) {
		log.debug("creating new dataset " + dataset + "  for project " + projectNumber);
		// create new dataset
		Dataset ds = new Dataset().setName(dataset).setProjectNumber(projectNumber).setIsPublic(true);
		try {
			// add to genomics datasets
			ds = genomics.datasets().create(ds).execute();

		} catch (IOException e) {
			log.error("error in creating new dataset", e);
		}
		return ds;
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Void delete(@RequestParam String dataset) {
		log.debug("delete dataset with " + dataset + "  for project " + projectNumber);
		try {
			// add to genomics datasets
			return genomics.datasets().delete(dataset).execute();
		} catch (IOException e) {
			log.error("error in creating new dataset", e);
		}
		return null;
	}
}
