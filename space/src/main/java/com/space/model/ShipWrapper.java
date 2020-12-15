package com.space.model;

public class ShipWrapper {

 Long id;
 String name;
 String planet;
 String shipType;
 Long prodDate;
 boolean isUsed;
 Double speed;
 Integer crewSize;

 public String getName() {
  return name;
 }

 public Long getId() {
  return id;
 }

 public void setId(Long id) {
  this.id = id;
 }

 public void setName(String name) {
  this.name = name;
 }

 public String getPlanet() {
  return planet;
 }

 public void setPlanet(String planet) {
  this.planet = planet;
 }

 public String getShipType() {
  return shipType;
 }

 public void setShipType(String shipType) {
  this.shipType = shipType;
 }

 public Long getProdDate() {
  return prodDate;
 }

 public void setProdDate(Long prodDate) {
  this.prodDate = prodDate;
 }

 public boolean isUsed() {
  return isUsed;
 }

 public void setUsed(boolean used) {
  isUsed = used;
 }

 public Double getSpeed() {
  return speed;
 }

 public void setSpeed(Double speed) {
  this.speed = speed;
 }

 public Integer getCrewSize() {
  return crewSize;
 }

 public void setCrewSize(Integer crewSize) {
  this.crewSize = crewSize;
 }
}
