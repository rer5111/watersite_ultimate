package org.example.userservice.service;

import org.example.userservice.model.SeasonModel;
import org.example.userservice.repository.SeasonRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeasonServiceTest {

    @Mock
    private SeasonRepository seasonRepository;

    private SeasonServiceImpl seasonService;

    @BeforeEach
    void setUp() {
        seasonService = new SeasonServiceImpl(seasonRepository);
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
}
