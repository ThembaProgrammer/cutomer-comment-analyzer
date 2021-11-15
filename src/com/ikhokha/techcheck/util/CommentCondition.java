package com.ikhokha.techcheck.util;

import java.util.function.Predicate;

/**
 * This class is for creating different metrics
 */

public class CommentCondition {

    private String key;
    private Predicate<String> condition;

    public CommentCondition(final String key){
        this.key = key;
    }

    public String getKey(){
        return key;
    }

    public Predicate<String> getCondition() {
        return condition;
    }

    public void setCondition(final Predicate<String> condition) {
        this.condition = condition;
    }

    public CommentCondition condition(final Predicate<String> condition){
        this.setCondition(condition);
        return this;
    }
}
