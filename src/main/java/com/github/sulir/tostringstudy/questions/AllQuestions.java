package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.Project;
import com.github.sulir.tostringstudy.ProjectClass;
import com.github.sulir.tostringstudy.Question;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AllQuestions extends Question {
    private Question[] questions = new Question[] {
            new CorpusInfo(),
            new Hierarchy(),
            new Calls(),
            new Nodes(),
            new SchematicFields(),
            new Super()
    };

    @Override
    public void analyze(Project project) {
        for (Question question : questions)
            question.analyze(project);
    }

    @Override
    public void analyze(Path file, CtModel model) {
        for (Question question : questions)
            question.analyze(file, model);
    }

    @Override
    public void analyze(ProjectClass projectClass) {
        for (Question question : questions)
            question.analyze(projectClass);
    }

    @Override
    public void analyze(CtMethod toString) {
        for (Question question : questions)
            question.analyze(toString);
    }

    @Override
    public String getAnswer() {
        return Arrays.stream(questions).map(Question::getAnswer).collect(Collectors.joining("\n\n"));
    }
}
