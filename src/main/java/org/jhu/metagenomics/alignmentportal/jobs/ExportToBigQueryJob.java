package org.jhu.metagenomics.alignmentportal.jobs;

import java.io.IOException;

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
import com.google.api.services.genomics.model.ExportVariantSetRequest;

@Component
public class ExportToBigQueryJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(ExportToBigQueryJob.class);

	@Value("${google.genomics.project.number}")
	private long projectNumber;

	@Value("${google.bigquery.dataset}")
	private String dataset;

	private final Genomics genomics;
	private final SequenceFileRepository repository;

	@Autowired
	public ExportToBigQueryJob(Genomics genomics, SequenceFileRepository repository) {
		this.genomics = genomics;
		this.repository = repository;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("exporting to big query " + file);

		String variantSetId = file.getDataset();
		String bqDataset = dataset;
		String bgTable = variantSetId;
		try {
			log.debug("exporting variant set " + variantSetId + " to big query " + bqDataset + ":" + bgTable);
			ExportVariantSetRequest req = new ExportVariantSetRequest().setProjectNumber(projectNumber)
					.setBigqueryDataset(bqDataset).setBigqueryTable(bgTable);
			String jobId = genomics.variantsets().export(variantSetId, req).execute().getJobId();
			log.debug("export to bg job submitted " + jobId + ", now polling");

			// now continue to poll until the job completes
			com.google.api.services.genomics.model.Job job = GenomicsUtils.pollUntilJobCompleted(genomics, jobId,
					repository, file);
			// make sure job didnt fail
			if (!GenomicsUtils.isSuccessful(job)) {
				throw new JobProcessingFailedException("export to big query ended with " + job.getStatus()
						+ " status. Details " + job.toPrettyString());
			}

			// indicate completion info on job
			file.setInfo("Exported to Big Query. {" + bqDataset + ":" + bgTable + "}");
			// update file path to point to big query
			file.setPath(bqDataset + ":" + bgTable);
		} catch (IOException e) {
			log.error("error in export to big query", e);
			throw new JobProcessingFailedException("error in export to big query", e);
		}

	}

	@Override
	public String getName() {
		return "Export to Big Query Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Export to Big Query In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Export to Big Query Completed";
	}
}
