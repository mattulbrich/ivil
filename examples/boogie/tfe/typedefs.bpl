type ref target;
type heap = <T>[ref T]T;

var memory:heap;

function loadBool(addr:ref int):int { memory[addr] }
