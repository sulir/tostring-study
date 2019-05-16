package com.github.sulir.tostringstudy;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectClass {
    private static final String[] TEST_DIRS = {"/test/", "/tests/", "/src/it/", "/jtt/"};
    private CtClass<?> ctClass;
    private List<ProjectClass> hierarchy = new ArrayList<>();

    public ProjectClass(CtClass ctClass) throws ClassNotFoundException {
        this.ctClass = ctClass;
        loadTypeHierarchy();
    }

    public CtClass<?> getCtClass() {
        return ctClass;
    }

    public List<ProjectClass> getTypeHierarchy() {
        return hierarchy;
    }

    public void analyze(Question question) {
        question.analyze(this);

        CtMethod toString = ctClass.getMethod("toString");
        if (toString != null && !toString.isAbstract())
            question.analyze(toString);
    }

    public boolean isNormal() {
        return !ctClass.isEnum() && !ctClass.isAnonymous() && !ctClass.isAbstract() && !isTest();
    }

    public int whereInHierarchy(String methodName, CtTypeReference... parameters) {
        int level = 0;

        for (ProjectClass clazz : hierarchy) {
            CtMethod method = clazz.getCtClass().getMethod(methodName, parameters);
            if (method != null && !method.isAbstract())
                return level;

            level++;
        }

        return -1;
    }

    private void loadTypeHierarchy() throws ClassNotFoundException {
        hierarchy.add(this);

        CtTypeReference superclassReference = ctClass.getSuperclass();
        if (superclassReference == null)
            return;

        CtType superclass = superclassReference.getTypeDeclaration();

        // Spoon bug workaround
        if (superclass == null && superclassReference.getSimpleName().contains(".")) {
            ClassLoader classLoader = ctClass.getFactory().getEnvironment().getInputClassLoader();
            Class clazz = classLoader.loadClass(superclassReference.getSimpleName());
            superclass = ctClass.getFactory().Code().createCtTypeReference(clazz).getTypeDeclaration();
        }

        if (superclass == null)
            throw new ClassNotFoundException(superclassReference.getQualifiedName());

        if (superclass instanceof CtClass)
            hierarchy.addAll(new ProjectClass((CtClass) superclass).getTypeHierarchy());
    }

    private boolean isTest() {
        SourcePosition position = ctClass.getPosition();

        if (position.isValidPosition()) {
            String path = ctClass.getPosition().getFile().getPath().replace(File.separator, "/");
            return Arrays.stream(TEST_DIRS).anyMatch(path::contains);
        } else {
            return false;
        }
    }
}
