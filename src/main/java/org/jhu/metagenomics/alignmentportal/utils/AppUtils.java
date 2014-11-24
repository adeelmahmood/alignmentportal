package org.jhu.metagenomics.alignmentportal.utils;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class AppUtils {

	public static File loadFile(String filePath) throws IOException {
		Resource r;
		if(filePath.startsWith("classpath")){
			r = new ClassPathResource(filePath.replace("classpath:", ""));
		}
		else{
			r = new FileSystemResource(filePath);
		}
		if(!r.exists()) {
			throw new RuntimeException("file " + filePath + " does not exists");
		}
		return r.getFile();
	}
}
