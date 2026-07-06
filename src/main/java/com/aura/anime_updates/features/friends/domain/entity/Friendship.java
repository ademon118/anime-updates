package com.aura.anime_updates.features.friends.domain.entity;

import com.aura.anime_updates.features.friends.domain.enums.FriendStatus;
import com.aura.anime_updates.features.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_one_id", "user_two_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_sender_id", nullable = false)
    private User requestSender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;
}
