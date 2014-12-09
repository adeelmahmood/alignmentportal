package org.jhu.metagenomics.alignmentportal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Set;

import javax.servlet.MultipartConfigElement;

import org.jhu.metagenomics.alignmentportal.utils.AppUtils;
import org.jhu.metagenomics.alignmentportal.utils.Constants;
import org.jhu.metagenomics.alignmentportal.utils.GenomicsUtils;
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
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.genomics.Genomics;
import com.google.api.services.storage.Storage;
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

	@Value("${google.genomics.service.account.file}")
	private String serviceAccountFile;
	
	@Value("${google.genomics.service.account.email}")
	private String serviceAccountEmail;
	
	@Value("${client.secrets.file:client_secrets.json}")
	private String clientSecretsFile;

	@Value("${google.api.key:}")
	private String apiKey;

	@Bean
	public Genomics genomics() throws IOException, GeneralSecurityException {
		GenomicsFactory factory = GenomicsFactory.builder(Constants.APP_NAME).setScopes(GenomicsUtils.getScopes())
				.setUserName("user" + GenomicsUtils.getScopes().toString())
				// .setVerificationCodeReceiver(Suppliers.ofInstance(newGooglePromptReceiver()))
				.setRootUrl(Constants.GENOMICS_ROOT_URL).setServicePath("/").build();
		// return factory.fromApiKey(apiKey);
		return factory.fromServiceAccount(serviceAccountEmail, AppUtils.loadFile(serviceAccountFile));
//		return factory.fromClientSecretsFile(AppUtils.loadFile(clientSecretsFile));
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
//		return GoogleNetHttpTransport.newTrustedTransport();
		return new ApacheHttpTransport();
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
	public GoogleCredential googleCredential() throws GeneralSecurityException, IOException {
		GoogleCredential credentials = new GoogleCredential.Builder().setTransport(httpTransport())
			    .setJsonFactory(jacksonFactory())
			    .setServiceAccountId(serviceAccountEmail)
			    .setServiceAccountScopes(GenomicsUtils.getAllScopes())
			    .setServiceAccountPrivateKeyFromP12File(AppUtils.loadFile(serviceAccountFile))
			    .build();
		return credentials;
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
//		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport(), jacksonFactory(),
//				clientSecrets, scopes).setDataStoreFactory(dataStoreFactory()).build();
//		// Authorize.
//		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user" + scopes.toString());
		return googleCredential();
	}
}