package com.meesho.tracking;

import com.meesho.tracking.config.ShipmentTrackerAppCfg;
import com.meesho.tracking.queue.consumer.KafkaConsumerImpl;
import com.meesho.tracking.resource.TrackShipmentResource;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by naveenkumar.av on 17/12/18.
 */
public class ShipmentTrackerApp extends io.dropwizard.Application<ShipmentTrackerAppCfg> {

    private Environment environment;

    private ShipmentTrackerAppCfg config;

    @Override
    public void initialize(Bootstrap<ShipmentTrackerAppCfg> bootstrap) {
        super.initialize(bootstrap);
    }

    @Override
    public void run(ShipmentTrackerAppCfg config, Environment environment) throws Exception {
        this.environment = environment;
        this.config = config;

        // MySQL Configuration
        //final DBIFactory factory = new DBIFactory();
        //final DBI jdbi = factory.build(environment, config.getDatabase(), "mysql");

        // Register resources
        environment.jersey().register(TrackShipmentResource.class);

        // Initialize consumer
        new KafkaConsumerImpl();

    }

    public static void main(String[] args) throws Exception {
        new ShipmentTrackerApp().run(args);
    }

}
