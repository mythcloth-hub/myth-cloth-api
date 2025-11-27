package com.mesofi.mythclothapi.references.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReferencePairRequest(
    @NotNull(message = "description must not be blank")
        @Size(max = 100, message = "description must not exceed 100 characters")
        String description) {}
