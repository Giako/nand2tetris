// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// i=1, sum=0
    @i
    M=1
    @sum
    M=0

(LOOP)
// if i > R1 end
    @i
    D=M
    @R1
    D=D-M
    @END
    D;JGT
// sum = sum + R0
    @R0
    D=M
    @sum
    M=M+D
// i++
    @i
    M=M+1
// goto loop
    @LOOP
    0;JMP

// end
(END)
    @sum
    D=M
    @R2
    M=D
    @END
    0;JMP
