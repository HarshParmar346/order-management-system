package com.harsh.common.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMetadata {

    private UUID eventId;

    private LocalDateTime createdAt;

    private Integer version;

}
