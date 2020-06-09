package de.bamberg.uni.isosysc.dsg.shared.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * @author amit
 *
 */
/*
 * class to covert basic user to users deatils as per requirement of digest authentication
 */
public class MyUser implements UserDetails  {
	
	private final User user;
	
	public MyUser(User user)
	{
		this.user = user;
	}
/*
 * To retrieve the roles of the users.
 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		//final List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()));
		List<GrantedAuthority> authoritiesList = new ArrayList<GrantedAuthority>();
		for(UserRole role: user.getRoles())
		{
			authoritiesList.add(new SimpleGrantedAuthority(role.toString()));
		}
		final List<GrantedAuthority> authorities = authoritiesList;
		return authorities;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	public User getUser() {
		return user;
	}
	
	

}
