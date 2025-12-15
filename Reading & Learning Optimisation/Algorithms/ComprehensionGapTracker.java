package Algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ComprehensionGapTracker {
    private final Map<String, List<Double>> skillScores;
    private final Random random;

    public ComprehensionGapTracker(List<String> skills) {
        this.skillScores = new HashMap<>();
        for (String skill : skills) {
            this.skillScores.put(skill, new java.util.ArrayList<>());
        }
        this.random = new Random();
    }

    public void trackAssessment(Map<String, Boolean> skillResults) {
        for (Map.Entry<String, Boolean> entry : skillResults.entrySet()) {
            String skill = entry.getKey();
            boolean correct = entry.getValue();
            if (skillScores.containsKey(skill)) {
                skillScores.get(skill).add(correct ? 1.0 : 0.0);
            }
        }
    }

    public double getSkillProficiency(String skill) {
        List<Double> scores = skillScores.get(skill);
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (double score : scores) {
            sum += score;
        }
        return sum / scores.size();
    }

    public double calculateSemanticGap(int studentSummaryLen, int keyConceptsLen) {
        if (keyConceptsLen == 0) {
            return 0.0;
        }

        double ratio = (double) Math.min(studentSummaryLen, keyConceptsLen)
                / Math.max(studentSummaryLen, keyConceptsLen);

        double simulatedScore = 0.5 + (0.4 * ratio) + (random.nextDouble() * 0.1);

        return Math.min(1.0, simulatedScore);
    }
}
