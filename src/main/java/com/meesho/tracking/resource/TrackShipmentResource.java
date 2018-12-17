package com.meesho.tracking.resource;

import com.codahale.metrics.annotation.Timed;
import com.meesho.tracking.models.TrackShipmentRequest;
import com.meesho.tracking.service.ShipmentTrackingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by naveenkumar.av on 17/12/18.
 */

@Slf4j
public class TrackShipmentResource {

    static ShipmentTrackingService trackingService = new ShipmentTrackingService();

    @Timed
    @POST
    @Path("/trackShipment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response bookingConfirm(TrackShipmentRequest trackShipmentRequest) {

        try {
            trackingService.track(trackShipmentRequest);
            return Response.ok().build();
        } catch (Exception e) {
            log.error("TrackingResource - Exception during tracking: {}", e);
            return Response.serverError().entity(ExceptionUtils.getStackTrace(e)).build();
        }

    }

}
