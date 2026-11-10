package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import java.util.*;
import static com.dts.practice.simulation.SimulationPresets.*;

@Service
@RequiredArgsConstructor
public class SimulationPresetService {
    private final SimulationExamAccess access;private final SimulationBankClient banks;private final SimulationExamRepository repo;
    private final SimulationLearningClock clock;
    @Value("${simulation.exams-enabled:true}")private boolean enabled=true;
    private ListView load(String license) {
        if(!enabled)throw SimulationExamFailure.unavailable();var actor=access.require(false);String code=SimulationLicensePolicy.require(license);
        var bank=banks.current();var catalogue=SimulationPresets.validate(banks.presets(),bank);var active=repo.active(actor.userId()).orElse(null);
        var latest=repo.latestPresets(actor.userId());List<Item> items=new ArrayList<>();
        for(var v:catalogue.items()) {
            var s=v.snapshot();var done=latest.get(v.presetVersionId());
            UUID activeId=active!=null&&active.preset!=null&&v.presetVersionId().equals(active.preset.presetVersionId())?active.id:null;
            items.add(new Item(v.presetSetId(),v.presetVersionId(),s.code(),s.title(),s.displayOrder(),"PUBLISHED",10,50,35,done!=null,
                    done==null?null:done.sessionId(),done==null?null:done.score(),done==null?null:done.passed(),done==null?null:done.completedAt(),activeId,
                    activeId!=null?"IN_PROGRESS":done==null?"NOT_STARTED":done.passed()?"PASSED":"FAILED"));
        }
        var view=active==null?null:SimulationExamRules.view(active,repo.frames(active.id),code,clock.now());
        return new ListView(new SimulationExamModel.Scope(actor.userId(),code,bank.bankVersion()),bank.bankVersion(),items.size(),List.copyOf(items),view);
    }
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)public ListView list(String license){return load(license);}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)public Detail detail(UUID set,String license) {
        if(set==null)throw SimulationExamFailure.invalid("A preset set UUID is required");var list=load(license);var item=list.items().stream().filter(x->x.presetSetId().equals(set)).findFirst().orElseThrow(SimulationExamFailure::missing);
        return new Detail(list.scope(),item,"PRESET",5,list.activeSession()==null,list.activeSession()==null?null:"ACTIVE_SESSION_EXISTS",list.activeSession());
    }
}
