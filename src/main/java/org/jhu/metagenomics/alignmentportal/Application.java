package org.jhu.metagenomics.alignmentportal;

import java.io.IOException;
import java.security.GeneralSecurityException;

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

import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.extensions.java6.auth.oauth2.GooglePromptReceiver;
import com.google.api.services.genomics.Genomics;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.cloud.genomics.utils.GenomicsFactory;
import com.google.common.base.Suppliers;

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
	
	@Bean
	public Genomics genomics() throws IOException, GeneralSecurityException{
		GenomicsFactory factory = GenomicsFactory.builder(Constants.APP_NAME)
				.setScopes(GenomicsUtils.getScopes())
				.setUserName("user" + GenomicsUtils.getScopes().toString())
//				.setVerificationCodeReceiver(Suppliers.ofInstance(new GooglePromptReceiver()))
				.setRootUrl(Constants.GENOMICS_ROOT_URL)
				.setServicePath("/")
				.build();
		return factory.fromApiKey("AIzaSyCk0xKvEGYyEjQYZ_l_EnwIsYKc34MIrAs");
//		return factory.fromClientSecretsFile(AppUtils.loadFile(clientSecretsFile));
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
}
