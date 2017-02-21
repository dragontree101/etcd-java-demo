package com.dragon.study.etcd;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import mousio.etcd4j.EtcdClient;

/**
 * Created by dragon on 16/4/12.
 */
@Configuration
public class EtcdConfig {

  @PostConstruct
  public void initEtcdClient() {
    try {
      EtcdClientHolder.setEtcdClient(new EtcdClient(new URI("http://127.0.0.1:4001")));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @PreDestroy
  public void destoryEtcdClient() {
    try {
      EtcdClient etcdClient = EtcdClientHolder.get();
      if (etcdClient != null) {
        etcdClient.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
