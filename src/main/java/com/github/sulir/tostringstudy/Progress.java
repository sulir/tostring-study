package com.github.sulir.tostringstudy;

import com.github.sulir.tostringstudy.questions.AllQuestions;

import java.io.*;
import java.nio.file.Path;

public class Progress {
    public static final String FILE_NAME = "progress.bin";

    private final Corpus corpus;
    private final Question questions;
    private final File file;

    private Progress(Corpus corpus, Question questions, File file) {
        this.corpus = corpus;
        this.questions = questions;
        this.file = file;
    }

    public Corpus getCorpus() {
        return corpus;
    }

    public Question getQuestions() {
        return questions;
    }

    public static Progress load(String directory) {
        Corpus corpus;
        Question questions;
        File file = Path.of(directory, FILE_NAME).toFile();

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            corpus = (Corpus) in.readObject();
            questions = (Question) in.readObject();
            System.out.println("Continuing an existing analysis.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Starting a new analysis.");
            corpus = new Corpus(directory);
            questions = new AllQuestions();
        }

        return new Progress(corpus, questions, file);
    }

    public void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(corpus);
            out.writeObject(questions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
