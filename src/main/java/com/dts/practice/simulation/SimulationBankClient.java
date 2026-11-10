package com.dts.practice.simulation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.Map;

@Component
public class SimulationBankClient {
    private static final String PREFIX="/api/v1/content-builder/internal/simulation-banks/";
    private final URI origin;
    private final SimulationHttp http;
    private final SimulationTokens tokens;
    public SimulationBankClient(@Value("${simulation.content-origin:http://localhost:8082}") String origin,SimulationHttp http,SimulationTokens tokens) {
        this.origin=SimulationHttp.origin(origin);this.http=http;this.tokens=tokens;
    }
    public SimulationPresets.Catalog presets() {
        return http.get(origin.resolve("/api/v1/content-builder/internal/simulation-presets/current"),Map.of("X-DTS-Simulation-Source-Token",tokens.source()),SimulationPresets.Catalog.class,false);
    }
    public SimulationPresets.Version preset(java.util.UUID version) {
        if(version==null)throw SimulationExamFailure.invalid("A preset version UUID is required");
        return http.get(origin.resolve("/api/v1/content-builder/internal/simulation-presets/"+version),Map.of("X-DTS-Simulation-Source-Token",tokens.source()),SimulationPresets.Version.class,true);
    }
    public SimulationBank.Snapshot current() {return load(null);}
    public SimulationBank.Snapshot load(String version) {
        if(version!=null) SimulationBank.version(version);
        var bank=http.get(origin.resolve(PREFIX+(version==null?"current":version)),Map.of("X-DTS-Simulation-Source-Token",tokens.source()),SimulationBank.Published.class,version!=null);
        return SimulationBank.validate(bank,version);
    }
}
