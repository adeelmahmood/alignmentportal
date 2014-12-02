package org.jhu.metagenomics.alignmentportal.jobs;

import java.util.Arrays;
import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileType;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.jhu.metagenomics.alignmentportal.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SamToolsSamToBamJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(SamToolsSamToBamJob.class);

	public final static String CMD = "./samtools";

	@Value("${samtools.path}")
	private String samtoolsPath;

	private final SequenceFileRepository repository;

	@Autowired
	public SamToolsSamToBamJob(SequenceFileRepository repository) {
		this.repository = repository;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("converting sam to bam for file " + file);

		// path to bam file
		String newPath = file.getPath().replaceAll(".sam", ".bam");

		// sam to bam command
		List<String> commands = Arrays.asList(CMD, "view", "-bS", file.getPath());
		int status = AppUtils.executeCommands(commands, samtoolsPath, true, newPath);
		if (status != 0) {
			throw new JobProcessingFailedException("samtools convert sam to bam failed with return status " + status);
		}

		// create a new sequence file for the bam file
		SequenceFile bamFile = SequenceFile.copy(file);
		bamFile.setPath(newPath);
		bamFile.setType(SequenceFileType.BAM);
		bamFile.setStatus(SequenceFileStatus.NEW);
		bamFile.setOwner("");
		repository.save(bamFile);
	}

	@Override
	public String getName() {
		return "SamTools Convert SAM to BAM Job";
	}

	@Override
	public String getInProgressStatus() {
		return "SAM to BAM In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "SAM to BAM Completed";
	}
}
