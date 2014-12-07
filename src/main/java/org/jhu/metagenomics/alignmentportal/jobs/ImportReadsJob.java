package org.jhu.metagenomics.alignmentportal.jobs;

import java.io.IOException;
import java.util.Arrays;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.jhu.metagenomics.alignmentportal.utils.GenomicsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.Dataset;
import com.google.api.services.genomics.model.ImportReadGroupSetsRequest;
import com.google.api.services.genomics.model.ReadGroupSet;

@Component
public class ImportReadsJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(ImportReadsJob.class);

	@Value("${google.genomics.storage.bucket}")
	private String bucket;

	private final Genomics genomics;
	private final SequenceFileRepository repository;

	@Autowired
	public ImportReadsJob(Genomics genomics, SequenceFileRepository repository) {
		this.genomics = genomics;
		this.repository = repository;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("importing reads from file " + file);

		try {
			// get the dataset
			Dataset dataset = genomics.datasets().get(file.getDataset()).execute();
			if (dataset == null) {
				throw new JobProcessingFailedException("no dataset found for " + file.getDataset());
			}

			// create a request for import
			Genomics.Readgroupsets.GenomicsImport req = genomics.readgroupsets().genomicsImport(
					new ImportReadGroupSetsRequest().setDatasetId(file.getDataset()).setSourceUris(
							Arrays.asList(file.getPath())));
			// get job id
			String jobId = req.execute().getJobId();
			log.debug("import read job submitted with job id " + jobId + " now polling ...");

			// now continue to poll until the job completes
			com.google.api.services.genomics.model.Job job = GenomicsUtils.pollUntilJobCompleted(genomics, jobId,
					repository, file);
			// make sure job didnt fail
			if (!GenomicsUtils.isSuccessful(job)) {
				throw new JobProcessingFailedException("import reads job ended with " + job.getStatus()
						+ " status. Details " + job.toPrettyString());
			}

			// capture imported read set ids
			if (job.getImportedIds() != null) {
				String ids = "";
				for (String id : job.getImportedIds()) {
					ReadGroupSet readGroupSet = genomics.readgroupsets().get(id).setFields("id,name,filename")
							.execute();
					log.debug("Imported read set " + readGroupSet.toPrettyString());
					ids += readGroupSet.getId() + ",";
				}
				file.setInfo("Imported as readset. {readgroupsets:" + ids.substring(0, ids.length() - 1) + "}");
				repository.save(file);
			}
		} catch (IOException e) {
			log.error("error in importing reads", e);
		}

	}

	@Override
	public String getName() {
		return "Import Reads from GCS to Genomics Dataset Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Import Reads In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Import Reads Completed";
	}
}
