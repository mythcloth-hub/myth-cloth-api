package com.mesofi.mythclothapi.integration.fb;

import java.io.Serializable;

public record FbTokenError(Integer code, String message) implements Serializable {}
