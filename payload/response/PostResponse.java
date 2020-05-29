package it.course.helpProject.payload.response;

import it.course.helpProject.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {

	private String title;

	private String content;

	// private Set<Tag> tags = new HashSet<Tag>();

	public static PostResponse create(Post post) {
		return new PostResponse(post.getTitle(), post.getContent());
		// ,post.getTags());

	}

}
