package de.dominikdassow.musicrs.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

@Slf4j
public abstract class Task {

    private final String name;

    private final StopWatch timer = new StopWatch();

    protected Task(String name) {
        this.name = name;
    }

    public void run() throws Exception {
        log("INIT");
        timer.start();

        init();
        execute();
        finish();

        timer.stop();
        log("FINISH - " + timer.getTotalTimeSeconds() + "s");
    }

    protected void init() {
    }

    abstract protected void execute() throws Exception;

    protected void finish() {
    }

    private void log(String message) {
        log.trace(name + " :: " + message);
    }
}
