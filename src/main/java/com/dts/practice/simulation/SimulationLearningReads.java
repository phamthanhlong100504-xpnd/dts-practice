package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import static com.dts.practice.simulation.SimulationLearningModel.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
public class SimulationLearningReads {
    private final SimulationAccess access;private final SimulationBankClient banks;private final SimulationLearningRepository repo;
    private final SimulationThumbnailBatch thumbnails;private final SimulationLearningClock clock;
    public record Scope(UUID userId,String licenseClass,String bankVersion) {}
    public record Chapter(int chapterId,int chapterNumber,String chapterName,int totalScenarios,int practicedScenarios,double completionPercent,String status) {}
    public record Overview(Scope scope,int totalScenarios,int practicedScenarios,double completionPercent,List<Chapter> chapters) {}
    public record Item(UUID scenarioId,int scenarioNumber,String title,int chapterId,int durationMs,String thumbnailUrl,OffsetDateTime thumbnailExpiresAt,
                       boolean isPracticed,Integer bestScore,int maxScore,boolean isBookmarked,int completedAttemptCount) {}
    public record Page(Scope scope,Chapter chapter,List<Item> items,Map<String,Integer> filterCounts,int page,int size,int totalPages,int filteredCount,UUID startScenarioId,Cursor cursor) {}
    public record Hint(String hazardSign,String recognitionTip) {}
    public record Detail(Scope scope,Item scenario,Hint hintPreview,Policy policy,Attempt latestAttempt,UUID previousScenarioId,UUID nextScenarioId) {}
    public Overview overview(String license,String version) {
        var actor=access.read(false);var code=SimulationLicensePolicy.require(license);var bank=banks.load(version);var stats=repo.stats(actor.userId(),bank.bankVersion());
        var chapters=bank.chapters().stream().map(c->chapter(bank,c.chapterId(),stats)).toList();int done=chapters.stream().mapToInt(Chapter::practicedScenarios).sum();
        return new Overview(new Scope(actor.userId(),code,bank.bankVersion()),120,done,percent(done,120),chapters);
    }
    public Page chapter(int chapter,String license,String version,String filter,String sort,int page,int size) {
        var actor=access.read(true);String code=SimulationLicensePolicy.require(license);var bank=banks.load(version);
        if(page<0 || page>10000 || size<1 || size>12 || !"SCENARIO_ASC".equals(sort))throw SimulationFailure.invalid("Use page>=0, size 1..12 and sort=SCENARIO_ASC");
        String selected=filter==null?"ALL":filter.strip().toUpperCase(Locale.ROOT);
        if(!Set.of("ALL","UNPRACTICED","PRACTICED","BOOKMARKED").contains(selected))throw SimulationFailure.invalid("Invalid scenario filter");
        var stats=repo.stats(actor.userId(),bank.bankVersion());var summary=chapter(bank,chapter,stats);
        var all=bank.scenarios().stream().filter(s->s.chapterId()==chapter).toList();var counts=new LinkedHashMap<String,Integer>();
        for(String mode:List.of("ALL","UNPRACTICED","PRACTICED","BOOKMARKED"))counts.put(mode,(int)all.stream().filter(s->matches(mode,stat(stats,s.scenarioId()))).count());
        var filtered=all.stream().filter(s->matches(selected,stat(stats,s.scenarioId()))).toList();int start=Math.min(filtered.size(),page*size),end=Math.min(filtered.size(),start+size);
        var rows=filtered.subList(start,end);var urls=thumbnails.resolve(actor,bank,rows);
        var items=rows.stream().map(s->item(s,stat(stats,s.scenarioId()),urls.get(s.scenarioId()))).toList();
        UUID startScenario=all.stream().filter(s->!stat(stats,s.scenarioId()).practiced()).findFirst().orElse(all.getFirst()).scenarioId();
        return new Page(new Scope(actor.userId(),code,bank.bankVersion()),summary,items,counts,page,size,(filtered.size()+size-1)/size,filtered.size(),startScenario,repo.cursor(actor.userId(),bank.bankVersion(),chapter));
    }
    public Detail scenario(UUID id,String license,String version) {
        var actor=access.read(true);String code=SimulationLicensePolicy.require(license);var bank=banks.load(version);var scenario=SimulationBank.scenario(bank,id);
        var stats=repo.stats(actor.userId(),bank.bankVersion());var urls=thumbnails.resolve(actor,bank,List.of(scenario));
        var list=bank.scenarios().stream().filter(s->s.chapterId()==scenario.chapterId()).toList();int index=list.indexOf(scenario);
        var latest=repo.latest(actor.userId(),bank.bankVersion(),id).map(r->SimulationLearningRules.view(r,clock.now())).orElse(null);
        Hint hint=repo.playing(actor.userId(),bank.bankVersion(),id)?null:new Hint(scenario.hazardSign(),scenario.recognitionTip());
        return new Detail(new Scope(actor.userId(),code,bank.bankVersion()),item(scenario,stat(stats,id),urls.get(id)),hint,SimulationLearningRules.POLICY,latest,
                index>0?list.get(index-1).scenarioId():null,index+1<list.size()?list.get(index+1).scenarioId():null);
    }
    private static Item item(SimulationBank.Scenario s,Stats stats,SimulationThumbnailClient.Thumbnail thumb) {
        if(thumb==null)throw SimulationFailure.unavailable();
        return new Item(s.scenarioId(),s.scenarioNumber(),s.title(),s.chapterId(),s.durationMs(),thumb.url(),thumb.expiredAt(),stats.practiced(),stats.bestScore(),5,stats.bookmarked(),stats.completedAttemptCount());
    }
    private static Stats stat(Map<UUID,Stats> stats,UUID id) {return stats.getOrDefault(id,new Stats(0,null,false));}
    private static boolean matches(String filter,Stats s) {return switch(filter) {case "PRACTICED"->s.practiced();case "UNPRACTICED"->!s.practiced();case "BOOKMARKED"->s.bookmarked();default->true;};}
    private static Chapter chapter(SimulationBank.Snapshot bank,int id,Map<UUID,Stats> stats) {
        var c=bank.chapters().stream().filter(v->v.chapterId()==id).findFirst().orElseThrow(SimulationFailure::notFound);
        int done=(int)bank.scenarios().stream().filter(s->s.chapterId()==id&&stat(stats,s.scenarioId()).practiced()).count();
        return new Chapter(id,id,c.name(),c.totalScenarios(),done,percent(done,c.totalScenarios()),done==0?"NOT_STARTED":done==c.totalScenarios()?"COMPLETED":"IN_PROGRESS");
    }
    private static double percent(int done,int total) {return Math.round(done*10000.0/total)/100.0;}
}
