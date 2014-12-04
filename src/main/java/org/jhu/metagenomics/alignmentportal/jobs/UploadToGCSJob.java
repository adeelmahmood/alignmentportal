package org.jhu.metagenomics.alignmentportal.jobs;

import java.io.FileInputStream;
import java.nio.channels.Channels;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;

@Component
public class UploadToGCSJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(UploadToGCSJob.class);

	@Value("${gcs.bucket}")
	private String bucket;

	private final GcsService service;

	@Autowired
	public UploadToGCSJob(GcsService service) {
		this.service = service;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("uploading to google cloud storage file " + file.getName() + " to bucket " + bucket);

		try {
			log.info("1");
			// create a fcs filename
			GcsFilename filename = new GcsFilename(bucket, file.getName());
			log.info("2 " + service);
			log.info("2.1 " + filename);
			log.info("2.2 " + GcsFileOptions.getDefaultInstance());
			// get the writable channel
			GcsOutputChannel outputChannel = service.createOrReplace(filename, GcsFileOptions.getDefaultInstance());
			log.info("3");
			// write to stream
			log.info("4");
			FileCopyUtils.copy(new FileInputStream(file.getPath()), Channels.newOutputStream(outputChannel));
			log.info("5");
		} catch (Exception e) {
			log.error("error in uploading to gcs", e);
			throw new JobProcessingFailedException("error in uploading to gcs", e);
		}
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
