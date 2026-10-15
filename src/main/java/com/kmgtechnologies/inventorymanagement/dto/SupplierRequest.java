package com.kmgtechnologies.inventorymanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(

        @NotBlank(message = "Supplier name is required")
        @Size(max = 255)
        String name,

        @Email(message = "Invalid email address")
        @Size(max = 255)
        String email,

        @Size(max = 255)
        String phone

) {}