package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.CodeElement;
import com.github.sulir.tostringstudy.Occurrences;
import com.github.sulir.tostringstudy.Question;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;

import java.nio.file.Path;

public class Calls extends Question {
    private int explicitInside;
    private int explicitOutside;
    private int implicitInside;
    private int implicitOutside;
    private Occurrences outsideToString = new Occurrences(20);

    @Override
    public void analyze(Path file, CtModel model) {
        model.filterChildren((CtExpression expression) -> true).forEach((CtExpression expression) -> {
            CodeElement element = new CodeElement(expression);
            boolean explicit = element.isExplicitToStringCall();
            boolean implicit = element.isImplicitToStringCall();

            if (explicit || implicit) {
                total++;
                boolean inside = element.isInsideToString();

                if (explicit && inside)
                    explicitInside++;
                else if (explicit)
                    explicitOutside++;
                else if (inside)
                    implicitInside++;
                else
                    implicitOutside++;

                if (!inside)
                    outsideToString.add(expression);
            }
        });
    }

    @Override
    public String getAnswer() {
        return String.format("Calls to toString:\n"
                        + " explicit inside: %s\n explicit outside: %s\n"
                        + " implicit inside: %s\n implicit outside: %s\n"
                        + " explicit: %s\n implicit: %s\n"
                        + " inside toStrings: %s\n outside toStrings: %s\n"
                        + " total: %d\nOccurrences of outside:\n%s",
                perCent(explicitInside), perCent(explicitOutside),
                perCent(implicitInside), perCent(implicitOutside),
                perCent(explicitInside + explicitOutside), perCent(implicitInside + implicitOutside),
                perCent(explicitInside + implicitInside), perCent(explicitOutside + implicitOutside),
                total, outsideToString.getExamples());
    }
}
