package com.aura.anime_updates.features.mobileApp.android.versionCheck.api;

import com.aura.anime_updates.features.mobileApp.android.versionCheck.api.request.AndroidAppVersionCheckRequest;
import com.aura.anime_updates.features.mobileApp.android.versionCheck.api.response.AndroidAppVersionCheckResponse;
import com.aura.anime_updates.features.mobileApp.android.versionCheck.domain.service.AndroidAppVersionCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/android/app-version")
@RequiredArgsConstructor
public class AndroidAppVersionCheckApi {

    private final AndroidAppVersionCheckService versionCheckService;

    @PostMapping("/check")
    public AndroidAppVersionCheckResponse checkAndroidAppVersion(@RequestBody AndroidAppVersionCheckRequest request) {
        return versionCheckService.checkVersionForUpdate(request.installedVersion());
    }
}