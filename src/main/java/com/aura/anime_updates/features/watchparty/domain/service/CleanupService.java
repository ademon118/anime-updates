package com.aura.anime_updates.features.watchparty.domain.service;

import com.aura.anime_updates.features.watchparty.domain.entity.PendingInvite;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class CleanupService {

    public static final long OFFLINE_GRACE_SECONDS = 60;
    public static final long INVITE_TTL_SECONDS = 900;

    private final WatchPartyManager partyManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> offlineGraceTasks = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> inviteExpiryTasks = new ConcurrentHashMap<>();

    public CleanupService(WatchPartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public void scheduleOfflineGracePeriod(String partyId, String userId, Runnable onGraceExpired) {
        cancelOfflineGracePeriod(partyId, userId);

        String taskKey = offlineGraceKey(partyId, userId);
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            offlineGraceTasks.remove(taskKey);
            log.info("Watch party offline grace expired partyId={} userId={}", partyId, userId);
            onGraceExpired.run();
        }, OFFLINE_GRACE_SECONDS, TimeUnit.SECONDS);

        offlineGraceTasks.put(taskKey, task);
        log.info(
                "Watch party offline grace scheduled partyId={} userId={} seconds={}",
                partyId,
                userId,
                OFFLINE_GRACE_SECONDS
        );
    }

    public void cancelOfflineGracePeriod(String partyId, String userId) {
        cancelTask(offlineGraceTasks, offlineGraceKey(partyId, userId));
    }

    public void scheduleInviteExpiry(String partyId, String token) {
        String taskKey = inviteExpiryKey(partyId, token);
        cancelTask(inviteExpiryTasks, taskKey);

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            inviteExpiryTasks.remove(taskKey);
            partyManager.getParty(partyId).ifPresent(party -> {
                PendingInvite invite = party.getPendingInvites().remove(token);
                if (invite != null) {
                    log.info("Watch party invite expired partyId={} token={}", partyId, token);
                    partyManager.cleanupIfAbandoned(partyId, party);
                }
            });
        }, INVITE_TTL_SECONDS, TimeUnit.SECONDS);

        inviteExpiryTasks.put(taskKey, task);
    }

    public void cancelInviteExpiry(String partyId, String token) {
        cancelTask(inviteExpiryTasks, inviteExpiryKey(partyId, token));
    }

    private void cancelTask(Map<String, ScheduledFuture<?>> tasks, String taskKey) {
        ScheduledFuture<?> existing = tasks.remove(taskKey);
        if (existing != null) {
            existing.cancel(false);
        }
    }

    private String offlineGraceKey(String partyId, String userId) {
        return partyId + ":offline:" + userId;
    }

    private String inviteExpiryKey(String partyId, String token) {
        return partyId + ":invite:" + token;
    }
}
