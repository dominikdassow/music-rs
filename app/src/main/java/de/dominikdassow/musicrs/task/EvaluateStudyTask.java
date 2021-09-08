package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.AppConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class EvaluateStudyTask
    extends Task {

    public EvaluateStudyTask() {
        super("Evaluate Study");
    }

    @Override
    protected void execute() throws IOException {
        if (!AppConfiguration.get().json.has("studyEvaluateFile")) {
            log.warn("No file to evaluate found.");

            return;
        }

        Map<String, Map<String, List<Double>>> results = new HashMap<>();

        Files.lines(Paths.get(AppConfiguration.get().json.get("studyEvaluateFile").asText()))
            .skip(1)
            .map(data -> data.split(","))
            .forEach(data -> {
                String algorithm = data[0], indicator = data[2];

                results.putIfAbsent(indicator, new HashMap<>());
                results.get(indicator).putIfAbsent(algorithm, new ArrayList<>());
                results.get(indicator).get(algorithm).add(Double.parseDouble(data[4]));
            });

        results.forEach((indicator, algorithms) -> {
            System.out.println(indicator);

            algorithms.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(-1.0)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> System.out.println("> " + entry.getKey() + "=" + entry.getValue()));
        });
    }
}
