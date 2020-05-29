package it.course.helpProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.course.helpProject.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByIsVisibleTrueAndCreatedBy(Long userId);

}
