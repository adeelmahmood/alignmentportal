package org.jhu.metagenomics.alignmentportal.jobs;

import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;

public interface JobProcessor {

	void processJobForFiles(List<SequenceFile> files, Class<? extends Job> job);
}
