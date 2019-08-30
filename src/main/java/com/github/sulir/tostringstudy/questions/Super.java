package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.Question;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class Super extends Question {
    private int superToStringCalls;

    @Override
    public void analyze(CtMethod<?> toString) {
        if (toString.getParent(CtClass.class).getSuperclass() != null) {
            total++;

             CtInvocation superToString = toString.filterChildren((CtInvocation invocation) ->
               invocation.getTarget() instanceof CtSuperAccess
                       && invocation.getExecutable().toString().equals("toString()")
            ).first();

            if (superToString != null)
                superToStringCalls++;
        }
    }

    @Override
    public String getAnswer() {
        return String.format("Calls of super.toString() are inside %s of %d toStrings of classes"
                + " with a non-Object direct superclass", perCent(superToStringCalls), total);
    }
}
