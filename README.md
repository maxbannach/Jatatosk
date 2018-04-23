# Jatatosk
A lightweight model-checker for a small fragment of MSO.

# Build Jatatosk
Simply execute the following commands to build Jatatosk. You may want to manually get a newer version of [Jdrasil](https://maxbannach.github.io/Jdrasil/), though.
```
mkdir bin
javac -cp libs/Jdrasil.jar:src -d bin src/*.java
```

# Run Jatatosk
Jatatosk expects a file that contains a vocabulary, a logical structure, as well as formula as input. The formula has to be from a small fragment of monadic second-order logic.
A detailed description of the supported fragment, as well as a precise specification of the input format, can be found in the [manual](https://github.com/maxbannach/Jatatosk/raw/master/manual.pdf).
If an instance is specified in a file `example.mso`, Jatatosk can be executed with the following command:
```
java -cp libs/Jdrasil.jar:bin/ Main < example.mso
```