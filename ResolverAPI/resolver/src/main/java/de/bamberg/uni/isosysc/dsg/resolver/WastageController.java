package de.bamberg.uni.isosysc.dsg.resolver;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import de.bamberg.uni.isosysc.dsg.shared.models.Wastage;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageBean;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageStatus;
import de.bamberg.uni.isosysc.dsg.shared.models.Image;
import de.bamberg.uni.isosysc.dsg.shared.models.ImageDetails;
import de.bamberg.uni.isosysc.dsg.shared.models.Location;
import de.bamberg.uni.isosysc.dsg.shared.models.Offer;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferStatus;
import de.bamberg.uni.isosysc.dsg.shared.models.Pagination;
import de.bamberg.uni.isosysc.dsg.shared.repositories.WastageRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.ImageRepository;
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
@RequestMapping("/resolver/wastages")
public class WastageController {
	
	@Autowired
	private WastageRepository wasteRepository;
	@Autowired
	private OfferRepository offerRepository;
	@Autowired
	private UserRepository userRepository;
	
	
	/*
	 * Get mapping for retrieving the watages based on city and date filtering.
	 */
	@GetMapping
	public Page<WastageBean> getWastages(@RequestParam(value="city", defaultValue="") String city, @RequestParam(value="date",defaultValue="") String dateReq, Pageable pageable, Authentication authentication) throws IOException
	{

		checkForDueDates();
		Date date = null;
		List<Wastage> wastageList = null; 
		List<WastageBean> wastageBeanList = new ArrayList<WastageBean>(); 

		if(!(dateReq.isEmpty()))
		{	
			try 
			{
				date = new SimpleDateFormat("yyyy-MM-dd").parse(dateReq);
			} catch (ParseException e) 
			{
				e.printStackTrace();
			}
		}
		if((!(city.equals(""))) && (date != null))  // user filters with both city and date.
		{
			if(pageable.getSort() == null)
				wastageList =   wasteRepository.findByLocationCityIgnoreCaseAndLastUpdateDateGreaterThan(city,date);  
			else
				wastageList =   wasteRepository.findByLocationCityIgnoreCaseAndLastUpdateDateGreaterThan(city,date,pageable.getSort());
		}
		else if((!(city.equals(""))) && (date == null))   // user only filters with city.
		{	 
			if(pageable.getSort() == null)
				wastageList =   wasteRepository.findByLocationCityIgnoreCase(city);
			else
				wastageList =   wasteRepository.findByLocationCityIgnoreCase(city,pageable.getSort());
		}
		else if((city.equals("")) && (date != null))   // user only filters with date.
		{
			if(pageable.getSort() == null)    
				wastageList =   wasteRepository.findByLastUpdateDateGreaterThan(date);
			else
				wastageList =   wasteRepository.findByLastUpdateDateGreaterThan(date,pageable.getSort());
		}
		else   // user does not provide any filter data.
		{
			if(pageable.getSort() == null)
				wastageList =   wasteRepository.findAll();
			else
				wastageList =   wasteRepository.findAll(pageable.getSort());
		}
		
		if(!(wastageList.isEmpty()))
		{
			for(Wastage wastage:wastageList)
			{
				if(wastage.getStatus() != WastageStatus.Deleted)
				{
				WastageBean bean = createLinksForWastage(wastage, authentication);
				wastageBeanList.add(bean);
				}

			}
			if(Pagination.createPageFromList(wastageBeanList, pageable) != null)
				return Pagination.createPageFromList(wastageBeanList, pageable) ;
			else
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No wastages found for the criterion.");
		}
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No wastages found for the criterion.");

	} 
	
	/*
	 * Get maping for a specific waste.
	 */
	@RequestMapping("/{id}")
	public WastageBean getWastageById(@PathVariable String id, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		Wastage wastage = null;
		Optional<Wastage> wastageObj = wasteRepository.findById(id);

		if(wastageObj.isPresent())
		{
			wastage = wastageObj.get();
			resp.addHeader("ETag", Integer.toString(wastage.getVersion()));  //Ading ETAG to response Header. 
			WastageBean bean = createLinksForWastage(wastage, authentication);   //Adding HATEOAS links.
			return bean;	
		}
		else 
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No wastages found for the criterion.");			
	}
	
	
	/*
	 * Get offers for a specific waste
	 */
	  @RequestMapping("/{id}/offers") 
	  public List<Offer> getOffersByWastageId(@PathVariable String id)
	  { 
		  List<Offer> offerList = offerRepository.findByWasteId(id); 
		  return offerList;
	  
	  }
	 

/*
 * Put mapping to resolve an offer.
 */
    @RequestMapping(value = "/{id}/resolve", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public WastageBean resolveWastage(@PathVariable("id") String id, Authentication authentication) throws IOException 
    {
    	Wastage resolvedWastage = null;
    	String userId = userRepository.findByUsername(authentication.getName()).getId();
    	Optional<Wastage> wastageObj = wasteRepository.findById(id);
    	if(wastageObj.isPresent())
    	{
    		resolvedWastage = wastageObj.get();

    		if(resolvedWastage.getStatus() == WastageStatus.Cleaning_in_progress)  // Wastage with Cleaning_in_progress status can be resolved.
    		{
        		List<Offer> offerList = offerRepository.findByWasteId(resolvedWastage.getId());
        		for(Offer offer: offerList)
        		{
        			if(offer.getOfferStatus() == OfferStatus.Accepted)
        			{
        				if(offer.getResolverId().equals(userId))  //same user whose offer is accepted can resolve the wastage.
        				{
        					offer.setOfferStatus(OfferStatus.SystemDeleted);
        					offerRepository.save(offer);
        					break;
        				}
        				else
        					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authrised to resolve the wastage.");
        			}
        		}
    			resolvedWastage.setStatus(WastageStatus.Resolved);
    			Wastage wastage = wasteRepository.save(resolvedWastage);
    			WastageBean bean = createLinksForWastage(wastage, authentication);
    			return bean;
    			
    		}
    		else
    		{
    			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wastage should be in Cleaning_in_progress status to be resolved.");
    		}
    	}
    	else
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Wastages found for the criterion.");   		

    }
    
 
	/*
	 * Function to delete offers for which due date has been passed and making wastages for those offers open for offers again.
	 */
    public void checkForDueDates() {
        Date currentDate = new Date(System.currentTimeMillis());
        List<Offer> offerList = offerRepository.findByOfferStatus(OfferStatus.Accepted);
        for(Offer offer: offerList)
        {
        	if(offer.getDueDate().before(currentDate))
        	{
        		offer.setOfferStatus(OfferStatus.SystemDeleted);
        		Wastage wastage = wasteRepository.findById(offer.getWasteId()).get();
        		wastage.setStatus(WastageStatus.Reported);
        		offerRepository.save(offer);
        		wasteRepository.save(wastage);
        	}
        }
    }
    

    
    /*
     * Function to create HATEOAS link for wastages.
     */
	public WastageBean createLinksForWastage(Wastage wastage , Authentication authentication) throws IOException
	{
		WastageBean bean = new WastageBean(wastage);
		Link selfLink = linkTo(WastageController.class).slash(wastage.getId()).withSelfRel().withType("Get");
		bean.add(selfLink);
		if((wastage.getStatus() != WastageStatus.Reported) && (wastage.getStatus() != WastageStatus.Deleted))
		{
			Link OffersLink = linkTo(methodOn(WastageController.class).getOffersByWastageId(wastage.getId())).withRel("All Offers made").withType("Get");
			bean.add(OffersLink);
		}
		if(wastage.getStatus() == WastageStatus.Cleaning_in_progress)
		{
			Link updateLink = linkTo(methodOn(WastageController.class).resolveWastage(wastage.getId(), authentication)).withRel("Resolve Wastage").withType("Put");
			bean.add(updateLink);		
		}
		if((wastage.getStatus() == WastageStatus.Reported) || (wastage.getStatus() == WastageStatus.OffersMade))
		{
			Offer offer = new Offer();
			HttpServletRequest req = null;
			HttpServletResponse resp = null;
			Link updateLink = linkTo(methodOn(OfferController.class).makeOffers(offer, req, resp, authentication)).withRel("Make an offer").withType("Post");
			bean.add(updateLink);
		}
		 return bean;
		
	}



	
	
	

}
