include "$ivil.p"

function
  'a f('a)

# to express injectivity of f, we need type quantification and weakly typed equality:
problem (\T_all 'a; (\forall x as 'a; (\T_all 'b; (\forall y as 'b; $weq(f(x),f(y)) -> $weq(x,y))))) -> !f(true) = f(false)
