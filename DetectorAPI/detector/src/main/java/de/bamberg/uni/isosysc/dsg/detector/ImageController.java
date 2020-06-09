package de.bamberg.uni.isosysc.dsg.detector;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import de.bamberg.uni.isosysc.dsg.shared.models.Image;
import de.bamberg.uni.isosysc.dsg.shared.models.ImageBean;
import de.bamberg.uni.isosysc.dsg.shared.models.Pagination;
import de.bamberg.uni.isosysc.dsg.shared.models.Wastage;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageBean;
import de.bamberg.uni.isosysc.dsg.shared.models.WastageStatus;
import de.bamberg.uni.isosysc.dsg.shared.repositories.ImageRepository;
import de.bamberg.uni.isosysc.dsg.shared.repositories.WastageRepository;

/*
 * Spring Boot Controller class for URLs mapping regarding images.
 */
@EnableMongoRepositories(basePackages = "de.bamberg.uni.isosysc.dsg.shared.repositories")
@RestController
@RequestMapping("detector/images")
public class ImageController {

	@Autowired
	private ImageRepository imageRepository;
	@Autowired
	private WastageRepository wastageRepository;
	
	
	/*
	 * Post Mapping for uploading an image.
	 */
	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
    public ImageBean uploadImage(@RequestParam("file") MultipartFile file, @RequestParam String title, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException { 
		Image image = new Image(); 
		if(file == null)
		{
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No data is provided for updation.");
		}
		else
		{
		image.setTitle(title);
		image.setTitle(title);
        image.setImage(new Binary(BsonBinarySubType.BINARY, file.getBytes()));  // Images are transformed into BSON binary to store in MongoDB.
        Image createdImage = imageRepository.save(image);
        ImageBean bean = createLinksForImage(createdImage, authentication, req, resp);  // Adding HATEOAS links to images.
		return bean;
		}
    }
	
	/*
	 * Get mapping for retrieving images.
	 */
	@GetMapping()
	public Page<ImageBean> getImages(Pageable pageable, Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{	
		List<Image> imageList = null;
		List<ImageBean> imageBeanList = new ArrayList<ImageBean>();
		
		if(pageable.getSort() == null)
			imageList =   imageRepository.findAll();
		else
			imageList =   imageRepository.findAll(pageable.getSort());
		
		if(!(imageList.isEmpty()))
		{
			for(Image image:imageList)
			{
				ImageBean bean = createLinksForImage(image, authentication, req, resp);
				imageBeanList.add(bean);
			}
			if(Pagination.createPageFromImageList(imageBeanList, pageable) != null)  // Adding Pagination.
				return Pagination.createPageFromImageList(imageBeanList, pageable) ;
			else
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No images found for the criterion.");
		}
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No images found for the criterion.");
	
	}
	
	
	/*
	 * Get mapping for retrieving an image.
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ImageBean getImageById(@PathVariable String id , Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		Optional<Image> imageObj = imageRepository.findById(id);
		if(imageObj.isPresent())
		{
			Image image = imageObj.get();
			ImageBean bean = createLinksForImage(image, authentication, req, resp);
			return bean;
		}
			
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No images found for this image ID.");
	}
	
	/*
	 * Delete mapping to delete an image.
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void deleteImage(@PathVariable String id , Authentication authentication, HttpServletRequest req, HttpServletResponse resp)
	{
		Optional<Image> imageObj = imageRepository.findById(id);
		if(imageObj.isPresent())
		{
			Image image = imageObj.get();
			imageRepository.deleteById(id);		
		}
			
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No images found for this image ID.");
	}

	/*
	 * Function to retrieve wastage for an image.
	 */
	public String getWastageByImageId(@PathVariable String imageId)
	{
		
		Wastage wastage = null;
		Optional<Wastage> wastageObj = wastageRepository.findByImagesImageId(imageId);

		if(wastageObj.isPresent())
		{
			wastage = wastageObj.get();
			return wastage.getId();
		}
		else 
			return null;
		
	}
	
	/*
	 * Function to add HATEOAS to images.
	 */
	public ImageBean createLinksForImage(Image image , Authentication authentication, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		ImageBean bean = new ImageBean(image);
		Link selfLink = linkTo(ImageController.class).slash(image.getId()).withSelfRel().withType("Get");
		bean.add(selfLink);
		if(getWastageByImageId(image.getId()) != null)
		{
			Link wastageLink = linkTo(methodOn(WastageController.class).getWastageById(getWastageByImageId(image.getId()), authentication, req, resp)).withRel("Get Wastage details").withType("Get");
			bean.add(wastageLink);
		}
		 return bean;
		
	}


	
	
}

