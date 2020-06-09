package de.bamberg.uni.isosysc.dsg.shared.models;

import org.springframework.data.annotation.Id;
import org.bson.types.Binary;


/*
 * Class for imitating image attributes.
 */
public class Image {
    @Id
    private String id;
    private String title;
    private Binary image;
	public String getId() {
		return id;
	}
	
	//Setters and Getters
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Binary getImage() {
		return image;
	}
	public void setImage(Binary image) {
		this.image = image;
	}
    
    
}
