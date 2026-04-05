package com.finance.zorvyn.dto.response;

import com.finance.zorvyn.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokentype;
    private Long userid;
    private String email;
    private String name;
    private Role role;


}
