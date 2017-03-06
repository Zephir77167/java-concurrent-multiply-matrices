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

<h2>License</h2>

Copyright 2017 Adrien HARNAY

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
