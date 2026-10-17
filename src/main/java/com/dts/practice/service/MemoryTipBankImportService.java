
package com.dts.practice.service;

import com.dts.practice.dto.request.ImportMemoryTipBankRequest;
import com.dts.practice.dto.request.ImportMemoryTipBankRequest.BankMemoryTip;
import com.dts.practice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemoryTipBankImportService {

    private static final String BANK_VERSION =
            "dts-2026-600-v1";

    private static final int EXPECTED_COUNT = 12;

    private static final String INSERT_SQL = """
            INSERT INTO public.memory_tips (
                bank_version,
                source_tip_id,
                content_builder_memory_tip_id,
                sort_order,
                title,
                quick_rule,
                explanation,
                status,
                published_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public int importBank(ImportMemoryTipBankRequest request) {

        validateBank(request);

        Long existing = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM public.memory_tips
                WHERE bank_version = ?
                """,
                Long.class,
                BANK_VERSION
        );

        if (existing != null && existing > 0) {
            throw BusinessException.conflict(
                    "Memory tip bank "
                            + BANK_VERSION
                            + " has already been imported"
            );
        }

        List<BankMemoryTip> tips = request.tips();

        jdbcTemplate.batchUpdate(
                INSERT_SQL,
                tips,
                100,
                (statement, tip) -> {

                    statement.setString(1, BANK_VERSION);

                    statement.setInt(
                            2,
                            tip.sourceTipId()
                    );

                    statement.setObject(
                            3,
                            tip.contentBuilderMemoryTipId()
                    );

                    statement.setInt(
                            4,
                            tip.sortOrder()
                    );

                    statement.setString(5, tip.title());
                    statement.setString(6, tip.quickRule());
                    statement.setString(7, tip.explanation());
                    statement.setString(8, tip.status());

                    statement.setObject(
                            9,
                            tip.publishedAt()
                    );
                }
        );

        return tips.size();
    }

    private void validateBank(
            ImportMemoryTipBankRequest request
    ) {

        if (request == null
                || !BANK_VERSION.equals(request.bankVersion())
                || request.tips() == null
                || request.tips().size() != EXPECTED_COUNT) {

            throw BusinessException.badRequest(
                    "Expected complete memory tip bank of 12 tips"
            );
        }

        Set<Integer> sourceIds = new HashSet<>();
        Set<Integer> sortOrders = new HashSet<>();
        Set<UUID> contentBuilderIds = new HashSet<>();

        for (BankMemoryTip tip : request.tips()) {

            if (tip == null) {
                throw BusinessException.badRequest(
                        "Memory tip cannot be null"
                );
            }

            Integer sourceId = tip.sourceTipId();

            if (sourceId == null
                    || sourceId < 1
                    || sourceId > EXPECTED_COUNT
                    || !sourceIds.add(sourceId)) {

                throw BusinessException.badRequest(
                        "Missing, repeated or invalid memory tip source ID"
                );
            }

            if (tip.contentBuilderMemoryTipId() == null
                    || !contentBuilderIds.add(
                            tip.contentBuilderMemoryTipId()
                    )) {

                throw BusinessException.badRequest(
                        "Tip " + sourceId
                                + " has missing or duplicate Content Builder UUID"
                );
            }

            Integer sortOrder = tip.sortOrder();

            if (sortOrder == null
                    || sortOrder < 1
                    || sortOrder > EXPECTED_COUNT
                    || !sortOrders.add(sortOrder)) {

                throw BusinessException.badRequest(
                        "Tip " + sourceId
                                + " has invalid or duplicate sort order"
                );
            }

            if (isBlank(tip.title())
                    || tip.title().length() > 255
                    || isBlank(tip.quickRule())
                    || isBlank(tip.explanation())) {

                throw BusinessException.badRequest(
                        "Tip " + sourceId
                                + " has incomplete content"
                );
            }

            if (!"PUBLISHED".equals(tip.status())
                    || tip.publishedAt() == null) {

                throw BusinessException.badRequest(
                        "Tip " + sourceId
                                + " must be published"
                );
            }
        }

        if (sourceIds.size() != EXPECTED_COUNT
                || sortOrders.size() != EXPECTED_COUNT
                || contentBuilderIds.size() != EXPECTED_COUNT) {

            throw BusinessException.badRequest(
                    "Memory tip bank is incomplete"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
