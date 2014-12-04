package org.jhu.metagenomics.alignmentportal.controllers;

import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobsController {

	private final SequenceFileRepository repository;

	@Autowired
	public JobsController(SequenceFileRepository repository) {
		this.repository = repository;
	}

	@RequestMapping("/datasets")
	public List<String> distinctDatasets() {
		return repository.getDatasetDistinctByDatasetOrderByCreatedDesc();
	}

	@RequestMapping("/{dataset}")
	public List<SequenceFile> list(@PathVariable String dataset) {
		return repository.findByDatasetOrderByCreatedDesc(dataset);
	}

	@RequestMapping("/start/{dataset}")
	public void startDataset(@PathVariable String dataset) {
		List<SequenceFile> files = repository.findByDatasetAndStatus(dataset, SequenceFileStatus.NEW);
		for (SequenceFile file : files) {
			file.setStatus(SequenceFileStatus.READY);
		}
		repository.save(files);
	}
}
