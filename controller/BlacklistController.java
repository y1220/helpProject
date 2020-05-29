package it.course.helpProject.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.course.helpProject.entity.Blacklist;
import it.course.helpProject.entity.BlacklistReason;
import it.course.helpProject.payload.request.BlacklistSignedAsVerifiedRequest;
import it.course.helpProject.payload.response.ApiResponseCustom;
import it.course.helpProject.payload.response.BlacklistResponse;
import it.course.helpProject.repository.BlacklistReasonRepository;
import it.course.helpProject.repository.BlacklistRepository;
import it.course.helpProject.repository.CommentRepository;
import it.course.helpProject.repository.PostRepository;
import it.course.helpProject.repository.UserRepository;

@RestController
@RequestMapping("/blacklist")
public class BlacklistController {

	@Value("${app.totalBan}")
	int totalBan;

	@Autowired
	BlacklistRepository blacklistRepository;

	@Autowired
	BlacklistReasonRepository blacklistReasonRepository;

	@Autowired
	PostRepository postRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	UserRepository userRepository;

	@GetMapping("/get-blacklist-to-verify")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getBlacklistToVerify(HttpServletRequest request) {

		List<Blacklist> bls = blacklistRepository.findByIsVerifiedFalse();

		if (bls.size() < 1)
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, null,
					"No Items found in blacklist", request.getRequestURI()), HttpStatus.OK);

		List<BlacklistResponse> blrs = bls.stream().map(BlacklistResponse::create).collect(Collectors.toList());

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, blrs, request.getRequestURI()), HttpStatus.OK);

	}

	@Transactional
	@PutMapping("/update-blacklist")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> updateBlacklist(
			@RequestBody List<BlacklistSignedAsVerifiedRequest> blacklistSignedAsVerifiedRequestList,
			HttpServletRequest request) {

		List<Blacklist> bls = new ArrayList<Blacklist>();

		for (BlacklistSignedAsVerifiedRequest blsv : blacklistSignedAsVerifiedRequestList) {

			Blacklist bl = blacklistRepository.findById(blsv.getBlacklistId()).get();
			bl.setVerified(true);

			if (blsv.isToBan()) {

				// Ctrl 100+
				List<Blacklist> blacklists = blacklistRepository.findByUserAndBlacklistedUntilIsNotNull(bl.getUser());
				int totalDays = 0;
				for (Blacklist b : blacklists) {
					int day = b.getBlacklistReason().getDays();
					totalDays = totalDays + day;
				}

				BlacklistReason blr = blacklistReasonRepository.findById(blsv.getBlacklistReasonId()).get();

				if ((totalDays + blr.getDays()) > totalBan) {
					Optional<BlacklistReason> bklr = blacklistReasonRepository.findByReason("PERMANENT BAN");
					LocalDate blacklistedUntil = bl.getBlacklistedFrom().plusDays(bklr.get().getDays());
					bl.setBlacklistedUntil(blacklistedUntil);
					bl.setBlacklistReason(bklr.get());
				} else {
					LocalDate blacklistedUntil = bl.getBlacklistedFrom().plusDays(blr.getDays());
					bl.setBlacklistedUntil(blacklistedUntil);
					bl.setBlacklistReason(blr);
				}

			}

			bls.add(bl);

		}

		blacklistRepository.saveAll(bls);

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, "Blacklist Updated", request.getRequestURI()),
				HttpStatus.OK);
	}

	@PutMapping("/remove-from-blacklist/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public ResponseEntity<ApiResponseCustom> removeFromBlacklist(@PathVariable Long id, HttpServletRequest request) {

		Blacklist bl = blacklistRepository.findById(id).get();
		bl.setBlacklistedUntil(null);
		blacklistRepository.save(bl);

		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, null, "Removed from Blacklist", request.getRequestURI()),
				HttpStatus.OK);
	}

}
