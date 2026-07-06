package com.aura.anime_updates.features.friends.domain.service;

import com.aura.anime_updates.features.friends.api.response.FriendResponseDTO;
import com.aura.anime_updates.features.friends.domain.entity.Friendship;
import com.aura.anime_updates.features.friends.domain.enums.FriendStatus;
import com.aura.anime_updates.features.friends.domain.repository.FriendshipRepository;
import com.aura.anime_updates.features.notification.NotificationService;
import com.aura.anime_updates.features.notification.payloadBuilders.FriendRequestNotificationPayloadBuilder;
import com.aura.anime_updates.features.user.domain.entity.User;
import com.aura.anime_updates.features.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;
    private final FriendRequestNotificationPayloadBuilder notificationPayloadBuilder;

    @Transactional
    public void sendRequest(Long senderId, String receiverUsername) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + senderId));

        User receiver = userRepository.findByUserName(receiverUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + receiverUsername));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("You cannot send a tomodachi request to yourself.");
        }

        User userOne = (sender.getId() < receiver.getId()) ? sender : receiver;
        User userTwo = (sender.getId() < receiver.getId()) ? receiver : sender;

        if (friendshipRepository.findByUserOneIdAndUserTwoId(userOne.getId(), userTwo.getId()).isPresent()) {
            throw new IllegalStateException("A tomodachi request already exists or you are already tomodachi.");
        }

        Friendship friendship = Friendship.builder()
                .userOne(userOne)
                .userTwo(userTwo)
                .requestSender(sender)
                .status(FriendStatus.PENDING)
                .build();

        friendshipRepository.save(friendship);
        List<User> users = new ArrayList<>();
        users.add(receiver);
        notificationService.sendNotificationToAllDevicesOfUsers(users, notificationPayloadBuilder.buildFriendRequestNotificationPayload(sender.getUserName()), null);
    }

    @Transactional
    public void remove(Long removerId, String targetUserUsername) {
        User remover = userRepository.findById(removerId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + removerId));

        User target = userRepository.findByUserName(targetUserUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + targetUserUsername));

        Long userOneId = Math.min(remover.getId(), target.getId());
        Long userTwoId = Math.max(remover.getId(), target.getId());

        Friendship friendship = friendshipRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId)
                .orElseThrow(() -> new RuntimeException("Friendship or request does not exist"));

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void accept(Long currentUserId, String senderUsername) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUserId));

        User sender = userRepository.findByUserName(senderUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + senderUsername));

        Friendship friendship = friendshipRepository.findByUserOne_IdOrUserTwo_IdAndStatusAndRequestSender_UserNameNot(
                        currentUser.getId(), currentUser.getId(), FriendStatus.PENDING, currentUser.getUserName())
                .orElseThrow(() -> new RuntimeException("No pending request from " + senderUsername));

        if (!friendship.getRequestSender().getUserName().equals(senderUsername)) {
            throw new RuntimeException("This request was not sent by " + senderUsername);
        }

        friendship.setStatus(FriendStatus.ACCEPTED);
        friendshipRepository.save(friendship);
        List<User> users = new ArrayList<>();
        users.add(sender);
        notificationService.sendNotificationToAllDevicesOfUsers(users, notificationPayloadBuilder.buildFriendRequestAcceptedNotificationPayload(senderUsername), null);
    }

    @Transactional
    public void decline(Long currentUserId, String senderUsername) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUserId));

        Friendship friendship = friendshipRepository.findByUserOne_IdOrUserTwo_IdAndStatusAndRequestSender_UserNameNot(
                        currentUser.getId(), currentUser.getId(), FriendStatus.PENDING, currentUser.getUserName())
                .orElseThrow(() -> new RuntimeException("No pending request from " + senderUsername));

        if (!friendship.getRequestSender().getUserName().equals(senderUsername)) {
            throw new RuntimeException("This request was not sent by " + senderUsername);
        }

        friendship.setStatus(FriendStatus.DECLINED);
        friendshipRepository.save(friendship);
    }

    public List<FriendResponseDTO> getFriendList(Long currentUserId) {
        return friendshipRepository.findAllByUserId(currentUserId).stream()
                .map(f -> {
                    User friend = f.getUserOne().getId().equals(currentUserId) ? f.getUserTwo() : f.getUserOne();
                    return FriendResponseDTO.builder()
                            .id(friend.getId())
                            .username(friend.getUserName())
                            .status(f.getStatus().toString())
                            .isSender(f.getRequestSender().getId().equals(currentUserId))
                            .build();
                })
                .toList();
    }
}
