package org.jhu.metagenomics.alignmentportal.jobs;

import java.util.ArrayList;
import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileType;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {

	@Value("${import.to.bigquery.enabled:false}")
	private boolean importToBigQuery;

	private final SequenceFileRepository repository;
	private final JobProcessor jobProcessor;

	@Autowired
	public JobScheduler(SequenceFileRepository repository, JobProcessor jobProcessor) {
		this.repository = repository;
		this.jobProcessor = jobProcessor;
	}

	@Scheduled(fixedDelay = 5000)
	public void scheduleJobs() {

		// ***********************************************
		// get all new files and decompress them
		// ***********************************************
		List<SequenceFile> newFiles = repository.findByStatus(SequenceFileStatus.READY);
		processFiles(newFiles, DecompressJob.class);

		// ***********************************************
		// get the decompressed reference file to build bowtie index
		// ***********************************************
		List<SequenceFile> decompressedReferenceFiles = repository.findByStatusAndType(
				SequenceFileStatus.DECOMPRESS_COMPLETED, SequenceFileType.REFERENCE);
		processFiles(decompressedReferenceFiles, BowtieIndexBuilderJob.class);

		// ***********************************************
		// get the indexed reference files and perform alignment
		// ***********************************************
		List<SequenceFile> referenceIndexFiles = repository.findByStatusAndType(
				SequenceFileStatus.BOWTIE_REFERENCE_INDEX_COMPLETED, SequenceFileType.REFERENCE);
		List<SequenceFile> sampleFilesNeedingAlignment = new ArrayList<SequenceFile>();
		SequenceFile sampleFile;
		// for each indexed reference file, get the corresponding sample file
		for (SequenceFile referenceIndexFile : referenceIndexFiles) {
			// look for sample file that needs alignment for this dataset
			sampleFile = repository.findByDatasetAndStatusAndType(referenceIndexFile.getDataset(),
					SequenceFileStatus.DECOMPRESS_COMPLETED, SequenceFileType.SAMPLE);
			if (sampleFile != null) {
				sampleFilesNeedingAlignment.add(sampleFile);
			}
		}
		// for sample files run the alignment job
		processFiles(sampleFilesNeedingAlignment, BowtieAlignmentJob.class);

		// ***********************************************
		// get the aligned files to convert them to bam
		// ***********************************************
		List<SequenceFile> alignedFiles = repository.findByStatusAndType(SequenceFileStatus.NEW,
				SequenceFileType.ALIGNED);
		processFiles(alignedFiles, SamToolsSamToBamJob.class);

		// ***********************************************
		// get the bam files to sort them
		// ***********************************************
		List<SequenceFile> bamFiles = repository.findByStatusAndType(SequenceFileStatus.NEW, SequenceFileType.BAM);
		processFiles(bamFiles, SamToolsSortBamJob.class);

		// ***********************************************
		// get the sorted bam files and generate variants file
		// ***********************************************
		List<SequenceFile> sortedBamFiles = repository.findByStatusAndType(SequenceFileStatus.NEW,
				SequenceFileType.SORTED_BAM);
		processFiles(sortedBamFiles, VariantsFileCreationJob.class);

		// ***********************************************
		// get the sorted bam files and variants file and upload them to GCS
		// ***********************************************
		List<SequenceFile> sortedBamFilesForUpload = repository.findByStatusAndType(
				SequenceFileStatus.VARIANTS_FILE_COMPLETED, SequenceFileType.SORTED_BAM);
		processFiles(sortedBamFilesForUpload, UploadToGCSJob.class);
		List<SequenceFile> variantFilesForUpload = repository.findByStatusAndType(SequenceFileStatus.NEW,
				SequenceFileType.VARIANTS);
		processFiles(variantFilesForUpload, UploadToGCSJob.class);

		// ***********************************************
		// get the uploaded to GCS sorted bam files and import the reads file
		// ***********************************************
		List<SequenceFile> importReadsFiles = repository.findByStatusAndType(SequenceFileStatus.NEW_IN_GOOGLE_CLOUD,
				SequenceFileType.SORTED_BAM);
		processFiles(importReadsFiles, ImportReadsJob.class);

		// ***********************************************
		// get the uploaded to GCS variants files and import the variants file
		// ***********************************************
		List<SequenceFile> importVariantsFiles = repository.findByStatusAndType(SequenceFileStatus.NEW_IN_GOOGLE_CLOUD,
				SequenceFileType.VARIANTS);
		processFiles(importVariantsFiles, ImportVariantsJob.class);

		// ***********************************************
		// get the imported variants file and export it to big query
		// ***********************************************
		if (importToBigQuery) {
			List<SequenceFile> importedVariantsFiles = repository.findByStatusAndType(
					SequenceFileStatus.IMPORT_VARIANTS_COMPLETED, SequenceFileType.VARIANTS);
			processFiles(importedVariantsFiles, ExportToBigQueryJob.class);
		}
	}

	private void processFiles(List<SequenceFile> files, Class<? extends Job> job) {
		if (files.size() > 0) {
			jobProcessor.processJobForFiles(files, job);
		}
	}
}
