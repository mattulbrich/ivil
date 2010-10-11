;
; Have extra sorts for boolean terms
;   taken from microsoft examples
;

:extrasorts ( Universe )
:extrasorts ( Type )

:extrafuns (
  ( ty Universe Type )
  
  ( ty.bool Type )
  ( ty.int Type )
  
  ( termTrue Universe )
  ( termFalse Universe )
  
  ( u2i Universe Int )
  ( i2u Int Universe)
)  

:assumption
  (distinct termFalse termTrue)

:assumption
  (forall 
    (?x Universe) 
    (implies 
      (= (ty ?x) ty.bool) 
      (or 
        (= ?x termTrue) 
        (= ?x termFalse))
  )) 
  
:assumption
  (forall (?x Int) (= ?x (u2i (i2u ?x))))
  
:assumption
  (forall ((?x Int) (?y Int)) (implies (= (i2u ?x) (i2u ?y)) (= ?x ?y)))
  
:assumption
  (forall (?x Universe) (iff (= (ty ?x) ty.int) (exists (?y Int) (= ?y (u2i ?x)))))

