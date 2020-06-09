package de.bamberg.uni.isosysc.dsg.shared.models;

import java.util.List;

import org.springframework.data.annotation.Id;

/*
 * Encapsulating users deatils.
 */
public class User {
	
	@Id private String id;
	private String name;
	private String username;
	private String password;
	private List<UserRole> roles;
	
	//Constructor
	public User(String id, String name, String username, String password, List<UserRole> roles) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.password = password;
		this.roles = roles;
	}
	
	public User() {
		// TODO Auto-generated constructor stub
	}
	
	//Setters and getters.
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public List<UserRole> getRoles() {
		return roles;
	}

	public void setRoles(List<UserRole> roles) {
		this.roles = roles;
	}

	
	

}
