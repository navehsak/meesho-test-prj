package com.meesho.tracking.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by naveenkumar.av on 17/12/18.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackShipmentRequest {

    private long orderId;

    private String awbNumber;  // Can use a uuid instead

    private long visibleDelayInMillis; // frequency in millis

}
