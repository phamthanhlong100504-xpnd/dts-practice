package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SimulationPresetContractTest {
    final ObjectMapper json=new ObjectMapper().findAndRegisterModules();final SimulationBank.Snapshot bank=SimulationFixtures.published().snapshot();
    @Test void allFifteenDefinitionsKeepTheirPublishedOrderQuotaAndCoverage() {
        var c=SimulationPresets.validate(SimulationPresetFixtures.catalogue(bank),bank);Set<UUID> all=new HashSet<>();
        for(var v:c.items()){assertEquals(10,v.snapshot().scenarios().size());v.snapshot().scenarios().forEach(s->all.add(s.scenarioId()));}
        assertEquals(120,all.size());assertEquals(List.of(1,8,36,49,51,73,77,84,101,119),c.items().getFirst().snapshot().scenarios().stream().map(SimulationBank.Scenario::scenarioNumber).toList());
    }
    @ParameterizedTest @ValueSource(strings={"status","current","publishedAt","presetSetId","presetVersionId","manifestSha256","sourceSha256","sourceVersion","sourceSetId","displayOrder","code","title","bankVersion","bankManifestSha256","scoringVersion","totalScenarios","maxTotalScore","passingScore","duplicateScenario","foreignScenario"})
    void invalidOrUnpublishedVersionCannotReachTheEngine(String field)throws Exception {
        ObjectNode root=json.valueToTree(SimulationPresetFixtures.version(bank,1,"v1"));ObjectNode snapshot=(ObjectNode)root.get("snapshot");
        switch(field) {
            case "status"->root.put(field,"ARCHIVED");case "current"->root.put(field,false);
            case "publishedAt","presetSetId","presetVersionId"->root.putNull(field);case "manifestSha256"->root.put(field,"bad");
            case "sourceVersion"->snapshot.put(field,"invalid source/version");case "title"->snapshot.put(field,"   ");
            case "sourceSetId","displayOrder","totalScenarios","maxTotalScore","passingScore"->snapshot.put(field,0);
            case "duplicateScenario"->{var array=(com.fasterxml.jackson.databind.node.ArrayNode)snapshot.get("scenarios");array.set(1,array.get(0).deepCopy());}
            case "foreignScenario"->((ObjectNode)snapshot.path("scenarios").get(0)).put("title","Changed pinned title");
            default->snapshot.put(field,"bad");
        }
        var value=json.treeToValue(root,SimulationPresets.Version.class);assertThrows(SimulationExamFailure.class,()->SimulationPresets.validate(value,bank));
    }
    @Test void emptyPublishedCatalogueIsAValidReadContract() {
        var empty=new SimulationPresets.Catalog(bank.bankVersion(),bank.manifestSha256(),List.of());assertEquals(empty,SimulationPresets.validate(empty,bank));
    }
    @Test void duplicateAndReorderedCurrentVersionsAreRejected() {
        var c=SimulationPresetFixtures.catalogue(bank);var items=new ArrayList<>(c.items());Collections.swap(items,0,1);
        assertThrows(SimulationExamFailure.class,()->SimulationPresets.validate(new SimulationPresets.Catalog(c.bankVersion(),c.bankManifestSha256(),items),bank));
        items.clear();items.add(c.items().getFirst());items.add(c.items().getFirst());assertThrows(SimulationExamFailure.class,()->SimulationPresets.validate(new SimulationPresets.Catalog(c.bankVersion(),c.bankManifestSha256(),items),bank));
    }
    @Test void wrongChapterQuotaIsRejectedEvenWhenEveryScenarioBelongsToTheBank() {
        var v=SimulationPresetFixtures.version(bank,1,"v1");var s=v.snapshot();var selected=new ArrayList<>(s.scenarios());selected.set(2,bank.scenarios().get(2));
        var invalid=new SimulationPresets.Snapshot(s.sourceVersion(),s.sourceSha256(),s.sourceSetId(),s.code(),s.title(),s.displayOrder(),s.bankVersion(),s.bankManifestSha256(),s.scoringVersion(),10,50,35,selected);
        assertThrows(SimulationExamFailure.class,()->SimulationPresets.validate(new SimulationPresets.Version(v.presetSetId(),v.presetVersionId(),invalid,v.manifestSha256(),"PUBLISHED",true,v.publishedAt()),bank));
    }
}
