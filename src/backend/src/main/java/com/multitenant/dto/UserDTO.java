package com.multitenant.dto;

import com.multitenant.model.core.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String role;
    private String tenantId;
    private String nume;
    private String email;
    private boolean activ;
    private Long uatId;

    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getTenantId(),
                user.getNume(),
                user.getEmail(),
                user.isActiv(),
                user.getUatId()
        );
    }
}
