package it.course.helpProject.payload;

import java.util.Date;

import it.course.helpProject.entity.Users;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
import lombok.NoArgsConstructor;
//import lombok.Setter;

//@Getter @Setter @AllArgsConstructor 
@NoArgsConstructor
public class UserProfile {

	private Long id;
	private String username;
	private Date joinedAt;

	public UserProfile(Long id, String username, Date joinedAt) {
		super();
		this.id = id;
		this.username = username;
		this.joinedAt = joinedAt;
	}

	// return all together
	public static UserProfile create(Users user) {
		return new UserProfile(user.getId(), user.getUsername(), user.getCreatedAt());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(Date joinedAt) {
		this.joinedAt = joinedAt;
	}

}
