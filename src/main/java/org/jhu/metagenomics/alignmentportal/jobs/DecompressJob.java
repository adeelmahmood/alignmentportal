package org.jhu.metagenomics.alignmentportal.jobs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.exceptions.JobProcessingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DecompressJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(DecompressJob.class);

	@Override
	public void process(SequenceFile file) throws JobProcessingFailedException {
		String path = file.getPath();
		String ext = FilenameUtils.getExtension(path);
		if ("gz".equals(ext)) {
			try {
				byte[] buffer = new byte[1024];
				String newPath = path.replace("." + ext, "");
				log.debug("decompressing file " + file);
				GZIPInputStream is = new GZIPInputStream(new FileInputStream(file.getPath()));
				FileOutputStream os = new FileOutputStream(newPath);
				int len;
				while ((len = is.read(buffer)) > 0) {
					os.write(buffer, 0, len);
				}
				os.close();
				is.close();

				// update the file path to unzipped path
				file.setPath(newPath);
			} catch (FileNotFoundException e) {
				log.error("error in decompressing file " + path, e);
				throw new JobProcessingFailedException("error in decompressing file " + path, e);
			} catch (IOException e) {
				log.error("error in decompressing file " + path, e);
				throw new JobProcessingFailedException("error in decompressing file " + path, e);
			}
		}
	}

	@Override
	public String getInProgressStatus() {
		return "Decompression In Progress";
	}

	@Override
	public String getCompletedStatus() {
		return "Decompression Completed";
	}

	@Override
	public String getName() {
		return "Decompression Job";
	}
}
