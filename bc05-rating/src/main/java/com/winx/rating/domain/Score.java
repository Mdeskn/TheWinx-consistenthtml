package com.winx.rating.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Score {

    @Column(nullable = false)
    private int value;

    protected Score() {}

    private Score(int value) {
        if (value < 1 || value > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5, got: " + value);
        }
        this.value = value;
    }

    public static Score of(int value) {
        return new Score(value);
    }

    public int getValue() {
        return value;
    }
}
