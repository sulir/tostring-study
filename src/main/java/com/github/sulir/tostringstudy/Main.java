package com.github.sulir.tostringstudy;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import spoon.Launcher;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: tostringstudy corpus_path");
            System.exit(1);
        }

        Logger.getLogger(Launcher.class).setLevel(Level.INFO);

        Progress progress = Progress.load(args[0]);
        Corpus corpus = progress.getCorpus();
        Question questions = progress.getQuestions();

        System.out.println("ANALYZING:");
        corpus.analyze(questions, progress);

        System.out.println("\nRESULTS:");
        System.out.println(questions.getAnswer());
    }
}
