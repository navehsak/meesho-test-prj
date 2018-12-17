package com.meesho.tracking.queue.delayQueue;

import com.meesho.tracking.models.QueueEvent;
import com.meesho.tracking.queue.producer.KafkaProducerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by naveenkumar.av on 17/12/18.
 */
@Slf4j
public class KafkaQueueDelayer extends DelayQueueChain implements Runnable {

    private final String topic;
    private final KafkaProducer<String, byte[]> producer;
    private final KafkaConsumer<String, byte[]> consumer;
    private final long visibleTime;


    public KafkaQueueDelayer(String topic, String bootstrapServers, String zookeeper, String clientId,
                             long visibleTimeInMillis) {
        this.visibleTime = visibleTimeInMillis;
        this.topic = topic;

        Properties producerProp = new Properties();

        //TODO: Boiler plate cfg append code retrieved from net, retrieve it from app cfg
        producerProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProp.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        producerProp.put("metadata.broker.list", bootstrapServers);
        producerProp.put("zk.connect", zookeeper);
        producerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        producerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.ByteArraySerializer");
        producerProp.put(ProducerConfig.RETRIES_CONFIG, 1);
        producerProp.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        producerProp.put("enable.auto.commit", "true");
        producerProp.put("auto.commit.interval.ms", "10");
        producerProp.put("session.timeout.ms", "30000");
        producerProp.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "30000");

        producer = new KafkaProducer<>(producerProp);

        Properties consumerProp = new Properties();
        consumerProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProp.put(ConsumerConfig.GROUP_ID_CONFIG, "");
        consumerProp.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 1000);
        consumerProp.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        consumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        consumer = new KafkaConsumer<>(consumerProp);
        consumer.subscribe(Arrays.asList(topic));

        Thread th = new Thread(this);
        th.start();
    }

    @Override
    public void delay(QueueEvent event) throws IOException, InterruptedException, ExecutionException {
        byte[] xx = event.ser();
        producer.send(new ProducerRecord<>(topic, event.key, xx)).get();
    }

    @Override
    public void run() {

        byte[] event;
        QueueEvent queueEvent = null;
        try {
            ConsumerRecords<String, byte[]> records = consumer.poll(1000);
            for (ConsumerRecord<String, byte[]> record : records) {
                event = record.value();
                if (event == null) {
                    log.info("KafkaQueueDelayer for Topic:{} Event from kafka queue is null", topic);
                    continue;
                }
                queueEvent = QueueEvent.deser(event);

                long timeElapsed = System.currentTimeMillis() - queueEvent.postedTime;
                long remainingTime = queueEvent.delayTime - timeElapsed;
                log.info("KafkaQueueDelayer for Topic:{}, Overall Remaining Time: {} for key: {}", topic, remainingTime, queueEvent.key);

                if (remainingTime >= visibleTime) {
                    delay(queueEvent);
                    continue;
                }

                if (remainingTime > 0) {
                    DelayQueueChain queue = getNextQueue(remainingTime);
                    queue.delay(queueEvent);
                    continue;
                }

                if (remainingTime <= 0) {
                    // Ingestion to drain queue if event expires
                    KafkaProducerImpl.getInstance().drain(queueEvent);
                }
            }

        } catch (Exception ex) {
            log.error("Exception in KafkaQueueDelayer in processing the event, ex = {}", ex.getStackTrace());
        }

    }

    public long getVisibleTime() {
        return visibleTime;
    }


}
