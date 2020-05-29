package it.course.helpProject.controller;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.course.helpProject.entity.Blacklist;
import it.course.helpProject.entity.BlacklistReason;
import it.course.helpProject.entity.Comment;
import it.course.helpProject.entity.Post;
import it.course.helpProject.entity.Role;
import it.course.helpProject.entity.RoleName;
import it.course.helpProject.entity.Users;
import it.course.helpProject.payload.request.BlacklistRequest;
import it.course.helpProject.payload.request.LoginRequest;
import it.course.helpProject.payload.request.SignUpRequest;
import it.course.helpProject.payload.response.ApiResponseCustom;
import it.course.helpProject.payload.response.JwtAuthenticationResponse;
import it.course.helpProject.repository.BlacklistReasonRepository;
import it.course.helpProject.repository.BlacklistRepository;
import it.course.helpProject.repository.CommentRepository;
import it.course.helpProject.repository.PostRepository;
import it.course.helpProject.repository.RoleRepository;
import it.course.helpProject.repository.UserRepository;
import it.course.helpProject.security.JwtTokenProvider;
import it.course.helpProject.security.UserPrincipal;
import it.course.helpProject.service.CtrlUserBan;
import it.course.helpProject.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JwtTokenProvider tokenProvider;

	@Autowired
	BlacklistReasonRepository blacklistReasonRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	PostRepository postRepository;

	@Autowired
	BlacklistRepository blacklistRepository;

	@Autowired
	CtrlUserBan ctrlUserBan;

	@Autowired
	UserService userService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticatUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {

		log.info("Call controller authenticatUser with parameter usernameOrEmail {}",
				loginRequest.getUsernameOrEmail());
		Optional<Users> u = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(),
				loginRequest.getUsernameOrEmail());

		if (!u.isPresent()) {
			log.error("User {} not found", loginRequest.getUsernameOrEmail());
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 401, "Unauthorized",
					"Bad credentials_", request.getRequestURI()), HttpStatus.FORBIDDEN);
		}

		if (u.isPresent() && ctrlUserBan.isBanned(u.get()).isPresent()) {
			log.info("User {} unauthorized to log in. Reason: banned!", loginRequest.getUsernameOrEmail());
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 401, "Unauthorized",
					"User Banned Until " + ctrlUserBan.isBanned(u.get()).get().getBlacklistedUntil(),
					request.getRequestURI()), HttpStatus.FORBIDDEN);
		}

		Authentication authentication = null;
		try {
			authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					u.isPresent() ? u.get().getUsername() : " ", loginRequest.getPassword()));
		} catch (BadCredentialsException e) {
			return null;
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwt = tokenProvider.generateToken(authentication);
		log.info("User {} succesfully logged", loginRequest.getUsernameOrEmail());

		return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));

	}

	@PutMapping("/signup-confirm/{identifier}")
	public ResponseEntity<ApiResponseCustom> signupConfirm(@PathVariable String identifier,
			HttpServletRequest request) {

		log.info("Call controller signupConfirm with identifier: {}", identifier);

		Optional<Users> u = userRepository.findByIdentifierCode(identifier);

		if (!u.isPresent()) {
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 403, null, "User has just confirmed", request.getRequestURI()),
					HttpStatus.FORBIDDEN);
		}

		Role userRole = roleRepository.findByName(RoleName.ROLE_READER).orElseThrow(() -> new RuntimeException());
		u.get().setRoles(Collections.singleton(userRole));

		userRepository.save(u.get());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, "User confirmed", request.getRequestURI()),
				HttpStatus.OK);

	}

	@PostMapping("/signup")
	@Transactional
	public ResponseEntity<ApiResponseCustom> registerUser(@Valid @RequestBody SignUpRequest signUpRequest,
			HttpServletRequest request) {

		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 403, null,
					"Username already in use !", request.getRequestURI()), HttpStatus.BAD_REQUEST);
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 403, null, "Email already in use !", request.getRequestURI()),
					HttpStatus.BAD_REQUEST);
		}

		Users user = new Users(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		userRepository.save(user);

		log.info("User creation successfully completed", signUpRequest.getEmail());
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"User creation successfully completed", request.getRequestURI()), HttpStatus.OK);

	}

	@Transactional
	@PutMapping("/change-password/{identifier}/{newPassword}")
	public ResponseEntity<ApiResponseCustom> changePassword(@PathVariable String identifier,
			@PathVariable @Size(min = 5, max = 8) String newPassword, HttpServletRequest request) {

		log.info("Call controller changePassword with parameter identifier {} and newPassword ******* ", identifier);

		Optional<Users> u = userRepository.findByIdentifierCode(identifier);
		if (!u.isPresent()) {
			log.error("User not found with identifier: {}", identifier);
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 404, "null",
					"User not found with identifier: " + identifier, request.getRequestURI()), HttpStatus.NOT_FOUND);
		}

		log.info("Encoding password");
		u.get().setPassword(passwordEncoder.encode(newPassword));

		userRepository.save(u.get());

		log.info("Password has been modified by user {}", u.get().getUsername());
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null,
						"Password has been modified by user " + u.get().getUsername(), request.getRequestURI()),
				HttpStatus.OK);

	}

	@PutMapping("/change-password-by-logged-user/{newPassword}")
	@PreAuthorize("hasRole('READER') or hasRole('EDITOR') or hasRole('MANAGING_EDITOR') or hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> changePasswordByLoggedUser(
			@PathVariable @Size(min = 5, max = 20) String newPassword, HttpServletRequest request) {

		UserPrincipal userPrincipal = UserService.getAuthenticatedUser();

		Users user = userRepository.findById(userPrincipal.getId()).get();

		log.info("Call controller changePasswordByLoggedUser with logged user {}", user.getUsername());

		log.info("Encoding password");
		user.setPassword(passwordEncoder.encode(newPassword));

		userRepository.save(user);

		log.info("User {} password changed succesfully", user.getUsername());

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"Password has been modified succesfully!", request.getRequestURI()), HttpStatus.OK);
	}

	// needs to re-implement
	@PutMapping("/forgot-password/{usernameOrEmail}")
	@Transactional
	public ResponseEntity<ApiResponseCustom> forgotPassword(
			@PathVariable @Size(min = 3, max = 120) String usernameOrEmail, HttpServletRequest request) {

		log.info("Call controller forgotPassword with parameter usernameOrEmail: {}", usernameOrEmail);

		Optional<Users> u = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
		if (!u.isPresent()) {
			log.error("User {} not found", usernameOrEmail);
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 404, null, "User not found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"Email has been sent to: " + usernameOrEmail, request.getRequestURI()), HttpStatus.OK);

	}

	@PostMapping("/add-user-to-blacklist")
	@PreAuthorize("hasRole('READER')")
	public ResponseEntity<ApiResponseCustom> addUserToBlacklist(@RequestBody BlacklistRequest blacklistRequest,
			HttpServletRequest request) {

		log.info("Call controller addUserToBlacklist with BlacklistRequest as parameter");

		Optional<Users> u = userRepository.findById(blacklistRequest.getUserId());
		if (!u.isPresent()) {
			log.error("User with id {} not found", blacklistRequest.getUserId());
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 404, null, "User not found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);
		}

		// RECOVER FROM SECURITY CONTEXT THE USER LOGGED IN
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		Optional<Users> reporter = userRepository.findById(userPrincipal.getId());

		Optional<BlacklistReason> blr = blacklistReasonRepository.findById(blacklistRequest.getBlacklistReasonId());
		if (!blr.isPresent()) {
			log.error("BlacklistReason with id {} not found", blacklistRequest.getBlacklistReasonId());
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 404, null,
					"Blacklist Reason not found", request.getRequestURI()), HttpStatus.NOT_FOUND);

		}

		Optional<Post> p = null;
		Comment c = new Comment();
		Long commentId = Long.valueOf(0);
		if (blacklistRequest.getCommentId() > Long.valueOf(0)) {

			c = commentRepository.findById(blacklistRequest.getCommentId()).get();
			if (c == null) {
				log.info("Comment not found");
				return new ResponseEntity<ApiResponseCustom>(
						new ApiResponseCustom(Instant.now(), 404, null, "Comment not found", request.getRequestURI()),
						HttpStatus.NOT_FOUND);
			}

			commentId = c.getId();
			p = Optional.of(c.getPost());

		} else {

			p = postRepository.findById(blacklistRequest.getPostId());
			if (!p.isPresent()) {
				log.info("Post with id {} not found", blacklistRequest.getPostId());
				return new ResponseEntity<ApiResponseCustom>(
						new ApiResponseCustom(Instant.now(), 404, null, "Post not found", request.getRequestURI()),
						HttpStatus.NOT_FOUND);

			}

		}

		boolean isExist = blacklistRepository.existsByPostAndReporterAndCommentIdAndBlacklistReason(p.get(),
				reporter.get(), commentId, blr.get());

		if (!isExist) {
			Blacklist bl = new Blacklist(blacklistRequest.getBlacklistedFrom(), null, u.get(), p.get(), commentId,
					blr.get(), reporter.get(), false);

			blacklistRepository.save(bl);
			log.info("New Blacklist added");
		} else {
			log.info("Post/Comment has already been reported");
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
					"Post/Comment has already been reported", request.getRequestURI()), HttpStatus.OK);
		}

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null,
						"User " + u.get().getUsername() + " has been added to blacklist", request.getRequestURI()),
				HttpStatus.OK);

	}

}
