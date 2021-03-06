package org.jhu.metagenomics.alignmentportal.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.joda.time.DateTime;

@Entity
public class SequenceFile {

	@Id
	@GeneratedValue
	private long id;

	private String dataset;
	private String name;
	private String path;
	private SequenceFileType type;
	private SequenceFileStatus status;
	@Column(length = 4000)
	private String info;

	private Date created = DateTime.now().toDate();

	@Override
	public String toString() {
		return "SequenceFile [id=" + id + ", dataset=" + dataset + ", path=" + path + ", type=" + type + ", status="
				+ status + ", info=" + info + ", created=" + created + "]";
	}

	public String toStringMin() {
		return "SeqFile [dataset=" + dataset + ", name=" + name + ", type=" + type + ", status=" + status + "]";
	}

	public static SequenceFile copy(SequenceFile from) {
		SequenceFile file = new SequenceFile();
		file.setDataset(from.getDataset());
		file.setName(from.getName());
		file.setPath(from.getPath());
		file.setStatus(from.getStatus());
		file.setType(from.getType());
		file.setInfo(from.getInfo());
		return file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public static enum SequenceFileType {
		REFERENCE, SAMPLE, UNKNOWN, ALIGNED, BAM, SORTED_BAM, VARIANTS
	}

	public static enum SequenceFileStatus {
		NEW("New"), IN_PROGRESS("In Progress"), DECOMPRESS_IN_PROGRESS("Decompression In Progress"), DECOMPRESS_COMPLETED(
				"Decompression Completed"), BOWTIE_BUILDING_REFERENCE_INDEX("Building Reference Index"), BOWTIE_REFERENCE_INDEX_COMPLETED(
				"Reference Index Completed"), BOWTIE_ALIGNMENT_IN_PROGRESS("Alignment In Progress"), BOWTIE_ALIGNMENT_COMPLETED(
				"Alignment Completed"), SAMTOOLS_SAM_TO_BAM_IN_PROGRESS("SAM to BAM In Progress"), SAMTOOLS_SAM_TO_BAM_COMPELTED(
				"SAM to BAM Completed"), SAMTOOLS_SORT_BAM_IN_PROGRESS("Sort BAM In Progress"), SAMTOOLS_SORT_BAM_COMPLETED(
				"Sort BAM Completed"), VARIANTS_FILE_IN_PROGRESS("Variants File In Progress"), VARIANTS_FILE_COMPLETED(
				"Variants File Completed"), UPLOAD_TO_GCS_IN_PROGRESS("Upload to GCS In Progress"), UPLOAD_TO_GCS_COMPLETED(
				"Upload to GCS Completed"), FAILED("Failed"), READY("Ready"), NEW_IN_GOOGLE_CLOUD("New In Google Cloud"), IMPORT_READS_IN_PROGRESS(
				"Import Reads In Progress"), IMPORT_READS_JOB_SUBMITTED("Import Reads Job Submitted"), IMPORT_READS_COMPLETED(
				"Import Reads Completed"), IMPORT_VARIANTS_IN_PROGRESS("Import Variants In Progress"), IMPORT_VARIANTS_COMPLETED(
				"Import Variants Completed"), EXPORT_TO_BIG_QUERY_IN_PROGRESS("Export to Big Query In Progress"), EXPORT_TO_BIG_QUERY_COMPLETED(
				"Export to Big Query Completed");

		private String status;

		SequenceFileStatus(String status) {
			this.status = status;
		}

		public String toString() {
			return status;
		}

		public String getStatus() {
			return status;
		}

		public static SequenceFileStatus fromStatus(String status) {
			if (status != null && !status.isEmpty()) {
				for (SequenceFileStatus st : SequenceFileStatus.values()) {
					if (st.getStatus().equals(status)) {
						return st;
					}
				}
			}
			return null;
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public SequenceFileType getType() {
		return type;
	}

	public void setType(SequenceFileType type) {
		this.type = type;
	}

	public SequenceFileStatus getStatus() {
		return status;
	}

	public void setStatus(SequenceFileStatus status) {
		this.status = status;
	}

}
