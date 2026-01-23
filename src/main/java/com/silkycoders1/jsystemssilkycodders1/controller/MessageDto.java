package com.silkycoders1.jsystemssilkycodders1.controller;

import java.util.List;

public record MessageDto(String role, Object content, List<Object> experimental_attachments) {}