<h2>Prerequisites:</h2>

- Java JDK 1.8

<h2>Dependencies:</h2>

- [JSON-Java](https://github.com/stleary/JSON-java)

<h2>Description</h2>

This program computes the product of 2 matrices, using two different algorithms (SimpleMatrixMultiply, a naïve algorithm, and AdvancedMatrixMultiply, implementing the [Strassen algorithm](https://en.wikipedia.org/wiki/Strassen_algorithm)).

It takes the path to a JSON file containing 5 properties:
 - `height1` (height of the first matrix),
 - `width1` (width of the first matrix),
 - `height2` (height of the second matrix),
 - `width2` (width of the second matrix),
 - `seed` (seed used to generate random numbers).
 
 Then generates two matrices, fills them with random number using the provided `seed`, with bounds [-1000000;1000000], and finally computes the product of the two matrices in both ways (if possible).

<h2>Usage:</h2>

- Windows
```
javac -cp "src;libs/*" -d "out" src/*.java
java -Xmx8192m  -cp "out;libs/*" SimpleMatrixMultiply resources/matrices-4096-4095-4095-4096.json
java -Xmx8192m -cp "out;libs/*" AdvancedMatrixMultiply resources/matrices-4096-4095-4095-4096.json
```

- Linux
```
javac -cp "src:libs/*" -d "out" src/*.java
java -Xmx8192m  -cp "out:libs/*" SimpleMatrixMultiply resources/matrices-4096-4095-4095-4096.json
java -Xmx8192m -cp "out:libs/*" AdvancedMatrixMultiply resources/matrices-4096-4095-4095-4096.json
```

Note: all tests have been executed using `-Xmx8192m` (or `-Xmx8192m` for computers that don't have 8Go of RAM), allocating 8Go of heap size to the JVM.

Note on AdvancedMatrixMultiply: computing matrices with a side size of more than 4096 requires more than 8Go of RAM, in order not to exceed the JVM heap size.

<hr />

List of files to test on (in resources/):
- matrices-1-1-1-1.json
- matrices-5-2-20-5.json
- matrices-9-9-9-9.json
- matrices-15-10-10-5.json
- matrices-2000-3000-3000-2000.json
- matrices-3001-2001-2001-3001.json
- matrices-4000-4000-4000-4000.json
- matrices-4096-4095-4095-4096.json

Running the program with `-v` displays the 2 generated matrices and the 2 results `m1 * m2` and `m2 * m1`

Results can be checked on (easier using the 4 first JSON files): http://wims.unice.fr/wims/wims.cgi?session=3ICB518C69.3&+lang=en&+module=tool%2Flinear%2Fmatmult

<h2>License</h2>

This software is under the [MIT License](https://opensource.org/licenses/MIT) (Copyright 2017, Adrien HARNAY).
