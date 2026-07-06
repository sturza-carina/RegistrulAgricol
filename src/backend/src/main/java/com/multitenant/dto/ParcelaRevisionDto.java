package com.multitenant.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ParcelaRevisionDto {
    private int revisionId;
    private Instant timestamp;
    private String author;
    private String actionType;
    private Map<String, FieldDiff> diffs;
}
