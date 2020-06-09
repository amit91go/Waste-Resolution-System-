package de.bamberg.uni.isosysc.dsg.resolver;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import de.bamberg.uni.isosysc.dsg.shared.models.Offer;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferBean;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferStatus;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferType;
import de.bamberg.uni.isosysc.dsg.shared.models.Pagination;
import de.bamberg.uni.isosysc.dsg.shared.models.Wastage;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageBean;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageStatus;
import de.bamberg.uni.isosysc.dsg.shared.repositories.WastageRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.OfferRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.UserRepository;


/**
 * @author amit
 *
 */
/*
 * Spring Boot Controller class for URLs mapping regarding offers.
 */
@EnableMongoRepositories(basePackages = "de.bamberg.uni.isosysc.dsg.shared.repositories")
@RestController
@RequestMapping("/resolver/offers")
public class OfferController {
	

	@Autowired
	private OfferRepository offerRepository;
	@Autowired
	private WastageRepository wasteRepository;
	@Autowired
	private UserRepository userRepository;

	
	/*
	 * Post mapping for creating a new offer.
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OfferBean makeOffers(@RequestBody Offer offer, HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		if(offer.getDaysRequired() == 0)  //Days required is mandatory to be entered.
		{
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Days required is a mandator.");
		}
		else
		{
		String userId = userRepository.findByUsername(authentication.getName()).getId();
		Offer createdOffer = null; 
		offer.setResolverId(userId);  //set resolver id to current user.
		offer.setCreationDate(new Date(System.currentTimeMillis()));
		offer.setDueDate(null);  // due date will added once the offer is accepted.
		Wastage wastageObj = wasteRepository.findById(offer.getWasteId()).get();
		if((wastageObj.getStatus() == WastageStatus.Reported) || (wastageObj.getStatus() == WastageStatus.OffersMade))  //offers can be created only for these wastage statuses. 
		{
			
			if(offer.getCost() > 0.0)
			{
				offer.setOfferStatus(OfferStatus.PendingAcceptance);
				offer.setOfferType(OfferType.Commercial);
				offer.setFinancerId("Not Accepted Yet");    // financer will be added once ofer is accepted.
				offer.setVersion(1);
				wastageObj.setStatus(WastageStatus.OffersMade);   //updating wastage offers.
				wasteRepository.save(wastageObj);
				createdOffer = offerRepository.save(offer);
			}
			else
			{
				offer.setOfferStatus(OfferStatus.Accepted);    // Voluntary offers will get accepted instantly. 
    			Calendar cal = Calendar.getInstance();
    			cal.setTime(new Date(System.currentTimeMillis()));
    			cal.add(Calendar.DATE,offer.getDaysRequired());
    			offer.setDueDate(cal.getTime());   // updating due date as per provided details.
				offer.setOfferType(OfferType.Voluntary);
				offer.setFinancerId("Not Applicable");
				List<Offer> offerList = offerRepository.findByWasteId(wastageObj.getId());
				for(Offer offerObj: offerList)
				{
					if(offerObj.getOfferStatus() != OfferStatus.Revoked)
					{
						offerObj.setOfferStatus(OfferStatus.SystemDeleted);
						offerRepository.save(offerObj);
					}
				}
				wastageObj.setStatus(WastageStatus.Cleaning_in_progress);
				wasteRepository.save(wastageObj);
				offer.setVersion(1);
				createdOffer = offerRepository.save(offer);			
			}
			
			
			OfferBean bean = createLinksForOffer(createdOffer, authentication, request, response);
			return bean;
		}
		else
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wastage status does not allow offer creation.");
		}
		}
	}
	
	
	/*
	 * Get Mapping for retrieving the offers.
	 */
	@GetMapping
	public Page<OfferBean> getOffers(Authentication authentication, Pageable pageable, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{

		List<Offer> offerList = null; 
		List<OfferBean> offerBeanList = new ArrayList<OfferBean>(); 
		String resolverId = userRepository.findByUsername(authentication.getName()).getId();
		if(pageable.getSort() == null)
			offerList =   offerRepository.findByResolverId(resolverId);
		else
			offerList =   offerRepository.findByResolverId(resolverId,pageable.getSort());
		
		
		if(!(offerList.isEmpty()))
		{
			for(Offer offer:offerList)
			{
				if(offer.getOfferStatus() != OfferStatus.Revoked)   //Revoked offers are not shown to the user.
				{
				OfferBean bean = createLinksForOffer(offer, authentication, req, resp);   //Adding HATEOAS liks.
				offerBeanList.add(bean);
				}
			}		

			if(Pagination.createPageFromOfferList(offerBeanList, pageable) != null)   // Adding pagination.
				return Pagination.createPageFromOfferList(offerBeanList, pageable) ;
			else
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No offers found for the resolver.");			
		}
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No offers found for the resolver.");

	} 

	
	/*
	 * Get mapping for a specific offer.
	 */
	@RequestMapping("/{id}")
	public OfferBean getOfferById(@PathVariable String id, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		Offer offer = null;
		String resolerId = userRepository.findByUsername(authentication.getName()).getId();
		Optional<Offer> offerObj = offerRepository.findById(id);

		if(offerObj.isPresent())
		{
			offer = offerObj.get();
			if(resolerId.equals(offer.getResolverId()))
			{
			resp.addHeader("ETag", Integer.toString(offer.getVersion()));   // Adding ETAG to response header.
			OfferBean bean = createLinksForOffer(offer, authentication, req, resp);   // Adding HATEOAS links.
			return bean;	
			}
			else
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Offer does not belong to the reslover.");					
		}
		else 
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Offers found for the criterion.");			
	}
	
	
	/*
	 * 
	 *  Delete mapping for revoking an offer.
	 */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String revokeOffer(@PathVariable("id") String id, Authentication authentication) throws IOException 
    {
    	Offer revokedOffer = new Offer();
    	String resolverId = userRepository.findByUsername(authentication.getName()).getId();
    	Optional<Offer> offerObj = offerRepository.findById(id);
    	if(offerObj.isPresent())
    	{
    		revokedOffer = offerObj.get();
    		if(revokedOffer.getOfferStatus() == OfferStatus.PendingAcceptance)
    		{
    			if(resolverId.equals(revokedOffer.getResolverId()))
    			{
    			revokedOffer.setOfferStatus(OfferStatus.Revoked);
    			revokedOffer.setVersion(revokedOffer.getVersion() + 1);   //updating the version 
    			updateWastageStatus(revokedOffer.getWasteId(), revokedOffer.getId());  //updating wastage status for deleted offer.
    			offerRepository.save(revokedOffer);
    			return "Offer has been revoked.";
    			}
    			else
    			{
    				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Offer does not belong to the reslover. ");

    			}
    		}
    		else
    		{
    			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Offer should be in Pending Acceptance status to be revoked.");
    		}
    	}
    	else
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Offers found for the criterion.");   		

    }
    
    
    /*
     * Function to update wastage status for deleted offers.
     */
    public void updateWastageStatus(String wastageId, String offerId)
    {
    	List<Offer> offerList = offerRepository.findByWasteId(wastageId);
    	int updateFlag = 0;
    	Wastage wastage = null;
    	for(Offer offer: offerList)
    	{
    		if((!(offer.getId().equals(offerId))) && (offer.getOfferStatus() != OfferStatus.Revoked) && (offer.getOfferStatus() != OfferStatus.SystemDeleted))
    		{
    			updateFlag++;
    		}
    	}
    	if(updateFlag == 0)
    	{
    		Optional<Wastage> wastageObj = wasteRepository.findById(wastageId);
        	if(wastageObj.isPresent())
        	{
        		wastage = wastageObj.get();
        		if(wastage.getStatus() == WastageStatus.OffersMade)
        		{
        			wastage.setStatus(WastageStatus.Reported);
        			wasteRepository.save(wastage);
        		}
        	}   		

        }
    }
    

    //Ading HATEOAS links to offers.
	public OfferBean createLinksForOffer(Offer offer, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		OfferBean bean = new OfferBean(offer);
		Link selfLink = linkTo(OfferController.class).slash(offer.getId()).withSelfRel().withType("Get");
		bean.add(selfLink);
		Link wastageLink = linkTo(methodOn(WastageController.class).getWastageById(offer.getWasteId(), authentication,req, resp )).withRel("Get Wastage Details").withType("Get");
		bean.add(wastageLink);
		return bean;
		
	}
}



