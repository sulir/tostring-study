package com.github.sulir.tostringstudy;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Corpus implements Serializable {
    private String path;
    private Set<String> analyzedClasses = new HashSet<>();
    private Set<String> finishedProjects = new HashSet<>();

    public Corpus(String path) {
        this.path = path;
    }

    public void analyze(Question question, Progress progress) throws IOException {
        Files.list(Path.of(path)).filter(Files::isDirectory).sorted().forEach(projectPath -> {
            String projectName = projectPath.getFileName().toString();
            if (finishedProjects.contains(projectName))
                return;

            Path source = projectPath.resolve("source");
            Path dependencies = projectPath.resolve("dependencies");

            try {
                Project project = new Project(source, dependencies, this);
                project.analyze(question);
            } catch (IOException e) {
                e.printStackTrace();
            }

            finishedProjects.add(projectName);
            progress.save();
        });
    }

    public String getPath() {
        return path;
    }

    public boolean registerClass(String name) {
        return analyzedClasses.add(name);
    }
}
