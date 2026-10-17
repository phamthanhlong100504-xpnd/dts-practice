package com.dts.practice.service;

import com.dts.practice.dto.response.TrafficSignListResponse;
import com.dts.practice.dto.response.TrafficSignResponse;
import com.dts.practice.entity.TrafficSign;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.TrafficSignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrafficSignReadService {

    private static final String BANK_VERSION = "dts-traffic-signs-v1";
    private static final String PUBLISHED = "PUBLISHED";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_QUERY_LENGTH = 255;
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

  
    private static final List<GroupDefinition> GROUPS = List.of(
            new GroupDefinition("cam", "Cấm"),
            new GroupDefinition("nguy_hiem", "Nguy hiểm"),
            new GroupDefinition("hieu_lenh", "Hiệu lệnh"),
            new GroupDefinition("chi_dan", "Chỉ dẫn"),
            new GroupDefinition("phu", "Phụ")
    );

    private final TrafficSignRepository trafficSignRepository;

    public TrafficSignListResponse list(
            String group,
            String q,
            int page,
            int size
    ) {
        if (page < 0) {
            throw BusinessException.badRequest(
                    "page must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw BusinessException.badRequest(
                    "size must be between 1 and " + MAX_PAGE_SIZE
            );
        }

        if (q != null && q.length() > MAX_QUERY_LENGTH) {
            throw BusinessException.badRequest(
                    "q must not exceed "
                            + MAX_QUERY_LENGTH
                            + " characters"
            );
        }

        String selectedGroup = normalizeGroup(group);
        String query = normalizeSearch(q);

        List<TrafficSign> available = trafficSignRepository
                .findByBankVersionAndStatusOrderByCategoryAscSourceSignIdAsc(
                        BANK_VERSION,
                        PUBLISHED
                )
                .stream()
                .filter(this::isVisible)
                .sorted(
                        Comparator.comparing(TrafficSign::getSourceSignId)
                                .thenComparing(TrafficSign::getId)
                )
                .toList();

        List<TrafficSign> matching = available.stream()
                .filter(sign ->
                        selectedGroup == null
                                || selectedGroup.equals(sign.getCategory())
                )
                .filter(sign ->
                        query.isEmpty()
                                || normalizeSearch(sign.getCode())
                                        .contains(query)
                                || normalizeSearch(sign.getName())
                                        .contains(query)
                )
                .toList();

        long totalElements = matching.size();
        long offset = (long) page * size;

        List<TrafficSignResponse> items = matching.stream()
                .skip(offset)
                .limit(size)
                .map(this::toResponse)
                .toList();

        int totalPages = (int) (
                (totalElements + size - 1L) / size
        );

        Map<String, Long> groupCounts = available.stream()
                .collect(
                        Collectors.groupingBy(
                                TrafficSign::getCategory,
                                Collectors.counting()
                        )
                );

        List<TrafficSignListResponse.Group> groups = GROUPS.stream()
                .map(definition ->
                        new TrafficSignListResponse.Group(
                                definition.code(),
                                definition.label(),
                                groupCounts.getOrDefault(
                                        definition.code(),
                                        0L
                                )
                        )
                )
                .toList();

        return new TrafficSignListResponse(
                BANK_VERSION,
                items,
                totalElements,
                page,
                size,
                totalPages,
                groups
        );
    }

    public TrafficSignResponse get(UUID id) {
        if (id == null) {
            throw BusinessException.badRequest(
                    "Traffic sign ID is required"
            );
        }

        TrafficSign sign = trafficSignRepository.findById(id)
                .filter(this::isVisible)
                .orElseThrow(() ->
                        BusinessException.notFound(
                                "Traffic sign not found"
                        )
                );

        return toResponse(sign);
    }

    private boolean isVisible(TrafficSign sign) {
        return BANK_VERSION.equals(sign.getBankVersion())
                && PUBLISHED.equals(sign.getStatus())
                && GROUPS.stream().anyMatch(group ->
                        group.code().equals(sign.getCategory())
                );
    }

    private String normalizeGroup(String group) {
        if (group == null || group.isBlank()) {
            return null;
        }

        String normalized = group.strip()
                .toLowerCase(Locale.ROOT);

        if (GROUPS.stream().noneMatch(definition ->
                definition.code().equals(normalized)
        )) {
            throw BusinessException.badRequest(
                    "group must be one of: "
                            + "cam, nguy_hiem, hieu_lenh, chi_dan, phu"
            );
        }

        return normalized;
    }

    private String normalizeSearch(String value) {
        if (value == null) {
            return "";
        }

        String text = value.strip()
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');

        return DIACRITICS.matcher(
                Normalizer.normalize(
                        text,
                        Normalizer.Form.NFD
                )
        ).replaceAll("");
    }

    private TrafficSignResponse toResponse(TrafficSign sign) {
        String tip = sign.getQuickTip();

        return new TrafficSignResponse(
                sign.getId(),
                sign.getBankVersion(),
                sign.getCode(),
                sign.getName(),
                sign.getCategory(),
                sign.getCategoryName(),
                sign.getMediaFileId(),
                sign.getSourceSignId(),
                sign.getMeaning(),
                tip == null || tip.isBlank() ? null : tip
        );
    }

    private record GroupDefinition(
            String code,
            String label
    ) {
    }
}