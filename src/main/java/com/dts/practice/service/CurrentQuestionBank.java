package com.dts.practice.service;

public final class CurrentQuestionBank {

    private static final String CURRENT_VERSION = "dts-2026-600-v1";

    private CurrentQuestionBank() {
    }

    public static String currentVersion() {
        return CURRENT_VERSION;
    }
}