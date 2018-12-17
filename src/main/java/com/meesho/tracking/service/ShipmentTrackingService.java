package com.meesho.tracking.service;

import com.meesho.tracking.models.QueueEvent;
import com.meesho.tracking.models.TrackShipmentRequest;
import com.meesho.tracking.queue.IDelayQueue;
import com.meesho.tracking.queue.producer.KafkaProducerImpl;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * Created by naveenkumar.av on 17/12/18.
 */
@NoArgsConstructor
public class ShipmentTrackingService {

    private static IDelayQueue queue = KafkaProducerImpl.getInstance();

    public void track(TrackShipmentRequest trackShipmentRequest){

        //TODO: validate request

        QueueEvent event = new QueueEvent(String.valueOf(trackShipmentRequest.getOrderId()), trackShipmentRequest.getAwbNumber().getBytes(),
                trackShipmentRequest.getVisibleDelayInMillis());

        // Ingest to async delay queue for async processing
        try {
            queue.put(String.valueOf(trackShipmentRequest.getOrderId()), event.ser(),
                    trackShipmentRequest.getVisibleDelayInMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
