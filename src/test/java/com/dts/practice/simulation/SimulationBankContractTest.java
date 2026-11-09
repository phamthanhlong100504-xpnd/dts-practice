package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class SimulationBankContractTest {
    final ObjectMapper json=new ObjectMapper().findAndRegisterModules();
    @Test void acceptsCompletePublishedBankAndFindsExactScenario() {
        var bank=SimulationBank.validate(SimulationFixtures.published(),null);
        assertEquals(120,bank.scenarios().size());assertEquals(6,bank.chapters().size());
        assertEquals(bank.scenarios().get(119),SimulationBank.scenario(bank,bank.scenarios().get(119).scenarioId()));
    }
    @Test void pinnedPublishedVersionCanBeReadAfterAnotherBankBecomesCurrent() {
        var p=SimulationFixtures.published();var old=SimulationFixtures.copy(p,"PUBLISHED",false,p.snapshot());
        assertEquals(p.snapshot(),SimulationBank.validate(old,p.snapshot().bankVersion()));
        assertThrows(BusinessException.class,()->SimulationBank.validate(old,null));
    }
    @ParameterizedTest @ValueSource(strings={"draft","unpublished-time","missing-scenario","duplicate-scenario","duplicate-media","wrong-version","wrong-chapter-count","wrong-chapter-order","wrong-scenario-order","bad-duration","bad-timeline","past-track","bad-video-mime","bad-thumbnail","missing-hint","missing-hash"})
    void malformedOrUnpublishedSnapshotNeverReachesLearner(String kind) throws Exception {
        ObjectNode root=json.valueToTree(SimulationFixtures.published());ObjectNode b=(ObjectNode)root.get("snapshot");
        ObjectNode s=(ObjectNode)b.withArray("scenarios").get(0);
        switch(kind) {
            case "draft" -> root.put("status","DRAFT");
            case "unpublished-time" -> root.remove("publishedAt");
            case "missing-scenario" -> b.withArray("scenarios").remove(119);
            case "duplicate-scenario" -> ((ObjectNode)b.withArray("scenarios").get(1)).set("scenarioId",s.get("scenarioId"));
            case "duplicate-media" -> s.set("thumbnailMediaId",s.get("videoMediaId"));
            case "wrong-version" -> b.put("bankVersion","../bank");
            case "wrong-chapter-count" -> ((ObjectNode)b.withArray("chapters").get(0)).put("totalScenarios",30);
            case "wrong-chapter-order" -> ((ObjectNode)b.withArray("chapters").get(0)).put("chapterId",2);
            case "wrong-scenario-order" -> s.put("scenarioNumber",2);
            case "bad-duration" -> s.put("durationMs",1);
            case "bad-timeline" -> ((ObjectNode)s.get("scoringTimeline")).put("t4StartMs",10000);
            case "past-track" -> ((ObjectNode)s.get("video")).put("videoTrackDurationMs",14999);
            case "bad-video-mime" -> ((ObjectNode)s.get("video")).put("mimeType","text/html");
            case "bad-thumbnail" -> ((ObjectNode)s.get("thumbnail")).put("fileName","image.mp4");
            case "missing-hint" -> s.remove("hazardSign");
            case "missing-hash" -> b.remove("manifestSha256");
        }
        var invalid=json.treeToValue(root,SimulationBank.Published.class);
        assertEquals(503,assertThrows(BusinessException.class,()->SimulationBank.validate(invalid,null)).getStatus().value());
    }
    @ParameterizedTest @ValueSource(strings={"A1","A","B1","UNKNOWN"})
    void unsupportedLicenseIsDenied(String code) {assertFalse(SimulationLicensePolicy.enabled(code));assertThrows(BusinessException.class,()->SimulationLicensePolicy.require(code));}
    @ParameterizedTest @ValueSource(strings={"B","C1","C","D1","D2","D","BE","C1E","CE","D1E","D2E","DE"})
    void carAndTrailerLicensesShareOneSimulationBank(String code) {assertTrue(SimulationLicensePolicy.enabled(code));assertEquals(code,SimulationLicensePolicy.require(" "+code.toLowerCase()+" "));}
    @Test void wrongPinnedBankAndUnknownScenarioAreRejected() {
        assertThrows(BusinessException.class,()->SimulationBank.validate(SimulationFixtures.published(),"other-v1"));
        assertEquals(404,assertThrows(BusinessException.class,()->SimulationBank.scenario(SimulationFixtures.published().snapshot(),java.util.UUID.randomUUID())).getStatus().value());
    }
}
