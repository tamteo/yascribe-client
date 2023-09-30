package com.miriya.miri.scribe.client.runner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;


/**
 * Defined a writer to write scribe report in markdown.
 */
public class MarkdownReportWriter {
    public static final String SUCCESSFUL_COLOR = "green";
    public static final String FAILURE_COLOR = "red";
    public static final String ABORT_COLOR = "gray";
    public static final String STARTED_COLOR = "black";
    public static final String IDLE_COLOR = "black";

    private String formatValueInColor(String value, String color) {
        return String.format("<span style=\"color:%s\">%s</span>", color, value);
    }

    private String formatTime(Duration duration) {
        long HH = duration.toHours();
        int MM = duration.toMinutesPart();
        int SS = duration.toSecondsPart();
        int MS = duration.toMillisPart();
        return HH > 0 ? String.format("%d hour %d min %d sec %d ms", HH, MM, SS, MS) :
                MM > 0 ? String.format("%d min %d sec %d ms", MM, SS, MS) :
                SS > 0 ? String.format("%d sec %d ms", SS, MS) : String.format("%d ms", MS);
    }

    /**
     * Writes the specified step report data.
     * @param data the step report data
     * @return the step report in markdown
     */
    public StringBuilder writeStepReportData(ScribeTestPlanStepRunner data) {
        StringBuilder builder = new StringBuilder();

        builder.append("### ")
                .append(data.getStep())
                .append("\n\n");

        builder.append("Time : ")
                .append(formatTime(data.getDuration()))
                .append("\n\n");

        builder.append("Status : ");
        ScribeTestPlanStepRunner.Status status = data.getStatus();
        switch (status) {
            case SUCCESSFUL -> builder.append(formatValueInColor(status.name(), SUCCESSFUL_COLOR));
            case ABORTED -> builder.append(formatValueInColor(status.name(), ABORT_COLOR));
            case FAILED -> builder.append(formatValueInColor(status.name(), FAILURE_COLOR));
            case STARTED -> builder.append(formatValueInColor(status.name(), STARTED_COLOR));
            case IDLE -> builder.append(formatValueInColor(status.name(), IDLE_COLOR));
        }
        builder.append("\n");

        String reason = data.getReason();
        if (reason != null && !reason.equals("")) {
            builder.append("\n")
                    .append("Reason : \n```text\n")
                    .append(reason)
                    .append("\n```\n");
        }

        return builder;
    }

    /**
     * Writes the specified scenario report data.
     * @param data the scenario report data
     * @return the scenario report in markdown
     */
    public StringBuilder writeScenarioReportData(ScribeTestPlanScenarioRunner data) {
        StringBuilder builder = new StringBuilder();

        builder.append("## ")
                .append(data.getScenario())
                .append("\n\n");

        builder.append("| Time | Steps | Successful | Failed | Aborted |\n")
                .append("| ---- | ----- | ---------- | ------ | ------- |\n")
                .append("| ")
                .append(formatTime(data.getDuration()))
                .append(" | ")
                .append(data.getStepCount())
                .append(" | ")
                .append(data.getSuccessfulCount())
                .append(" | ")
                .append(data.getFailureCount() > 0 ? formatValueInColor(String.valueOf(data.getFailureCount()), FAILURE_COLOR) : "0")
                .append(" | ")
                .append(data.getAbortedCount() > 0 ? formatValueInColor(String.valueOf(data.getAbortedCount()), ABORT_COLOR) : "0")
                .append(" |\n\n");

        for (ScribeTestPlanStepRunner step : data.getSteps()) {
            builder.append(writeStepReportData(step)).append("\n");
        }

        return builder;
    }

    /**
     * Writes the specified scribe report data.
     * @param data the scribe report data
     * @return the scribe report in markdown
     */
    public StringBuilder writeScribeReportData(ScribeTestPlanRunner data) {
        StringBuilder builder = new StringBuilder();

        builder.append("# ")
                .append(data.getScribe())
                .append("\n\n");

        builder.append("| Browser | Date | Time | Scenarios | Steps | Successful | Failed | Aborted |\n")
                .append("| ------- | ---- | ---- | --------- | ----- | ---------- | ------ | ------- |\n")
                .append("| ")
                .append(data.getBrowser())
                .append(" | ")
                .append(ScribeTestPlanRunner.getDateFormat().format(data.getDate()))
                .append(" | ")
                .append(formatTime(data.getDuration()))
                .append(" | ")
                .append(data.getScenarioCount())
                .append(" | ")
                .append(data.getStepCount())
                .append(" | ")
                .append(data.getSuccessfulCount())
                .append(" | ")
                .append(data.getFailureCount() > 0 ? formatValueInColor(String.valueOf(data.getFailureCount()), FAILURE_COLOR) : "0")
                .append(" | ")
                .append(data.getAbortedCount() > 0 ? formatValueInColor(String.valueOf(data.getAbortedCount()), ABORT_COLOR) : "0")
                .append(" |\n\n");

        for (ScribeTestPlanScenarioRunner scenario : data.getScenarios()) {
            builder.append(writeScenarioReportData(scenario)).append("\n");
        }

        return builder;
    }

    /**
     * Writes the specified report into the specified file.
     * @param file the file
     * @param data the report data
     */
    public void writeScribeReportData(File file, ScribeTestPlanRunner data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(writeScribeReportData(data).toString());
        writer.close();
    }
}
