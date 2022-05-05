# Minecraft Boolean Function Compiler

The thing to run is src/main/java/NorCircuit.java

Compiles bitwise functions into redstone schematics.
Also helps with pasting schematics into a Minecraft world,
should there be multiple schematic files generated.

Functions that can be compiled are written in a
programming-language style of input. There is
(as of now) no good way to specify dependencies
between input files.

Circuits to be compiled go in main/resources/circuits.
There are a few example circuits in there.

Sorry for the lack of documentation on things.

The goal is to output a .schematic file.
https://minecraft.fandom.com/wiki/Schematic_file_format
