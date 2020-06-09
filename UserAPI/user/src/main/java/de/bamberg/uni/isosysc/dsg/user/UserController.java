package de.bamberg.uni.isosysc.dsg.user;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import de.bamberg.uni.isosysc.dsg.shared.models.User;
import de.bamberg.uni.isosysc.dsg.shared.models.UserRole;
import de.bamberg.uni.isosysc.dsg.shared.repositories.UserRepository;

/**
 * @author amit
 *
 */
@EnableMongoRepositories(basePackages = "de.bamberg.uni.isosysc.dsg.shared.repositories")
@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserRepository userRepository;



	/*
	 * Post Mapping for creating a user
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public String createUsers(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) 
	{
		if(user.getName().equals("") || user.getUsername().equals("") || user.getPassword().equals(""))
		{
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, Username or Password missing.");
		}
		else if(user.getRoles() == null)
		{
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No roles have been chosen by the user.");
		}
		else
		{
			if(userRepository.findByUsername(user.getUsername()) == null)
				{
						User newUser = userRepository.save(user);
						return "User " + newUser.getName() + " created with Id: " + newUser.getId();
					}
			else
			{
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists.");
			}
		}
	}

	/*
	 * Get Mapping to access user details.
	 */
	@GetMapping("/{id}")
	public User getUser(@PathVariable("id") String id)
	{

		Optional<User> user = userRepository.findById(id);
		User userObj = new User();
		if(user.isPresent())
		{
			userObj = user.get();
			return userObj;
		}
		else
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No User found for the criterion.");
		}

	}

	/*
	 * Get maping to authenicate a user from android APP.
	 */
	@GetMapping("/authenticate/{username}")
	public List<UserRole> authenticate(@RequestParam(value="credentials") String credentials, @PathVariable("username") String username) throws UnsupportedEncodingException
	{
		User user = userRepository.findByUsername(username);
		String userCredentials = user.getUsername()+":"+user.getPassword();
		String encoding = Base64.getEncoder().encodeToString((userCredentials).getBytes("UTF-8"));
		if(credentials.equals(encoding))
			return user.getRoles();
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No User found for the criterion.");


	}


}
