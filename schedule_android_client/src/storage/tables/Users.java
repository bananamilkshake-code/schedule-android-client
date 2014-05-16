package storage.tables;

import java.util.SortedMap;
import java.util.TreeMap;

public class Users {
	public class User {
		public final String email;
		public final String name;

		public User(String name, String email) {
			this.name = name;
			this.email = email;
		}
	}
	
	public SortedMap<Integer, User> users = new TreeMap<Integer, User>();
	
}
