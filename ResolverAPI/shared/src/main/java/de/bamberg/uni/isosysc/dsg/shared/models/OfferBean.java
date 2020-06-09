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
 * Encapsulating the offer class to add HATEAOS support.
 */
public class OfferBean extends RepresentationModel<OfferBean> {

	Offer offer;
	
	@JsonCreator
	public OfferBean(@JsonProperty("Offer")Offer offer) {
		super();
		this.offer = offer;
	}

	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}



}
