package org.jhu.metagenomics.alignmentportal.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class AppUtils {

	private static final Logger log = LoggerFactory.getLogger(AppUtils.class);

	public static File loadFile(String filePath) throws IOException {
		Resource r;
		if (filePath.startsWith("classpath")) {
			r = new ClassPathResource(filePath.replace("classpath:", ""));
		} else {
			r = new FileSystemResource(filePath);
		}
		if (!r.exists()) {
			throw new RuntimeException("file " + filePath + " does not exists");
		}
		return r.getFile();
	}

	public static int executeCommands(List<String> commands, String workingDirectory) {
		return executeCommands(commands, workingDirectory, false, null);
	}

	public static int executeCommands(List<String> commands, String workingDirectory, boolean captureStdout,
			String stdoutFile) {
		log.debug("Executing commands -> " + Arrays.toString(commands.toArray()) + ", working directory "
				+ workingDirectory);
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(new File(workingDirectory));
		if (!captureStdout) {
			pb.redirectErrorStream(true);
		}
		int retStatus = 1;

		// create sub process to control
		Process p = null;
		try {
			if (captureStdout) {
				pb.redirectOutput(new File(stdoutFile));
			}
			p = pb.start();

			// get IO streams from process to capture output
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				log.debug(line);
			}

		} catch (IOException e) {
			log.error("error in executing commands", e);
		} finally {
			try {
				retStatus = p.waitFor();
			} catch (InterruptedException e) {
				log.error("error in waiting for status for commands", e);
				retStatus = 1;
			}
			try {
				// close IO streams
				p.getInputStream().close();
				p.getOutputStream().close();
				p.getErrorStream().close();
			} catch (IOException e) {
				log.warn("error in closing command process IO stream", e);
			}
		}
		return retStatus;
	}
}
