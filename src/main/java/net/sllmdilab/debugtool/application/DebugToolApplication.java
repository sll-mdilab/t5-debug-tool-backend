package net.sllmdilab.debugtool.application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("net.sllmdilab.debugtool.*")
public class DebugToolApplication extends SpringBootServletInitializer {

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DebugToolApplication.class);
    }
	
	public static void main(String[] args) {
        SpringApplication.run(DebugToolApplication.class, args);
    }
}
