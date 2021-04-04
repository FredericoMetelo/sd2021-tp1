package tp1.api.server.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class UsersResource implements RestUsers {

	private final Map<String,User> users = new HashMap<String, User>();

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());

	public UsersResource() {
	}

	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user is valid, if not return HTTP CONFLICT (409)
		if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || 
				user.getEmail() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		synchronized ( this ) {

			// Check if userId does not exist exists, if not return HTTP CONFLICT (409)
			if( users.containsKey(user.getUserId())) {
				Log.info("User already exists.");
				throw new WebApplicationException( Status.CONFLICT );
			}

			//Add the user to the map of users
			users.put(user.getUserId(), user);
			
		}
		
		return user.getUserId();
	}


	@Override
	public User getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);

		// Check if user is valid, if not return HTTP CONFLICT (409)
		if(userId == null || password == null) {
			Log.info("UserId or passwrod null.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		User user = null;
		
		synchronized (this) {
			user = users.get(userId);
		}
		 

		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		//Check if the password is correct
		if( !user.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		return user;
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);
		// TODO confirmar se os throws param a execução do metodo ou continua se pararem => chamar getr user em vez desta salpichada toda
		// Check if user is valid, if not return HTTP CONFLICT (409)
		if(userId == null || password == null) {
			Log.info("UserId or passwrod null.");
			throw new WebApplicationException( Status.CONFLICT );
		}
		User original = users.get(userId);
		// Check if user exists
		if( original == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		if( !user.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}
		if (user.getPassword() != null) original.setPassword(user.getPassword());
		if (user.getEmail() != null) original.setEmail(user.getEmail());
		if (user.getFullName() != null) original.setFullName(user.getFullName());
		return original;
	}


	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);
		// Check if user is valid, if not return HTTP CONFLICT (409)
		if(userId == null || password == null) {
			Log.info("UserId or passwrod null.");
			throw new WebApplicationException( Status.CONFLICT );
		}
		User original = users.get(userId);
		// Check if user exists
		if( original == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		if( !original.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		return users.remove(userId);
	}


	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);

		if(pattern == null) throw new WebApplicationException(Status.CONFLICT);

		List<User> homies = new LinkedList<>();
		for(User u : users.values()){
			if(u.getFullName().contains(pattern)) homies.add(u);
		}
		//TODO duvida: Devemos lançar alguma exceção tipo not found? Não me parece fazer sentido mas perguntar pelo sim pelo não?
		return homies;
	}


}
