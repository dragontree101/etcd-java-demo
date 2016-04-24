package com.dragon.study.etcd;


import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mousio.client.promises.ResponsePromise;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdKeysResponse;

/**
 * Created by dragon on 16/4/12.
 */
public class EtcdWatcher {

  public static void main(String[] args) {
    List<String> addressList = new ArrayList<>();
    EtcdClient etcdClient = new EtcdClient();

    String path1 = "/com/dragon/study/etcd";
    String path2 = "/com/dragon/study/etcd2";

    start(addressList, etcdClient, path1);
    start(addressList, etcdClient, path2);

  }

  private static void start(List<String> addressList, EtcdClient etcdClient, String path) {
    try {
      EtcdKeysResponse dirResponse = etcdClient.getDir(path).send().get();
      //parent dir modify index
      //long modifyIndex = dirResponse.node.getModifiedIndex();
      List<EtcdKeysResponse.EtcdNode> nodes = dirResponse.node.nodes;
      for (EtcdKeysResponse.EtcdNode node : nodes) {
        addressList.add(node.value);
      }
      EtcdResponsePromise responsePromise = etcdClient.getDir(path).recursive().waitForChange().send();
      responsePromise.addListener(new ResponsePromiseListener(path, etcdClient, addressList));
    } catch (Exception e) {
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

        } catch (IOException e) {
          e.printStackTrace();
          // 需要对于watch失败的情况做而外的处理
        }
      }




    }
  }
}
