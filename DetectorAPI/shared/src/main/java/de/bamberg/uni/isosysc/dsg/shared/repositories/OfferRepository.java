package de.bamberg.uni.isosysc.dsg.shared.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import de.bamberg.uni.isosysc.dsg.shared.models.Offer;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferStatus;

/**
 * 
 * @author amit
 *
 */
/*
 * To persist offers to MongoDB
 */
public interface OfferRepository extends MongoRepository<Offer, String>{

	List<Offer> findByWasteId(String wasteId);
	List<Offer> findByOfferStatus(OfferStatus offerStatus);
	List<Offer> findByOfferStatus(OfferStatus offerStatus, Sort sort);
	List<Offer> findByResolverId(String resolverId);
	List<Offer> findByResolverId(String resolverId, Sort sort);
}
