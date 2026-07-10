package org.benchmark;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TlcTripData {
    @JsonProperty("VendorID")
    private String vendorId;

    @JsonProperty("tpep_pickup_datetime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickupTime;

    @JsonProperty("tpep_dropoff_datetime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dropoffTime;

    @JsonProperty("passenger_count")
    private int passengerCount;

    @JsonProperty("trip_distance")
    private double tripDistance;

    @JsonProperty("pickup_longitude")
    private String pickUpLongitude;

    @JsonProperty("pickup_latitude")
    private String pickUpLatitude;

    @JsonProperty("RateCodeID")
    private int rateCodeId;

    @JsonProperty("store_and_fwd_flag")
    private char storeAndFwdFlag;

    @JsonProperty("dropoff_longitude")
    private String dropoffLongitude;

    @JsonProperty("dropoff_latitude")
    private String dropoffLatitude;

    @JsonProperty("payment_type")
    private int paymentType;

    @JsonProperty("fare_amount")
    private double fareAmount;

    @JsonProperty("extra")
    private double extra;

    @JsonProperty("mta_tax")
    private double mtaTax;

    @JsonProperty("improvement_surcharge")
    private double improvementSurcharge;

    @JsonProperty("tip_amount")
    private double tipAmount;

    @JsonProperty("tolls_amount")
    private double tollsAmount;

    @JsonProperty("total_amount")
    private double totalAmount;

    private int dummyRow = 0;
}
