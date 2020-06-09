package de.bamberg.uni.isosysc.dsg.shared.models;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * 
 * @author amit
 *
 */
/*
 * Encapsulating wastage class to add HATEOAS functionality
 */
public class WastageBean extends RepresentationModel<WastageBean> {

	Wastage wastage;
	
	@JsonCreator
	public WastageBean(@JsonProperty("Wastage")Wastage wastage) {
		super();
		this.wastage = wastage;
	}

	
	//Setters and Getters
	public Wastage getWastage() {
		return wastage;
	}

	public void setWastage(Wastage wastage) {
		this.wastage = wastage;
	}
	
	
}
