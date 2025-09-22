package com.aura.anime_updates.features.newreleasefetcher.dataprocessor.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataProcessingUtils {

    Pattern episodeRegexPattern = Pattern.compile(".*-\\s*([^\\(\\s]+)\\s*\\(1080p\\)");
    Pattern episodeVersionRegexPattern = Pattern.compile("-\\s*(\\d+)(?:v(\\d+))?\\s*\\(");

    public Integer getReleaseVersionFromRawTitle(String rawTitle) {
        Matcher matcher = episodeVersionRegexPattern.matcher(rawTitle);

        if (matcher.find()) {
            String version = matcher.group(2);
            return version != null ? Integer.parseInt(version) : 1;
        }

        return 1;
    }

    public String getEpisodeFromRawTitle (String rawTitle) {
        Matcher matcher = episodeVersionRegexPattern.matcher(rawTitle);
        return matcher.find() ? matcher.group(1) : null;
    }

    public String getAnimeShowTitleFromCategory(String category) {
        return category.replaceAll("\\s*-\\s*1080", "").trim();
    }
}
