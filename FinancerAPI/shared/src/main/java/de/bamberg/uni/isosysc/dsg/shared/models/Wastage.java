package de.bamberg.uni.isosysc.dsg.shared.models;

import java.util.Date;
import java.util.List;

import javax.annotation.Generated;
import javax.validation.constraints.NotEmpty;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * 
 * @author amit
 *
 */
/*
 * Encapsulating wastage details.
 */
public class Wastage{
	

	@Id private String id;
	private Date lastUpdateDate;
	private String description;
	private String userId;
	private Location location;
	private List<ImageDetails> images;
	private WastageStatus status;
	@JsonIgnore
	private int version;  //version will not be present in JSON request or response.
	
	
	//Setters and getters.
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getlastUpdateDate() {
		return lastUpdateDate;
	}
	public void setlastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}

	public List<ImageDetails> getImages() {
		return images;
	}

	public void setImages(List<ImageDetails> images) {
		this.images = images;
	}

	public WastageStatus getStatus() {
		return status;
	}
	public void setStatus(WastageStatus status) {
		this.status = status;
	}


	public int getVersion() {
		return version;
	}


	public void setVersion(int version) {
		this.version = version;
	}









	
	
	
	

}
