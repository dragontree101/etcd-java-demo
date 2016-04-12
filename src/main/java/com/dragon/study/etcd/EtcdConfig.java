package com.dragon.study.etcd;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

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
    EtcdClientHolder.setEtcdClient(new EtcdClient());
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
