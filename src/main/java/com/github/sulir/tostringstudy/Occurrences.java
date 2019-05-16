package com.github.sulir.tostringstudy;

import org.apache.commons.lang3.StringUtils;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Occurrences implements Comparable<Occurrences>, Serializable {
    private final String[] examples;
    private int occurrenceCount;

    public Occurrences(int exampleCount) {
        examples = new String[exampleCount];
    }

    public void add(CtElement element) {
        int position;

        if (occurrenceCount < examples.length)
            position = occurrenceCount;
        else
            position = ThreadLocalRandom.current().nextInt(occurrenceCount);

        if (position < examples.length)
            examples[position] = describe(element);

        occurrenceCount++;
    }

    public int count() {
        return occurrenceCount;
    }

    public String getExamples() {
        return Arrays.stream(examples).filter(Objects::nonNull).collect(Collectors.joining("\n---\n")) + "\n";
    }

    @Override
    public int compareTo(Occurrences other) {
        return occurrenceCount - other.occurrenceCount;
    }

    private String describe(CtElement element) {
        String location;

        if (element.getPosition().isValidPosition()) {
            location = "file://" + element.getPosition().getFile() + " : "
                    + element.getPosition().getLine();
        } else {
            location = "unknown location";
        }

        if (!(element instanceof CtExecutable) && element.getParent(CtExecutable.class) != null)
            element = element.getParent(CtExecutable.class);

        if (element instanceof CtExecutable && ((CtExecutable) element).getBody() != null)
            element = ((CtExecutable) element).getBody();

        String code;
        SourcePosition position = element.getPosition();

        if (position.isValidPosition()) {
            int start = position.getSourceStart();
            int end = position.getSourceEnd() + 1;
            code = element.getPosition().getCompilationUnit().getOriginalSourceCode().substring(start, end);
        } else {
            code = element.toString();
        }


        return location + " \n" + StringUtils.abbreviate(code, 1000);
    }
}
