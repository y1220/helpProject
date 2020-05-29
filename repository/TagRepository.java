package it.course.helpProject.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import it.course.helpProject.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

	Set<Tag> findByTagNameIn(Set<String> tagNames);

}
