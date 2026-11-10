package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static com.dts.practice.simulation.SimulationExamModel.*;

/** Actual engine/shortcut services and clock; repositories are isolated in-memory Mockito fixtures.
 * PostgreSQL concurrency remains in SimulationShortcutRestartPostgresTest. */
class SimulationShortcutRestartTest {
 final UUID user=UUID.randomUUID();final ObjectMapper json=new ObjectMapper().findAndRegisterModules();
 final AtomicReference<Instant> now=new AtomicReference<>(Instant.parse("2026-11-04T01:00:00Z"));
 final SimulationExamRepository repo=mock(SimulationExamRepository.class);
 final SimulationShortcutRepository store=mock(SimulationShortcutRepository.class);
 final SimulationLearningRepository lessons=mock(SimulationLearningRepository.class);
 final SimulationAccess access=mock(SimulationAccess.class);final SimulationExamAccess examAccess=mock(SimulationExamAccess.class);
 final SimulationExamMediaClient media=mock(SimulationExamMediaClient.class);
 final SimulationLearningClock clock=mock(SimulationLearningClock.class);
 final Map<UUID,SimulationExamRepository.Receipt> legacy=new HashMap<>();
 final Map<UUID,SimulationShortcutRepository.Stored> events=new HashMap<>();
 final List<Frame> frames=new ArrayList<>();final Row row=new Row();final SimulationShortcutModel.Protocol protocol=new SimulationShortcutModel.Protocol();
 SimulationExamService engine;SimulationShortcutService shortcuts;
 @BeforeEach void setup()throws Exception {
  when(clock.now()).thenAnswer(x->now.get());var actor=new SimulationAccess.Actor(user,"test-only");when(examAccess.require(anyBoolean())).thenReturn(actor);
  row.id=UUID.randomUUID();row.user=user;row.license="B";row.bank="fixture";row.scoring="unchanged";row.manifest="a".repeat(64);row.policyVersion="fixed";row.passingScore=35;row.status="IN_PROGRESS";row.currentPosition=1;row.created=now.get();row.started=now.get();row.updated=now.get();
  var source=SimulationFixtures.published().snapshot();
  for(int i=1;i<=10;i++) {var old=source.scenarios().get(i-1);var f=new Frame();f.session=row.id;f.position=i;f.playbackId=UUID.randomUUID();f.state="NOT_STARTED";f.checkpointColumn=true;f.duration=60000;f.scenarioId=old.scenarioId();f.chapterName="fixture";f.etag="fixture";
   f.snapshot=new SimulationBank.Scenario(old.scenarioId(),old.scenarioNumber(),old.chapterId(),old.title(),60000,old.videoMediaId(),old.thumbnailMediaId(),old.hazardSign(),old.recognitionTip(),new SimulationBank.Timeline(17000,18000,19000,20000,21000,22000),old.video(),old.thumbnail());frames.add(f);
  }
  protocol.kind="EXAM";protocol.target=row.id;protocol.user=user;protocol.secret=UUID.randomUUID();protocol.revision=1;
  when(repo.get(eq(user),eq(row.id),anyBoolean())).thenReturn(row);when(repo.frames(row.id)).thenReturn(frames);when(repo.frame(eq(row.id),anyInt())).thenAnswer(a->frames.get((int)a.getArgument(1)-1));
  when(repo.encode(any())).thenAnswer(a->json.writeValueAsString(a.getArgument(0)));when(repo.decode(anyString(),eq(Session.class))).thenAnswer(a->json.readValue((String)a.getArgument(0),Session.class));
  when(repo.receipt(eq(user),any())).thenAnswer(a->Optional.ofNullable(legacy.get(a.getArgument(1))));
  doAnswer(a->{legacy.put(a.getArgument(1),new SimulationExamRepository.Receipt(a.getArgument(2),a.getArgument(3),a.getArgument(4),json.writeValueAsString(a.getArgument(5))));return null;}).when(repo).receipt(eq(user),any(),any(),anyString(),anyString(),any(Session.class),any());
  when(store.ready()).thenReturn(true);when(store.get("EXAM",row.id)).thenReturn(protocol);when(store.encode(any())).thenAnswer(a->json.writeValueAsString(a.getArgument(0)));
  when(store.event(eq(user),any())).thenAnswer(a->events.get(a.getArgument(1)));
  doAnswer(a->{SimulationShortcutModel.Receipt r=a.getArgument(2);events.put(r.eventId(),new SimulationShortcutRepository.Stored(r.targetKind(),r.targetId(),r.operation(),a.getArgument(1),r));return null;}).when(store).event(eq(user),anyString(),any(SimulationShortcutModel.Receipt.class));
  when(media.playback(any(),any(),any(),anyBoolean())).thenAnswer(a->{Frame f=a.getArgument(2);return new SimulationExamMediaClient.Video(row.id,f.position,f.playbackId,row.bank,row.manifest,f.snapshot.videoMediaId(),"http://localhost:9000/test",OffsetDateTime.now().plusMinutes(5),"video/mp4",f.duration,"CURRENT");});
  engine=new SimulationExamService(examAccess,mock(SimulationBankClient.class),repo,media,new SimulationExamRules(),clock);
  shortcuts=new SimulationShortcutService(access,examAccess,lessons,repo,store,clock);
 }
 void mode(String mode){row.mode=mode;if(mode.equals("PRESET"))row.preset=SimulationPresets.ref(SimulationPresetFixtures.version(SimulationFixtures.published().snapshot(),1,"fixture"));}
 Session current(){return engine.get(row.id,"B");}
 SimulationShortcutModel.State state(){return shortcuts.state("EXAM",row.id,"B");}
 Event event(int ms){var s=current();return new Event(UUID.randomUUID(),s.currentPosition(),s.playbackEventId(),ms);}
 Session play(){var p=engine.playback(row.id,"B");return engine.start(row.id,event(p.restartAtMs()),"B");}
 Restart restartIntent(){var s=current();return new Restart(UUID.randomUUID(),s.currentPosition(),s.playbackEventId());}
 Session restart(Restart request){return engine.restart(row.id,request,"B");}
 SimulationShortcutModel.Request skipIntent(){var s=state();return new SimulationShortcutModel.Request(UUID.randomUUID(),s.position(),s.playbackEventId(),0,null);}
 SimulationShortcutModel.Ack skip(SimulationShortcutModel.Request req){return shortcuts.skip("EXAM",row.id,req,"B");}
 void react(int ms){var f=frames.get(row.currentPosition-1);Instant start=f.segmentStarted;for(int t=f.segmentStart+3000;t<ms;t+=3000){now.set(start.plusMillis(t-f.segmentStart));engine.heartbeat(row.id,event(t),"B");}now.set(start.plusMillis(ms-f.segmentStart));engine.reaction(row.id,event(ms),"B");}
 void next(){var s=state();shortcuts.advance(row.id,new SimulationShortcutModel.Request(UUID.randomUUID(),s.position(),s.playbackEventId(),null,s.reactionAcknowledgement()),"B");}
 @ParameterizedTest @ValueSource(strings={"RANDOM","PRESET"}) void restartUnansweredRearmsOnlyAfterStartOnFreshNonce(String mode){mode(mode);play();skip(skipIntent());UUID skippedNonce=current().playbackEventId();play();assertFalse(state().skipIntro().available());var req=restartIntent();restart(req);assertNotEquals(skippedNonce,current().playbackEventId());assertEquals(0,state().startAtMs());assertEquals("NOT_PLAYING",state().skipIntro().reason());play();assertTrue(state().skipIntro().available());skip(skipIntent());assertFalse(state().skipIntro().available());assertEquals(1,row.currentPosition);assertEquals(0,row.completedCount);assertNull(frames.getFirst().responseMs);}
 @ParameterizedTest @CsvSource({"RANDOM,0","PRESET,0","RANDOM,17432","PRESET,17432"}) void storedReactionIncludingZeroNeverRearmsOrChangesScore(String mode,int ms){mode(mode);play();if(ms>0){skip(skipIntent());play();}react(ms);var firstEvent=frames.getFirst().responseEvent;String proof=state().reactionAcknowledgement();restart(restartIntent());play();assertFalse(state().skipIntro().available());assertEquals("REACTION_ALREADY_RECORDED",state().skipIntro().reason());assertNotEquals(proof,state().reactionAcknowledgement());assertThrows(SimulationShortcutFailure.class,()->skip(skipIntent()));assertEquals(ms,frames.getFirst().responseMs);assertEquals(firstEvent,frames.getFirst().responseEvent);next();assertEquals(ms==0?0:5,frames.getFirst().score);assertEquals(ms,frames.getFirst().responseMs);}
 @ParameterizedTest @ValueSource(strings={"RANDOM","PRESET"}) void oldRestartAckRetryCannotUndoNewSkip(String mode){mode(mode);play();skip(skipIntent());var intent=restartIntent();var restarted=restart(intent);play();skip(skipIntent());UUID nonce=current().playbackEventId();assertEquals(restarted,restart(intent));assertEquals(nonce,current().playbackEventId());assertEquals(10000,state().startAtMs());play();assertFalse(state().skipIntro().available());assertThrows(SimulationShortcutFailure.class,()->skip(skipIntent()));assertEquals(0,row.completedCount);}
 @ParameterizedTest @ValueSource(strings={"RANDOM","PRESET"}) void doubleRestartDifferentEventOldNonceIsRejected(String mode){mode(mode);play();skip(skipIntent());var original=restartIntent();restart(original);var different=new Restart(UUID.randomUUID(),original.position(),original.playbackEventId());assertThrows(SimulationExamFailure.class,()->restart(different));play();assertTrue(state().skipIntro().available());skip(skipIntent());assertEquals(1,row.currentPosition);}
 @ParameterizedTest @ValueSource(strings={"RANDOM","PRESET"}) void oldSkipReceiptCannotReapplyAfterRestart(String mode){mode(mode);play();var intent=skipIntent();var first=skip(intent);restart(restartIntent());UUID fresh=current().playbackEventId();var recovered=skip(intent);assertEquals(first.receipt(),recovered.receipt());assertEquals(fresh,recovered.state().playbackEventId());assertEquals(0,recovered.state().startAtMs());play();assertTrue(state().skipIntro().available());skip(skipIntent());play();assertFalse(state().skipIntro().available());}
 @ParameterizedTest @ValueSource(strings={"RANDOM","PRESET"}) void renewalCannotRearmSkipAndStaleNonceCannotAct(String mode){mode(mode);play();var old=skipIntent();skip(old);play();var nonce=current().playbackEventId();engine.playback(row.id,"B");assertEquals(nonce,current().playbackEventId());assertFalse(state().skipIntro().available());assertThrows(SimulationShortcutFailure.class,()->skip(new SimulationShortcutModel.Request(UUID.randomUUID(),1,old.playbackEventId(),0,null)));}
 @ParameterizedTest @ValueSource(strings={"RANDOM","PRESET"}) void repeatedRestartThenSkipStillUsesOriginalMillisecondsAndFivePoints(String mode){mode(mode);play();for(int i=0;i<3;i++){skip(skipIntent());restart(restartIntent());play();assertTrue(state().skipIntro().available());}skip(skipIntent());play();react(17432);assertEquals(17432,frames.getFirst().responseMs);next();assertEquals(5,frames.getFirst().score);assertEquals(2,row.currentPosition);assertEquals(1,row.completedCount);}
 @ParameterizedTest @ValueSource(strings={"RANDOM","PRESET"}) void restartOfCurrentPositionDoesNotChangeOtherPositionsOrOrder(String mode){mode(mode);play();skip(skipIntent());play();react(17432);next();String completed=jsonValue(frames.getFirst());List<UUID> order=frames.stream().map(f->f.scenarioId).toList();play();skip(skipIntent());restart(restartIntent());play();assertTrue(state().skipIntro().available());assertEquals(completed,jsonValue(frames.getFirst()));assertEquals(order,frames.stream().map(f->f.scenarioId).toList());assertEquals(1,row.completedCount);assertTrue(protocol.skipped.contains(1));}
 String jsonValue(Frame f){return Arrays.asList(f.scenarioId,f.playbackId,f.responseMs,f.responseEvent,f.score,f.complete,f.lastMs,f.segmentStart,f.completed).toString();}
 @Test void learningAndZeroResponseKeepTheirOriginalPolicy(){assertTrue(SimulationShortcutRules.usedInPlayback(false,true,null,0));assertTrue(SimulationShortcutRules.usedInPlayback(true,true,0,0));assertFalse(SimulationShortcutRules.usedInPlayback(true,true,null,0));assertTrue(SimulationShortcutRules.usedInPlayback(true,true,null,10000));}
}
