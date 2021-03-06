package org.jhu.metagenomics.alignmentportal.jobs;

import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class JobProcessorImpl implements JobProcessor {

	private static final Logger log = LoggerFactory.getLogger(JobProcessorImpl.class);

	private final ApplicationContext context;
	private final SequenceFileRepository repository;

	@Autowired
	public JobProcessorImpl(SequenceFileRepository repository, ApplicationContext context) {
		this.repository = repository;
		this.context = context;
	}

	@Async
	public void processJobForFiles(List<SequenceFile> files, Class<? extends Job> job) {
		for(SequenceFile file : files) {
			System.out.println("Received file " + file);
		}
		try {
			Job jobBean = context.getBean(job);
			for (SequenceFile file : files) {
				// set in progress status
				file.setStatus(SequenceFileStatus.fromStatus(jobBean.getInProgressStatus()));
				// set job as owner of this file
				file.setInfo(jobBean.getName() +" processing ...");
				repository.saveAndFlush(file);
			}
			for (SequenceFile file : files) {
				try {
					// process the file
					context.getBean(job).process(file);
					// set completed status
					file.setStatus(SequenceFileStatus.fromStatus(jobBean.getCompletedStatus()));
					repository.save(file);
					log.debug("completed job [" + jobBean.getName() + "] on file " + file.toStringMin());
				} catch (JobProcessingFailedException e) {
					// set failed status
					file.setStatus(SequenceFileStatus.FAILED);
					file.setInfo(e.getMessage());
					repository.save(file);
				}
			}
		} catch (BeansException e) {
			log.error("error in getting job bean", e);
		} catch (Exception e) {
			log.error("unexpected error", e);
		}
	}
}
