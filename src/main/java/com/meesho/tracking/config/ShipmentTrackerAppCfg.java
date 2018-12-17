package com.meesho.tracking.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Application config for shipment tracking service
 *
 * Created by naveenkumar.av on 17/12/18.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShipmentTrackerAppCfg extends Configuration {


    //TODO:
//    @JsonProperty("database")
//    @Valid
//    @NotNull
//    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty
    @NotEmpty
    public String environment = "environment";

    //TODO: Kafka server config

}
