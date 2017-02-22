package com.dragon.study.etcd;


import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mousio.client.promises.ResponsePromise;
import mousio.client.retry.RetryNTimes;
import mousio.client.retry.RetryWithExponentialBackOff;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.responses.EtcdVersionResponse;

/**
 * Created by dragon on 16/4/12.
 */
public class EtcdWatcher {

  public static void main(String[] args) {

//    try {
//      EtcdClient client = new EtcdClient(new URI("http://127.0.0.1:2379"));
//      EtcdKeysResponse response =client.get("/dragon/key1").send().get();
//      System.out.println("cluster is " + response.node.value);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }

    System.out.println("------");

    List<String> addressList = new ArrayList<>();
    try {
      EtcdClient client = new EtcdClient(new URI("http://127.0.0.1:2379"));
      client.setRetryHandler(new RetryNTimes(200, 3));
      //
      String path1 = "/dragon/key2";
      //    String path2 = "/com/dragon/study/etcd2";
      //
      start(addressList, client, path1);
//          start(addressList, etcdClient, path2);
//      Thread.sleep(10000);
//      int index = 0;
//      while(index ++ < 10) {
//        System.out.println("--------" + index);
//        try {
//          EtcdKeysResponse response = client.get("/dragon/key1").send().get();
//        } catch (Exception e) {
//          System.out.println("~~~~~~");
//          e.printStackTrace();
//        }
//        Thread.sleep(3000);
//      }

//      client.get(path1).waitForChange().shouldBeWaiting()
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static void start(List<String> addressList, EtcdClient etcdClient, String path) {
    try {
//      EtcdKeysResponse dirResponse = etcdClient.getDir(path).send().get();
//      //parent dir modify index
//      //long modifyIndex = dirResponse.node.getModifiedIndex();
//      List<EtcdKeysResponse.EtcdNode> nodes = dirResponse.node.nodes;
//      for (EtcdKeysResponse.EtcdNode node : nodes) {
//        addressList.add(node.value);
//      }
      EtcdResponsePromise responsePromise = etcdClient.get(path).recursive().waitForChange().send();
      responsePromise.addListener(new ResponsePromiseListener(path, etcdClient, addressList));
    } catch (Exception e) {
      System.out.println("????????????");
      e.printStackTrace();
    }
  }

  static class ResponsePromiseListener implements ResponsePromise.IsSimplePromiseResponseHandler<EtcdKeysResponse> {

    String path;
    List<String> addressList;
    EtcdClient etcdClient;

    public ResponsePromiseListener(String path, EtcdClient etcdClient, List<String> addressList) {
      this.path = path;
      this.etcdClient = etcdClient;
      this.addressList = addressList;
    }

    @Override
    public void onResponse(ResponsePromise<EtcdKeysResponse> responsePromise) {
      long modifyIndex = 0;
      try {
        EtcdKeysResponse response = responsePromise.getNow();
        if (response != null) {
            modifyIndex = response.node.modifiedIndex;
            if (response.action.name().equals("expire")) {
            if (addressList.contains(response.prevNode.value)) {
              addressList.remove(new String(response.prevNode.value));
            }
          } else if (response.action.name().equals("set") || response.action.name()
              .equals("update")) {
            if(!addressList.contains(response.node.value)) {
              addressList.add(new String(response.node.value));
            }
          } else {
            System.out.println(response.action.name() + "~~~~~~~~~~~~");
          }

          System.out.println(StringUtils.join(addressList, "|"));

        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          //如果监听失败,就无法再次监听
          this.etcdClient.getDir(this.path).recursive().waitForChange(modifyIndex+1).send().addListener(this);

        } catch (Throwable e) {
          System.out.println("????????????");
          e.printStackTrace();
          // 需要对于watch失败的情况做而外的处理
        }
        System.out.println("===========");
      }




    }
  }
}
