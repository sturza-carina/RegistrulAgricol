package com.multitenant.aspect;

import com.multitenant.annotation.GdprAudited;
import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.audit.GdprActionType;
import com.multitenant.model.audit.GdprAuditLog;
import com.multitenant.security.UserDetailsImpl;
import com.multitenant.service.GdprAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Aspect
@Component
public class GdprAuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(GdprAuditAspect.class);
    private final GdprAuditService gdprAuditService;

    public GdprAuditAspect(GdprAuditService gdprAuditService) {
        this.gdprAuditService = gdprAuditService;
    }

    @Around("@annotation(gdprAudited)")
    public Object audit(ProceedingJoinPoint joinPoint, GdprAudited gdprAudited) throws Throwable {
        Object result = null;
        Throwable exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            try {
                createAndSaveLog(joinPoint, gdprAudited, result, exception);
            } catch (Exception e) {
                logger.error("Error creating GDPR audit log entry", e);
            }
        }
    }

    private void createAndSaveLog(ProceedingJoinPoint joinPoint, GdprAudited gdprAudited, Object result, Throwable exception) {
        Instant timestamp = Instant.now();
        String currentTenant = TenantContext.getCurrentTenant();

        // 1. Extract User
        String utilizator = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                utilizator = userDetails.getUsername();
            } else {
                utilizator = auth.getName();
            }
        }

        // 2. Extract request HTTP method & endpoint
        String endpoint = "unknown";
        GdprActionType tipActiune = GdprActionType.VIEW;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                endpoint = request.getRequestURI();
                String method = request.getMethod();
                if ("POST".equalsIgnoreCase(method)) {
                    tipActiune = GdprActionType.CREATE;
                } else if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                    tipActiune = GdprActionType.UPDATE;
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    tipActiune = GdprActionType.DELETE;
                } else {
                    tipActiune = GdprActionType.VIEW;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not determine HTTP request details for GDPR audit", e);
        }

        // 3. Extract Entity name from annotation
        String entitateVizata = gdprAudited.entity();

        // 4. Extract ID (or IDs for list endpoints)
        String idPersoanaVizata = null;
        if (exception == null) {
            // Check if ID is in the parameters (e.g. @PathVariable Long id)
            idPersoanaVizata = extractIdFromArguments(joinPoint);

            // For CREATE, LIST or detailed search, try to extract from result
            if (result != null) {
                Object body = result;
                if (result instanceof ResponseEntity<?> responseEntity) {
                    body = responseEntity.getBody();
                }

                if (body != null) {
                    if (body instanceof org.springframework.data.domain.Page<?> page) {
                        idPersoanaVizata = extractIdsFromList(page.getContent());
                    } else if (body instanceof Collection<?> collection) {
                        idPersoanaVizata = extractIdsFromList(collection);
                    } else if (idPersoanaVizata == null) {
                        // Single returned object
                        idPersoanaVizata = extractIdFromObject(body);
                    }
                }
            }
        } else {
            // If action threw an error, log the ID from parameters if available
            idPersoanaVizata = extractIdFromArguments(joinPoint);
        }

        // 5. Create log entry
        GdprAuditLog log = new GdprAuditLog();
        log.setTimestamp(timestamp);
        log.setUtilizator(utilizator);
        log.setTipActiune(tipActiune);
        log.setEntitateVizata(entitateVizata);
        log.setIdPersoanaVizata(idPersoanaVizata);
        log.setEndpoint(endpoint);
        log.setTenantId(currentTenant);

        // 6. Save log in separate REQUIRES_NEW transaction (handled internally in service)
        gdprAuditService.saveAuditLog(log);
    }

    private String extractIdFromArguments(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                String name = parameterNames[i];
                if ("id".equalsIgnoreCase(name) || "persoanaId".equalsIgnoreCase(name) || "gospodarieId".equalsIgnoreCase(name)) {
                    if (args[i] != null) {
                        return args[i].toString();
                    }
                }
            }
        }

        // Fallback: search for first Long or Integer in arguments
        for (Object arg : args) {
            if (arg instanceof Long || arg instanceof Integer) {
                return arg.toString();
            }
        }

        return null;
    }

    private String extractIdFromObject(Object obj) {
        try {
            Method getIdMethod = obj.getClass().getMethod("getId");
            Object idValue = getIdMethod.invoke(obj);
            if (idValue != null) {
                return idValue.toString();
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return null;
    }

    private String extractIdsFromList(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return "[]";
        }
        List<String> ids = new java.util.ArrayList<>();
        for (Object item : collection) {
            if (item != null) {
                String id = extractIdFromObject(item);
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids.toString(); // Output format: [1, 2, 3]
    }
}
