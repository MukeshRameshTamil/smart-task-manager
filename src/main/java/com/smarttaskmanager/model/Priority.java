package com.smarttaskmanager.model;

public enum Priority {
    HIGH,
    MEDIUM,
    LOW;

    public int weight() {
        return switch (this) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }
}

