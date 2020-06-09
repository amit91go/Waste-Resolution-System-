package de.bamberg.uni.isosysc.dsg.financer;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import de.bamberg.uni.isosysc.dsg.shared.models.Offer;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferBean;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferStatus;
import de.bamberg.uni.isosysc.dsg.shared.models.Pagination;
import de.bamberg.uni.isosysc.dsg.shared.models.Wastage;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageBean;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageStatus;
import de.bamberg.uni.isosysc.dsg.shared.repositories.WastageRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.OfferRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.UserRepository;

/*
 * Spring Boot Controller class for URLs mapping regarding Offers.
 */
@EnableMongoRepositories(basePackages = "de.bamberg.uni.isosysc.dsg.shared.repositories")
@RestController
@RequestMapping("financer/offers")
public class OfferController {

	@Autowired
	private OfferRepository offerRepository;
	@Autowired
	private WastageRepository wasteRepository;
	@Autowired
	private UserRepository userRepository;

	/*
	 * Get Mapping for retrieving active offers.
	 */
	@GetMapping
	public Page<OfferBean> getOffers(Pageable pageable, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{

		List<Offer> offerList = null; 
		List<OfferBean> offerBeanList = new ArrayList<OfferBean>(); 

		if(pageable.getSort() == null)
			offerList =   offerRepository.findByOfferStatus(OfferStatus.PendingAcceptance);
		else
			offerList =   offerRepository.findByOfferStatus(OfferStatus.PendingAcceptance, pageable.getSort());


		if(!(offerList.isEmpty()))
		{
			for(Offer offer:offerList)
			{
				OfferBean bean = createLinksForOffer(offer, authentication, req, resp); // Adding HATEOAS links
				offerBeanList.add(bean);
			}		

			if(Pagination.createPageFromOfferList(offerBeanList, pageable) != null)   //Adding Pagination
				return Pagination.createPageFromOfferList(offerBeanList, pageable) ;
			else
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending acceptance offers found.");			
		}
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending acceptance offers found.");

	} 

	/*
	 * Get mapping for a specific offer.
	 */
	@RequestMapping("/{id}")
	public OfferBean getOfferById(@PathVariable String id, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		Offer offer = null;
		Optional<Offer> offerObj = offerRepository.findById(id);

		if(offerObj.isPresent())
		{
			offer = offerObj.get();
			resp.addHeader("ETag", Integer.toString(offer.getVersion()));  // Adding ETAG to response header.
			OfferBean bean = createLinksForOffer(offer, authentication, req, resp);  // Adding HATEOAS links
			return bean;	
		}
		else 
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Offers found for the criterion.");			
	}

	
	/*
	 * Put mapping to accept an offer.
	 */
	@RequestMapping(value = "/{id}/accept", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public OfferBean acceptOffer(@PathVariable("id") String id, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) 
	{
		Offer acceptedOffer = new Offer();
		String userId = userRepository.findByUsername(authentication.getName()).getId();
		Optional<Offer> offerObj = offerRepository.findById(id);
		if(offerObj.isPresent())
		{
			acceptedOffer = offerObj.get();
			if(req.getHeader("If-Match") == null)  //If-Match value should be present.
			{
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Request Header If-Match is mandatory for this request.");
			}
			if((Integer.parseInt(req.getHeader("If-Match")) != (acceptedOffer.getVersion())))  //If-Match value should match current version.
			{
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Offer has changed since last access.");
			}
			else if(acceptedOffer.getOfferStatus() != OfferStatus.PendingAcceptance)  //Only Pending Acceptance offers can be accepted.
			{
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Offer should be in Pending Acceptance status to be accepted.");
			}
			else
			{
					acceptedOffer.setOfferStatus(OfferStatus.Accepted);
					acceptedOffer.setFinancerId(userId);   //setting financerId to cuurent user.
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date(System.currentTimeMillis()));
					cal.add(Calendar.DATE,acceptedOffer.getDaysRequired());
					acceptedOffer.setDueDate(cal.getTime());
					acceptedOffer.setVersion(acceptedOffer.getVersion() + 1);    //updating the version.
					updateWastageStatus(acceptedOffer.getWasteId(), acceptedOffer.getId());    //updating wastage status for accepted offer.
					resp.addHeader("ETag", Integer.toString(acceptedOffer.getVersion()));    // Adding ETag to response header.
					Offer offer = offerRepository.save(acceptedOffer);       
					OfferBean offerBean = createLinksForOffer(offer, authentication, req, resp);  // Adding HATEOAS links
	    			return offerBean;
			}

		}
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Offers found for the criterion.");   		

	}

	
	/*
	 * function to updated wastage status for accepted offers.
	 */
	public void updateWastageStatus(String wastageId, String offerId)
	{
		List<Offer> offerList = offerRepository.findByWasteId(wastageId);
		Wastage wastage = null;
		for(Offer offer: offerList)
		{
			if((!(offer.getId().equals(offerId))) && (offer.getOfferStatus() != OfferStatus.Revoked) && (offer.getOfferStatus() != OfferStatus.SystemDeleted))
			{   //All other offers for that wastage are marked as deleted.
				offer.setOfferStatus(OfferStatus.SystemDeleted);
				offerRepository.save(offer);
			}
		}

		Optional<Wastage> wastageObj = wasteRepository.findById(wastageId);
		if(wastageObj.isPresent())
		{
			wastage = wastageObj.get();
			if(wastage.getStatus() == WastageStatus.OffersMade)
			{
				wastage.setStatus(WastageStatus.Cleaning_in_progress);   //updating wastage status.
				wasteRepository.save(wastage);
			}
		}   		

	}

	
	/*
	 * Function to add HATEOAS links to offers.
	 */
	public OfferBean createLinksForOffer(Offer offer, Authentication authentication, HttpServletRequest req, HttpServletResponse resp)
	{
		OfferBean bean = new OfferBean(offer);
		Link selfLink = linkTo(OfferController.class).slash(offer.getId()).withSelfRel();
		bean.add(selfLink);
		Link wastageLink = linkTo(methodOn(WastageController.class).getWastageById(offer.getWasteId(), authentication, req, resp)).withRel("Get Wastage Details").withType("Get");
		bean.add(wastageLink);
		Link acceptLink = linkTo(methodOn(OfferController.class).acceptOffer(offer.getId(), authentication, req, resp)).withRel("Accept Offer").withType("Put");
		bean.add(acceptLink);
		return bean;
		
	}
	
}



