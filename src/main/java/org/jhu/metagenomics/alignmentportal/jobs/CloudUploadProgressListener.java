package org.jhu.metagenomics.alignmentportal.jobs;

import java.io.IOException;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

public class CloudUploadProgressListener implements MediaHttpUploaderProgressListener {

	@Override
	public void progressChanged(MediaHttpUploader uploader) throws IOException {
		
		System.out.println("Cloud progress " + uploader.getNumBytesUploaded() + ", " + uploader.getProgress() + ", " + uploader.getChunkSize());
		
	}

}
