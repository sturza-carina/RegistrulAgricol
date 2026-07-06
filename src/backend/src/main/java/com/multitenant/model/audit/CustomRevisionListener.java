package com.multitenant.model.audit;

import com.multitenant.security.UserDetailsImpl;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                customRevisionEntity.setUsername(userDetails.getUsername());
            } else {
                customRevisionEntity.setUsername(authentication.getName());
            }
        } else {
            customRevisionEntity.setUsername("anonymous");
        }
    }
}
