package de.bamberg.uni.isosysc.dsg.shared.models;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * Encapsulating the offer details for a wastage.
 */
public class Offer {


	@Id private String id;
	@Persistent private Date creationDate;
	@Persistent private Date dueDate;
	@Persistent private String resolverId;
	@Persistent private String financerId;
	@Persistent private String wasteId;
	@Persistent private double cost;
	@Persistent private OfferType offerType;
	@Persistent private OfferStatus offerStatus;
	@Persistent private int daysRequired;
	@JsonIgnore private int version;

	
	// Setters and getters 
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getResolverId() {
		return resolverId;
	}
	public void setResolverId(String resolverId) {
		this.resolverId = resolverId;
	}
	public String getFinancerId() {
		return financerId;
	}
	public void setFinancerId(String financerId) {
		this.financerId = financerId;
	}
	public String getWasteId() {
		return wasteId;
	}
	public void setWasteId(String wasteId) {
		this.wasteId = wasteId;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public OfferType getOfferType() {
		return offerType;
	}
	public void setOfferType(OfferType offerType) {
		this.offerType = offerType;
	}
	public OfferStatus getOfferStatus() {
		return offerStatus;
	}
	public void setOfferStatus(OfferStatus offerStatus) {
		this.offerStatus = offerStatus;
	}
	public int getDaysRequired() {
		return daysRequired;
	}
	public void setDaysRequired(int daysRequired) {
		this.daysRequired = daysRequired;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	
	
}
