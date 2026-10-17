package com.dts.practice.service;

import com.dts.practice.dto.response.MemoryTipListResponse;
import com.dts.practice.dto.response.MemoryTipResponse;
import com.dts.practice.entity.MemoryTip;
import com.dts.practice.repository.MemoryTipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemoryTipReadService {

    private static final String BANK_VERSION = "dts-2026-600-v1";
    private static final String PUBLISHED = "PUBLISHED";

    private final MemoryTipRepository memoryTipRepository;

    public MemoryTipListResponse list() {
        List<MemoryTipResponse> items = memoryTipRepository
                .findByBankVersionAndStatusOrderBySortOrderAsc(
                        BANK_VERSION,
                        PUBLISHED
                )
                .stream()
                .filter(tip ->
                        BANK_VERSION.equals(tip.getBankVersion())
                                && PUBLISHED.equals(tip.getStatus())
                )
                .sorted(
                        Comparator.comparing(MemoryTip::getSortOrder)
                                .thenComparing(MemoryTip::getSourceTipId)
                                .thenComparing(MemoryTip::getId)
                )
                .map(this::toResponse)
                .toList();

        return new MemoryTipListResponse(
                BANK_VERSION,
                items,
                items.size()
        );
    }

    private MemoryTipResponse toResponse(MemoryTip tip) {
        return new MemoryTipResponse(
                tip.getId(),
                tip.getSourceTipId(),
                tip.getSortOrder(),
                tip.getTitle(),
                tip.getQuickRule(),
                tip.getExplanation()
        );
    }
}