package com.znyar.tasktrackerapi.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDto {
    @NotNull
    Long id;
    @NotNull
    String name;
    @NotNull
    @JsonProperty("created_at")
    Instant createdAt = Instant.now();
    @NotNull
    Long ordinal;
    @NotNull
    String description;
}
