package it.course.helpProject.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {

	private String email;

	private String password;

	private String title;

	private String content;

	private boolean suggestFlag;

	private boolean commentFlag;

}
