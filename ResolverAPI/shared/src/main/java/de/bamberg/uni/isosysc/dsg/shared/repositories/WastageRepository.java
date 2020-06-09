package de.bamberg.uni.isosysc.dsg.shared.repositories;


import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import de.bamberg.uni.isosysc.dsg.shared.models.Location;
import de.bamberg.uni.isosysc.dsg.shared.models.Wastage;

/**
 * 
 * @author amit
 *
 */
/*
 * To persist wastage details to MongoDB
 */
public interface WastageRepository extends MongoRepository<Wastage, String> {
	

	List<Wastage> findByLocationCityIgnoreCase(String city);
	List<Wastage> findByLastUpdateDateGreaterThan(Date lastUpdateDate);
	List<Wastage> findByLocationCityIgnoreCaseAndLastUpdateDateGreaterThan(String city,Date lastUpdateDate);
	List<Wastage> findByLocationCityIgnoreCase(String city, Sort sort);
	List<Wastage> findByLastUpdateDateGreaterThan(Date lastUpdateDate, Sort sort);
	List<Wastage> findByLocationCityIgnoreCaseAndLastUpdateDateGreaterThan(String city,Date lastUpdateDate, Sort sort);
	List<Wastage> findByUserId(String userId);
	List<Wastage> findByUserId(String userId, Sort sort);
	Optional<Wastage> findByImagesImageId(String imageId, Sort sort);
	Optional<Wastage> findByImagesImageId(String imageId);

}
