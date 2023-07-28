package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example {
    private static final Logger logger = LoggerFactory.getLogger(Example.class);

    static {
      try (InputStream is = Example.class.getClassLoader().
              getResourceAsStream("logging.properties")) {
          LogManager.getLogManager().readConfiguration(is);
      } catch (IOException e) {
          e.printStackTrace();
      }
    }

    public static void main(String[] args) {

        try {

            Properties props = new Properties();
            props.load(Example.class.getClassLoader().getResourceAsStream("application.properties"));

            ConnectionHandler ch = new ConnectionHandler(props);

            ch.connect();

            String content = props.getProperty("content");
            int qos = Integer.parseInt(props.getProperty("qos"));
            String topic = props.getProperty("topic");

            ch.publish(topic, content, qos);

            int numMessages = Integer.parseInt(props.getProperty("numMessages"));

            CountDownLatch numMsgRcv = new CountDownLatch(numMessages);
            SampleReceiver sr = new SampleReceiver(numMsgRcv);
            ch.subscribe(topic, qos, sr);
            numMsgRcv.await(1, TimeUnit.HOURS);

            ch.disconnect();

        } catch (Exception e) {
            logger.error("Exception:", e);
        }
    }

}