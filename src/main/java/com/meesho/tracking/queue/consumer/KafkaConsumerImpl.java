package com.meesho.tracking.queue.consumer;

import com.meesho.tracking.models.QueueEvent;
import com.meesho.tracking.queue.IDelayQueue;
import com.meesho.tracking.queue.producer.KafkaProducerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by naveenkumar.av on 17/12/18.
 */
@Slf4j
public class KafkaConsumerImpl implements Runnable {

    private IDelayQueue delayQueue = KafkaProducerImpl.getInstance();

    private final KafkaConsumer<String, byte[]> consumer;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(10); //TODO: cfg

    public KafkaConsumerImpl() {

        Properties consumerProp = new Properties();
        consumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "");
        consumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, "allocation");
        consumerProp.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 1000);
        consumerProp.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        consumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumer = new KafkaConsumer<>(consumerProp);

        Thread th = new Thread(this);
        th.start();
    }

    @Override
    public void run() {
        QueueEvent queueEvent;
        try{

            ConsumerRecords<String, byte[]> records = consumer.poll(1000); //TODO: cfg

            for (ConsumerRecord<String, byte[]> record : records) {
                byte[] event = record.value();
                if (event == null) {
                    log.error("Event from kafka queue is null");
                    continue;
                }
                queueEvent = QueueEvent.deser(event);

                QueueEvent finalQueueEvent = queueEvent;
                Runnable runnableTask = () -> {
                    // Calling Allocation service for retrying allotment
                    callExtServiceForTrackingDetails(finalQueueEvent);
                };
                threadPool.execute(runnableTask);
            }

        } catch (Exception e) {

        }
    }

    private void callExtServiceForTrackingDetails(QueueEvent queueEvent) {
        String orderId = queueEvent.key;
        byte[] value = queueEvent.value;

        //TODO: call ext tracking service for tracking info
        boolean trackingInfoFetched = true;

        if(trackingInfoFetched) {
            //Update order management service
            //TODO:
        } else {
            // Ingest to async delay queue for async processing
            try {
                delayQueue.put(String.valueOf(orderId), queueEvent.ser(),
                        queueEvent.getDelayTime());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //TODO: implement limited retries mechanism
        }
    }

}
