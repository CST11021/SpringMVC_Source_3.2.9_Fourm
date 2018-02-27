package com.whz.transactional;

import lombok.Getter;
import lombok.Setter;

public class User {

	@Getter
	@Setter
	private int id;
	@Getter
	@Setter
	private String name;
	@Getter
	@Setter
	private int age;
	@Getter
	@Setter
	private String sex;

	public User() {}
	public User(String name, int age, String sex) {
		this.name = name;
		this.age = age;
		this.sex = sex;
	}
	public User(int id, String name, int age, String sex) {
		this.id = id;
		this.name = name;
		this.age = age;
		this.sex = sex;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				", age=" + age +
				", sex='" + sex + '\'' +
				'}';
	}
}