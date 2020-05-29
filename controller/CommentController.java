package it.course.helpProject.controller;

import java.time.Instant;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.course.helpProject.entity.Comment;
import it.course.helpProject.entity.Post;
import it.course.helpProject.payload.request.CommentRequest;
import it.course.helpProject.payload.response.ApiResponseCustom;
import it.course.helpProject.repository.CommentRepository;
import it.course.helpProject.repository.PostRepository;
import it.course.helpProject.repository.TagRepository;

@RestController
@RequestMapping("/comments")
public class CommentController {

	@Autowired
	PostRepository postRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	TagRepository tagRepository;

	@PostMapping("/create-comment")
	public ResponseEntity<ApiResponseCustom> createComment(@RequestBody CommentRequest comment,
			HttpServletRequest request) {

		Optional<Post> p = postRepository.findById(comment.getPostId());
		if (!p.isPresent() || !p.get().isVisible())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 401, null, "The post doesn't exist", request.getRequestURI()),
					HttpStatus.FORBIDDEN);
		Comment c = new Comment();
		c.setReview(comment.getReview());
		c.setPost(postRepository.findById(comment.getPostId()).get());

		commentRepository.save(c);

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"New comment successfully created", request.getRequestURI()), HttpStatus.OK);
	}

	@PutMapping("/publish-comment/{id}")
	public ResponseEntity<ApiResponseCustom> publishComment(@PathVariable Long id, HttpServletRequest request) {

		Optional<Comment> c = commentRepository.findById(id);

		if (!c.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 404, null, "Comment not found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);

		if (c.get().isVisible())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 401, null,
					"The comment has been already visible", request.getRequestURI()), HttpStatus.FORBIDDEN);

		c.get().setVisible(true);
		commentRepository.save(c.get());

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"The comment has been successfully setted as visible", request.getRequestURI()), HttpStatus.OK);

	}

	@PutMapping("/Unpublish-comment/{id}")
	@Transactional
	public ResponseEntity<ApiResponseCustom> unpublishComment(@PathVariable Long id, HttpServletRequest request) {

		Optional<Comment> c = commentRepository.findById(id);

		if (!c.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 404, null, "Comment not found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);

		if (!c.get().isVisible())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 401, null,
					"The comment is not visible", request.getRequestURI()), HttpStatus.FORBIDDEN);

		c.get().setVisible(false);

		commentRepository.save(c.get());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null,
						"The comment has been successfully setted as not visible", request.getRequestURI()),
				HttpStatus.OK);

	}
}
