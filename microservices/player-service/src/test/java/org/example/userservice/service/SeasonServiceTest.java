package org.example.userservice.service;

import org.example.userservice.model.SeasonModel;
import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserModel;
import org.example.userservice.dto.SeasonParticipationResponse;
import org.example.userservice.repository.SeasonRepository;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeasonServiceTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private UserRepository userRepository;

    private SeasonServiceImpl seasonService;

    @BeforeEach
    void setUp() {
        seasonService = new SeasonServiceImpl(seasonRepository, userRepository);
    }

    @Test
    void findByTitle_returnsMatchingSeasons() {
        SeasonModel season = new SeasonModel(1L, "Season 1", LocalDate.now(), LocalDate.now().plusMonths(3), "Desc", "img.png");
        when(seasonRepository.findByTitle("Season 1")).thenReturn(List.of(season));

        List<SeasonModel> result = seasonService.findByTitle("Season 1");

        assertThat(result).containsExactly(season);
        verify(seasonRepository).findByTitle("Season 1");
    }

    @Test
    void addSeason_savesSeason() {
        SeasonModel season = new SeasonModel(0L, "Season 2", LocalDate.now(), LocalDate.now().plusMonths(3), "Desc", "img.png");

        seasonService.addSeason(season);

        verify(seasonRepository).save(season);
    }

    @Test
    void deleteSeason_deletesById() {
        seasonService.deleteSeason(2L);

        verify(seasonRepository).deleteById(2L);
    }

    @Test
    void findPage_returnsEmptyWhenPageInvalid() {
        List<SeasonModel> result = seasonService.findPage(-1);

        assertThat(result).isEmpty();
    }

    @Test
    void findPage_returnsPageContent() {
        SeasonModel season = new SeasonModel(1L, "Season 1", LocalDate.now(), LocalDate.now().plusMonths(3), "Desc", "img.png");
        Page<SeasonModel> page = new PageImpl<>(List.of(season));
        when(seasonRepository.findAll(any(PageRequest.class))).thenReturn(page);

        List<SeasonModel> result = seasonService.findPage(1);

        assertThat(result).containsExactly(season);
    }

    @Test
    void getParticipation_returnsOrderedSeasonsAndComputedPhase() {
        LocalDate today = LocalDate.now();
        UserModel player = new UserModel(42L, "player", "hash", Set.of(RoleEnum.USER),
                new AccountModel(42L, "player", today));
        player.setSeasons(List.of(
                new SeasonModel(2L, "Second", today.plusDays(1), today.plusMonths(1), "Future", "future.png"),
                new SeasonModel(1L, "First", today.minusYears(1), today.minusMonths(11), "Past", "past.png")
        ));
        when(userRepository.findWithSeasonsById(42L)).thenReturn(Optional.of(player));

        var response = seasonService.getParticipation(42L);

        assertThat(response.playerId()).isEqualTo(42L);
        assertThat(response.totalSeasons()).isEqualTo(2);
        assertThat(response.seasons()).extracting("title").containsExactly("First", "Second");
        assertThat(response.seasons()).extracting("phase").containsExactly(
                SeasonParticipationResponse.Phase.COMPLETED,
                SeasonParticipationResponse.Phase.UPCOMING);
    }

    @Test
    void getParticipation_returnsEmptyHistoryForMissingPlayer() {
        when(userRepository.findWithSeasonsById(404L)).thenReturn(Optional.empty());

        var response = seasonService.getParticipation(404L);

        assertThat(response.playerId()).isEqualTo(404L);
        assertThat(response.totalSeasons()).isZero();
        assertThat(response.seasons()).isEmpty();
    }
}
