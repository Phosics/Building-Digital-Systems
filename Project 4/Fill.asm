// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

	@SCREEN
	D = A
	@startAddress	
	M = D		// Pointing to the start of the screen
	@KBD
	D = A
	D = D - 1
	@endAddress
	M = D	// Pointing to the end of the screen
(LOOP)
	@startAddress
	D = M
	@address
	M = D		// Making the address pointing to the screen
	
	@KBD
	D = M		// Getting if a key was pressed

	@WHITE
	D ;JEQ		// If no key is pressed - make the screen WHITE
	@BLACK
	D ;JNE		// If a key is pressed - make the screen BLACK
(WHITE)
	@address
	D = M
	@endAddress
	D = M - D
	@LOOP
	D ;JLT		// Jumping if got to the end of the screen
	
	@address
	AD = M
	M = 0		// Making the pixel WHITE
	
	@address
	M = D + 1	// Getting the next address
	
	@WHITE
	0;JMP
(BLACK)
	@address
	D = M
	@endAddress
	D = M - D
	@LOOP
	D ;JLT		// Jumping if got to the end of the screen
	
	@address
	AD = M
	M = -1		// Making the pixel BLACK
	
	@address
	M = D + 1	// Getting the next address
	
	@BLACK
	0;JMP