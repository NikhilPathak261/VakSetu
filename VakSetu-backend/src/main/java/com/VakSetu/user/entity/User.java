package com.vaksetu.user.entity;

import com.vaksetu.common.entity.BaseEntity;
import com.vaksetu.common.enums.BadgeType;
import com.vaksetu.common.enums.Rank;
import com.vaksetu.common.enums.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Double overallScore;

    @Column(nullable = false)
    private Integer reputation;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_rank", nullable = false)
    private Rank rank;

    @Column(nullable = false)
    private Integer totalStars;

    @Column(nullable = false)
    private Integer topContributorFinishes;

    @Column(nullable = false)
    private Integer highestSessionStars;

    @Column(nullable = false)
    private Integer sessionsCompleted;

    @Column(nullable = false)
    private Integer debatesCompleted;

    @Column(nullable = false)
    private Integer roleplaysCompleted;

    @Column(nullable = false)
    private Integer gdSessionsJoined;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private BadgeType contributorBadge;

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private UserSkill userSkill;
}
