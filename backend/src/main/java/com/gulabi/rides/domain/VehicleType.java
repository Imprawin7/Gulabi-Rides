package com.gulabi.rides.domain;
public enum VehicleType {
  MINI(40,12), SEDAN(60,16), PRIME(90,22);
  public final int base, perKm;
  VehicleType(int base,int perKm){ this.base=base; this.perKm=perKm; }
}
