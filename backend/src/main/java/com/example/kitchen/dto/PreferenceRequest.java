package com.example.kitchen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PreferenceRequest(
        @NotNull @NotBlank String name)  {

}
