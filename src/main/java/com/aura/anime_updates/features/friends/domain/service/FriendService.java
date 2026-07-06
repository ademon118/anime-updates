package com.aura.anime_updates.features.friends.domain.service;

import com.aura.anime_updates.features.friends.api.response.FriendResponseDTO;
import com.aura.anime_updates.features.friends.domain.entity.Friendship;
import com.aura.anime_updates.features.friends.domain.enums.FriendStatus;
import com.aura.anime_updates.features.friends.domain.exceptions.FriendException;
import com.aura.anime_updates.features.friends.domain.repository.FriendshipRepository;
import com.aura.anime_updates.features.notification.NotificationService;
import com.aura.anime_updates.features.notification.payloadBuilders.FriendRequestNotificationPayloadBuilder;
import com.aura.anime_updates.features.user.domain.entity.User;
import com.aura.anime_updates.features.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        validateUsername(receiverUsername);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> FriendException.userNotFound(String.valueOf(senderId)));

        User receiver = userRepository.findByUserName(receiverUsername)
                .orElseThrow(() -> FriendException.userNotFound(receiverUsername));

        if (sender.getId().equals(receiver.getId())) {
            throw FriendException.selfRequest();
        }

        User userOne = (sender.getId() < receiver.getId()) ? sender : receiver;
        User userTwo = (sender.getId() < receiver.getId()) ? receiver : sender;

        if (friendshipRepository.findByUserOneIdAndUserTwoId(userOne.getId(), userTwo.getId()).isPresent()) {
            throw FriendException.alreadyExists();
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
        notificationService.sendNotificationToAllDevicesOfUsers(
                users,
                notificationPayloadBuilder.buildFriendRequestNotificationPayload(sender.getUserName()),
                null
        );
    }

    @Transactional
    public void remove(Long removerId, String targetUserUsername) {
        validateUsername(targetUserUsername);

        User remover = userRepository.findById(removerId)
                .orElseThrow(() -> FriendException.userNotFound(String.valueOf(removerId)));

        User target = userRepository.findByUserName(targetUserUsername)
                .orElseThrow(() -> FriendException.userNotFound(targetUserUsername));

        Long userOneId = Math.min(remover.getId(), target.getId());
        Long userTwoId = Math.max(remover.getId(), target.getId());

        Friendship friendship = friendshipRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId)
                .orElseThrow(FriendException::friendshipNotFound);

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void accept(Long currentUserId, String senderUsername) {
        validateUsername(senderUsername);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> FriendException.userNotFound(String.valueOf(currentUserId)));

        User sender = userRepository.findByUserName(senderUsername)
                .orElseThrow(() -> FriendException.userNotFound(senderUsername));

        Friendship friendship = friendshipRepository.findByUserOne_IdOrUserTwo_IdAndStatusAndRequestSender_UserNameNot(
                        currentUser.getId(), currentUser.getId(), FriendStatus.PENDING, currentUser.getUserName())
                .orElseThrow(() -> FriendException.pendingRequestNotFound(senderUsername));

        if (!friendship.getRequestSender().getUserName().equals(senderUsername)) {
            throw FriendException.invalidRequestSender(senderUsername);
        }

        friendship.setStatus(FriendStatus.ACCEPTED);
        friendshipRepository.save(friendship);
        List<User> users = new ArrayList<>();
        users.add(sender);
        notificationService.sendNotificationToAllDevicesOfUsers(
                users,
                notificationPayloadBuilder.buildFriendRequestAcceptedNotificationPayload(senderUsername),
                null
        );
    }

    @Transactional
    public void decline(Long currentUserId, String senderUsername) {
        validateUsername(senderUsername);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> FriendException.userNotFound(String.valueOf(currentUserId)));

        Friendship friendship = friendshipRepository.findByUserOne_IdOrUserTwo_IdAndStatusAndRequestSender_UserNameNot(
                        currentUser.getId(), currentUser.getId(), FriendStatus.PENDING, currentUser.getUserName())
                .orElseThrow(() -> FriendException.pendingRequestNotFound(senderUsername));

        if (!friendship.getRequestSender().getUserName().equals(senderUsername)) {
            throw FriendException.invalidRequestSender(senderUsername);
        }

        friendship.setStatus(FriendStatus.DECLINED);
        friendshipRepository.save(friendship);
    }

    public List<FriendResponseDTO> getFriendList(Long currentUserId) {
        if (!userRepository.existsById(currentUserId)) {
            throw FriendException.userNotFound(String.valueOf(currentUserId));
        }

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

    private void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw FriendException.usernameRequired();
        }
    }
}
