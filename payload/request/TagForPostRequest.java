package it.course.helpProject.payload.request;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagForPostRequest {

	private Long postId;

	private Set<String> tagNames;
}
