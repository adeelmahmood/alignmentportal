package org.jhu.metagenomics.alignmentportal.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.GenomicsScopes;
import com.google.api.services.genomics.model.Job;
import com.google.api.services.storage.StorageScopes;

public class GenomicsUtils {

	private static final Logger log = LoggerFactory.getLogger(GenomicsUtils.class);

	private static final String JOB_SUCCESS = "success";
	private static final String JOB_FAILURE = "failure";
	private static final int DELAY = 10000;

	public static List<String> getScopes() {
		List<String> scopes = new ArrayList<String>();
		scopes.add(GenomicsScopes.GENOMICS);
		scopes.add(GenomicsScopes.DEVSTORAGE_READ_WRITE);
		scopes.add(GenomicsScopes.GENOMICS_READONLY);
		scopes.add(GenomicsScopes.BIGQUERY);
		return scopes;
	}

	public static Set<String> getBigqueryScopes() {
		Set<String> scopes = new HashSet<String>();
		scopes.add(BigqueryScopes.BIGQUERY);
		return scopes;
	}

	public static Set<String> getStorageScopes() {
		Set<String> scopes = new HashSet<String>();
		scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);
		scopes.add(StorageScopes.DEVSTORAGE_READ_ONLY);
		scopes.add(StorageScopes.DEVSTORAGE_READ_WRITE);
		return scopes;
	}

	public static Job pollUntilJobCompleted(Genomics genomics, String jobId, SequenceFileRepository repository,
			SequenceFile file) throws IOException {
		// now continue to poll until the job completes
		Genomics.Jobs.Get jobRequest = genomics.jobs().get(jobId);
		com.google.api.services.genomics.model.Job job = jobRequest.execute();
		int secsPassed = 0;
		while (!isCompleted(job)) {
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			secsPassed += DELAY / 1000;

			// update file info
			file.setInfo(getJobStatus(job) + ", started " + secsPassed + " secs ago. processing ...");
			file = repository.saveAndFlush(file);

			try {
				job = jobRequest.execute();
			} catch (GoogleJsonResponseException e) {
				// Occasionally the API will fail when getting job status.
				// We'll just ignore and retry in a bit
				// We know the Job itself should be valid because the very
				// first job fetch isn't
				// in a special catch block.
			}
		}
		log.debug("job " + job.getId() + " completed with " + job.getStatus() + " status");
		return job;
	}

	public static boolean isCompleted(com.google.api.services.genomics.model.Job job) {
		return JOB_SUCCESS.equalsIgnoreCase(job.getStatus()) || JOB_FAILURE.equalsIgnoreCase(job.getStatus());
	}

	public static boolean isSuccessful(com.google.api.services.genomics.model.Job job) {
		return JOB_SUCCESS.equalsIgnoreCase(job.getStatus());
	}

	public static String getJobStatus(com.google.api.services.genomics.model.Job job) {
		return job.getDetailedStatus() != null ? job.getDetailedStatus() : "Import Reads: " + job.getStatus();
	}

}
