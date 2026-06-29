package com.vaksetu.common.constants;

public final class AppConstants {

    public static final Integer INITIAL_SKILL_SCORE = 50;
    public static final Integer INITIAL_REPUTATION = 50;

    public static final Integer MAX_REPUTATION = 100;
    public static final Integer MIN_REPUTATION = 0;

    public static final Integer MAX_SKILL_SCORE = 100;
    public static final Integer MIN_SKILL_SCORE = 0;

    public static final int MAX_GD_PARTICIPANTS = 100;

    public static final Integer SESSION_REPUTATION_REWARD = 2;
    public static final Integer SESSION_ABANDONMENT_PENALTY = 10;
    public static final Integer REPEATED_REPORT_PENALTY = 20;

    public static final Double SKILL_HISTORY_WEIGHT = 0.7;
    public static final Double SESSION_RATING_WEIGHT = 0.3;

    public static final Integer RISING_CONTRIBUTOR_THRESHOLD = 5;
    public static final Integer SKILLED_CONTRIBUTOR_THRESHOLD = 15;
    public static final Integer ELITE_CONTRIBUTOR_THRESHOLD = 30;
    public static final Integer COMMUNITY_VOICE_THRESHOLD = 50;

    public static final Integer ROLEPLAY_PREPARATION_SECONDS = 180;
    public static final Integer ROLEPLAY_SESSION_DURATION_SECONDS = 900;

    public static final Integer QUEUE_ENTRY_TTL_MINUTES = 15;
    public static final Integer STALE_SESSION_TTL_MINUTES = 60;
    public static final long CLEANUP_FIXED_DELAY_MS = 60_000L;

    private AppConstants() {
    }
}
