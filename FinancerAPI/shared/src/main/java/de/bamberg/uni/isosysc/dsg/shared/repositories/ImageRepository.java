package de.bamberg.uni.isosysc.dsg.shared.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.bamberg.uni.isosysc.dsg.shared.models.Image;

/**
 * 
 * @author amit
 *
 */
/*
 * To persist images to MongoDB
 */
public interface ImageRepository extends MongoRepository<Image, String>{

}
