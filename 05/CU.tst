// TEST CU

load CU.hdl,
output-file CU.out,
compare-to CU.cmp,
output-list type%B1.1.1 address%B1.15.1 a%B1.1.1 comp%B1.6.1 dest%B1.3.1 
            writeM%B1.1.1 loadA%B1.1.1 loadD%B1.1.1 selAM%B1.1.1
            selInA%B1.1.1 aluCtl%B1.6.1;

// C instructions
set type 1,
set address %B000000000000000;

// compute D=D-1
set a 0,
set comp %B001110,
set dest %B010,
eval,
output;

// compute D|M;JGT
set a 1,
set comp %B010101,
set dest %B000,
eval,
output;

// compute AMD=-1
set a 0,
set comp %B111010,
set dest %B111,
eval,
output;

// A instruction
set type 0,
set address %B000000000000111,
set comp %B000000,
set dest %B000,
eval,
output;

set type 1,
set address %B000000000000000;

// MD=M+1
set a 1,
set comp %B110111,
set dest %B011,
eval,
output;

// 0;JMP
set a 0,
set comp %B000000,
set dest %B000,
eval,
output;
