var f:<a>[int, a]int where (forall <a> x:int, y:a :: f[x,y] == 0);
function f<a>(x:int, y:a):int{ 0 }

procedure T()
{
	assert (forall <a> x:int, y:a :: f[x,y] == f(x,y));
}
