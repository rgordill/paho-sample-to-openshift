package com.mycompany.app;

import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleReceiver implements IMqttMessageListener {

  private static final Logger logger = LoggerFactory.getLogger(SampleReceiver.class);

  private CountDownLatch numMsgRcv;

  SampleReceiver(CountDownLatch numMsgRcv){
    this.numMsgRcv = numMsgRcv;
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    numMsgRcv.countDown();
    
    logger.info("Message (" + numMsgRcv.getCount() + " Arrived on topic ["+ topic + "]: " + message.getPayload());
  }

}
