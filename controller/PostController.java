package it.course.helpProject.controller;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.course.helpProject.entity.Post;
import it.course.helpProject.entity.Tag;
import it.course.helpProject.payload.request.PostRequest;
import it.course.helpProject.payload.request.TagForPostRequest;
import it.course.helpProject.payload.request.TagForSearch;
import it.course.helpProject.payload.response.ApiResponseCustom;
import it.course.helpProject.payload.response.PostResponse;
import it.course.helpProject.repository.CommentRepository;
import it.course.helpProject.repository.PostRepository;
import it.course.helpProject.repository.TagRepository;

@RestController
@RequestMapping("/posts")
public class PostController {

	@Autowired
	PostRepository postRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	TagRepository tagRepository;

	@GetMapping("/view-all-published-posts")
	public ResponseEntity<ApiResponseCustom> viewAllPublishedPosts(HttpServletRequest request) {

		List<Post> allPosts = postRepository.findAllByIsVisibleTrue(Sort.by(Sort.Direction.DESC, "updatedAt"));

		// I wanted to print also tags though i couldn't figure out
		List<PostResponse> pr = allPosts.stream().map(PostResponse::create).collect(Collectors.toList());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, pr, request.getRequestURI()), HttpStatus.OK);

	}

	@PostMapping("/create-post")
	public ResponseEntity<ApiResponseCustom> createPost(@RequestBody PostRequest post, HttpServletRequest request) {

		Post p = new Post();
		p.setTitle(post.getTitle());
		p.setContent(post.getContent());
		p.setSuggestFlag(post.isSuggestFlag());
		p.setCommentFlag(post.isCommentFlag());
		// p.setTags(post.getTags());

		postRepository.save(p);

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"New post successfully created", request.getRequestURI()), HttpStatus.OK);
	}

	@PutMapping("/publish-post/{id}")
	public ResponseEntity<ApiResponseCustom> publishPost(@PathVariable Long id, HttpServletRequest request) {

		Optional<Post> p = postRepository.findById(id);

		if (!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 404, null, "Post not found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);

		if (p.get().isVisible())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 401, null,
					"The Post has been already visible", request.getRequestURI()), HttpStatus.FORBIDDEN);

		p.get().setVisible(true);
		postRepository.save(p.get());

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"Post has been successfully setted as visible", request.getRequestURI()), HttpStatus.OK);

	}

	@PutMapping("/unpublish-post/{id}")
	@Transactional
	public ResponseEntity<ApiResponseCustom> unpublishPost(@PathVariable Long id, HttpServletRequest request) {

		Optional<Post> p = postRepository.findById(id);

		if (!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 404, null, "Post not found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);

		if (!p.get().isVisible())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 401, null, "The Post is not visible", request.getRequestURI()),
					HttpStatus.FORBIDDEN);

		p.get().setVisible(false);

		postRepository.save(p.get());

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"Post has been successfully setted as not visible", request.getRequestURI()), HttpStatus.OK);

	}

	@PutMapping("/add-tags-to-posts")
	public ResponseEntity<ApiResponseCustom> addTagsToPosts(@RequestBody TagForPostRequest tags,
			HttpServletRequest request) {

		Optional<Post> post = postRepository.findById(tags.getPostId());
		Set<Tag> tagsToBeAdded = tagRepository.findByTagNameIn(tags.getTagNames());

		if (post.isEmpty() || tagsToBeAdded.size() == 0)
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 404, null, "No tags OR posts found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);

		post.get().setTags(tagsToBeAdded);
		postRepository.save(post.get());

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"Posts successfully updated with new tags", request.getRequestURI()), HttpStatus.OK);

	}

	@GetMapping("/get-posts-by-tags")
	public ResponseEntity<ApiResponseCustom> getPostsByTags(@RequestBody TagForSearch tagNames,
			HttpServletRequest request) {

		Set<Tag> tags = tagRepository.findByTagNameIn(tagNames.getTagNames());

		if (tags.isEmpty())// error in aws
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 404, null, "Tags not found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);

		Set<Post> posts = new HashSet<Post>();

		for (Tag t : tags) {
			for (Post p : t.getPosts()) {
				if (!posts.contains(p) && p.isVisible())
					posts.add(p);
			}
		}
		// it was necessary to exclude tags to print the result(Reason not sure sorry..)
		List<PostResponse> pr = posts.stream().map(PostResponse::create).collect(Collectors.toList());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, pr, request.getRequestURI()), HttpStatus.OK);

	}

}
