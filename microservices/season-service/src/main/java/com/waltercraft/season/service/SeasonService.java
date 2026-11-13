package com.waltercraft.season.service;

import com.waltercraft.season.dto.SeasonCreateRequest;
import com.waltercraft.season.dto.SeasonResponse;
import com.waltercraft.season.dto.SeasonUpdateRequest;
import com.waltercraft.season.entity.Season;
import com.waltercraft.season.entity.SeasonStatus;
import com.waltercraft.season.exception.ConflictException;
import com.waltercraft.season.exception.ResourceNotFoundException;
import com.waltercraft.season.repository.SeasonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SeasonService {

    private static final Logger log = LoggerFactory.getLogger(SeasonService.class);

    private final SeasonRepository seasonRepository;

    public SeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    public Page<SeasonResponse> listPublic(Pageable pageable) {
        return seasonRepository.findAllByActiveTrue(pageable).map(SeasonResponse::from);
    }

    public Page<SeasonResponse> listAll(Pageable pageable) {
        return seasonRepository.findAll(pageable).map(SeasonResponse::from);
    }

    public SeasonResponse get(Long id) {
        return SeasonResponse.from(findSeason(id));
    }

    @Transactional
    public SeasonResponse create(SeasonCreateRequest request) {
        if (seasonRepository.existsByCode(request.code())) {
            throw new ConflictException("Сезон с кодом '" + request.code() + "' уже существует.");
        }
        SeasonStatus status = request.status() != null
                ? request.status()
                : (Boolean.FALSE.equals(request.active()) ? SeasonStatus.DRAFT : SeasonStatus.PUBLISHED);
        Season season = new Season(
                request.code(),
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.image(),
                request.displayOrder(),
                request.organizer() == null ? "" : request.organizer().trim(),
                status
        );
        season.setStartsAt(request.startsAt());
        Season saved = seasonRepository.saveAndFlush(season);
        log.info("Season created seasonId={} status={}", saved.getId(), saved.getStatus());
        return SeasonResponse.from(saved);
    }

    @Transactional
    public SeasonResponse update(Long id, SeasonUpdateRequest request) {
        Season season = findSeason(id);
        if (!season.getVersion().equals(request.version())) {
            throw new ConflictException("Сезон уже был изменен другим процессом. Обновите страницу и повторите попытку.");
        }
        if (seasonRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new ConflictException("Код сезона '" + request.code() + "' уже используется другим сезоном.");
        }
        SeasonStatus status = request.status() != null
                ? request.status()
                : (Boolean.FALSE.equals(request.active()) ? SeasonStatus.DRAFT : SeasonStatus.PUBLISHED);
        season.replace(
                request.code(),
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                request.image(),
                request.displayOrder(),
                request.organizer() == null ? season.getOrganizer() : request.organizer().trim(),
                status
        );
        if (request.startsAt() != null) {
            season.setStartsAt(request.startsAt());
        }
        try {
            Season saved = seasonRepository.saveAndFlush(season);
            log.info("Season updated seasonId={} status={}", saved.getId(), saved.getStatus());
            return SeasonResponse.from(saved);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException("Сезон уже был изменен другим процессом. Обновите страницу и повторите попытку.");
        }
    }

    @Transactional
    public void delete(Long id) {
        Season season = findSeason(id);
        seasonRepository.delete(season);
        log.info("Season deleted seasonId={}", id);
    }

    private Season findSeason(Long id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сезон с идентификатором " + id + " не найден."));
    }
}
