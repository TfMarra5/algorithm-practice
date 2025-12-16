package Algorithms;

import Config.Config;
import java.lang.Math;
import java.text.DecimalFormat;

public class ChallengeScoreCalculator {

    public double calculateChallengeScore(
            int complexityLevel,
            int wordCount,
            double averagePassRate,
            boolean hasMultimedia
    ) {

        double baseScore = (Config.BASE_SCORE_A * complexityLevel)
                + (Config.LENGTH_BETA * Math.sqrt(wordCount));

        double difference = averagePassRate - 0.5;
        double multiplier = 1.0 + (Config.SUCCESS_GAMMA * (difference * difference));

        double bonus = hasMultimedia ? Config.BONUS_DELTA : 0.0;

        double challengeScore = (baseScore * multiplier) + bonus;

        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(challengeScore).replace(',', '.'));
    }
}
