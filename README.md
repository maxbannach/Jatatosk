# Jatatosk
A lightweight model-checker for a small fragment of MSO.

# Build and Run
Simply execute the following commands to build and run Jatatosk. You may want to manually get a newer version of [Jdrasil](https://maxbannach.github.io/Jdrasil/), though.
```
mkdir bin
javac -cp libs/Jdrasil.jar:src -d bin src/*.java
java -cp libs/Jdrasil.jar:bin/ Main < example.mso
```