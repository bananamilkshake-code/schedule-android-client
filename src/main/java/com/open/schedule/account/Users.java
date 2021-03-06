package com.open.schedule.account;

import java.util.HashMap;

public class Users {
	private HashMap<Integer, User> users = new HashMap<Integer, User>();

	public void add(Integer userId, String name) {
		this.users.put(userId, new User(name, name));
	}

	public class User {
		public String name;
		public String email;

		public User(String name, String email) {
			this.name = name;
			this.email = email;
		}
	}
}
