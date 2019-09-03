package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.CodeElement;
import com.github.sulir.tostringstudy.Question;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtIterator;

import java.util.*;

public class SchematicFields extends Question {
    private int schematic;
    private int schematicWithFields;
    private Map<String, Integer> fieldsReadStats = new TreeMap<>();

    @Override
    public void analyze(CtMethod<?> toString) {
        total++;

        Map<String, Boolean> fields = new HashMap<>();
        toString.getParent(CtClass.class).getDeclaredFields().stream().filter((field) ->
                !(field.isStatic() && field.isFinal())
        ).forEach((field) ->
                fields.put(field.getSimpleName(), false)
        );

        CtIterator iterator = new CtIterator(toString.getBody());
        while (iterator.hasNext()) {
            CtElement elem = iterator.next();
            CodeElement element = new CodeElement(elem);

            if (element.isInsignificant() || elem instanceof CtReturn
                    || elem instanceof CtThisAccess || elem instanceof CtSuperAccess)
                continue;

            if (elem instanceof CtLiteral && (element.isString() || element.isNull()))
                continue;

            if (element.isStringBuildingOperation())
                continue;

            if (elem instanceof CtFieldRead && ((CtFieldRead) elem).getTarget() instanceof CtThisAccess) {
                CtField field = ((CtFieldRead) elem).getVariable().getFieldDeclaration();
                if (field != null && !(field.isStatic() && field.isFinal()))
                    fields.computeIfPresent(((CtFieldRead) elem).getVariable().getSimpleName(), (k, v) -> true);

                continue;
            }

            if (elem instanceof CtIf || elem instanceof CtConditional)
                element = new CodeElement(elem.getValueByRole(CtRole.CONDITION));
            if (element.isFieldNullChecking())
                continue;

            return;
        }

        schematic++;

        if (fields.size() != 0) {
            schematicWithFields++;
            long readCount = fields.values().stream().filter((read) -> read).count();
            double readPercent = 100d * readCount / fields.size();

            String label;
            if (readCount == 0)
                label = "None";
            else if (readCount == fields.size())
                label = "All";
            else if (readPercent < 25)
                label = "0-25%";
            else if (readPercent < 50)
                label = "25-50%";
            else if (readPercent < 75)
                label = "50-75%";
            else
                label = "75-100%";

            int count = fieldsReadStats.containsKey(label) ? fieldsReadStats.get(label) + 1 : 1;
            fieldsReadStats.put(label, count);
        }
    }

    @Override
    public String getAnswer() {
        StringBuilder answer = new StringBuilder(String.format("Schematic toStrings: %s, non-schematic: %s\n"
                        + "Fields of classes read (percentages):\n",
                perCent(schematic), perCent(total - schematic)));

        for (Map.Entry<String, Integer> entry : fieldsReadStats.entrySet()) {
            answer.append(String.format(" %s: %d (%f%%)\n",
                    entry.getKey(), entry.getValue(), 100d * entry.getValue() / schematicWithFields));
        }

        return answer.toString();
    }
}
