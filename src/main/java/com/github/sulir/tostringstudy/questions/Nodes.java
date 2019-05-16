package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.Occurrences;
import com.github.sulir.tostringstudy.Question;
import org.apache.commons.lang3.StringUtils;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtReference;

import java.util.*;

public class Nodes extends Question {
    private Map<Set<String>, Occurrences> nodeSets = new HashMap<>();

    @Override
    public void analyze(CtMethod<?> toString) {
        total++;
        Set<String> nodeTypes = new HashSet<>();

        toString.getBody().filterChildren(element ->
                !(element instanceof CtBlock || element instanceof CtReference || element instanceof CtTypeAccess)
        ).forEach(element -> {
            String nodeType = element.getClass().getSimpleName();
            nodeTypes.add(StringUtils.removeEnd(StringUtils.removeStart(nodeType, "Ct"), "Impl"));
        });

        if (!nodeSets.containsKey(nodeTypes))
            nodeSets.put(nodeTypes, new Occurrences(20));

        nodeSets.get(nodeTypes).add(toString);
    }

    @Override
    public String getAnswer() {
        StringBuilder result = new StringBuilder("AST node type sets:\n");

        nodeSets.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry<Set<String>, Occurrences>::getValue).reversed())
                .forEach(entry -> {
            Set<String> nodeSet = entry.getKey();
            Occurrences occurrences = entry.getValue();
            result.append(String.format("%d: %s\nExamples:\n%s\n",
                    occurrences.count(), nodeSet, occurrences.getExamples()));
        });

        return result.toString();
    }
}
