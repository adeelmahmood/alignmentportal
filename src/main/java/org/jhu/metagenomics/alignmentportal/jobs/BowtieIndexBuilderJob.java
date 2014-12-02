package org.jhu.metagenomics.alignmentportal.jobs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.jhu.metagenomics.alignmentportal.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BowtieIndexBuilderJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(BowtieIndexBuilderJob.class);
	
	public final static String CMD = "./bowtie2-build";
	public final static String REF = "ref";

	@Value("${bowtie.path}")
	private String bowtiePath;
	
	@Value("${bowtie.reference.index.dir}")
	private String indexDir;
	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("building reference index from file " + file);

		// get working directory
		String parentDir = new File(file.getPath()).getParent();
		// create indexes directory
		String indexesDir = FilenameUtils.concat(parentDir, indexDir);
		try {
			FileUtils.forceMkdir(new File(indexesDir));
		} catch (IOException e) {
			log.error("error in creating indexes dir", e);
		}

		// reference index build command
		List<String> commands = Arrays.asList(CMD, file.getPath(), FilenameUtils.concat(indexesDir, REF));
		int status = AppUtils.executeCommands(commands, bowtiePath);
		if(status != 0) {
			throw new JobProcessingFailedException("reference index build command failed with return status " + status);
		}
	}

	@Override
	public String getName() {
		return "Bowtie Reference Index Builder Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Building Reference Index";
	}

	@Override
	public String getCompletedStatus() {
		return "Reference Index Completed";
	}
}
