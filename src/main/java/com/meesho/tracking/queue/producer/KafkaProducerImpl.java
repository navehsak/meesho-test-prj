package com.meesho.tracking.queue.producer;

import com.meesho.tracking.models.QueueEvent;
import com.meesho.tracking.queue.IDelayQueue;
import com.meesho.tracking.queue.delayQueue.DelayQueueChain;
import com.meesho.tracking.queue.delayQueue.KafkaQueueDelayer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by naveenkumar.av on 17/12/18.
 */
public class KafkaProducerImpl implements IDelayQueue {

    private static IDelayQueue instance = null;

    private final KafkaProducer<String, byte[]> producer;

    private final List<DelayQueueChain> queueList = new LinkedList<>();

    static String bootstrapServers = "";
    static String zookeeper = "";
    static String clientId = "";

    public static IDelayQueue getInstance() {
        if (null != instance)
            return instance;

        synchronized (KafkaProducerImpl.class.getName()) {
            if (null != instance)
                return instance;

            instance = new KafkaProducerImpl("", "", "");
        }

        return instance;
    }

    KafkaProducerImpl(String bootstrapServers, String zookeeper, String clientId) {
        this.bootstrapServers = bootstrapServers;
        this.zookeeper = zookeeper;
        this.clientId = clientId;
        Properties producerProp = new Properties();
        //TODO: populate producer cfg above from args
        producer = new KafkaProducer<>(producerProp);

        //Note: Configuring delay queue here itself this can be moved to separate constructs
        configureQueueDelays();
    }

    @Override
    public void put(String key, byte[] value, long visibleDelayInMillis) {
        try {
            DelayQueueChain chain = getStartQueue(visibleDelayInMillis);
            chain.delay(QueueEvent.deser(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drain(QueueEvent event) throws IOException, InterruptedException, ExecutionException {
        event.postedTime = System.currentTimeMillis();
        byte[] xx = event.ser();
        producer.send(new ProducerRecord<>("drainTopic", event.key, xx)).get();
    }

    /**
     * The queue needs to registered in the descending order of the delay time.
     */
    private void configureQueueDelays() {
        DelayQueueChain queue4 = new KafkaQueueDelayer("topic1", bootstrapServers, zookeeper,
                clientId, 3600000); //1hr
        DelayQueueChain queue5 = new KafkaQueueDelayer("topic2", bootstrapServers, zookeeper,
                clientId, 1800000); //30 mins
        DelayQueueChain queue6 = new KafkaQueueDelayer("topic3", bootstrapServers, zookeeper,
                clientId, 600000);  //10 mins
        DelayQueueChain queue7 = new KafkaQueueDelayer("topic4", bootstrapServers, zookeeper,
                clientId, 60000);   //1 min

        //Note: Can add more if require more granularity

        queueList.add(queue4);
        queueList.add(queue5);
        queueList.add(queue6);
        queueList.add(queue7);

        // Queue list maintains the order
        addQueueList(queueList);
    }

    private void addQueueList(List<DelayQueueChain> queueList) {
        for (DelayQueueChain queue : queueList) {
            queue.populateQueueList(queueList);
        }
    }

    private DelayQueueChain getStartQueue(long delayTime) {
        int i = 0;
        while (i < (queueList.size() - 1) && queueList.get(i).getVisibleTime() > delayTime) {
            i++;
        }
        return queueList.get(i);
    }

}

