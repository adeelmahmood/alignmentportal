package org.jhu.metagenomics.alignmentportal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.servlet.MultipartConfigElement;

import org.jhu.metagenomics.alignmentportal.utils.AppUtils;
import org.jhu.metagenomics.alignmentportal.utils.Constants;
import org.jhu.metagenomics.alignmentportal.utils.GenomicsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.Genomics.Reads.Search;
import com.google.api.services.genomics.model.SearchReadsRequest;
import com.google.api.services.genomics.model.SearchReadsResponse;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.cloud.genomics.utils.GenomicsFactory;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableJpaRepositories
@EnableSpringDataWebSupport
@EnableScheduling
@EnableAsync
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Value("${client.secrets.file:classpath:client_secrets.json}")
	private String clientSecretsFile;

	@Value("${google.api.key:}")
	private String apiKey;

	@Bean
	public Genomics genomics() throws IOException, GeneralSecurityException {
		GenomicsFactory factory = GenomicsFactory
							.builder(Constants.APP_NAME)
							.setScopes(GenomicsUtils.getScopes())
							.setUserName("user" + GenomicsUtils.getScopes().toString())
							// .setVerificationCodeReceiver(Suppliers.ofInstance(new
							// GooglePromptReceiver()))
							.setRootUrl(Constants.GENOMICS_ROOT_URL)
							.setServicePath("/")
							.build();
		// return factory.fromApiKey(apiKey);
		return factory.fromClientSecretsFile(AppUtils.loadFile(clientSecretsFile));
	}
	
	@Bean
	public GcsService gcsService() {
		return GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(Long.MAX_VALUE);
		factory.setMaxRequestSize(Long.MAX_VALUE);
		factory.setFileSizeThreshold("128KB");
		return factory.createMultipartConfig();
	}
	
	@Autowired Genomics genomics;
	@Value("${google.genomics.project.number}") Long projectNumber;
	private static final Logger log = LoggerFactory.getLogger(Application.class);
	
	@PostConstruct
	public void init() throws IOException {
//		List<Dataset> datasets = genomics.datasets().list().setProjectNumber(projectNumber).execute().getDatasets();
//		List<String> datasetIds = new ArrayList<String>();
//		for(Dataset dataset : datasets) {
//			datasetIds.add(dataset.getId());
//		}
//		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
//		SearchReadGroupSetsRequest request = new SearchReadGroupSetsRequest().setDatasetIds(datasetIds);
//		List<String> readGroupSetIds = new ArrayList<String>();
//		for(GenericJson result : Paginator.ReadGroupSets.create(genomics).search(request)) {
//			log.debug(result + "");
//			readGroupSetIds.add(result.get("id").toString());
//			log.info("id captured as " + result.get("id"));
//		}
//		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
//		SearchReadsRequest req = new SearchReadsRequest().setReadGroupSetIds(readGroupSetIds);
//		for(Object result : Paginator.Reads.create(genomics).search(req)) {
//			log.debug(result + "");
//		}
		
//		SearchReadsRequest req = new SearchReadsRequest()
//									.setReadGroupSetIds(Arrays.asList("CKvp4rmFDxCKp_en5KX_pCE"))
//									.setPageSize(2)
////									.setReferenceName(referenceName)
//									.setPageToken("Ek5DaGREUzNad05ISnRSa1I0UTB0d1gyVnVOVXRZWDNCRFJSSWJaMmw4T1RZeU5qSTBNM3h5WldaOFRrTmZNREF4TkRFMkxqRjhHQUFvQWcYAiDa_fmdDw")
//									;
//		SearchReadsResponse x = genomics.reads().search(req).execute();
//		x.
//		System.out.println(x);
	}
}
