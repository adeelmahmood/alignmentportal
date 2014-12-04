package org.jhu.metagenomics.alignmentportal.domain;

import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SequenceFileRepository extends JpaRepository<SequenceFile, Long> {

	List<SequenceFile> findByDatasetOrderByCreatedDesc(String dataset);
	
	List<SequenceFile> findByStatus(SequenceFileStatus status);

	List<SequenceFile> findByDatasetAndStatus(String dataset, SequenceFileStatus status);

	List<SequenceFile> findByStatusAndType(SequenceFileStatus status, SequenceFileType type);

	SequenceFile findByDatasetAndStatusAndType(String dataset, SequenceFileStatus status, SequenceFileType type);
	
	@Query("select distinct dataset from SequenceFile")
	List<String> getDatasetDistinctByDatasetOrderByCreatedDesc();

}
