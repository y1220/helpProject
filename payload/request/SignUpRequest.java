package it.course.helpProject.payload.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

	@NotBlank
	@Size(min = 3, max = 20)
	@Pattern(regexp = "[A-Za-z0-9]+", message = "please provide a name made by characters or numbers(mixed one is also fine)")
	private String username;

	@NotBlank
	@Size(min = 6, max = 120)
	@Email
	@Pattern(regexp = ".+@.+\\..+", message = "Please provide a valid email address")
	private String email;

	@NotBlank
	@Size(min = 5, max = 8)
	private String password;

}
