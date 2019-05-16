package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.Project;
import com.github.sulir.tostringstudy.ProjectClass;
import com.github.sulir.tostringstudy.Question;
import org.apache.commons.io.FilenameUtils;
import spoon.reflect.CtModel;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CorpusInfo extends Question {
    private String corpusPath;
    private Set<String> artifacts = new HashSet<>();
    private int javaFiles;
    private int classes;

    @Override
    public void analyze(Project project) {
        corpusPath = project.getCorpus().getPath();
        Set<String> jars = Arrays.stream(project.getDependencies())
                .map(FilenameUtils::getName)
                .collect(Collectors.toSet());
        artifacts.addAll(jars);
    }

    @Override
    public void analyze(Path file, CtModel model) {
        System.out.println(Path.of(corpusPath).relativize(file));
        javaFiles++;
    }

    @Override
    public void analyze(ProjectClass projectClass) {
        classes++;
    }

    @Override
    public String getAnswer() {
        return String.format("Analyzed Java files: %d, successfully resolved classes: %d. Artifact JARs: %d.",
                javaFiles, classes, artifacts.size());
    }
}
