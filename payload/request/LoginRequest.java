package it.course.helpProject.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

	@NotBlank(message = "Username or Email must not be blank")
	private String usernameOrEmail;

	@NotBlank(message = "Password must not be blank")
	@Size(min = 5, max = 8)
	private String password;

}
