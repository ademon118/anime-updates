package com.aura.anime_updates.features.watchparty.domain.service;

import com.aura.anime_updates.features.watchparty.domain.entity.PendingInvite;
import com.aura.anime_updates.features.watchparty.domain.entity.WatchParty;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service
public class CleanupService {

    private static final long GRACE_PERIOD_SECONDS = 60;
    public static final long INVITE_TTL_SECONDS = 900;

    private final WatchPartyManager manager;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> gracePeriods = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> inviteExpiries = new ConcurrentHashMap<>();

    public CleanupService(WatchPartyManager manager, @Lazy SimpMessagingTemplate messagingTemplate) {
        this.manager = manager;
        this.messagingTemplate = messagingTemplate;
    }

    public void startGracePeriod(String partyId, String userId) {
        manager.getParty(partyId).ifPresent(p -> p.getActiveMembers().remove(userId));

        String key = graceKey(partyId, userId);
        cancelExisting(key);

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            gracePeriods.remove(key);
            manager.getParty(partyId).ifPresent(party -> expireMember(partyId, party, userId));
        }, GRACE_PERIOD_SECONDS, TimeUnit.SECONDS);

        gracePeriods.put(key, task);
    }

    private void expireMember(String partyId, WatchParty party, String userId) {
        // Grace period only marks the user offline; membership stays until explicit leave.
        party.getActiveMembers().remove(userId);
    }

    public void cancelGracePeriod(String partyId, String userId) {
        String key = graceKey(partyId, userId);
        cancelScheduled(gracePeriods, key);
        manager.getParty(partyId).ifPresent(p -> p.getActiveMembers().add(userId));
    }

    public void scheduleInviteExpiry(String partyId, String token) {
        String key = inviteKey(partyId, token);
        cancelScheduled(inviteExpiries, key);

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            inviteExpiries.remove(key);
            manager.getParty(partyId).ifPresent(party -> {
                PendingInvite invite = party.getPendingInvites().remove(token);
                if (invite != null) {
                    manager.cleanupIfAbandoned(partyId, party);
                }
            });
        }, INVITE_TTL_SECONDS, TimeUnit.SECONDS);

        inviteExpiries.put(key, task);
    }

    public void cancelInviteExpiry(String partyId, String token) {
        cancelScheduled(inviteExpiries, inviteKey(partyId, token));
    }

    private void cancelScheduled(Map<String, ScheduledFuture<?>> tasks, String key) {
        ScheduledFuture<?> existing = tasks.remove(key);
        if (existing != null) {
            existing.cancel(false);
        }
    }

    private void cancelExisting(String key) {
        cancelScheduled(gracePeriods, key);
    }

    private String graceKey(String partyId, String userId) {
        return partyId + ":" + userId;
    }

    private String inviteKey(String partyId, String token) {
        return partyId + ":invite:" + token;
    }
}
