package com.finance.zorvyn.dto.request;

import com.finance.zorvyn.entity.Role;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    private Role role;
    private Boolean active;

}
