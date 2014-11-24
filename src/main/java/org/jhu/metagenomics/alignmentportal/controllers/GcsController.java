package org.jhu.metagenomics.alignmentportal.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.appengine.tools.cloudstorage.GcsService;

@RestController
@RequestMapping("/gcs")
public class GcsController {

	private static final Logger log = LoggerFactory.getLogger(GcsController.class);

	@Value("${google.genomics.storage.bucket}")
	private String storageBucket;

	private final GcsService gcsService;
	
	@Autowired
	public GcsController(GcsService gcsService) {
		this.gcsService = gcsService;
	}
	
	@RequestMapping("/upload")
	public void upload() {
		
	}
	
}
