package de.bamberg.uni.isosysc.dsg.shared.models;


/**  
 * 
 * @author amit
 *
 */
/*
 * Encapsulating the location details of the wastage.
 */
public class Location {
	
	String country;
	private String state;
	private String city;
	private int zipcode;
	private double latitude;
	private double longitude;
	private String address; 
	
	public Location()
	{
		
	}
	
	public Location(Location location) {
		super();
		this.country = location.country;
		this.state = location.state;
		this.city = location.city;
		this.zipcode = location.zipcode;
		this.latitude = location.latitude;
		this.longitude = location.longitude;
		this.address = location.address;
	}
	
	public Location(Long latitude, long longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public Location(String country, String state, String city, int zipcode, String address) {
		super();
		this.country = country;
		this.state = state;
		this.city = city;
		this.zipcode = zipcode;
		this.address = address;
	}
	
	

	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public int getZipcode() {
		return zipcode;
	}
	public void setZipcode(int zipcode) {
		this.zipcode = zipcode;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	


}
