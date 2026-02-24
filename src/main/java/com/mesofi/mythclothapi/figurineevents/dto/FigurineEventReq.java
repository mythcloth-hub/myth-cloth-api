package com.mesofi.mythclothapi.figurineevents.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FigurineEventReq {
  @NotNull(message = "description must not be blank")
  @Size(max = 100, message = "description must not exceed 100 characters")
  private String description;

  @Past
  @NotNull(message = "event date must be provided")
  private LocalDate eventDate;

  @Positive @NotNull private Long figurineId;
}
