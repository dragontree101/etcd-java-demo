package com.dragon.study.etcd;


import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    try {
      EtcdKeysResponse dirResponse = etcdClient.getDir("/com/dragon/study/etcd").send().get();
      List<EtcdKeysResponse.EtcdNode> nodes = dirResponse.node.nodes;
      for (EtcdKeysResponse.EtcdNode node : nodes) {
        addressList.add(node.value);
      }
      EtcdResponsePromise responsePromise = etcdClient.getDir("/com/dragon/study/etcd").recursive().waitForChange().send();
      responsePromise.addListener(new ResponsePromiseListener(etcdClient, addressList));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static class ResponsePromiseListener implements ResponsePromise.IsSimplePromiseResponseHandler<EtcdKeysResponse> {

    List<String> addressList;
    EtcdClient etcdClient;
    public ResponsePromiseListener(EtcdClient etcdClient, List<String> addressList) {
      this.etcdClient = etcdClient;
      this.addressList = addressList;
    }

    @Override
    public synchronized void onResponse(ResponsePromise<EtcdKeysResponse> responsePromise) {
      try {
        EtcdKeysResponse response = responsePromise.getNow();
        if (response != null) {
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
      }

      try {
        this.etcdClient.getDir("/com/dragon/study/etcd").recursive().waitForChange().send().addListener(this);
      } catch (IOException e) {
        e.printStackTrace();
      }



    }
  }
}
