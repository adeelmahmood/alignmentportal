package org.jhu.metagenomics.alignmentportal.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.h2.util.IOUtils;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileStatus;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFile.SequenceFileType;
import org.jhu.metagenomics.alignmentportal.domain.SequenceFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

	private static final Logger log = LoggerFactory.getLogger(FileController.class);

	@Value("${file.upload.path}")
	private String uploadPath;
	
	private final SequenceFileRepository repository;

	@Autowired
	public FileController(SequenceFileRepository repository) {
		this.repository = repository;
	}

	@RequestMapping("/download")
	public void download(@RequestParam("file") String filename, HttpServletResponse response) throws IOException {
		File file = new File(filename);
		// set response
		response.setContentLength(new Long(file.length()).intValue());
		response.setHeader("Content-Disposition", "attachment; filename=" + FilenameUtils.getName(filename));
		try {
			FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());
		} catch (IOException e) {
			log.error("error in downloading file", e);
		}
		return;
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, @RequestParam String dataset,
			@RequestParam String fileType) throws IOException {
		// figure out upload path based on given params
		String filePath = getDirPath(dataset, fileType) + file.getOriginalFilename();
		// upload file
		File uploadedFile = uploadFile(filePath, file);
		// once the file has been successfully uploaded we can create a job
		// request for it to be processed
		if (uploadedFile != null) {
			// save file entries in repository
			SequenceFile seqFile = createNewSequenceFileEntry(dataset, fileType, uploadedFile);
			return new ResponseEntity<>(seqFile, HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>("unable to upload file " + file.getOriginalFilename(), HttpStatus.NOT_FOUND);
	}

	private File uploadFile(String fileName, MultipartFile file) {
		if (!file.isEmpty()) {
			try {
				InputStream is = file.getInputStream();
				File uploadFile = new File(fileName);
				// byte[] bytes = file.getBytes();
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(uploadFile));
				IOUtils.copy(is, stream);
				// stream.write(bytes);
				stream.close();
				is.close();
				log.debug("Successfully uploaded " + file.getOriginalFilename() + " into " + uploadFile.getPath());
				return uploadFile;
			} catch (Exception e) {
				log.error("error in uploading file", e);
			}
		} else {
			log.warn("uploaded file " + file.getOriginalFilename() + " is empty");
		}
		return null;
	}

	private SequenceFile createNewSequenceFileEntry(String dataset, String fileType, File file) {
		SequenceFile seqFile = new SequenceFile();
		seqFile.setDataset(dataset);
		seqFile.setPath(file.getAbsolutePath());
		seqFile.setName(file.getName());
		seqFile.setStatus(SequenceFileStatus.NEW);
		seqFile.setType("refGenome".equals(fileType) ? SequenceFileType.REFERENCE
				: fileType.equals("sampleSeq") ? SequenceFileType.SAMPLE : SequenceFileType.UNKNOWN);
		seqFile = repository.save(seqFile);
		log.info("sequence file " + seqFile + " saved");
		return seqFile;
	}

	private String getDirPath(String dataset, String fileType) throws IOException {
		String dir = uploadPath + dataset + "/" + UUID.randomUUID() + "/";
		FileUtils.forceMkdir(new File(dir));
		return dir;
	}
}