
package it.course.helpProject.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.course.helpProject.entity.Blacklist;
import it.course.helpProject.entity.BlacklistReason;
import it.course.helpProject.entity.Role;
import it.course.helpProject.entity.RoleName;
import it.course.helpProject.entity.Users;
import it.course.helpProject.payload.UserProfile;
import it.course.helpProject.payload.request.ChangeRoleRequest;
import it.course.helpProject.payload.response.ApiResponseCustom;
import it.course.helpProject.payload.response.BannedUserProfile;
import it.course.helpProject.payload.response.BlacklistResponse;
import it.course.helpProject.repository.BlacklistReasonRepository;
import it.course.helpProject.repository.BlacklistRepository;
import it.course.helpProject.repository.CommentRepository;
import it.course.helpProject.repository.PostRepository;
import it.course.helpProject.repository.RoleRepository;
import it.course.helpProject.repository.TagRepository;
import it.course.helpProject.repository.UserRepository;
import it.course.helpProject.security.UserPrincipal;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	BlacklistRepository blacklistRepository;

	@Autowired
	BlacklistReasonRepository blacklistReasonRepository;

	@Autowired
	PostRepository postRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	TagRepository tagRepository;

	@PostMapping("change-role")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGING_EDITOR')")
	public ResponseEntity<ApiResponseCustom> changeRole(@RequestBody ChangeRoleRequest changeRoleRequest,
			HttpServletRequest request) {

		// FIND USER BY ID
		Optional<Users> u = userRepository.findById(changeRoleRequest.getId());
		if (!u.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 401, null, "User Not Found", request.getRequestURI()),
					HttpStatus.FORBIDDEN);

		// FIND ROLE BY OLD ROLE NAME
		Optional<Role> oldRole = roleRepository.findByName(RoleName.valueOf(changeRoleRequest.getOldRoleName()));
		if (!oldRole.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 401, null, "Invalid User old role", request.getRequestURI()),
					HttpStatus.BAD_REQUEST);

		// FIND ROLE BY NEW ROLE NAME
		Optional<Role> newRole = roleRepository.findByName(RoleName.valueOf(changeRoleRequest.getNewRoleName()));
		if (!newRole.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 401, null, "Invalid User new role", request.getRequestURI()),
					HttpStatus.BAD_REQUEST);

		// EXTRACT ROLES COLLECTION FROM USER
		Set<Role> userRoles = u.get().getRoles();

		// REMOVE OLD ROLE & ADD NEW ROLE
		userRoles.remove(oldRole.get());
		userRoles.add(newRole.get());

		// SET THE NEW COLLECTION OF ROLES INTO THE USER
		u.get().setRoles(userRoles);
		// SAVE THE USER WITH THE NEW COLLECTION OF ROLES
		userRepository.save(u.get());
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, "User role updated", request.getRequestURI()),
				HttpStatus.OK);

	}

	@PutMapping("/change-newsletter-permission")
	@PreAuthorize("hasRole('READER')")
	public ResponseEntity<ApiResponseCustom> changeNewsletterPermission(HttpServletRequest request) {

		// RECOVER FROM SECURITY CONTEXT THE USER LOGGED IN
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

		// FIND USER
		Optional<Users> u = userRepository.findByUsername(userPrincipal.getUsername());

		// SAVE NEW NEWSLETTER PERMISSION
		userRepository.save(u.get());

		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
				"Newletter permission updated", request.getRequestURI()), HttpStatus.OK);

	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGING_EDITOR') or hasRole('READER') or hasRole('EDITOR')")
	public ResponseEntity<ApiResponseCustom> viewMyDetails(HttpServletRequest request) {

		// RECOVER FROM SECURITY CONTEXT THE USER LOGGED IN
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, userPrincipal, request.getRequestURI()), HttpStatus.OK);

	}

	@GetMapping("/view-all-users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> viewAllUsers(HttpServletRequest request) {

		List<Users> allUsers = userRepository.findAll();
		List<UserProfile> ups = new ArrayList<UserProfile>();

		for (Users u : allUsers) {
			ups.add(UserProfile.create(u));
		}

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, ups, request.getRequestURI()), HttpStatus.OK);
	}

	@GetMapping("/view-one-user/{username}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> viewOneUser(@PathVariable String username, HttpServletRequest request) {

		Optional<Users> u = userRepository.findByUsername(username);
		UserProfile up = UserProfile.create(u.get());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, up, request.getRequestURI()), HttpStatus.OK);
	}

	@GetMapping("/view-all-banned-users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> viewAllBannedUsers(HttpServletRequest request) {

		List<Blacklist> bls = blacklistRepository.bannedUserProfileList(LocalDate.now());

		List<BannedUserProfile> bup = new ArrayList<BannedUserProfile>();

		for (Blacklist bl : bls) {
			Users u = userRepository.findById(bl.getUser().getId()).get();
			BlacklistReason blr = blacklistReasonRepository.findById(bl.getBlacklistReason().getId()).get();
			List<BlacklistResponse> blacklists = blacklistRepository.findByUserAndIsVerifiedTrue(u).stream()
					.map(BlacklistResponse::create).collect(Collectors.toList());
			bup.add(BannedUserProfile.create(u, bl, blr, blacklists, blacklists.size()));
		}

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, bup, request.getRequestURI()), HttpStatus.OK);
	}

}
