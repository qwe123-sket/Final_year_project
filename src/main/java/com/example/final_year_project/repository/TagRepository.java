package com.example.final_year_project.repository;

import com.example.final_year_project.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameIn(List<String> names);

    List<Tag> findByIdIn(List<Long> ids);
}
