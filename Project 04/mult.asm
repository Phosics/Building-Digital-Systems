// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.

	@R2
	M=0			// Making the location RAM[2] = 0
	@R1
	D=M			// Lodaing R1 into D
	@index
	M=D			// Lodaing R1 value into the parameter 'index'
(LOOP)
	@index
	D=M-1		// Lodaing the Memory value -1 into D
	M=D			// Updating the Memory
	@END
	D;JLT		// Jumping to END if D = 0
	
	@R0
	D=M			// Lodaing R0 into D
	@R2
	M=D+M		// R2 += D
	
	@LOOP
	0;JMP		// Jumping back to the start of the loop
(END)
	@END
	0;JMP