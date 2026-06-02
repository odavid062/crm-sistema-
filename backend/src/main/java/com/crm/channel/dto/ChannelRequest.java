package com.crm.channel.dto;

import com.crm.channel.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ChannelRequest(
        @NotBlank String name,
        @NotNull ChannelType type,
        Map<String, Object> config,
        Boolean isDefault
) {}
