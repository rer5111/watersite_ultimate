package com.waltercraft.application.service;

import com.waltercraft.application.dto.ApplicationCreateRequest;
import com.waltercraft.application.dto.ApplicationResponse;
import com.waltercraft.application.dto.ApplicationUpdateRequest;
import com.waltercraft.application.entity.ApplicationStatus;
import com.waltercraft.application.entity.ServerApplication;
import com.waltercraft.application.exception.ConflictException;
import com.waltercraft.application.exception.ResourceNotFoundException;
import com.waltercraft.application.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Page<ApplicationResponse> list(Pageable pageable, Authentication authentication) {
        Page<ServerApplication> applications = isAdmin(authentication)
                ? applicationRepository.findAll(pageable)
                : applicationRepository.findAllByApplicantUsername(authentication.getName(), pageable);
        return applications.map(ApplicationResponse::from);
    }

    public ApplicationResponse get(long id, Authentication authentication) {
        ServerApplication application = findVisible(id, authentication);
        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse create(ApplicationCreateRequest request, Authentication authentication) {
        ServerApplication application = new ServerApplication(authentication.getName(), request.text());
        ServerApplication saved = applicationRepository.saveAndFlush(application);
        log.info("Application created applicationId={} status={}", saved.getId(), saved.getStatus());
        return ApplicationResponse.from(saved);
    }

    @Transactional
    public ApplicationResponse update(long id, ApplicationUpdateRequest request) {
        ServerApplication application = applicationRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        if (application.getVersion() != request.version()) {
            throw new ConflictException("Заявка уже изменена. Обновите данные и повторите запрос.");
        }
        application.replace(request.text(), request.status());
        try {
            ServerApplication saved = applicationRepository.saveAndFlush(application);
            log.info("Application reviewed applicationId={} status={}", saved.getId(), saved.getStatus());
            return ApplicationResponse.from(saved);
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ConflictException("Заявка уже изменена. Обновите данные и повторите запрос.");
        }
    }

    @Transactional
    public void delete(long id, Authentication authentication) {
        ServerApplication application = findVisible(id, authentication);
        if (!isAdmin(authentication) && application.getStatus() != ApplicationStatus.PENDING) {
            throw new ConflictException("Удалить можно только заявку, которая ещё рассматривается.");
        }
        applicationRepository.delete(application);
        applicationRepository.flush();
        log.info("Application deleted applicationId={}", id);
    }

    private ServerApplication findVisible(long id, Authentication authentication) {
        return (isAdmin(authentication)
                ? applicationRepository.findById(id)
                : applicationRepository.findByIdAndApplicantUsername(id, authentication.getName()))
                .orElseThrow(ResourceNotFoundException::new);
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
