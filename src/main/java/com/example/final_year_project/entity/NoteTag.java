package com.example.final_year_project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "note_tag", indexes = {
    @Index(name = "idx_note_tag_note", columnList = "note_id"),
    @Index(name = "idx_note_tag_tag", columnList = "tag_id")
}, uniqueConstraints = @UniqueConstraint(columnNames = {"note_id", "tag_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
