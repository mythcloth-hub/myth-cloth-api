package com.mesofi.mythclothapi.figurineimages.dto;

import java.net.URI;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FigurineImageReq {
  @NotNull(message = "imageUrl must not be blank")
  @Size(max = 100, message = "imageUrl must not exceed 100 characters")
  private URI imageUrl;

  private boolean isOfficialImage = true;
  @Positive @NotNull private Long figurineId;
}
