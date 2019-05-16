package com.github.sulir.tostringstudy.questions;

import com.github.sulir.tostringstudy.ProjectClass;
import com.github.sulir.tostringstudy.Question;

public class Hierarchy extends Question {
    private int inClass;
    private int inSuperclass;
    private int inIndirectSuperclass;
    private int inRoot;

    public void analyze(ProjectClass clazz) {
        if (!clazz.isNormal())
            return;

        total++;

        switch (clazz.whereInHierarchy("toString")) {
            case -1:
                inRoot++;
                break;
            case 0:
                inClass++;
                break;
            case 1:
                inSuperclass++;
                break;
            default:
                inIndirectSuperclass++;
        }
    }

    @Override
    public String getAnswer() {
        return String.format("ToStrings in hierarchy:\n total: %d\n in class: %s\n" +
                " direct superclass: %s\n indirect superclass: %s\n in Object: %s",
                total, perCent(inClass),
                perCent(inSuperclass), perCent(inIndirectSuperclass), perCent(inRoot));
    }


}
