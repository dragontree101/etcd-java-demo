package com.dragon.study.etcd;

import mousio.etcd4j.EtcdClient;

/**
 * Created by dragon on 16/4/12.
 */
public class EtcdClientHolder {

  private static EtcdClient etcdClient;

  public static EtcdClient get() {
    return etcdClient;
  }

  protected static void setEtcdClient(EtcdClient client) {
    EtcdClientHolder.etcdClient = client;
  }
}
