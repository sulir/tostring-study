package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.Occurrences;
import com.github.sulir.tostringstudy.Question;
import spoon.reflect.CtModel;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtTypeReference;

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
            boolean explicit = isExplicitToStringCall(expression);
            boolean implicit = isImplicitToStringCall(expression);

            if (explicit || implicit) {
                total++;
                boolean inside = isInsideToString(expression);

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

    private boolean isExplicitToStringCall(CtExpression expression) {
        if (expression instanceof CtInvocation) {
            CtInvocation call = (CtInvocation) expression;

            // getSignature() causes StackOverflowError
            return call.getExecutable().getSimpleName().equals("toString")
                    && call.getExecutable().getParameters().isEmpty();
        } else {
            return false;
        }
    }

    private boolean isImplicitToStringCall(CtExpression expression) {
        if (expression instanceof CtBinaryOperator) {
            CtBinaryOperator operator = (CtBinaryOperator) expression;

            if (operator.getKind() == BinaryOperatorKind.PLUS) {
                return isStringAndNonString(operator.getLeftHandOperand(), operator.getRightHandOperand())
                        || isStringAndNonString(operator.getRightHandOperand(), operator.getLeftHandOperand());
            }
        }

        return false;
    }

    private boolean isInsideToString(CtExpression expression) {
        CtExecutable method = expression.getParent(CtExecutable.class);
        return method != null && method.getSignature().equals("toString()");
    }

    private boolean isStringAndNonString(CtExpression maybeString, CtExpression maybeNonString) {
        CtTypeReference type1 = maybeString.getType();
        CtTypeReference type2 = maybeNonString.getType();

        return type1 != null && type1.getQualifiedName().equals("java.lang.String")
                && type2 != null && !type2.getQualifiedName().equals("java.lang.String");
    }
}
