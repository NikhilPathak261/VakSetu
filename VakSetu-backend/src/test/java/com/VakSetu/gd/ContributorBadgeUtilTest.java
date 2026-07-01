package com.vaksetu.gd;

import static org.assertj.core.api.Assertions.assertThat;

import com.vaksetu.common.enums.BadgeType;
import com.vaksetu.common.util.ContributorBadgeUtil;
import org.junit.jupiter.api.Test;

class ContributorBadgeUtilTest {

    @Test
    void calculatesBadgeThresholdsFromSharedUtility() {
        assertThat(ContributorBadgeUtil.calculateBadge(null)).isEqualTo(BadgeType.NONE);
        assertThat(ContributorBadgeUtil.calculateBadge(4)).isEqualTo(BadgeType.NONE);
        assertThat(ContributorBadgeUtil.calculateBadge(5)).isEqualTo(BadgeType.RISING_CONTRIBUTOR);
        assertThat(ContributorBadgeUtil.calculateBadge(15)).isEqualTo(BadgeType.SKILLED_CONTRIBUTOR);
        assertThat(ContributorBadgeUtil.calculateBadge(30)).isEqualTo(BadgeType.ELITE_CONTRIBUTOR);
        assertThat(ContributorBadgeUtil.calculateBadge(50)).isEqualTo(BadgeType.COMMUNITY_VOICE);
    }
}
