package dev.steady.steady.domain;

public enum SteadyStatus {

    RECRUITING,
    CLOSED,
    FINISHED;

    public static SteadyStatus from(String status) {
        return SteadyStatus.valueOf(status.toUpperCase());
    }

}
