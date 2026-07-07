package com.aura.anime_updates.features.friends.domain.repository;

import com.aura.anime_updates.features.friends.domain.entity.Friendship;
import com.aura.anime_updates.features.friends.domain.enums.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE f.userOne.id = :userId OR f.userTwo.id = :userId")
    List<Friendship> findAllByUserId(Long userId);

    Optional<Friendship> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    List<Friendship> findByStatusAndRequestSender_UserNameNot(FriendStatus status, String username);

    Optional<Friendship> findByUserOne_IdAndUserTwo_IdAndStatus(Long userOneId, Long userTwoId, FriendStatus status);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
            "WHERE ((f.userOne.id = :u1 AND f.userTwo.id = :u2) OR (f.userOne.id = :u2 AND f.userTwo.id = :u1)) " +
            "AND f.status = 'ACCEPTED'")
    boolean existsByUserId1AndUserId2(Long u1, Long u2);
}