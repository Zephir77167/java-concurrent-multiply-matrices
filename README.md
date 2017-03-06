<h2>Prerequisites:</h2>

- Java JDK 1.8

<h2>Usage:</h2>

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

<hr />

List of files to test on (in resources/):
- matrices-10-20.json
- matrices-1000-1000.json
- matrices-1000-1500.json
- matrices-2000-2000.json
- matrices-2000-3000.json

Running the program with `-v` displays the 2 generated matrices and the 2 results `m1 * m2` and `m2 * m1`

Results can be checked on (easier using 10-20.json): http://wims.unice.fr/wims/wims.cgi?session=3ICB518C69.3&+lang=en&+module=tool%2Flinear%2Fmatmult
