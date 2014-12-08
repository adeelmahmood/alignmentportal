package org.jhu.metagenomics.alignmentportal.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.repackaged.org.joda.time.DateTime;

@Component
public class UploadToGCSJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(UploadToGCSJob.class);

	@Value("${google.genomics.storage.bucket}")
	private String bucket;

	private final Storage storage;
	private final SequenceFileRepository repository;

	@Autowired
	public UploadToGCSJob(Storage storage, SequenceFileRepository repository) {
		this.storage = storage;
		this.repository = repository;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("uploading to google cloud storage file " + file.getName() + " to bucket " + bucket);

		String cloudPath = file.getDataset() + "/" + file.getType() + "/" + DateTime.now().getMillis() + "-"
				+ file.getName();
		String completeCloudPath = "gs://" + bucket + "/" + cloudPath;
		try {
			// create new storage object
			StorageObject object = new StorageObject();
			object.setBucket(bucket);

			// read from file and upload to GCS
			File fileObj = new File(file.getPath());
			InputStream stream = new FileInputStream(fileObj);
			try {
				String contentType = URLConnection.guessContentTypeFromStream(stream);
				InputStreamContent content = new InputStreamContent(contentType, stream);
				
				Storage.Objects.Insert insert = storage.objects().insert(bucket, null, content);
				insert.setName(cloudPath);

				insert.execute();
			} finally {
				stream.close();
			}
			file.setInfo("File uploaded to Google Cloud Storage");

			// create a new seq file for cloud file
			SequenceFile cloudFile = SequenceFile.copy(file);
			cloudFile.setPath(completeCloudPath);
			cloudFile.setInfo("");
			cloudFile.setStatus(SequenceFileStatus.NEW_IN_GOOGLE_CLOUD);
			repository.save(cloudFile);
		} catch (IOException e) {
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
