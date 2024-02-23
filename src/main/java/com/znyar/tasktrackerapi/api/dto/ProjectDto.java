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
public class ProjectDto {
    @NotNull
    Long id;
    @NotNull
    String name;
    @NotNull
    @JsonProperty("created_at")
    Instant createdAt = Instant.now();
    @JsonProperty("updated_at")
    Instant updatedAt = Instant.now();
}
