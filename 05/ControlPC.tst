// TEST ControlPC

load ControlPC.hdl,
output-file ControlPC.out,
compare-to ControlPC.cmp,
output-list jump%B1.3.1 type%B1.1.1 zr%B1.1.1 ng%B1.1.1
            loadPC%B1.1.1 inc%B1.1.1;

// C instructions
set type 1;

// compute D=D-1
set jump %B000,
set zr %B0,
set ng %B1,
eval,
output;

// compute D|M;JGT
set jump %B001,
set zr %B0,
set ng %B1,
eval,
output;

// A instruction
set type 0,
set jump %B001,
set zr %B0,
set ng %B1,
eval,
output;

set type 1;

// 0;JMP
set jump %B111,
set zr %B1,
set ng %B0,
eval,
output;
