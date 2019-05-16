package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.Occurrences;
import com.github.sulir.tostringstudy.Question;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;

public class NullChecks extends Question {
    private Occurrences occurrences = new Occurrences(10);

    @Override
    public void analyze(CtMethod<?> toString) {
        total++;

        CtBinaryOperator nullCheck = toString.filterChildren((CtBinaryOperator operator) -> {
            if (operator.getKind() != BinaryOperatorKind.EQ && operator.getKind() != BinaryOperatorKind.NE)
                return false;

            return isFieldReadAndNull(operator.getLeftHandOperand(), operator.getRightHandOperand())
                    || isFieldReadAndNull(operator.getRightHandOperand(), operator.getLeftHandOperand());
        }).first();

        if (nullCheck != null)
            occurrences.add(toString);
    }

    @Override
    public String getAnswer() {
        return String.format("From %d toStrings, %s have some null checks.\nExamples:\n%s",
                total, perCent(occurrences.count()), occurrences.getExamples());
    }

    private boolean isFieldReadAndNull(CtExpression maybeField, CtExpression maybeNull) {
        return maybeField instanceof CtFieldRead
                && maybeNull instanceof CtLiteral && ((CtLiteral) maybeNull).getValue() == null;
    }
}
