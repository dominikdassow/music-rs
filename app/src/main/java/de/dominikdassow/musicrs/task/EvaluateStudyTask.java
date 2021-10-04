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

        /*
        try {
            String directory = AppConfiguration.get().dataDirectory + "/results/Full";
            PrintWriter writer = new PrintWriter(new FileWriter(directory + "/HV.csv"));

            writer.println("algorithm,playlist,hv");
            writer.flush();

            Files.lines(Paths.get(directory + "/QualityIndicatorSummary.csv"))
                .skip(1)
                .map(data -> data.split(","))
                .forEach(data -> {
                    String algorithm;

                    switch (data[0]) {
                        case "NOOP__50":
                            algorithm = "NOOP";
                            break;
                        case "NSGAII__100__25000__0_95__0_005":
                            algorithm = "NSGA-II";
                            break;
                        case "SMSEMOA__100__25000__0_95__0_01":
                            algorithm = "SMS-EMOA";
                            break;
                        case "MACO1__30__100__1_0__2_0__0_1":
                            algorithm = "m-ACO";
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + data[0]);
                    }

                    writer.println(String.join(",",
                        algorithm,
                        data[1].replace("MPC_", ""),
                        data[4]
                    ));
                });

            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        try {
            String directory = AppConfiguration.get().dataDirectory + "/results/Full";
            String studyDirectory = AppConfiguration.get().dataDirectory + "/study/FullMusicPlaylistContinuationStudy";

            PrintWriter writer = new PrintWriter(new FileWriter(directory + "/ExecutionTime.csv"));

            writer.println("algorithm,playlist,time");
            writer.flush();

            Files.walk(Paths.get(studyDirectory + "/executionTime"))
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    String[] fileData = file.getFileName().toString().split("\\.");

                    try {
                        String algorithm;
                        String time = Files.readString(file);

                        switch (fileData[1]) {
                            case "NOOP__50":
                                algorithm = "NOOP";
                                break;
                            case "NSGAII__100__25000__0_95__0_005":
                                algorithm = "NSGA-II";
                                break;
                            case "SMSEMOA__100__25000__0_95__0_01":
                                algorithm = "SMS-EMOA";
                                break;
                            case "MACO1__30__100__1_0__2_0__0_1":
                                algorithm = "m-ACO";
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + fileData[1]);
                        }

                        writer.println(String.join(",",
                            algorithm,
                            fileData[0].replace("MPC_", ""),
                            time
                        ));
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }

                });

            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        try {
            String directory = AppConfiguration.get().dataDirectory + "/results/Full";
            PrintWriter writer = new PrintWriter(new FileWriter(directory + "/Evaluation.csv"));

            writer.println("algorithm,hv,time");
            writer.flush();

            Map<String, Tuple2<List<Double>, List<Integer>>> dataPoints = new HashMap<>() {{
                put("NOOP", Tuple.tuple(new ArrayList<>(), new ArrayList<>()));
                put("NSGA-II", Tuple.tuple(new ArrayList<>(), new ArrayList<>()));
                put("SMS-EMOA", Tuple.tuple(new ArrayList<>(), new ArrayList<>()));
                put("m-ACO", Tuple.tuple(new ArrayList<>(), new ArrayList<>()));
            }};

            Files.lines(Paths.get(directory + "/HV.csv"))
                .skip(1)
                .filter(data -> !data.isEmpty())
                .map(data -> data.split(","))
                .forEach(data -> dataPoints.get(data[0]).v1.add(Double.parseDouble(data[2])));

            Files.lines(Paths.get(directory + "/ExecutionTime.csv"))
                .skip(1)
                .filter(data -> !data.isEmpty())
                .map(data -> data.split(","))
                .forEach(data -> dataPoints.get(data[0]).v2.add(Integer.parseInt(data[2])));

            dataPoints.forEach((algorithm, values) -> writer.println(String.join(",",
                algorithm,
                String.valueOf(values.v1.stream().mapToDouble(v -> v).average().orElseThrow()),
                String.valueOf(values.v2.stream().mapToInt(v -> v).average().orElseThrow())
            )));

            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        */
    }
}
