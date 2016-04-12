package com.dragon.study.etcd;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdKeysResponse;

/**
 * Created by dragon on 16/4/12.
 */
@Component
public class EtcdResource {

//  @Scheduled(fixedDelay = 1000L, initialDelay = 1000L)
  public void getInfo() {
    EtcdClient client = EtcdClientHolder.get();
    System.out.println(
        "cluster is " + client.version().cluster + ", server is " + client.version().server);
  }

//  @Scheduled(fixedDelay = 2000L, initialDelay = 2000L)
  public void getAndSetKeyValue() {
    EtcdClient client = EtcdClientHolder.get();
    try {
      EtcdKeysResponse response = client.put("key1", "value1").send().get();
      System.out.println("value is " + response.node.value);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Scheduled(fixedDelay = 15000L, initialDelay = 1000L)
  public void sendHeartbeat() {
    try {
      register("com.dragon.study.etcd", "127.0.0.1:1234", 20);
      //相同时间触发两个事件,可能接受不到监听事件
      Thread.sleep(100);
      register("com.dragon.study.etcd", "127.0.0.1:6789", 5);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void register(String serviceName, String address, int ttl) throws Exception {
    String key = serviceName.replaceAll("\\.", "/");
    EtcdClient client = EtcdClientHolder.get();
    client.put(key + "/" + address, address).ttl(ttl).send().get();
  }
}
