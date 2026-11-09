package com.dts.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LicenseCatalogItemResponse(
        String code,
        String group,
        String title,
        String description,
        String bankVersion,
        @Schema(description = "Actual number of applicable questions in the current bank")
        long totalStudyQuestions,
        @Schema(description = "Actual number of critical questions for this license in the current bank")
        long criticalStudyQuestions,
        int totalQuestions,
        int durationMinutes,
        int passingScore,
        int criticalQuestionCount,
        @Schema(description = "Study counts match the curriculum and exam quotas can be filled")
        boolean bankCountsMatch,
        @Schema(description = "The current bank has enough eligible questions to generate an exam")
        boolean examAvailable,
        @Schema(description = "DTS supports simulation learning for this license class")
        boolean simulationEnabled,
        @Schema(description = "Simulation learning is enabled and a valid published bank is available")
        boolean simulationReady,
        String simulationBankVersion
) {
    public LicenseCatalogItemResponse(String code,String group,String title,String description,String bankVersion,
            long totalStudyQuestions,long criticalStudyQuestions,int totalQuestions,int durationMinutes,int passingScore,
            int criticalQuestionCount,boolean bankCountsMatch,boolean examAvailable) {
        this(code,group,title,description,bankVersion,totalStudyQuestions,criticalStudyQuestions,totalQuestions,durationMinutes,
                passingScore,criticalQuestionCount,bankCountsMatch,examAvailable,false,false,null);
    }
    /** Capability follows product support, not temporary bank availability or the selected tab. */
    @com.fasterxml.jackson.annotation.JsonProperty("performanceMode")
    public String performanceMode() { return simulationEnabled ? "COMBINED" : "LEGACY_THEORY"; }
    @com.fasterxml.jackson.annotation.JsonProperty("learningModules")
    public java.util.List<String> learningModules() { return simulationEnabled ? java.util.List.of("THEORY","SIMULATION") : java.util.List.of("THEORY"); }
    @com.fasterxml.jackson.annotation.JsonProperty("performancePolicyVersion")
    public String performancePolicyVersion() { return simulationEnabled ? "COMBINED_EXP_V1" : "EXP_2026_01"; }
    @com.fasterxml.jackson.annotation.JsonProperty("simulationProgressScope")
    public String simulationProgressScope() { return simulationEnabled ? "USER_BANK_VERSION_SHARED_LICENSES" : null; }
    public LicenseCatalogItemResponse withSimulation(boolean enabled,boolean ready,String version) {
        return new LicenseCatalogItemResponse(code,group,title,description,bankVersion,totalStudyQuestions,criticalStudyQuestions,
                totalQuestions,durationMinutes,passingScore,criticalQuestionCount,bankCountsMatch,examAvailable,enabled,ready,version);
    }
}
