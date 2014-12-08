package org.jhu.metagenomics.alignmentportal.jobs;

import java.util.Arrays;
import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.jhu.metagenomics.alignmentportal.utils.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.appengine.repackaged.org.joda.time.DateTime;

@Component
public class UploadToGCSWithGSUtilJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(UploadToGCSWithGSUtilJob.class);

	private final static String CMD = "./gsutil";

	@Value("${gsutil.path}")
	private String gsutilPath;

	@Value("${google.genomics.storage.bucket}")
	private String bucket;

	private final SequenceFileRepository repository;

	@Autowired
	public UploadToGCSWithGSUtilJob(SequenceFileRepository repository) {
		this.repository = repository;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("uploading to google cloud storage file " + file.getName() + " to bucket " + bucket);

		String cloudPath = "gs://" + bucket + "/" + file.getDataset() + "/" + file.getType() + "/"
				+ DateTime.now().getMillis() + "-" + file.getName();

		// upload to GCS storage command
		List<String> commands = Arrays.asList(CMD, "cp", file.getPath(), cloudPath);
		int status = AppUtils.executeCommands(commands, gsutilPath);
		if (status != 0) {
			throw new JobProcessingFailedException("google cloud storage upload failed with return status " + status);
		}

		file.setInfo("File uploaded to Google Cloud Storage");

		// create a new seq file for cloud file
		SequenceFile cloudFile = SequenceFile.copy(file);
		cloudFile.setPath(cloudPath);
		cloudFile.setInfo("");
		cloudFile.setStatus(SequenceFileStatus.NEW_IN_GOOGLE_CLOUD);
		repository.save(cloudFile);
	}

	@Override
	public String getName() {
		return "Upload to Google Cloud Storage Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Upload to GCS In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Upload to GCS Completed";
	}
}
