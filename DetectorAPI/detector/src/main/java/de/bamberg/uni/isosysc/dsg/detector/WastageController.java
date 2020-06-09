package de.bamberg.uni.isosysc.dsg.detector;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import de.bamberg.uni.isosysc.dsg.shared.models.Wastage;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageBean;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageStatus;
import de.bamberg.uni.isosysc.dsg.shared.models.ImageDetails;
import de.bamberg.uni.isosysc.dsg.shared.models.Offer;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferBean;
import de.bamberg.uni.isosysc.dsg.shared.models.OfferStatus;
import de.bamberg.uni.isosysc.dsg.shared.models.Pagination;
import de.bamberg.uni.isosysc.dsg.shared.repositories.WastageRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.ImageRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.OfferRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.UserRepository;


/*
 * Spring Boot Controller class for URLs mapping regarding wastages.
 */
@EnableMongoRepositories(basePackages = "de.bamberg.uni.isosysc.dsg.shared.repositories")
@RestController
@RequestMapping("/detector/wastages")
public class WastageController {

	@Autowired
	private WastageRepository wasteRepository;
	@Autowired
	private OfferRepository offerRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ImageRepository imageRepository;

	/*
	 * Post Mapping for reporting a wastage.
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WastageBean reportWastages( @RequestBody Wastage wastage, HttpServletRequest req, HttpServletResponse resp, Authentication authentication) {
		if((wastage.getDescription().equals("")) || (wastage.getLocation() == null))
		{
			//Description and Location are mandatory for wastage creation.
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description and Location are mandatory.");
		}
		wastage.setStatus(WastageStatus.Reported);
		Date date = new Date(System.currentTimeMillis());
		wastage.setlastUpdateDate(date);
		wastage.setUserId(userRepository.findByUsername(authentication.getName()).getId());
		if(wastage.getImages() != null)
		{
			List<ImageDetails> images = new ArrayList<ImageDetails>();
			for(ImageDetails image: wastage.getImages())
			{
				if(imageRepository.findById(image.getImageId()).isPresent())
				{
					if(!(checkWastageByImageId(image.getImageId())))
					{
						ImageDetails imageObj = new ImageDetails();
						imageObj.setImageId(image.getImageId());
						imageObj.setImageUrl(req.getLocalAddr() + ":" + req.getLocalPort() + "/detector/images/" + image.getImageId());
						images.add(imageObj);
					}
					else
					{
						//Same image can not be used for different wastages.
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ImageID: " + image.getImageId() + " aleary used for another wastage.");
					}

				}
				else
				{
					//ImageId provided does not exist.
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ImageID: " + image.getImageId() + " not uploaded.");
				}
			}
			wastage.setImages(images);
		}
		wastage.setVersion(1); //to check against concurrent modifications.
		Wastage createdWastage = wasteRepository.save(wastage);
		resp.addHeader("ETag", Integer.toString(wastage.getVersion()));  // adding ETag to response Header.
		WastageBean bean = createLinksForWastage(createdWastage, authentication, req, resp);

		return bean;
	}

	
	/*
	 *  Get mapping for retrieving all active wastages. 
	 */
	@GetMapping
	public Page<WastageBean> getWastages(@RequestParam(value="city", defaultValue="") String city, @RequestParam(value="date",defaultValue="") String dateReq, Pageable pageable, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{

		checkForDueDates(); // checking if any wastages have been opened again for offers as due date passed for any accepted offers.
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
		if((!(city.equals(""))) && (date != null)) // users filters with both city and date.
		{
			if(pageable.getSort() == null)
				wastageList =   wasteRepository.findByLocationCityIgnoreCaseAndLastUpdateDateGreaterThan(city,date);
			else
				wastageList =   wasteRepository.findByLocationCityIgnoreCaseAndLastUpdateDateGreaterThan(city,date,pageable.getSort());
		}
		else if((!(city.equals(""))) && (date == null))  // users only filters with only city.
		{	
			if(pageable.getSort() == null)
				wastageList =   wasteRepository.findByLocationCityIgnoreCase(city);
			else
				wastageList =   wasteRepository.findByLocationCityIgnoreCase(city,pageable.getSort());
		}
		else if((city.equals("")) && (date != null))  // users only filters with only date.
		{
			if(pageable.getSort() == null)
				wastageList =   wasteRepository.findByLastUpdateDateGreaterThan(date);
			else
				wastageList =   wasteRepository.findByLastUpdateDateGreaterThan(date,pageable.getSort());
		}
		else  // user does not provide any filtering parameters.
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
				if(wastage.getStatus() != WastageStatus.Deleted)  // Deleted wastages are not shown to users.
				{
				WastageBean bean = createLinksForWastage(wastage, authentication, req, resp);  // Adding HATEOAS links to wastages.
				wastageBeanList.add(bean);
				}

			}
			if(Pagination.createPageFromList(wastageBeanList, pageable) != null)        // Adding pagination as per request.
				return Pagination.createPageFromList(wastageBeanList, pageable) ;
			else
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No wastages found for the criterion.");
		}
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No wastages found for the criterion.");

	} 


	/*
	 * Get mapping for retrieving a specific waste.
	 */
	@RequestMapping("/{id}")
	public WastageBean getWastageById(@PathVariable String id, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		
		Wastage wastage = null;
		Optional<Wastage> wastageObj = wasteRepository.findById(id);

		if(wastageObj.isPresent())
		{
			wastage = wastageObj.get();
			WastageBean bean = createLinksForWastage(wastage, authentication, req, resp);  // Adding HATEOAS links to wastages.
			resp.addHeader("ETag", Integer.toString(wastage.getVersion()));   // adding ETag to response Header.
			return bean;	
		}
		else 
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No wastages found for the criterion for the criterion.");


	}

	/*
	 * Delete mapping for a specific wastage.
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void deleteWastage(@PathVariable("id") String id, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) 
	{
		Wastage deletedWastage = null;
		Optional<Wastage> wastageObj = wasteRepository.findById(id);
		if(wastageObj.isPresent())
		{
			deletedWastage = wastageObj.get();
			if(!(deletedWastage.getUserId().equals(userRepository.findByUsername(authentication.getName()).getId())))
			{	//Same user who created the wastage has access to delete it.
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This wastage is not reported by current detector.");
			}
			else
			{
				if((deletedWastage.getStatus() != WastageStatus.Cleaning_in_progress) && (deletedWastage.getStatus() != WastageStatus.Resolved))
				{
					//wastages can be deleted only if no offer is accepted for same.
					deletedWastage.setStatus(WastageStatus.Deleted);
					List<Offer> offerList = offerRepository.findByWasteId(deletedWastage.getId());
					for(Offer offer: offerList)
					{ // Marking all offers for the wastage as deleted. 
						offer.setOfferStatus(OfferStatus.SystemDeleted);
						offerRepository.save(offer);
					}
					wasteRepository.save(deletedWastage);
				}
				else
				{
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wastage should not have accepted status to be deleted.");
				}
			}
		}
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Wastages found for the criterion.");   		

	}

	/*
	 * Put Mapping for updating a wastage.
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public WastageBean updateWastage(@RequestHeader("If-Match") int eTag, @PathVariable("id") String id,@RequestBody Wastage wastage, Authentication authentication, HttpServletRequest req, HttpServletResponse resp)
	{
		if(wastage != null)
		{
		Wastage wastageObj = null;
		Optional<Wastage> opWastage = wasteRepository.findById(id);
		
		if(opWastage.isPresent())
		{
			wastageObj = opWastage.get();
			if(!(wastageObj.getUserId().equals(userRepository.findByUsername(authentication.getName()).getId())))
			{	//Same user who created the wastage has access to update it.
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This wastage is not reported by current detector.");
			}
			else if(wastageObj.getStatus() == WastageStatus.Deleted)
			{
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This wastage had already been deleted.");
			}
			else if((eTag != wastageObj.getVersion()))
			{ // User has a stale version of wastage.
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "This wastage has been updated since last access.");
			}
			else
			{	
				int version = wastageObj.getVersion();
				wastage.setUserId(wastageObj.getUserId());
				if(wastage.getLocation() == null)
				{
					wastage.setLocation(wastageObj.getLocation());
				}
				if(wastage.getDescription() == null)
				{
					wastage.setDescription(wastageObj.getDescription());
				}
				if(wastage.getImages() != null)
				{
					List<ImageDetails> images = new ArrayList<ImageDetails>();
					for(ImageDetails image: wastage.getImages())
					{
						if(imageRepository.findById(image.getImageId()).isPresent())
						{
							ImageDetails imageObj = new ImageDetails();
							imageObj.setImageId(image.getImageId());
							imageObj.setImageUrl(req.getLocalAddr() + ":" + req.getLocalPort() + "/detector/images/" + image.getImageId());
							images.add(imageObj);
						}
						else
						{
							throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ImageID: " + image.getImageId() + " not uploaded.");
						}
					}

					wastage.setImages(images);
				}
				else
				{
					if(wastageObj.getImages() != null)
						wastage.setImages(wastageObj.getImages());
				}
				wastage.setId(id);
				wastage.setStatus(wastageObj.getStatus());
				wastage.setlastUpdateDate(new Date(System.currentTimeMillis()));	
				wastage.setVersion(version + 1);
				Wastage updatedWastage = wasteRepository.save(wastage);
				resp.addHeader("ETag", Integer.toString(wastage.getVersion()));  // Adding ETag to the response header.
				WastageBean bean = createLinksForWastage(updatedWastage, authentication, req, resp);				
				return bean;
			}
		}
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Wastages found for the criterion.");
		}
		else
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No data is provided for updation.");
	}



	public boolean checkWastageByImageId(String imageId)
	{
		Optional<Wastage> wastageObj = wasteRepository.findByImagesImageId(imageId);
		if(wastageObj.isPresent())
			return true;
		else 
			return false;		
	}

	
	
	/*
	 *  Get mapping for retrieving a specific offer from many offers made for a wastage.
	 */
	@RequestMapping("{wastageId}/offers/{offerId}")
	public OfferBean getOfferById(@PathVariable String wastageId, @PathVariable String offerId, Authentication authentication , HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		Offer offer = null;
		String resolerId = userRepository.findByUsername(authentication.getName()).getId();
		Optional<Offer> offerObj = offerRepository.findById(offerId);

		if(offerObj.isPresent())
		{
			offer = offerObj.get();
			if(resolerId.equals(offer.getResolverId()))
			{
			OfferBean bean = createLinksForOffer(wastageId, offer, authentication, req, resp);
			return bean;	
			}
			else
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Offer does not belong to the reslover.");					
		}
		else 
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Offers found for the criterion.");			
	}
	
    /*
     * Get Mapping for retrieving offers for a specific wastage.
     */
	@RequestMapping("{wastageId}/offers")
	public List<OfferBean> getOffersByWastageId(@PathVariable String wastageId, Authentication authentication, HttpServletRequest req, HttpServletResponse resp)
	{
		List<Offer> offerList = offerRepository.findByWasteId(wastageId);
		List<OfferBean> offerListBean = new ArrayList<OfferBean>();
		for(Offer offer: offerList)
		{
			OfferBean bean = null;
			try {
				bean = createLinksForOffer( wastageId , offer, authentication, req , resp); // Adding HATEOAS links to offer.
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			offerListBean.add(bean);
		}
		
		return offerListBean;

	}
	
	
	/*
	 * Funcation to generate links for a wastage.
	 */
	public WastageBean createLinksForWastage(Wastage wastage , Authentication authentication, HttpServletRequest req, HttpServletResponse resp)
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
    
	
	/*
	 * Function to generate links for an offer.
	 */
	public OfferBean createLinksForOffer(String wastageId, Offer offer, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		OfferBean bean = new OfferBean(offer);
		Link selfLink = linkTo(methodOn(WastageController.class).getOfferById(wastageId, offer.getId(), authentication, req, resp)).withSelfRel().withType("Get");
		bean.add(selfLink);
		Link wastageLink = linkTo(methodOn(WastageController.class).getWastageById(offer.getWasteId(), authentication, req, resp)).withRel("Get Wastage Details").withType("Get");
		bean.add(wastageLink);
		return bean;
		
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




}
