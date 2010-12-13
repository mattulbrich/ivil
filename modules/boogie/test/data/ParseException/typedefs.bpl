type ref target;
type heap T = <T>[(ref T) int]T;

var memory:heap bool;

function loadBool(addr:int):int { memory[addr] }
