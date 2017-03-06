<h2>Prerequisites:</h2>

- Java JDK 1.8

<h2>Description</h2>

This program computes the product of 2 matrices, using two different algorithms (SimpleMatrixMultiply and AdvancedMatrixMultiply).

It takes the path to a JSON file containing 3 properties:
 - `height` (height of the first matrix),
 - `width` (width of the first matrix),
 - `seed` (seed used to generate random numbers) .
 
 Then generates two matrices, fills them with random number using the provided `seed`, with bounds [-1000000;1000000], and finally computes the product of the two matrices.

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

Results can be checked on (easier using matrices-10-20.json): http://wims.unice.fr/wims/wims.cgi?session=3ICB518C69.3&+lang=en&+module=tool%2Flinear%2Fmatmult

<h2>License</h2>

This software is under the [MIT License](https://opensource.org/licenses/MIT) (Copyright 2017, Adrien HARNAY).
