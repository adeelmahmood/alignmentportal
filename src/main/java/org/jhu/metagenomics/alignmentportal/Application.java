package org.jhu.metagenomics.alignmentportal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.MultipartConfigElement;

import org.jhu.metagenomics.alignmentportal.utils.AppUtils;
import org.jhu.metagenomics.alignmentportal.utils.Constants;
import org.jhu.metagenomics.alignmentportal.utils.GenomicsUtils;
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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Data;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.QueryRequest;
import com.google.api.services.bigquery.model.QueryResponse;
import com.google.api.services.bigquery.model.TableCell;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.genomics.Genomics;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
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
		GenomicsFactory factory = GenomicsFactory.builder(Constants.APP_NAME).setScopes(GenomicsUtils.getScopes())
				.setUserName("user" + GenomicsUtils.getScopes().toString())
				// .setVerificationCodeReceiver(Suppliers.ofInstance(new
				// GooglePromptReceiver()))
				.setRootUrl(Constants.GENOMICS_ROOT_URL).setServicePath("/").build();
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

	@Bean
	public JacksonFactory jacksonFactory() {
		return JacksonFactory.getDefaultInstance();
	}

	@Bean
	public HttpTransport httpTransport() throws GeneralSecurityException, IOException {
		return GoogleNetHttpTransport.newTrustedTransport();
	}

	@Bean
	public FileDataStoreFactory dataStoreFactory() throws IOException {
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home"),
				String.format(".store/%s", Constants.APP_NAME.replace("/", "_"))));
		return dataStoreFactory;
	}

	@Bean
	public GoogleClientSecrets googleClientSecrets() throws FileNotFoundException, IOException {
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jacksonFactory(), new InputStreamReader(
				new FileInputStream(AppUtils.loadFile(clientSecretsFile))));
		return clientSecrets;
	}

	@Bean
	public Bigquery bigQuery() throws FileNotFoundException, IOException, GeneralSecurityException {
		Credential credential = getCredentials(googleClientSecrets(), GenomicsUtils.getBigqueryScopes());
		return new Bigquery.Builder(httpTransport(), jacksonFactory(), credential).setApplicationName(
				Constants.APP_NAME).build();
	}

	@Bean
	public Storage storage() throws FileNotFoundException, IOException, GeneralSecurityException {
		Credential credential = getCredentials(googleClientSecrets(), GenomicsUtils.getStorageScopes());
		return new Storage.Builder(httpTransport(), jacksonFactory(), credential)
				.setApplicationName(Constants.APP_NAME).build();
	}

	private Credential getCredentials(GoogleClientSecrets clientSecrets, Set<String> scopes) throws IOException,
			GeneralSecurityException {
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport(), jacksonFactory(),
				clientSecrets, scopes).setDataStoreFactory(dataStoreFactory()).build();
		// Authorize.
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user" + scopes.toString());
	}

	@Autowired
	Genomics genomics;
	@Autowired
	Bigquery bigquery;
	@Autowired
	Storage storage;
	@Value("${google.genomics.project.number}")
	String projectNumber;
	@Value("${google.genomics.storage.bucket}")
	String bucket;

	@PostConstruct
	public void init() throws IOException {
		// test storage
//		Storage.Buckets.Get getBucket = storage.buckets().get(bucket);
//		getBucket.setProjection("full");
//		Bucket bucket = getBucket.execute();
//		System.out.println("name: " + bucket);
//		System.out.println("location: " + bucket.getLocation());
//		System.out.println("timeCreated: " + bucket.getTimeCreated());
//		System.out.println("owner: " + bucket.getOwner());

		// test bigquery
		QueryRequest req = new QueryRequest()
				.setQuery("select reference_name from apds._______________________1417982909670");
		QueryResponse resp = bigquery.jobs().query(projectNumber, req).execute();
		if (resp.getJobComplete()) {
			printRows(resp.getRows(), System.out);
		}
	}

	private static void printRows(List<TableRow> rows, PrintStream out) {
		if (rows != null) {
			for (TableRow row : rows) {
				for (TableCell cell : row.getF()) {
					// Data.isNull() is the recommended way to check for the
					// 'null object' in TableCell.
					out.printf("%s, ", Data.isNull(cell.getV()) ? "null" : cell.getV().toString());
				}
				out.println();
			}
		}
	}
}
