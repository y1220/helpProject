package it.course.helpProject.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import it.course.helpProject.entity.audit.UserAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//public class Comment extends DateAudit {
public class Comment extends UserAudit {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

//	@NotNull
//	@Email
//	@Size(max = 120)
//	private String email;
//
//	@NotNull
//	@Size(max = 100)
//	private String password;

	@Column(name = "review", nullable = false, columnDefinition = "TEXT")
	private String review;

	@Column(name = "is_visible", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
	private boolean isVisible;

	// Resolve infinite Recursion with Jackson JSON and Hibernate JPA in
	// bidirectional association
	@JsonBackReference
	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;

}
