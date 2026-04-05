package com.example.kitchen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PreferenceRequest(
        @NotBlank @Size(max = 50) String name)  {

}
