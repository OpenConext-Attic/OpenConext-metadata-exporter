package me;

import me.web.BasicAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.TraceWebFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;

@SpringBootApplication(exclude = {TraceWebFilterAutoConfiguration.class, MetricFilterAutoConfiguration.class})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public Filter basicAuthenticationFilter(@Value("${client.username}") String userName, @Value("${client.password}") String password) {
    /*
     * We can use Spring security, but our needs are so dead simple we do it ourselves without the complexity of the security filters
     */
    return new BasicAuthenticationFilter(userName, password);
  }

}

