package com.open.schedule.storage.tables;

import java.util.SortedMap;
import java.util.TreeMap;

public class Users {
	public SortedMap<Integer, User> users = new TreeMap<Integer, User>();

	public void add(Integer userId, String name) {
		users.put(userId, new User(name, name));
	}

	public class User {
		public final String email;
		public final String name;

		public User(String name, String email) {
			this.name = name;
			this.email = email;
		}
	}

}
