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
 * Encapsulating image class to add HATEOAS functionality.
 */
public class ImageBean extends RepresentationModel<ImageBean> {

	Image image;
	
	@JsonCreator
	public ImageBean(@JsonProperty("Image")Image image) {
		super();
		this.image = image;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}


	
	
}

