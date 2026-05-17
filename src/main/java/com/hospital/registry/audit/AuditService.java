package com.hospital.registry.audit;

import com.hospital.registry.model.AuditLog;
import com.hospital.registry.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, Long entityId, String action, String oldValue, String newValue) {
        String user = currentUser();
        log.debug("Audit: user='{}' action={} {}#{} old='{}' new='{}'", user, action, entityType, entityId, oldValue, newValue);
        AuditLog entry = new AuditLog();
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setAction(action);
        entry.setChangedBy(user);
        entry.setOldValue(oldValue);
        entry.setNewValue(newValue);
        auditLogRepository.save(entry);
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
