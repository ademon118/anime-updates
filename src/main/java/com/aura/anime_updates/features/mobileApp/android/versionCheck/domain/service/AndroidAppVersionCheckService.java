package com.aura.anime_updates.features.mobileApp.android.versionCheck.domain.service;

import com.aura.anime_updates.features.mobileApp.android.versionCheck.api.response.AndroidAppVersionCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class AndroidAppVersionCheckService {

    @Value("${android.latest-app-symlink-path}")
    String symLinkPath;

    public AndroidAppVersionCheckResponse checkVersionForUpdate(String installedVersion) {
        return new AndroidAppVersionCheckResponse(needsUpdate(installedVersion));
    }

    private boolean needsUpdate(String installedVersion) {
        String latestVersion = getLatestVersion();
        return latestVersion != null && !installedVersion.equals(latestVersion);
    }

    private String getLatestVersion() {
        try {
            Path latestApp = Files.readSymbolicLink(Path.of(symLinkPath));
            return latestApp.getFileName().toString().replaceAll("anime-updates-v([\\\\d\\\\.]+)\\\\.apk", "$1");
        } catch (IOException e) {
            log.error("Failed to check latest file name with error: {}", e.getMessage());
        }
        return null;
    }
}
