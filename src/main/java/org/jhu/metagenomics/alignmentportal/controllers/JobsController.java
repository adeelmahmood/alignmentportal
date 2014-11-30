package org.jhu.metagenomics.alignmentportal.controllers;

import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobsController {

	private final SequenceFileRepository repository;

	@Autowired
	public JobsController(SequenceFileRepository repository) {
		this.repository = repository;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<SequenceFile> list(@PageableDefault(direction = Sort.Direction.DESC, sort = "created") Pageable page) {
		return repository.findAll(page).getContent();
	}
}
