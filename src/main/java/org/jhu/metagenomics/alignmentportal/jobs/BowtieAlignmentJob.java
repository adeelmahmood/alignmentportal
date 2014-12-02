package org.jhu.metagenomics.alignmentportal.jobs;

import java.io.File;
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
public class BowtieAlignmentJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(BowtieAlignmentJob.class);

	private final static String CMD = "./bowtie2";

	@Value("${bowtie.path}")
	private String bowtiePath;

	@Value("${bowtie.reference.index.dir}")
	private String indexDir;

	private final SequenceFileRepository repository;

	@Autowired
	public BowtieAlignmentJob(SequenceFileRepository repository) {
		this.repository = repository;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("performing alignment for file " + file);

		// get the related reference file
		SequenceFile reference = repository.findByDatasetAndStatusAndType(file.getDataset(),
				SequenceFileStatus.BOWTIE_REFERENCE_INDEX_COMPLETED, SequenceFileType.REFERENCE);
		log.debug("0");
		log.debug("0.5 " + reference);
		log.debug("1 " + reference.getPath());
		log.debug("2 " + new File(reference.getPath()));
		log.debug("3 " + new File(reference.getPath()).getParent());
		
		// get reference file parent directory
		String referenceParentDir = new File(reference.getPath()).getParent();
		log.debug("refeece parent dir is " + referenceParentDir);
		// get name of reference base
		String referenceIndexDir = referenceParentDir + "/" + indexDir + "/" + BowtieIndexBuilderJob.REF;
		log.debug("index " + referenceIndexDir);
		log.debug("2");
		
		// figure out name
		String alignedFile = FilenameUtils.concat(new File(file.getPath()).getParent(), "aligned.sam");
		log.debug("3");
		
		// alignment command
		List<String> commands = Arrays.asList(CMD, "-x", referenceIndexDir, "-U", file.getPath(), "-S", alignedFile);
		log.debug("4");
		AppUtils.executeCommands(commands, bowtiePath);
		log.debug("5");
	}

	@Override
	public String getName() {
		return "Bowtie Alignment Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Alignment In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Alignment Completed";
	}
}
