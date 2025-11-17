package com.mesofi.mythclothapi.error;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class Problem {
  public static ProblemDetail of(HttpStatus status, String title, String detail) {
    ProblemDetail pd = ProblemDetail.forStatus(status);
    pd.setTitle(title);
    pd.setDetail(detail);
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }
}
