package de.bamberg.uni.isosysc.dsg.shared.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import de.bamberg.uni.isosysc.dsg.shared.models.User;


/**
 * 
 * @author amit
 *
 */
/*
 * To persist user details to MongoDB
 */
public interface UserRepository extends MongoRepository<User, String> {
	
	User findByUsername(String Username);

}


