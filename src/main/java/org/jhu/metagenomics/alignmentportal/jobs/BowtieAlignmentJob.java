package org.jhu.metagenomics.alignmentportal.jobs;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BowtieAlignmentJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(BowtieAlignmentJob.class);

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
	}

	@Override
	public String getName() {
		return "Bowtie Alignment Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Bowtie Alignment In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Bowtie Alignment Completed";
	}

}