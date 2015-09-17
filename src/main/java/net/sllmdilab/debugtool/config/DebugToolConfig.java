package net.sllmdilab.debugtool.config;

import java.net.URI;

import net.sllmdilab.commons.database.MLDBClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;

@Configuration
public class DebugToolConfig {

	@Value("${DT_DATABASE_HOST}")
	private String databaseHost;

	@Value("${DT_DATABASE_PORT}")
	private String databasePort;

	@Value("${DT_DATABASE_USER}")
	private String databaseUser;

	@Value("${DT_DATABASE_PASSWORD}")
	private String databasePassword;

	@Value("${DT_DATABASE_NAME}")
	private String databaseName;

	@Bean
	public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
		PropertyPlaceholderConfigurer placeholderConfigurer = new PropertyPlaceholderConfigurer();
		placeholderConfigurer.setSystemPropertiesModeName("SYSTEM_PROPERTIES_MODE_OVERRIDE");
		return placeholderConfigurer;
	}

	@Bean
	public ContentSource contentSource() throws Exception {

		URI uri = new URI("xcc://" + databaseUser + ":" + databasePassword + "@" + databaseHost + ":" + databasePort
				+ "/" + databaseName);

		return ContentSourceFactory.newContentSource(uri);
	}
	
	@Bean
	public MLDBClient mldbClient() throws Exception {
		return new MLDBClient(contentSource());
	}
}
