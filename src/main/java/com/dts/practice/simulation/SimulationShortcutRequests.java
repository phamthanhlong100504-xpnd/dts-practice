package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
final class SimulationShortcutRequests {
    private SimulationShortcutRequests() {}
    static UUID uuid(String s){try{return SimulationTokens.canonical(s);}catch(Exception e){throw SimulationShortcutFailure.invalid();}}
    static SimulationShortcutModel.Request parse(JsonNode body,String operation){
        Set<String> required=new HashSet<>(Set.of("eventId","position","playbackEventId"));if(operation.equals("SKIP_INTRO"))required.add("mediaTimeMs");if(operation.equals("ADVANCE_AFTER_REACTION"))required.add("reactionAcknowledgement");
        if(body==null||!body.isObject())throw SimulationShortcutFailure.invalid();Set<String> keys=new HashSet<>();body.fieldNames().forEachRemaining(keys::add);if(!keys.equals(required))throw SimulationShortcutFailure.invalid();
        int position=integer(body,"position");if(position<1||position>10)throw SimulationShortcutFailure.invalid();
        Integer ms=operation.equals("SKIP_INTRO")?integer(body,"mediaTimeMs"):null;if(ms!=null&&ms<0)throw SimulationShortcutFailure.invalid();
        String proof=operation.equals("ADVANCE_AFTER_REACTION")?text(body,"reactionAcknowledgement"):null;if(proof!=null&&!proof.matches("[0-9a-f]{64}"))throw SimulationShortcutFailure.invalid();
        return new SimulationShortcutModel.Request(uuid(text(body,"eventId")),position,uuid(text(body,"playbackEventId")),ms,proof);
    }
    private static String text(JsonNode n,String key){if(!n.path(key).isTextual()||n.path(key).asText().isBlank())throw SimulationShortcutFailure.invalid();return n.path(key).asText();}
    private static int integer(JsonNode n,String key){if(!n.path(key).isIntegralNumber()||!n.path(key).canConvertToInt())throw SimulationShortcutFailure.invalid();return n.path(key).asInt();}
}
