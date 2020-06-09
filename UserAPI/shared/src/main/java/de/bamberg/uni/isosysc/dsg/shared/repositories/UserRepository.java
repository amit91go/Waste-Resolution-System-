package de.bamberg.uni.isosysc.dsg.shared.repositories;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import de.bamberg.uni.isosysc.dsg.shared.models.User;


/**
 * 
 * @author amit
 *
 */
/*
 * To persist user details in MongoDB
 */
public interface UserRepository extends MongoRepository<User, String> {
	
	User findByUsername(String Username);
	
	
    @Override
    @Cacheable(value = "userCache", key = "#userId")
	Optional<User> findById(String userId);

}


