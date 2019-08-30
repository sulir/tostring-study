package com.github.sulir.tostringstudy;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

public class CodeElement {
    private CtElement element;

    public CodeElement(CtElement element) {
        this.element = element;
    }

    public boolean isExplicitToStringCall() {
        if (element instanceof CtInvocation) {
            CtInvocation call = (CtInvocation) element;

            // getSignature() causes StackOverflowError
            return call.getExecutable().getSimpleName().equals("toString")
                    && call.getExecutable().getParameters().isEmpty();
        } else {
            return false;
        }
    }

    public boolean isImplicitToStringCall() {
        if (element instanceof CtBinaryOperator) {
            CtBinaryOperator operator = (CtBinaryOperator) element;

            if (operator.getKind() == BinaryOperatorKind.PLUS) {
                return isStringAndNonString(operator.getLeftHandOperand(), operator.getRightHandOperand())
                        || isStringAndNonString(operator.getRightHandOperand(), operator.getLeftHandOperand());
            }
        }

        return false;
    }

    public boolean isInsideToString() {
        CtExecutable method = element.getParent(CtExecutable.class);
        return method != null && method.getSignature().equals("toString()");
    }

    public boolean isPlus() {
        return element instanceof CtBinaryOperator && ((CtBinaryOperator) element).getKind() == BinaryOperatorKind.PLUS;
    }

    public boolean isInsignificant() {
        return element instanceof CtBlock || element instanceof CtReference || element instanceof CtTypeAccess;
    }

    public boolean isStringBuildingOperation() {
        if (isPlus()) {
            CodeElement left = new CodeElement(((CtBinaryOperator) element).getLeftHandOperand());
            CodeElement right = new CodeElement(((CtBinaryOperator) element).getRightHandOperand());
            return left.isString() || right.isString();
        }

        if (isExplicitToStringCall())
            return true;

        if (element.toString().startsWith("java.lang.String.format("))
            return true;

        if (element instanceof CtLocalVariable || element instanceof CtConstructorCall
                || element instanceof CtVariableRead || element instanceof CtInvocation) {
            CtTypeReference type = ((CtTypedElement) element).getType();

            if (element instanceof CtInvocation)
                type = ((CtInvocation) element).getTarget().getType();

            return type != null && (type.getQualifiedName().equals("java.lang.StringBuilder")
                    || type.getQualifiedName().equals("java.lang.StringBuffer"));
        }

        return false;
    }

    public boolean isString() {
        if (element instanceof CtTypedElement) {
            CtTypeReference type = ((CtTypedElement) element).getType();

            return type != null && type.getQualifiedName().equals("java.lang.String");
        }

        return false;
    }

    public boolean isNonString() {
        if (element instanceof CtTypedElement) {
            CtTypeReference type = ((CtTypedElement) element).getType();

            return type != null && !type.getQualifiedName().equals("java.lang.String");
        }

        return false;
    }

    public boolean isNull() {
        return element instanceof CtLiteral && ((CtLiteral) element).getValue() == null;
    }

    public boolean isFieldNullChecking() {
        if (!(element instanceof CtBinaryOperator))
            return false;

        CtBinaryOperator operator = (CtBinaryOperator) element;
        if (operator.getKind() != BinaryOperatorKind.EQ && operator.getKind() != BinaryOperatorKind.NE)
            return false;

        return isFieldReadAndNull(operator.getLeftHandOperand(), operator.getRightHandOperand())
                || isFieldReadAndNull(operator.getRightHandOperand(), operator.getLeftHandOperand());
    }

    private boolean isStringAndNonString(CtExpression maybeString, CtExpression maybeNonString) {
        return new CodeElement(maybeString).isString() && new CodeElement(maybeNonString).isNonString();
    }

    private boolean isFieldReadAndNull(CtExpression maybeField, CtExpression maybeNull) {
        return maybeField instanceof CtFieldRead
                && maybeNull instanceof CtLiteral && ((CtLiteral) maybeNull).getValue() == null;
    }
}
