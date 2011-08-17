function f<a>(x:int, y:a):int{ 0 }

procedure T()
{
	var x : int;
	
	assert x == 0 ==> x == f(x, x);
}
