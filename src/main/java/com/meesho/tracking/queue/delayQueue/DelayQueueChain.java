package com.meesho.tracking.queue.delayQueue;

import com.meesho.tracking.models.QueueEvent;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by naveenkumar.av on 17/12/18.
 */
public abstract class DelayQueueChain {

    private List<DelayQueueChain> queueList = new LinkedList<DelayQueueChain>();

    public void populateQueueList(List<DelayQueueChain> queueList) {
        this.queueList.addAll(queueList);
    }

    DelayQueueChain getNextQueue(long delayTime) {
        int i = 0;
        while (i < (queueList.size()-1) && queueList.get(i).getVisibleTime() > delayTime) {
            i++;
        }
        return queueList.get(i);
    }

    public abstract void delay(QueueEvent event) throws IOException, InterruptedException, ExecutionException;

    public abstract long getVisibleTime();
}
