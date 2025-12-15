package Algorithms;

import java.util.List;
import Config.Config;

public class AdaptivePacer {
    private int currentLevel;

    public AdaptivePacer(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    private double calculateEngagementPenalty(double timeSec, int wordCount) {
        if (wordCount == 0) {
            return 0.0;
        }

        double rate = timeSec / wordCount;

        if (rate < Config.ENGAGEMENT_RATE_MIN) {
            return 0.2;
        }

        if (rate > Config.ENGAGEMENT_RATE_MAX) {
            return 0.3;
        }

        return 0.0;
    }

    public int determineNextLevel(List<Double> scores, List<TimeEntry> timeData) {
        if (scores == null || scores.isEmpty() || timeData == null || timeData.isEmpty()) {
            return currentLevel;
        }

        double sumScores = 0.0;
        for (double score : scores) {
            sumScores += score;
        }
        double avgScore = sumScores / scores.size();

        double totalPenalty = 0.0;
        for (TimeEntry entry : timeData) {
            totalPenalty += calculateEngagementPenalty(entry.getTimeSec(), entry.getWordCount());
        }
        double avgPenalty = totalPenalty / timeData.size();

        int deltaR;

        if (avgScore >= Config.PERFORMANCE_THRESHOLD_HIGH && avgPenalty < 0.1) {
            deltaR = 1;
        } else if (avgScore < Config.PERFORMANCE_THRESHOLD_LOW || avgPenalty >= 0.25) {
            deltaR = -1;
        } else {
            deltaR = 0;
        }

        currentLevel = Math.max(1, currentLevel + deltaR);
        return currentLevel;
    }
}
