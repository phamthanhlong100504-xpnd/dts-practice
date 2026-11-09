package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimulationCatalogService {
    private final SimulationAccess access;
    private final SimulationBankClient banks;
    private final SimulationThumbnailClient thumbnails;

    public Catalog catalog(String license) {
        var actor=access.read(false);String code=SimulationLicensePolicy.require(license);var bank=banks.current();
        return new Catalog(actor.userId(),code,bank.bankVersion(),bank.scoringVersion(),bank.manifestSha256(),true,120,
                bank.chapters(),bank.scenarios().stream().map(s->new Scenario(s.scenarioId(),s.scenarioNumber(),s.chapterId(),s.title(),s.durationMs())).toList());
    }
    public SimulationThumbnailClient.Thumbnail thumbnail(String license,String version,UUID scenarioId) {
        var actor=access.read(true);SimulationLicensePolicy.require(license);
        var bank=banks.load(version);var scenario=SimulationBank.scenario(bank,scenarioId);
        return thumbnails.resolve(actor,bank,scenario);
    }
    public record Scenario(UUID scenarioId,int scenarioNumber,int chapterId,String title,int durationMs) {}
    public record Catalog(UUID userId,String licenseClass,String bankVersion,String scoringVersion,String manifestSha256,boolean bankReady,
                          int totalScenarios,List<SimulationBank.Chapter> chapters,List<Scenario> scenarios) {}
}
