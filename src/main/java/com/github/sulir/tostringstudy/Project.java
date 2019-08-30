package com.github.sulir.tostringstudy;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Project {
    private Path source;
    private String[] dependencies;
    private Corpus corpus;
    private static final String BLACKLIST = "/jspwiki/source/deps/org/apache/lucene/analysis/standard/UAX29URLEmailTokenizerImpl.java";

    public Project(Path source, Path dependencies, Corpus corpus) throws IOException {
        this.source = source;
        this.dependencies = Files.list(dependencies).map(Path::toString).toArray(String[]::new);
        this.corpus = corpus;
    }

    public void analyze(Question question) throws IOException {
        question.analyze(this);

        Files.walk(source).sorted().forEach(file -> {
            String pathName = file.toString().replace(File.separator, "/");

            if (pathName.endsWith(".java") && !pathName.contains("/test/resources") && !pathName.endsWith(BLACKLIST)) {
                CtModel model = buildModel(file, dependencies);

                if (model != null)
                    analyzeModel(file, model, question);
            }
        });
    }

    public String[] getDependencies() {
        return dependencies;
    }

    public Corpus getCorpus() {
        return corpus;
    }

    private CtModel buildModel(Path file, String[] jars) {
        Launcher launcher = buildLauncher(file, jars, true);

        try {
            return launcher.buildModel();
        } catch (Exception e) {
            try {
                launcher = buildLauncher(file, jars, false);
                return launcher.buildModel();
            } catch (Exception ex) {
                System.err.println(ex.toString() + "\n  in file " + file);
                return null;
            }
        }
    }

    private Launcher buildLauncher(Path file, String[] jars, boolean fullClasspath) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setComplianceLevel(10);
        launcher.getEnvironment().setNoClasspath(!fullClasspath);

        launcher.addInputResource(file.toString());
        launcher.getEnvironment().setSourceClasspath(jars);
        return launcher;
    }

    private void analyzeModel(Path file, CtModel model, Question question) {
        CtType[] types = model.getAllTypes().toArray(new CtType[0]);
        if (types.length == 0 || !corpus.registerClass(types[0].getQualifiedName()))
            return;

        question.analyze(file, model);

        for (CtClass ctClass : model.getElements(new TypeFilter<>(CtClass.class))) {
            ProjectClass projectClass = null;

            try {
                projectClass = new ProjectClass(ctClass);
            } catch (Exception e) {
                System.err.println(e.toString() + "\n  in class " + ctClass.getQualifiedName());
            }

            if (projectClass != null)
                projectClass.analyze(question);
        }
    }
}
