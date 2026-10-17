
package com.dts.practice.service;

import com.dts.practice.dto.request.ImportTrafficSignBankRequest;
import com.dts.practice.dto.request.ImportTrafficSignBankRequest.BankTrafficSign;
import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrafficSignBankImportService {

    private static final String BANK_VERSION =
            "dts-traffic-signs-v1";

    private static final int EXPECTED_COUNT = 221;

    private static final Set<String> LICENSES = Set.of(
            "A1", "A", "B1", "B", "C1", "C",
            "D1", "D2", "D", "BE", "C1E",
            "CE", "D1E", "D2E", "DE"
    );

    private static final Map<String, Integer> CATEGORY_COUNTS =
            Map.of(
                    "cam", 59,
                    "nguy_hiem", 83,
                    "hieu_lenh", 41,
                    "chi_dan", 25,
                    "phu", 13
            );

    private static final String INSERT_SQL = """
            INSERT INTO public.traffic_signs (
                bank_version,
                source_sign_id,
                content_builder_traffic_sign_id,
                code,
                name,
                category,
                category_name,
                meaning,
                quick_tip,
                source_image,
                media_file_id,
                applicable_licenses,
                status,
                published_at
            )
            VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                CAST(? AS jsonb), ?, ?
            )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public int importBank(ImportTrafficSignBankRequest request) {

        validateBank(request);

        Long existing = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM public.traffic_signs
                WHERE bank_version = ?
                """,
                Long.class,
                BANK_VERSION
        );

        if (existing != null && existing > 0) {
            throw BusinessException.conflict(
                    "Traffic sign bank "
                            + BANK_VERSION
                            + " has already been imported"
            );
        }

        List<BankTrafficSign> signs = request.signs();

        jdbcTemplate.batchUpdate(
                INSERT_SQL,
                signs,
                100,
                (statement, sign) -> {

                    statement.setString(1, BANK_VERSION);

                    statement.setInt(
                            2,
                            sign.sourceSignId()
                    );

                    statement.setObject(
                            3,
                            sign.contentBuilderTrafficSignId()
                    );

                    statement.setString(4, sign.code());
                    statement.setString(5, sign.name());
                    statement.setString(6, sign.category());
                    statement.setString(7, sign.categoryName());
                    statement.setString(8, sign.meaning());
                    statement.setString(9, sign.quickTip());
                    statement.setString(10, sign.sourceImage());

                    statement.setObject(
                            11,
                            sign.mediaFileId()
                    );

                    statement.setString(
                            12,
                            toJson(sign.applicableLicenses())
                    );

                    statement.setString(
                            13,
                            sign.status()
                    );

                    statement.setObject(
                            14,
                            sign.publishedAt()
                    );
                }
        );

        return signs.size();
    }

    private void validateBank(
            ImportTrafficSignBankRequest request
    ) {

        if (request == null
                || !BANK_VERSION.equals(request.bankVersion())
                || request.signs() == null
                || request.signs().size() != EXPECTED_COUNT) {

            throw BusinessException.badRequest(
                    "Expected complete traffic sign bank of 221 signs"
            );
        }

        Set<Integer> sourceIds = new HashSet<>();
        Set<UUID> contentBuilderIds = new HashSet<>();
        Set<UUID> mediaIds = new HashSet<>();

        Set<String> codes = new HashSet<>();
        Set<String> imageNames = new HashSet<>();

        Map<String, Integer> categoryCounts =
                new HashMap<>();

        for (BankTrafficSign sign : request.signs()) {

            if (sign == null) {
                throw BusinessException.badRequest(
                        "Traffic sign cannot be null"
                );
            }

            Integer sourceId = sign.sourceSignId();

            if (sourceId == null
                    || sourceId < 1
                    || sourceId > EXPECTED_COUNT
                    || !sourceIds.add(sourceId)) {

                throw BusinessException.badRequest(
                        "Missing, repeated or invalid traffic sign source ID"
                );
            }

            if (sign.contentBuilderTrafficSignId() == null
                    || !contentBuilderIds.add(
                            sign.contentBuilderTrafficSignId()
                    )) {

                throw BusinessException.badRequest(
                        "Sign " + sourceId
                                + " has missing or duplicate Content Builder UUID"
                );
            }

            if (sign.mediaFileId() == null
                    || !mediaIds.add(sign.mediaFileId())) {

                throw BusinessException.badRequest(
                        "Sign " + sourceId
                                + " has missing or duplicate Media UUID"
                );
            }

            if (isBlank(sign.code())
                    || sign.code().length() > 32) {

                throw BusinessException.badRequest(
                        "Sign " + sourceId + " has invalid code"
                );
            }

            String codeKey = sign.code()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (!codes.add(codeKey)) {
                throw BusinessException.badRequest(
                        "Duplicate traffic sign code: "
                                + sign.code()
                );
            }

            if (isBlank(sign.name())
                    || sign.name().length() > 255
                    || isBlank(sign.categoryName())
                    || sign.categoryName().length() > 100
                    || isBlank(sign.meaning())
                    || isBlank(sign.quickTip())) {

                throw BusinessException.badRequest(
                        "Sign " + sourceId
                                + " has incomplete content"
                );
            }

            if (!CATEGORY_COUNTS.containsKey(sign.category())) {
                throw BusinessException.badRequest(
                        "Sign " + sourceId
                                + " has invalid category"
                );
            }

            categoryCounts.merge(
                    sign.category(),
                    1,
                    Integer::sum
            );

            String image = sign.sourceImage();

            if (isBlank(image)
                    || !image.matches("[A-Za-z0-9_-]+\\.webp")
                    || !imageNames.add(image)) {

                throw BusinessException.badRequest(
                        "Sign " + sourceId
                                + " has invalid or duplicate image filename"
                );
            }

            validateLicenses(sign);

            if (!"PUBLISHED".equals(sign.status())
                    || sign.publishedAt() == null) {

                throw BusinessException.badRequest(
                        "Sign " + sourceId
                                + " must be published"
                );
            }
        }

        if (sourceIds.size() != EXPECTED_COUNT
                || contentBuilderIds.size() != EXPECTED_COUNT
                || mediaIds.size() != EXPECTED_COUNT
                || codes.size() != EXPECTED_COUNT
                || imageNames.size() != EXPECTED_COUNT) {

            throw BusinessException.badRequest(
                    "Traffic sign bank is incomplete"
            );
        }

        for (Map.Entry<String, Integer> entry
                : CATEGORY_COUNTS.entrySet()) {

            if (!entry.getValue().equals(
                    categoryCounts.get(entry.getKey())
            )) {

                throw BusinessException.badRequest(
                        "Incorrect traffic sign count for category "
                                + entry.getKey()
                );
            }
        }
    }

    private void validateLicenses(BankTrafficSign sign) {

        List<String> licenses = sign.applicableLicenses();

        if (licenses == null || licenses.isEmpty()) {
            throw BusinessException.badRequest(
                    "Sign " + sign.sourceSignId()
                            + " has no applicable licenses"
            );
        }

        Set<String> unique = new HashSet<>();

        for (String license : licenses) {

            if (license == null
                    || !LICENSES.contains(license)
                    || !unique.add(license)) {

                throw BusinessException.badRequest(
                        "Sign " + sign.sourceSignId()
                                + " has invalid or duplicate license"
                );
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String toJson(Object value) {

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Cannot serialize traffic sign licenses",
                    exception
            );
        }
    }
}
