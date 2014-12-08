package org.jhu.metagenomics.alignmentportal.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.repackaged.org.joda.time.DateTime;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class GCSTests {

	@Configuration
	@ComponentScan("org.jhu.metagenomics.alignmentportal")
	@PropertySource("application.properties")
	static class Config {
	}
	
	@Value("${google.genomics.storage.bucket}")
	private String bucket;

	@Autowired
	private Storage storage;

	String file = "/Users/adeel/metagenomics/reads_1.fq";
	
	@Test
	public void testUpload() throws IOException {
		System.out.println("Starting upload");
		long s = DateTime.now().getMillis();
		// create new storage object
		StorageObject object = new StorageObject();
		object.setBucket(bucket);

		// read from file and upload to GCS
		File fileObj = new File(file);
		InputStream stream = new FileInputStream(fileObj);
		try {
			String contentType = URLConnection.guessContentTypeFromStream(stream);
			InputStreamContent content = new InputStreamContent(contentType, stream);

			Storage.Objects.Insert insert = storage.objects().insert(bucket, null, content);
			insert.setName(file);
			insert.getMediaHttpUploader().setDisableGZipContent(true);
			System.out.println(insert);
			
			insert.execute();
		} finally {
			stream.close();
		}
		
		System.out.println("Done in " + (DateTime.now().getMillis()-s) + " ms");
	}

}
