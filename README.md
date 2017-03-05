Usage:

- Windows
```
javac -cp "src;libs/*" -d "out" src/*.java
java -cp "out;libs/*" SimpleMatrixMultiply resources/matrices-10-20-and-20-10.json
```

- Linux
```
javac -cp "src:libs/*" -d "out" src/*.java
java -cp "out:libs/*" SimpleMatrixMultiply resources/matrices-10-20-and-20-10.json
```

List of files to test on:
- matrices-2-4-and-4-2.json
- matrices-4-4-and-4-4.json
- matrices-10-20-and-20-10.json
