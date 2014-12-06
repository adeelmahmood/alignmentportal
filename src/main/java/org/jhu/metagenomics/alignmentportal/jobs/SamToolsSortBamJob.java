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
public class SamToolsSortBamJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(SamToolsSortBamJob.class);

	public final static String CMD = "./samtools";

	@Value("${samtools.path}")
	private String samtoolsPath;

	private final SequenceFileRepository repository;

	@Autowired
	public SamToolsSortBamJob(SequenceFileRepository repository) {
		this.repository = repository;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("sorting bam file " + file);

		// path to sorted bam file
		String newPath = file.getPath().replaceAll(".bam", ".sorted");

		// sam to bam command
		List<String> commands = Arrays.asList(CMD, "sort", file.getPath(), newPath);
		int status = AppUtils.executeCommands(commands, samtoolsPath);
		if (status != 0) {
			throw new JobProcessingFailedException("samtools sort bam failed with return status " + status);
		}
		
		file.setInfo("Bam file sorted");

		String finalPath = newPath + ".bam";
		// create a new sequence file for the bam file
		SequenceFile sortedBamFile = SequenceFile.copy(file);
		sortedBamFile.setPath(finalPath);
		sortedBamFile.setName(FilenameUtils.getName(finalPath));
		sortedBamFile.setType(SequenceFileType.SORTED_BAM);
		sortedBamFile.setStatus(SequenceFileStatus.NEW);
		repository.save(sortedBamFile);
	}

	@Override
	public String getName() {
		return "SamTools Sort BAM Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Sort BAM In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Sort BAM Completed";
	}
}
