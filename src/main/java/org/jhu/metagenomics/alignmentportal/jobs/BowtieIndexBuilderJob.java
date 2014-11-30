package org.jhu.metagenomics.alignmentportal.jobs;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BowtieIndexBuilderJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(BowtieIndexBuilderJob.class);
	
	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
	}

	@Override
	public String getName() {
		return "Bowtie Reference Index Builder Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Bowtie Reference Index In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Bowtie Reference Index Completed";
	}
}
