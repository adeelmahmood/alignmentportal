package org.jhu.metagenomics.alignmentportal.misc;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.Channels;

import org.junit.Test;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

public class MiscTests {

	@Test
	public void test() {
		String path = "aligned.sam";
		String newPath = path.replaceAll(".sam", ".bam");
		System.out.println(newPath);
	}

	@Test
	public void upload() throws IOException {
		String content = "abc";
		GcsService service = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
		GcsOutputChannel channel = service.createOrReplace(new GcsFilename("mg-alignment-portal-bucket", "abc.txt"),
				GcsFileOptions.getDefaultInstance());
		ObjectOutputStream out = new ObjectOutputStream(Channels.newOutputStream(channel));
		out.writeObject(content);
		out.close();
	}

}
