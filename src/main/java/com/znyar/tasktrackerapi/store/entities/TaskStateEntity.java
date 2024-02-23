package com.znyar.tasktrackerapi.store.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task_state")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    @Column(unique = true)
    String name;
    Long ordinal;
    @Builder.Default
    Instant createdAt = Instant.now();
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="task_state_id", referencedColumnName = "id")
    List<TaskEntity> tasks = new ArrayList<>();
}
