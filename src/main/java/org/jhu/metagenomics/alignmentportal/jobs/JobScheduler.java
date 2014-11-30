package org.jhu.metagenomics.alignmentportal.jobs;

import java.util.ArrayList;
import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileType;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {

	private final SequenceFileRepository repository;
	private final JobProcessor jobProcessor;

	@Autowired
	public JobScheduler(SequenceFileRepository repository, JobProcessor jobProcessor) {
		this.repository = repository;
		this.jobProcessor = jobProcessor;
	}

	@Scheduled(fixedRate = 10000)
	public void scheduleJobs() {

		// get all new files and decompress them
		List<SequenceFile> newFiles = repository.findByStatus(SequenceFileStatus.NEW);
		if (newFiles.size() > 0) {
			jobProcessor.processJobForFiles(newFiles, DecompressJob.class);
		}

		// get the decompressed reference file to build bowtie index
		List<SequenceFile> decompressedReferenceFiles = repository.findByStatusAndType(
				SequenceFileStatus.DECOMPRESS_COMPLETED, SequenceFileType.REFERENCE);
		if (decompressedReferenceFiles.size() > 0) {
			jobProcessor.processJobForFiles(decompressedReferenceFiles, BowtieIndexBuilderJob.class);
		}

		// get the indexed reference files
		List<SequenceFile> referenceIndexFiles = repository.findByStatusAndType(
				SequenceFileStatus.BOWTIE_REFERENCE_INDEX_COMPLETED, SequenceFileType.REFERENCE);
		List<SequenceFile> sampleFilesNeedingAlignment = new ArrayList<SequenceFile>();
		SequenceFile sampleFile;
		// for each indexed reference file, get the corresponding sample file
		for (SequenceFile referenceIndexFile : referenceIndexFiles) {
			// look for sample file that needs alignment for this dataset
			sampleFile = repository.findByDatasetAndStatusAndType(referenceIndexFile.getDataset(),
					SequenceFileStatus.DECOMPRESS_COMPLETED, SequenceFileType.SAMPLE);
			sampleFilesNeedingAlignment.add(sampleFile);
		}
		// for sample files run the alignment job
		if (sampleFilesNeedingAlignment.size() > 0) {
			jobProcessor.processJobForFiles(sampleFilesNeedingAlignment, DecompressJob.class);
		}

	}
}
