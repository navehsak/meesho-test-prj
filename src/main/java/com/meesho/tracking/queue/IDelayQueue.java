package com.meesho.tracking.queue;

import com.meesho.tracking.models.QueueEvent;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by naveenkumar.av on 17/12/18.
 */
public interface IDelayQueue {

    void put(String key, byte[] value, long frequency);

    void drain(QueueEvent event) throws IOException, InterruptedException, ExecutionException;

}
