# A study of string representations in Java

This repository contains the script and the program needed to reproduce the results in our paper [String Representations of Java Objects: An Empirical Study](https://sulir.github.io/papers/Sulir20string.pdf).

To build the source code corpus and save it to the given CORPUS_PATH directory, run:
```
./build-corpus.sh "CORPUS_PATH"
```

Then you can run the analysis program by:
```
./gradlew run --args="CORPUS_PATH"
```

The analysis results will be written to the standard output.
