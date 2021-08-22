package de.dominikdassow.musicrs.task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Task {

    public enum Type {
        DOWNLOAD_SPOTIFY_DATA, IMPORT_DATA, MAKE_RECOMMENDATIONS, EVALUATE_SAMPLES, CONDUCT_STUDY
    }

    private final String name;

    protected Task(String name) {
        this.name = name;
    }

    public void run() {
        log("INIT");
        long start = System.currentTimeMillis();

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

        log("FINISH - " + (System.currentTimeMillis() - start) + "ms");
    }

    protected void init() throws Exception {}

    abstract protected void execute() throws Exception;

    protected void finish() {}

    private void log(String message) {
        log.trace(name + " :: " + message);
    }
}
