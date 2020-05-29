package it.course.helpProject.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import it.course.helpProject.entity.audit.UserAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post")
/* @Data */ @Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(callSuper=false)
//public class Post extends DateAudit {
public class Post extends UserAudit {

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

	@Column(name = "title", nullable = false, columnDefinition = "VARCHAR(100)")
	private String title;

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "suggestFlag", nullable = false, columnDefinition = "tinyint(1)")
	private boolean suggestFlag;

	@Column(name = "commentFlag", nullable = false, columnDefinition = "tinyint(1)")
	private boolean commentFlag;

	@Column(name = "is_visible", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
	private boolean isVisible;

	@JsonManagedReference // Resolve infinite Recursion with Jackson JSON and
	// Hibernate JPA in
	// bidirectional association
	@OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.MERGE, orphanRemoval = true)
	private List<Comment> comments = new ArrayList<Comment>();

	@ManyToMany
	@JoinTable(name = "post_tag", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private Set<Tag> tags = new HashSet<Tag>();

}
