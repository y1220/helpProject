package it.course.helpProject.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import it.course.helpProject.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	List<Post> findAllByIsVisibleTrue(Sort by);

	List<Post> findByIsVisibleTrueAndCreatedBy(Long userId);

}
