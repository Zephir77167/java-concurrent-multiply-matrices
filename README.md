Usage:

- Windows
```
javac -cp "src;libs/*" -d "out" src/*.java
java -cp "out;libs/*" SimpleMatrixMultiply resources/matrices-2000-3000.json
```

- Linux
```
javac -cp "src:libs/*" -d "out" src/*.java
java -cp "out:libs/*" SimpleMatrixMultiply resources/matrices-2000-3000.json
```

List of files to test on (in resources/):
- matrices-2-4.json
- matrices-4-4.json
- matrices-10-20.json
- matrices-2000-3000.json

Running the program with `-v` displays the 2 generated matrices and the 2 results `m1 * m2` and `m2 * m1`

Results can be checked on: http://wims.unice.fr/wims/wims.cgi?session=3ICB518C69.3&+lang=en&+module=tool%2Flinear%2Fmatmult
