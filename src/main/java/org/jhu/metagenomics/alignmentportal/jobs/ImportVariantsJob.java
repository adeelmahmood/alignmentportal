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
import com.google.api.services.genomics.model.ImportVariantsRequest;
import com.google.api.services.genomics.model.VariantSet;

@Component
public class ImportVariantsJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(ImportVariantsJob.class);

	private static String fileFormat = "vcf";

	@Value("${google.genomics.storage.bucket}")
	private String bucket;

	private final Genomics genomics;
	private final SequenceFileRepository repository;

	@Autowired
	public ImportVariantsJob(Genomics genomics, SequenceFileRepository repository) {
		this.genomics = genomics;
		this.repository = repository;
	}

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		log.debug("importing variants from file " + file);

		try {
			// get the dataset
			Dataset dataset = genomics.datasets().get(file.getDataset()).execute();
			if (dataset == null) {
				throw new JobProcessingFailedException("no dataset found for " + file.getDataset());
			}

			// create a request for import
			Genomics.Variantsets.ImportVariants req = genomics.variantsets().importVariants(file.getDataset(),
					new ImportVariantsRequest().setSourceUris(Arrays.asList(file.getPath())).setFormat(fileFormat));
			// get job id
			String jobId = req.execute().getJobId();
			log.debug("import variants job submitted with job id " + jobId + " now polling ...");

			// now continue to poll until the job completes
			com.google.api.services.genomics.model.Job job = GenomicsUtils.pollUntilJobCompleted(genomics, jobId,
					repository, file);
			// make sure job didnt fail
			if (!GenomicsUtils.isSuccessful(job)) {
				throw new JobProcessingFailedException("import reads job ended with " + job.getStatus()
						+ " status. Details " + job.toPrettyString());
			}

			// capture imported variant set information
			VariantSet variantSet = genomics.variantsets().get(file.getDataset()).execute();
			log.debug("imported variant set " + variantSet.getId());
			file.setInfo("Imported as variant set. {variantsset:" + variantSet.getId() + "}");
			repository.save(file);
		} catch (IOException e) {
			log.error("error in importing reads", e);
		}
	}

	@Override
	public String getName() {
		return "Import Variants from GCS to Genomics Dataset Job";
	}

	@Override
	public String getInProgressStatus() {
		return "Import Variants In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Import Variants Completed";
	}
}
