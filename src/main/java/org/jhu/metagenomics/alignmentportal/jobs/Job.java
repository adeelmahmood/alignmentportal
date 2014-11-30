package org.jhu.metagenomics.alignmentportal.jobs;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;

public interface Job {

	String getName();
		
	String getInProgressStatus();

	String getCompletedStatus();

	void process(SequenceFile file) throws JobProcessingFailedException;
}
