package me;

import me.web.BasicAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;

@SpringBootApplication()
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public Filter basicAuthenticationFilter(@Value("${client.username}") String userName, @Value("${client.password}") String password) {
    /*
     * We can use the Spring security, but our needs are so simple we do it ourselves wihtout the complexity of the security filters
     */
    return new BasicAuthenticationFilter(userName, password);
  }

}

