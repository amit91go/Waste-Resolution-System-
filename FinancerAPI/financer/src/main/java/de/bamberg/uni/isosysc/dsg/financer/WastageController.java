package de.bamberg.uni.isosysc.dsg.financer;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import de.bamberg.uni.isosysc.dsg.shared.models.Wastage;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageBean;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageStatus;
import de.bamberg.uni.isosysc.dsg.shared.models.Offer;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferBean;
import de.bamberg.uni.isosysc.dsg.shared.repositories.WastageRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.OfferRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.UserRepository;


/**
 * @author amit
 *
 */

/*
 * Spring Boot Controller class for URLs mapping regarding wastages.
 */
@EnableMongoRepositories(basePackages = "de.bamberg.uni.isosysc.dsg.shared.repositories")
@RestController
@RequestMapping("/financer/wastages")
public class WastageController {
	
	@Autowired
	private WastageRepository wasteRepository;
	@Autowired
	private OfferRepository offerRepository;
	
	/*
	 * Get Mapping for retrieving a specific wastage.
	 */
	@RequestMapping("/{id}")
	public WastageBean getWastageById(@PathVariable String id, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) 
	{
		Wastage wastage = null;
		Optional<Wastage> wastageObj = wasteRepository.findById(id);

		if(wastageObj.isPresent())
		{
			wastage = wastageObj.get();
			WastageBean bean = createLinksForWastage(wastage, authentication, req, resp);  // Adding HATEOAS links
			return bean;	
		}
		else 
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No wastages found for the criterion.");
			
			
	}
	
	
	/*
	 * Get mapping for offers of a specific wastage.
	 */
	@RequestMapping("/{wasatageId}/offers")
	public List<OfferBean> getOffersByWastageId(@PathVariable String wasatageId, Authentication authentication, HttpServletRequest req, HttpServletResponse resp)
	{
		List<Offer> offerList = offerRepository.findByWasteId(wasatageId);
		List<OfferBean> offerListBean = new ArrayList<OfferBean>();
		for(Offer offer: offerList)
		{
			OfferBean bean = new OfferBean(offer);
			Link selfLink = linkTo(OfferController.class).slash(offer.getId()).withSelfRel().withType("Get");
			bean.add(selfLink);
			Link wastageLink = linkTo(methodOn(WastageController.class).getWastageById(offer.getWasteId(), authentication, req, resp)).withRel("Get Wastage Details").withType("Get");
			bean.add(wastageLink);
			Link acceptLink = linkTo(methodOn(OfferController.class).acceptOffer(offer.getId(), authentication, req, resp)).withRel("Accept Offer").withType("Put");
			bean.add(acceptLink);
			offerListBean.add(bean);
		}
		
		return offerListBean;
	}


	/*
	 * Function to add HATEOAS links.
	 */
	public WastageBean createLinksForWastage(Wastage wastage, Authentication authentication, HttpServletRequest req, HttpServletResponse resp)
	{
		WastageBean bean = new WastageBean(wastage);
		Link selfLink = linkTo(WastageController.class).slash(wastage.getId()).withSelfRel().withType("Get");
		bean.add(selfLink);
		if((wastage.getStatus() != WastageStatus.Reported) && (wastage.getStatus() != WastageStatus.Deleted))
		{
			Link OffersLink = linkTo(methodOn(WastageController.class).getOffersByWastageId(wastage.getId(), authentication, req, resp)).withRel("All Offers made").withType("Get");
			bean.add(OffersLink);
		}
		 return bean;
		
	}	
	
	

}
