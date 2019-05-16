package com.github.sulir.tostringstudy;

import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;

import java.io.Serializable;
import java.nio.file.Path;

public abstract class Question implements Serializable {
    protected int total;

    public void analyze(Project project) {

    }

    public void analyze(Path file, CtModel model) {

    }

    public void analyze(ProjectClass projectClass) {

    }

    public void analyze(CtMethod<?> toString) {

    }

    public abstract String getAnswer();

    protected String perCent(int value) {
        return String.format("%d (%f%%)", value, 100d* value / total);
    }
}
