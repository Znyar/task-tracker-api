package com.znyar.tasktrackerapi.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskStateDto {
    @NotNull
    Long id;
    @NotNull
    String name;
    @NotNull
    @JsonProperty("created_at")
    Instant createdAt = Instant.now();
    @JsonProperty("left_task_state_id")
    Long leftTaskStateId;
    @JsonProperty("right_task_state_id")
    Long rightTaskStateId;
    @NotNull
    List<TaskDto> tasks;
}
