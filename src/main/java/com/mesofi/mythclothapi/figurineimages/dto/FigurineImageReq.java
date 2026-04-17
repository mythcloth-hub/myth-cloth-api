package com.mesofi.mythclothapi.figurineimages.dto;

import java.net.URI;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FigurineImageReq {
  @NotNull(message = "imageUrl must not be blank")
  private URI imageUrl;

  private boolean isOfficialImage = true;

  @Positive @NotNull private Long figurineId;
}
