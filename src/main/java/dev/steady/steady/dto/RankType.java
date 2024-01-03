package dev.steady.steady.dto;

public enum RankType {

    ALL,
    STUDY,
    PROJECT;

    public static RankType from(String type) {
        return valueOf(type.toUpperCase());
    }

}
