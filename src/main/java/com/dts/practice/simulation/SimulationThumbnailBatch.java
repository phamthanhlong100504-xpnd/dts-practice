package com.dts.practice.simulation;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class SimulationThumbnailBatch {
    private final SimulationThumbnailClient client;
    private final ExecutorService jobs=Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore capacity=new Semaphore(24);
    Map<UUID,SimulationThumbnailClient.Thumbnail> resolve(SimulationAccess.Actor actor,SimulationBank.Snapshot bank,List<SimulationBank.Scenario> scenarios) {
        if(scenarios.isEmpty())return Map.of();
        if(!capacity.tryAcquire(scenarios.size()))throw SimulationFailure.unavailable();
        List<Future<SimulationThumbnailClient.Thumbnail>> tasks=new ArrayList<>();
        try {
            for(var s:scenarios) tasks.add(jobs.submit(()->client.resolve(actor,bank,s)));
            long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(9);var result=new HashMap<UUID,SimulationThumbnailClient.Thumbnail>();
            for(var f:tasks) {var value=f.get(Math.max(1,deadline-System.nanoTime()),TimeUnit.NANOSECONDS);result.put(value.scenarioId(),value);}
            return result;
        } catch(InterruptedException e) {Thread.currentThread().interrupt();throw SimulationFailure.unavailable();}
        catch(Exception e) {throw SimulationFailure.unavailable();}
        finally {tasks.forEach(f->{if(!f.isDone())f.cancel(true);});capacity.release(scenarios.size());}
    }
    @PreDestroy public void close() {jobs.close();}
}
