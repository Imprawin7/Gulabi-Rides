package com.gulabi.rides.events;
import com.gulabi.rides.domain.Ride;
public interface RideEventPublisher { void publish(Ride ride, String type); }
