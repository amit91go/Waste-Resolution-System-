package de.bamberg.uni.isosysc.dsg.shared.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import de.bamberg.uni.isosysc.dsg.shared.repositories.UserRepository;

/*
 * Class to get user datails for digest authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
 
	
    @Autowired
    private UserRepository userRepository;
    
    /*
     * Retreiving user details based on username
     */
 
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new MyUser(user);
    }


}
