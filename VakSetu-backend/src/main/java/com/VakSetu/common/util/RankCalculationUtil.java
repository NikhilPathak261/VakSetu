package com.vaksetu.common.util;

import com.vaksetu.common.enums.Rank;

public final class RankCalculationUtil {

    private RankCalculationUtil() {
    }

    public static Rank calculateRank(Double overallScore) {
        if (overallScore == null || overallScore <= 20) {
            return Rank.NOVICE;
        }

        if (overallScore <= 40) {
            return Rank.COMMUNICATOR;
        }

        if (overallScore <= 60) {
            return Rank.CONVERSATIONALIST;
        }

        if (overallScore <= 80) {
            return Rank.ORATOR;
        }

        if (overallScore <= 90) {
            return Rank.INFLUENCER;
        }

        return Rank.VAKSETU_MASTER;
    }
}
