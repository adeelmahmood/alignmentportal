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

	public final static String CMD = "./bowtie2";

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

		// get reference file parent directory
		String referenceParentDir = new File(reference.getPath()).getParent();
		// get name of reference base
		String referenceIndexDir = FilenameUtils.normalize(referenceParentDir + "/" + indexDir + "/"
				+ BowtieIndexBuilderJob.REF);

		// figure out name
		String alignedFile = file.getPath() + ".aligned.sam";

		// alignment command
		List<String> commands = Arrays.asList(CMD, "-x", referenceIndexDir, "-U", file.getPath(), "-S", alignedFile);
		int status = AppUtils.executeCommands(commands, bowtiePath);
		if(status != 0) {
			throw new JobProcessingFailedException("reference index build command failed with return status " + status);
		}
		
		file.setInfo("Alignment with reference completed");

		//create a new sequence file for the aligned file
		SequenceFile af = SequenceFile.copy(file);
		af.setPath(alignedFile);
		af.setName(FilenameUtils.getName(alignedFile));
		af.setType(SequenceFileType.ALIGNED);
		af.setStatus(SequenceFileStatus.NEW);
		repository.save(af);
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
