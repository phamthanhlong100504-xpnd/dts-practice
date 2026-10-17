package com.dts.practice.service;

import com.dts.practice.dto.response.TrafficSignListResponse;
import com.dts.practice.dto.response.TrafficSignResponse;
import com.dts.practice.entity.TrafficSign;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.TrafficSignRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrafficSignReadServiceTest {

    private static final String BANK = "dts-traffic-signs-v1";
    private static final String FIXTURE = "/traffic-signs-screen10-real.json";
    private static final Map<String, Long> EXPECTED_COUNTS = Map.of(
            "cam", 59L, "nguy_hiem", 83L, "hieu_lenh", 41L,
            "chi_dan", 25L, "phu", 13L
    );

    private static List<TrafficSignResponse> realResponses;
    private TrafficSignRepository repository;
    private TrafficSignReadService service;
    private List<TrafficSign> repositoryRows;

    @BeforeAll
    static void loadCapturedRealBank() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream input = TrafficSignReadServiceTest.class.getResourceAsStream(FIXTURE)) {
            assertNotNull(input, "Missing actual API fixture: " + FIXTURE);
            JsonNode root = mapper.readTree(input);
            assertEquals(BANK, root.path("bankVersion").asText());
            assertEquals(221L, root.path("totalElements").asLong());
            assertTrue(root.path("items").isArray());

            List<TrafficSignResponse> loaded = new ArrayList<>();
            for (JsonNode item : root.path("items")) {
                loaded.add(mapper.treeToValue(item, TrafficSignResponse.class));
            }
            realResponses = List.copyOf(loaded);
        }

        assertEquals(221, realResponses.size());
        assertEquals(221L, realResponses.stream().map(TrafficSignResponse::id).distinct().count());
        assertTrue(realResponses.stream().allMatch(row -> BANK.equals(row.bankVersion())));
        assertEquals(
                IntStream.rangeClosed(1, 221).boxed().toList(),
                realResponses.stream().map(TrafficSignResponse::sortOrder).sorted().toList()
        );
        assertEquals(EXPECTED_COUNTS, realResponses.stream().collect(Collectors.groupingBy(
                TrafficSignResponse::groupCode, Collectors.counting()
        )));
    }

    @BeforeEach
    void setUp() {
        repository = mock(TrafficSignRepository.class);
        service = new TrafficSignReadService(repository);
        repositoryRows = realResponses.stream().map(row -> TrafficSign.builder()
                .id(row.id()).bankVersion(row.bankVersion())
                .sourceSignId(row.sortOrder()).code(row.code()).name(row.name())
                .category(row.groupCode()).categoryName(row.groupName())
                .meaning(row.meaning()).quickTip(row.memoryTip())
                .mediaFileId(row.mediaFileId()).status("PUBLISHED").build()
        ).collect(Collectors.toCollection(ArrayList::new));

        // Reorder actual rows to verify that page order does not depend on input order.
        Collections.reverse(repositoryRows);
        when(repository.findByBankVersionAndStatusOrderByCategoryAscSourceSignIdAsc(
                BANK, "PUBLISHED"
        )).thenReturn(repositoryRows);
    }

    @Test
    void pagesCoverTheActualBankOnceInStableSourceOrder() {
        List<TrafficSignResponse> all = new ArrayList<>();
        for (int page = 0; page < 3; page++) {
            TrafficSignListResponse result = service.list(null, null, page, 100);
            assertEquals(page, result.page());
            assertEquals(100, result.size());
            assertEquals(221L, result.totalElements());
            assertEquals(3, result.totalPages());
            assertEquals(page == 2 ? 21 : 100, result.items().size());
            all.addAll(result.items());
        }

        List<UUID> actualIds = ids(all);
        assertEquals(221, new HashSet<>(actualIds).size());
        assertEquals(ids(realResponses.stream().sorted(
                Comparator.comparing(TrafficSignResponse::sortOrder)
        ).toList()), actualIds);
        assertEquals(all.subList(0, 100), service.list(null, null, 0, 100).items());

        TrafficSignListResponse farPage = service.list(null, null, Integer.MAX_VALUE, 100);
        assertTrue(farPage.items().isEmpty());
        assertEquals(221L, farPage.totalElements());
        assertEquals(3, farPage.totalPages());
    }

    @Test
    void groupCountsStayGlobalWhileRowsAreFiltered() {
        for (Map.Entry<String, Long> expected : EXPECTED_COUNTS.entrySet()) {
            TrafficSignListResponse result = service.list(
                    " " + expected.getKey().toUpperCase(Locale.ROOT) + " ", null, 0, 100
            );
            assertEquals(expected.getValue().longValue(), result.totalElements());
            assertEquals(expected.getValue().intValue(), result.items().size());
            assertTrue(result.items().stream().allMatch(row -> expected.getKey().equals(row.groupCode())));
            assertEquals(List.of("cam", "nguy_hiem", "hieu_lenh", "chi_dan", "phu"),
                    result.groups().stream().map(TrafficSignListResponse.Group::groupCode).toList());
            assertEquals(EXPECTED_COUNTS, result.groups().stream().collect(Collectors.toMap(
                    TrafficSignListResponse.Group::groupCode,
                    TrafficSignListResponse.Group::totalElements
            )));
        }
    }

    @Test
    void codeSearchIgnoresCaseAndSurroundingWhitespace() {
        TrafficSignResponse sample = realResponses.get(0);
        TrafficSignListResponse lower = service.list(null,
                " " + sample.code().toLowerCase(Locale.ROOT) + " ", 0, 100);
        TrafficSignListResponse upper = service.list(null,
                sample.code().toUpperCase(Locale.ROOT), 0, 100);
        assertTrue(ids(lower.items()).contains(sample.id()));
        assertEquals(lower.items(), upper.items());
        assertEquals(lower.totalElements(), upper.totalElements());
    }

    @Test
    void vietnameseNameSearchWithAndWithoutAccentsWorksTogetherWithGroup() {
        TrafficSignResponse sample = realResponses.stream()
                .filter(row -> row.name().toLowerCase(Locale.ROOT).contains("đ"))
                .findFirst().orElseThrow(() -> new AssertionError("Expected an actual Vietnamese name with đ"));
        // Only generate a query from an imported name; no sign content is fabricated.
        String plainQuery = Normalizer.normalize(sample.name(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D');
        TrafficSignListResponse accented = service.list(sample.groupCode(), sample.name(), 0, 100);
        TrafficSignListResponse plain = service.list(sample.groupCode(), plainQuery, 0, 100);
        TrafficSignListResponse upper = service.list(sample.groupCode(), plainQuery.toUpperCase(Locale.ROOT), 0, 100);
        assertTrue(ids(accented.items()).contains(sample.id()));
        assertTrue(ids(plain.items()).contains(sample.id()));
        assertEquals(accented.items(), plain.items());
        assertEquals(plain.items(), upper.items());
        assertEquals(accented.totalElements(), plain.totalElements());
        assertTrue(plain.items().stream().allMatch(row -> sample.groupCode().equals(row.groupCode())));
    }

    @Test
    void detailPreservesActualMediaMeaningAndMemoryTip() {
        TrafficSignResponse expected = realResponses.get(0);
        TrafficSign entity = repositoryRows.stream().filter(row -> expected.id().equals(row.getId()))
                .findFirst().orElseThrow();
        when(repository.findById(expected.id())).thenReturn(Optional.of(entity));
        assertEquals(expected, service.get(expected.id()));
    }

    @Test
    void emptyRepositoryAndUnmatchedSearchReturnValidEmptyPages() {
        String unmatchedQuery = "__screen10_no_match_" + UUID.randomUUID();
        TrafficSignListResponse unmatched = service.list(null, unmatchedQuery, 0, 20);
        assertTrue(unmatched.items().isEmpty());
        assertEquals(0L, unmatched.totalElements());
        assertEquals(0, unmatched.totalPages());
        assertEquals(221L, unmatched.groups().stream().mapToLong(TrafficSignListResponse.Group::totalElements).sum());

        when(repository.findByBankVersionAndStatusOrderByCategoryAscSourceSignIdAsc(
                BANK, "PUBLISHED"
        )).thenReturn(List.of());
        TrafficSignListResponse empty = service.list(null, null, 0, 20);
        assertTrue(empty.items().isEmpty());
        assertEquals(0L, empty.totalElements());
        assertEquals(0, empty.totalPages());
        assertEquals(5, empty.groups().size());
        assertTrue(empty.groups().stream().allMatch(group -> group.totalElements() == 0L));
    }

    @Test
    void readsTheRepositoryAgainWhenTheImportedBankBecomesAvailable() {
        when(repository.findByBankVersionAndStatusOrderByCategoryAscSourceSignIdAsc(
                BANK, "PUBLISHED"
        )).thenReturn(List.of(), repositoryRows);
        assertEquals(0L, service.list(null, null, 0, 20).totalElements());
        assertEquals(221L, service.list(null, null, 0, 20).totalElements());
        verify(repository, times(2)).findByBankVersionAndStatusOrderByCategoryAscSourceSignIdAsc(
                BANK, "PUBLISHED"
        );
    }

    @ParameterizedTest
    @CsvSource({"-1,20", "0,0", "0,101"})
    void rejectsInvalidPagination(int page, int size) {
        assertBadRequest(() -> service.list(null, null, page, size));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "ROAD_MARKING"})
    void rejectsUnsupportedGroups(String group) {
        assertBadRequest(() -> service.list(group, null, 0, 20));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsQueryLongerThanThePublishedLimit() {
        assertBadRequest(() -> service.list(null, "x".repeat(256), 0, 20));
        verifyNoInteractions(repository);
    }

    @Test
    void missingSignUsesTheExistingNotFoundContract() {
        UUID missing = new UUID(0L, 0L);
        assertFalse(realResponses.stream().anyMatch(row -> missing.equals(row.id())));
        when(repository.findById(missing)).thenReturn(Optional.empty());
        BusinessException error = assertThrows(BusinessException.class, () -> service.get(missing));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("NOT_FOUND", error.getErrorCode());
    }

    private static List<UUID> ids(List<TrafficSignResponse> rows) {
        return rows.stream().map(TrafficSignResponse::id).toList();
    }

    private static void assertBadRequest(org.junit.jupiter.api.function.Executable action) {
        BusinessException error = assertThrows(BusinessException.class, action);
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("BAD_REQUEST", error.getErrorCode());
    }
}