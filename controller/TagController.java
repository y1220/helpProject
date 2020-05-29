package it.course.helpProject.controller;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.course.helpProject.entity.Tag;
import it.course.helpProject.payload.response.ApiResponseCustom;
import it.course.helpProject.repository.CommentRepository;
import it.course.helpProject.repository.PostRepository;
import it.course.helpProject.repository.TagRepository;

@RestController
@RequestMapping("/tags")
public class TagController {

	@Autowired
	PostRepository postRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	TagRepository tagRepository;

	@PostMapping("/create-tag/{tagName}")
	public ResponseEntity<ApiResponseCustom> createTag(@PathVariable String tagName, HttpServletRequest request) {

		Tag t = new Tag();
		t.setTagName(tagName);

		tagRepository.save(t);

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"New tag successfully created", request.getRequestURI()), HttpStatus.OK);
	}

}
