package com.vaksetu.feedback.entity;

import com.vaksetu.common.entity.BaseEntity;
import com.vaksetu.common.enums.SessionType;
import com.vaksetu.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "feedback",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "session_id",
                                "session_type",
                                "evaluator_id",
                                "target_user_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType sessionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Column
    private Integer fluencyRating;

    @Column
    private Integer pronunciationRating;

    @Column
    private Integer grammarRating;

    @Column
    private Integer confidenceRating;

    @Column
    private Integer empathyRating;

    @Column
    private Integer listeningRating;

    @Column
    private Integer engagementRating;
}
