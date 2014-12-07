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

	@Scheduled(fixedRate = 5000)
	public void scheduleJobs() {

		// ***********************************************
		// get all new files and decompress them
		// ***********************************************
		List<SequenceFile> newFiles = repository.findByStatus(SequenceFileStatus.READY);
		if (newFiles.size() > 0) {
			jobProcessor.processJobForFiles(newFiles, DecompressJob.class);
		}

		// ***********************************************
		// get the decompressed reference file to build bowtie index
		// ***********************************************
		List<SequenceFile> decompressedReferenceFiles = repository.findByStatusAndType(
				SequenceFileStatus.DECOMPRESS_COMPLETED, SequenceFileType.REFERENCE);
		if (decompressedReferenceFiles.size() > 0) {
			jobProcessor.processJobForFiles(decompressedReferenceFiles, BowtieIndexBuilderJob.class);
		}

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
			sampleFilesNeedingAlignment.add(sampleFile);
		}
		// for sample files run the alignment job
		if (sampleFilesNeedingAlignment.size() > 0) {
			jobProcessor.processJobForFiles(sampleFilesNeedingAlignment, BowtieAlignmentJob.class);
		}

		// ***********************************************
		// get the aligned files to convert them to bam
		// ***********************************************
		List<SequenceFile> alignedFiles = repository.findByStatusAndType(SequenceFileStatus.NEW,
				SequenceFileType.ALIGNED);
		if (alignedFiles.size() > 0) {
			jobProcessor.processJobForFiles(alignedFiles, SamToolsSamToBamJob.class);
		}

		// ***********************************************
		// get the bam files to sort them
		// ***********************************************
		List<SequenceFile> bamFiles = repository.findByStatusAndType(SequenceFileStatus.NEW, SequenceFileType.BAM);
		if (bamFiles.size() > 0) {
			jobProcessor.processJobForFiles(bamFiles, SamToolsSortBamJob.class);
		}

		// ***********************************************
		// get the sorted bam files and generate variants file
		// ***********************************************
		List<SequenceFile> sortedBamFiles = repository.findByStatusAndType(SequenceFileStatus.NEW,
				SequenceFileType.SORTED_BAM);
		if (sortedBamFiles.size() > 0) {
			jobProcessor.processJobForFiles(sortedBamFiles, VariantsFileCreationJob.class);
		}

		// ***********************************************
		// get the sorted bam files and variants file and upload them to GCS
		// ***********************************************
		List<SequenceFile> sortedBamFilesForUpload = repository.findByStatusAndType(
				SequenceFileStatus.VARIANTS_FILE_COMPLETED, SequenceFileType.SORTED_BAM);
		if (sortedBamFilesForUpload.size() > 0) {
			jobProcessor.processJobForFiles(sortedBamFilesForUpload, UploadToGCSJob.class);
		}
		List<SequenceFile> variantFilesForUpload = repository.findByStatusAndType(SequenceFileStatus.NEW,
				SequenceFileType.VARIANTS);
		if (variantFilesForUpload.size() > 0) {
			jobProcessor.processJobForFiles(variantFilesForUpload, UploadToGCSJob.class);
		}

		// ***********************************************
		// get the uploaded to GCS sorted bam files and import the reads file
		// ***********************************************
		List<SequenceFile> importReadsFiles = repository.findByStatusAndType(SequenceFileStatus.NEW_IN_GOOGLE_CLOUD,
				SequenceFileType.SORTED_BAM);
		if (importReadsFiles.size() > 0) {
			jobProcessor.processJobForFiles(importReadsFiles, ImportReadsJob.class);
		}

		// ***********************************************
		// get the uploaded to GCS variants files and import the variants file
		// ***********************************************
		List<SequenceFile> importVariantsFiles = repository.findByStatusAndType(SequenceFileStatus.NEW_IN_GOOGLE_CLOUD,
				SequenceFileType.VARIANTS);
		if (importVariantsFiles.size() > 0) {
			jobProcessor.processJobForFiles(importVariantsFiles, ImportVariantsJob.class);
		}

		// ***********************************************
		// get the imported variants file and export it to big query
		// ***********************************************
		List<SequenceFile> importedVariantsFiles = repository.findByStatusAndType(
				SequenceFileStatus.IMPORT_VARIANTS_COMPLETED, SequenceFileType.VARIANTS);
		if (importedVariantsFiles.size() > 0) {
			jobProcessor.processJobForFiles(importedVariantsFiles, ExportToBigQueryJob.class);
		}

	}
}
