package com.gos.veleta;

public class WindInfo {

	Integer degrees;
	Integer speedKm;
	Integer speedMiles;
	ErrorInfo error;

	String areaName;
	String region;
	String country;
	String provider;

	long timeStamp = System.currentTimeMillis();

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public ErrorInfo getError() {
		return error;
	}

	public void setError(ErrorInfo error) {
		this.error = error;
	}

	public Integer getDegrees() {
		return degrees;
	}

	public void setDegrees(Integer degrees) {
		this.degrees = degrees;
	}

	public Integer getSpeedKm() {
		return speedKm;
	}

	public void setSpeedKm(Integer speed) {
		this.speedKm = speed;
	}

	public Integer getSpeedMiles() {
		return speedMiles;
	}

	public void setSpeedMiles(Integer speedMiles) {
		this.speedMiles = speedMiles;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public boolean hasError() {

		return this.error != null;
	}
}
