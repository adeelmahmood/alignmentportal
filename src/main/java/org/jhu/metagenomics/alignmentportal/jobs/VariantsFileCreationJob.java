package org.jhu.metagenomics.alignmentportal.jobs;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileType;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.jhu.metagenomics.alignmentportal.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VariantsFileCreationJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(VariantsFileCreationJob.class);

	public final static String ST_CMD = "samtools";
	public final static String BCF_CMD = "bcftools";

	@Value("${samtools.path}")
	private String samtoolsPath;

	@Value("${bcftools.path}")
	private String bcftoolsPath;

	private final SequenceFileRepository repository;

	@Autowired
	public VariantsFileCreationJob(SequenceFileRepository repository) {
		this.repository = repository;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("generating variants file from sorted bam file " + file);

		// get original reference file
		SequenceFile reference = repository.findByDatasetAndStatusAndType(file.getDataset(),
				SequenceFileStatus.BOWTIE_REFERENCE_INDEX_COMPLETED, SequenceFileType.REFERENCE);

		// path to variants file
		String newPath = file.getPath().replaceAll("sorted.bam", "vcf");

		// generate variants file command
		// List<String> commands = Arrays.asList(ST_CMD, "mpileup", "-uf",
		// reference.getPath(), file.getPath(), "|",
		// BCF_CMD, "call", "-mv", "-Ov");
		List<String> commands = Arrays.asList("/bin/sh", "-c", samtoolsPath + "/" + ST_CMD + " mpileup -uf "
				+ reference.getPath() + " " + file.getPath() + " | " + bcftoolsPath + "/" + BCF_CMD + " call -mv -Ov");
		int status = AppUtils.executeCommands(commands, samtoolsPath, true, newPath);
		if (status != 0) {
			throw new JobProcessingFailedException("generation of variants file failed with return status " + status);
		}
		
		file.setInfo("Variants file generated");

		// create a new sequence file for the variants file
		SequenceFile variantsFile = SequenceFile.copy(file);
		variantsFile.setPath(newPath);
		variantsFile.setName(FilenameUtils.getName(newPath));
		variantsFile.setType(SequenceFileType.VARIANTS);
		variantsFile.setStatus(SequenceFileStatus.NEW);
		repository.save(variantsFile);
	}

	@Override
	public String getName() {
		return "Variants Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Variants File In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Variants File Completed";
	}
}
