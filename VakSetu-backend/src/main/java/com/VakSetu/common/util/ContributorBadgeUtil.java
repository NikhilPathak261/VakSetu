package com.vaksetu.common.util;

import com.vaksetu.common.constants.AppConstants;
import com.vaksetu.common.enums.BadgeType;

public final class ContributorBadgeUtil {

    private ContributorBadgeUtil() {
    }

    public static BadgeType calculateBadge(Integer finishes) {
        if (finishes == null || finishes < AppConstants.RISING_CONTRIBUTOR_THRESHOLD) {
            return BadgeType.NONE;
        }

        if (finishes >= AppConstants.COMMUNITY_VOICE_THRESHOLD) {
            return BadgeType.COMMUNITY_VOICE;
        }

        if (finishes >= AppConstants.ELITE_CONTRIBUTOR_THRESHOLD) {
            return BadgeType.ELITE_CONTRIBUTOR;
        }

        if (finishes >= AppConstants.SKILLED_CONTRIBUTOR_THRESHOLD) {
            return BadgeType.SKILLED_CONTRIBUTOR;
        }

        return BadgeType.RISING_CONTRIBUTOR;
    }
}
