package com.dts.practice.service;

import com.dts.practice.dto.response.MemoryTipListResponse;
import com.dts.practice.dto.response.MemoryTipResponse;
import com.dts.practice.entity.MemoryTip;
import com.dts.practice.repository.MemoryTipRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemoryTipReadServiceTest {

    private static final String BANK = "dts-2026-600-v1";
    private static final String FIXTURE = "/memory-tips-screen11-real.json";
    private static List<MemoryTip> realTips;

    private MemoryTipRepository repository;
    private MemoryTipReadService service;
    private List<MemoryTip> repositoryRows;

    @BeforeAll
    static void loadActualDatabaseSnapshot() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        try (InputStream input = MemoryTipReadServiceTest.class.getResourceAsStream(FIXTURE)) {
            assertNotNull(input, "Missing actual database fixture: " + FIXTURE);
            JsonNode root = mapper.readTree(input);
            assertEquals(BANK, root.path("bankVersion").asText());
            assertEquals(12L, root.path("rowCount").asLong());
            assertEquals(12L, root.path("publishedCount").asLong());
            assertTrue(root.path("items").isArray());

            List<MemoryTip> loaded = new ArrayList<>();
            for (JsonNode item : root.path("items")) {
                loaded.add(mapper.treeToValue(item, MemoryTip.class));
            }
            realTips = List.copyOf(loaded);
        }

        assertEquals(12, realTips.size());
        assertEquals(12L, realTips.stream().map(MemoryTip::getId).distinct().count());
        assertEquals(12L, realTips.stream().map(MemoryTip::getContentBuilderMemoryTipId).distinct().count());
        assertEquals(IntStream.rangeClosed(1, 12).boxed().toList(),
                realTips.stream().map(MemoryTip::getSortOrder).toList());
        assertEquals(IntStream.rangeClosed(1, 12).boxed().toList(),
                realTips.stream().map(MemoryTip::getSourceTipId).sorted().toList());
        for (MemoryTip tip : realTips) {
            assertNotNull(tip.getId());
            assertNotNull(tip.getContentBuilderMemoryTipId());
            assertNotNull(tip.getPublishedAt());
            assertEquals(BANK, tip.getBankVersion());
            assertEquals("PUBLISHED", tip.getStatus());
            assertFalse(tip.getTitle().isBlank());
            assertFalse(tip.getQuickRule().isBlank());
            assertFalse(tip.getExplanation().isBlank());
        }
    }

    @BeforeEach
    void setUp() {
        repository = mock(MemoryTipRepository.class);
        service = new MemoryTipReadService(repository);
        repositoryRows = realTips.stream().map(MemoryTipReadServiceTest::copyActualTip)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(repositoryRows);
        when(repository.findByBankVersionAndStatusOrderBySortOrderAsc(BANK, "PUBLISHED"))
                .thenReturn(repositoryRows);
    }

    @Test
    void returnsTheEntireRealBankInStableOrderWithStableIds() {
        MemoryTipListResponse first = service.list();
        assertEquals(BANK, first.bankVersion());
        assertEquals(12L, first.totalElements());
        assertEquals(12, first.items().size());
        assertEquals(realTips.stream().map(MemoryTip::getId).toList(), ids(first.items()));
        assertEquals(IntStream.rangeClosed(1, 12).boxed().toList(),
                first.items().stream().map(MemoryTipResponse::sortOrder).toList());

        Collections.rotate(repositoryRows, 5);
        assertEquals(first, service.list());
        verify(repository, times(2)).findByBankVersionAndStatusOrderBySortOrderAsc(BANK, "PUBLISHED");
    }

    @Test
    void preservesEveryImportedContentField() {
        Map<UUID, MemoryTip> byId = realTips.stream().collect(Collectors.toMap(MemoryTip::getId, tip -> tip));
        for (MemoryTipResponse actual : service.list().items()) {
            MemoryTip expected = byId.get(actual.id());
            assertNotNull(expected);
            assertEquals(expected.getSourceTipId(), actual.sourceTipId());
            assertEquals(expected.getSortOrder(), actual.sortOrder());
            assertEquals(expected.getTitle(), actual.title());
            assertEquals(expected.getQuickRule(), actual.quickRule());
            assertEquals(expected.getExplanation(), actual.explanation());
        }
    }

    @Test
    void emptyRepositoryReturnsAValidEmptyBank() {
        when(repository.findByBankVersionAndStatusOrderBySortOrderAsc(BANK, "PUBLISHED"))
                .thenReturn(List.of());
        MemoryTipListResponse result = service.list();
        assertEquals(BANK, result.bankVersion());
        assertTrue(result.items().isEmpty());
        assertEquals(0L, result.totalElements());
    }

    @Test
    void totalReflectsActualPublishedRowsInsteadOfAnExpectedImportCount() {
        List<MemoryTip> actualSubset = List.of(
                copyActualTip(realTips.get(0)), copyActualTip(realTips.get(1))
        );
        when(repository.findByBankVersionAndStatusOrderBySortOrderAsc(BANK, "PUBLISHED"))
                .thenReturn(actualSubset);
        MemoryTipListResponse result = service.list();
        assertEquals(2L, result.totalElements());
        assertEquals(actualSubset.stream().map(MemoryTip::getId).toList(), ids(result.items()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DRAFT", "ARCHIVED", "HIDDEN"})
    void neverExposesNonPublishedContent(String status) {
        // Vary only publication metadata on an in-memory copy of an imported tip.
        MemoryTip excluded = repositoryRows.get(0);
        excluded.setStatus(status);
        MemoryTipListResponse result = service.list();
        assertEquals(11L, result.totalElements());
        assertFalse(ids(result.items()).contains(excluded.getId()));
    }

    @Test
    void neverMixesAnotherBankVersion() {
        // Keep the real content unchanged; vary only the copied version metadata.
        MemoryTip excluded = repositoryRows.get(0);
        excluded.setBankVersion("dts-2026-600-v2");
        MemoryTipListResponse result = service.list();
        assertEquals(BANK, result.bankVersion());
        assertEquals(11L, result.totalElements());
        assertFalse(ids(result.items()).contains(excluded.getId()));
    }

    @Test
    void readsAgainWhenTheActualImportedBankBecomesAvailable() {
        when(repository.findByBankVersionAndStatusOrderBySortOrderAsc(BANK, "PUBLISHED"))
                .thenReturn(List.of(), repositoryRows);
        assertEquals(0L, service.list().totalElements());
        assertEquals(12L, service.list().totalElements());
        verify(repository, times(2)).findByBankVersionAndStatusOrderBySortOrderAsc(BANK, "PUBLISHED");
    }

    private static List<UUID> ids(List<MemoryTipResponse> rows) {
        return rows.stream().map(MemoryTipResponse::id).toList();
    }

    private static MemoryTip copyActualTip(MemoryTip source) {
        return MemoryTip.builder()
                .id(source.getId()).bankVersion(source.getBankVersion())
                .contentBuilderMemoryTipId(source.getContentBuilderMemoryTipId())
                .sourceTipId(source.getSourceTipId()).sortOrder(source.getSortOrder())
                .title(source.getTitle()).quickRule(source.getQuickRule())
                .explanation(source.getExplanation()).status(source.getStatus())
                .publishedAt(source.getPublishedAt()).build();
    }
}