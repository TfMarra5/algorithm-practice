import Algorithms.AdaptivePacer;
import Algorithms.ChallengeScoreCalculator;
import Algorithms.ComprehensionGapTracker;
import Algorithms.PersonalizedQuestionGenerator;
import Algorithms.TimeEntry;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        System.out.println("--- Reading Optimisation Algorithms Demo (Java) ---");

        System.out.println("\n--- 1. Adaptive Difficulty Pacing (ADP) ---");
        AdaptivePacer pacer = new AdaptivePacer(5);

        List<Double> scores1 = Arrays.asList(0.95, 0.90, 0.88);
        List<TimeEntry> timeData1 = Arrays.asList(
                new TimeEntry(150, 100),
                new TimeEntry(145, 100)
        );
        System.out.printf(
                "S1: Initial level: %d -> Next level: %d%n",
                5,
                pacer.determineNextLevel(scores1, timeData1)
        );

        List<Double> scores2 = Arrays.asList(0.50, 0.65, 0.45);
        List<TimeEntry> timeData2 = Arrays.asList(
                new TimeEntry(300, 100),
                new TimeEntry(320, 100)
        );
        System.out.printf(
                "S2: Initial level: %d -> Next level: %d%n",
                pacer.getCurrentLevel(),
                pacer.determineNextLevel(scores2, timeData2)
        );

        System.out.println("\n--- 2. Personalized Question Generation (PQG) ---");

        Map<String, String> templates = new HashMap<>();
        templates.put("Vocabulary", "Define the word '{topic}' in the context of the text.");
        templates.put("Cause-Effect", "Explain the cause of '{topic}' as described in the article.");
        templates.put("Inference", "What conclusion can we draw about '{topic}'?");

        List<Map.Entry<String, Boolean>> history = Arrays.asList(
                new SimpleEntry<>("Vocabulary", false),
                new SimpleEntry<>("Vocabulary", false),
                new SimpleEntry<>("Inference", true),
                new SimpleEntry<>("Inference", true),
                new SimpleEntry<>("Cause-Effect", false),
                new SimpleEntry<>("Cause-Effect", true)
        );

        Map<String, List<String>> currentText = new HashMap<>();
        currentText.put("Vocabulary", Arrays.asList("Inertia", "Gravity"));
        currentText.put("Cause-Effect", Arrays.asList("Universe Expansion", "Climate Change"));
        currentText.put("Detail", Arrays.asList("Mission Date"));

        PersonalizedQuestionGenerator pqg = new PersonalizedQuestionGenerator(templates);
        List<String> questions = pqg.generateQuestions(history, currentText);

        System.out.println("Weak concept focus: Vocabulary, Cause-Effect");
        System.out.println("Generated questions:");
        questions.forEach(q -> System.out.println(" - " + q));

        System.out.println("\n--- 3. Comprehension Gap Tracker (CGT) ---");
        List<String> skills = Arrays.asList("Inference", "Vocabulary", "Detail");
        ComprehensionGapTracker cgt = new ComprehensionGapTracker(skills);

        Map<String, Boolean> assessment1 = Map.of(
                "Inference", true,
                "Vocabulary", false,
                "Detail", true
        );
        Map<String, Boolean> assessment2 = Map.of(
                "Inference", true,
                "Vocabulary", true,
                "Detail", false
        );

        cgt.trackAssessment(assessment1);
        cgt.trackAssessment(assessment2);

        System.out.printf(
                "Inference proficiency: %.2f%n",
                cgt.getSkillProficiency("Inference")
        );
        System.out.printf(
                "Detail proficiency: %.2f%n",
                cgt.getSkillProficiency("Detail")
        );

        int studentLen = 80;
        int keyConceptLen = 100;
        System.out.printf(
                "Semantic gap score (len %d vs %d): %.3f%n",
                studentLen,
                keyConceptLen,
                cgt.calculateSemanticGap(studentLen, keyConceptLen)
        );

        System.out.println("\n--- 4. Reading Challenge Gamification Score (RCGS) ---");
        ChallengeScoreCalculator rcgs = new ChallengeScoreCalculator();

        double scoreA = rcgs.calculateChallengeScore(10, 500, 0.5, false);
        double scoreB = rcgs.calculateChallengeScore(5, 100, 0.9, true);

        System.out.printf("S1 (Complex / Ideal): %.2f points%n", scoreA);
        System.out.printf("S2 (Simple / High success / Bonus): %.2f points%n", scoreB);
    }
}
