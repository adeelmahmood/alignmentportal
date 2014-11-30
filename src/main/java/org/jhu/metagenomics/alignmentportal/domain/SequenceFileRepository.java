package org.jhu.metagenomics.alignmentportal.domain;

import java.util.List;

import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenceFileRepository extends JpaRepository<SequenceFile, Long> {

	List<SequenceFile> findByStatus(SequenceFileStatus status);

	List<SequenceFile> findByStatusAndType(SequenceFileStatus status, SequenceFileType type);

	SequenceFile findByDatasetAndStatusAndType(String dataset, SequenceFileStatus status, SequenceFileType type);

}
