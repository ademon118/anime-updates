package com.aura.anime_updates.features.newreleasefetcher.dataprocessor.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataProcessingUtils {


    private static final Pattern episodeAndVersionRegexPattern = Pattern.compile("-\\s*(\\d+)(?:v(\\d+))?\\s*\\(");
    private static final Pattern batchReleaseRegexPattern = Pattern.compile(".*\\[Batch\\]$", Pattern.CASE_INSENSITIVE);

    public static Integer getReleaseVersionFromRawTitle(String rawTitle) {
        Matcher matcher = episodeAndVersionRegexPattern.matcher(rawTitle);

        if (matcher.find()) {
            String version = matcher.group(2);
            return version != null ? Integer.parseInt(version) : 1;
        }

        return 1;
    }

    public static String getEpisodeFromRawTitle (String rawTitle) {
        Matcher matcher = episodeAndVersionRegexPattern.matcher(rawTitle);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static String getAnimeShowTitleFromCategory(String category) {
        return category.replaceAll("\\s*-\\s*1080", "").trim();
    }

    public static boolean isBatchRelease(String rawTitle) {
        return rawTitle != null && batchReleaseRegexPattern.matcher(rawTitle.trim()).matches();
    }
}
