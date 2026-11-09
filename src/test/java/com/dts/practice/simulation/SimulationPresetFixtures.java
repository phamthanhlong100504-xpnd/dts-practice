package com.dts.practice.simulation;
import java.util.*;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
final class SimulationPresetFixtures {
    static final int[][] ORDERS={
        {1,8,36,49,51,73,77,84,101,119},
        {2,10,31,46,52,64,76,85,93,110},
        {3,12,34,45,54,65,80,87,95,114},
        {4,15,37,48,55,66,81,88,97,117},
        {5,17,33,44,56,68,75,89,99,112},
        {6,18,35,47,57,69,78,90,102,115},
        {7,20,32,50,58,67,79,83,104,118},
        {9,21,38,53,59,70,74,86,105,116},
        {11,23,39,45,60,71,75,82,106,120},
        {13,24,40,46,61,72,77,85,107,113},
        {14,25,41,48,62,64,78,88,92,109},
        {16,26,42,51,63,66,80,84,94,111},
        {19,27,43,52,55,65,81,87,96,103},
        {22,28,30,49,56,68,79,86,98,108},
        {1,29,30,47,57,73,76,83,91,100}
    };
    static UUID id(String text){return UUID.nameUUIDFromBytes(text.getBytes(StandardCharsets.UTF_8));}
    static String sha(String text){try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    static SimulationPresets.Version version(SimulationBank.Snapshot bank,int set,String edition) {
        var selected=Arrays.stream(ORDERS[set-1]).mapToObj(n->bank.scenarios().get(n-1)).toList();
        var snapshot=new SimulationPresets.Snapshot("fixture-presets-"+edition,sha("fixture-source-"+edition),set,String.format(Locale.ROOT,"SIM-SET-%02d",set),"Đề số "+set+(edition.equals("v1")?"":" "+edition),set,bank.bankVersion(),bank.manifestSha256(),bank.scoringVersion(),10,50,35,selected);
        return new SimulationPresets.Version(id("preset-set-"+set),id("preset-version-"+set+edition),snapshot,sha("fixture-manifest-"+set+edition),"PUBLISHED",true,Instant.parse("2026-11-03T00:00:00Z"));
    }
    static SimulationPresets.Catalog catalogue(SimulationBank.Snapshot bank) {
        return new SimulationPresets.Catalog(bank.bankVersion(),bank.manifestSha256(),java.util.stream.IntStream.rangeClosed(1,15).mapToObj(n->version(bank,n,"v1")).toList());
    }
}
