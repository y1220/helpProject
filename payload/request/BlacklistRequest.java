package it.course.helpProject.payload.request;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlacklistRequest {

	@NotNull
	private LocalDate blacklistedFrom;

	@NotNull
	private LocalDate blacklistedUntil;

	@NotNull
	private Long userId;

	@NotNull
	private Long postId;

	private Long commentId = Long.valueOf(0);

	@NotNull
	private Long blacklistReasonId;

}
