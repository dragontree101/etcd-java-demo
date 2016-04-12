package com.dragon.study.etcd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by dragon on 16/4/12.
 */
@SpringBootApplication
@EnableScheduling
public class Bootstrap {

  public static void main(String[] args) throws Exception {
    SpringApplication app = new SpringApplication(Bootstrap.class);
    app.run(args);
  }
}
