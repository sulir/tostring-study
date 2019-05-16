package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.Occurrences;
import com.github.sulir.tostringstudy.Question;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;

import java.nio.file.Path;

public class Calls extends Question {
    private Occurrences outsideToString = new Occurrences(20);

    @Override
    public void analyze(Path file, CtModel model) {
        model.filterChildren((CtInvocation call) ->
            // getSignature() causes StackOverflowError
            call.getExecutable().getSimpleName().equals("toString") && call.getExecutable().getParameters().isEmpty()
        ).forEach((CtInvocation call) -> {
            total++;

            CtExecutable method = call.getParent(CtExecutable.class);
            if (method == null || !method.getSignature().equals("toString()"))
                outsideToString.add(call);
        });
    }

    @Override
    public String getAnswer() {
        return String.format("Calls to toString:\n total: %d\n from toStrings: %s\n " +
                "outside toStrings: %s\nOccurrences of outside:\n%s",
                total, perCent(total - outsideToString.count()),
                perCent(outsideToString.count()), outsideToString.getExamples());
    }
}
