package com.aura.anime_updates.features.friends.domain.entity;

import com.aura.anime_updates.features.friends.domain.enums.FriendStatus;
import com.aura.anime_updates.features.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name= "friendships", uniqueConstraints = {
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

    @Column(nullable = false)
    private User userOne;

    @Column(nullable = false)
    private User userTwo;

    @Column(nullable = false)
    private User requestSender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;
}


