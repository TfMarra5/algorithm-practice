package Algorithms;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersonalizedQuestionGenerator {
    private final Map<String, String> templates;

    public PersonalizedQuestionGenerator(Map<String, String> conceptTemplates) {
        this.templates = conceptTemplates;
    }

    private Set<String> identifyWeakConcepts(List<Map.Entry<String, Boolean>> assessmentHistory) {
        Map<String, Integer> errorCounts = new HashMap<>();

        for (Map.Entry<String, Boolean> entry : assessmentHistory) {
            String concept = entry.getKey();
            boolean correct = entry.getValue();
            if (!correct) {
                errorCounts.put(concept, errorCounts.getOrDefault(concept, 0) + 1);
            }
        }

        return errorCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(HashSet::new, Set::add, Set::addAll);
    }

    public List<String> generateQuestions(
            List<Map.Entry<String, Boolean>> studentHistory,
            Map<String, List<String>> currentTextConcepts
    ) {
        Set<String> weakConcepts = identifyWeakConcepts(studentHistory);
        List<String> generatedQuestions = new java.util.ArrayList<>();

        for (String concept : weakConcepts) {
            if (templates.containsKey(concept) && currentTextConcepts.containsKey(concept)) {
                List<String> topics = currentTextConcepts.get(concept);

                if (!topics.isEmpty()) {
                    String template = templates.get(concept);
                    String topic = topics.get(0);
                    generatedQuestions.add(template.replace("{topic}", topic));
                }
            }
        }

        return generatedQuestions;
    }
}
