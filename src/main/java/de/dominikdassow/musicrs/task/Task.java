package de.dominikdassow.musicrs.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

@Slf4j
public abstract class Task {

    public enum Type {
        DOWNLOAD_SPOTIFY_DATA, IMPORT_DATA, MAKE_RECOMMENDATIONS, EVALUATE_SAMPLES
    }

    private final String name;

    private final StopWatch timer = new StopWatch();

    protected Task(String name) {
        this.name = name;
    }

    public void run() {
        log("INIT");
        timer.start();

        try {
            init();
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            return;
        }

        try {
            execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            finish();
        }

        timer.stop();
        log("FINISH - " + timer.getTotalTimeSeconds() + "s");
    }

    protected void init() throws Exception {}

    abstract protected void execute() throws Exception;

    protected void finish() {}

    private void log(String message) {
        log.info(name + " :: " + message);
    }
}
