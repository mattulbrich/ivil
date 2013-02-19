
(declare-sort Universe)
(declare-sort Type)

(declare-fun ty (Universe Type) Bool)
(declare-fun ty.bool () Type)
(declare-fun ty.int () Type)

(declare-fun termTrue () Universe)
(declare-fun termFalse () Universe)

(declare-fun unique (Universe) Int)

(declare-fun u2i (Universe) Int)
(declare-fun i2u (Int) Universe)

(declare-fun u2b (Universe) Bool)
(declare-fun b2u (Bool) Universe)

; Typing ty is a total function.
; Yet, we only axiomatize only the partial function aspect.
(assert
 (forall ((?x Universe) (?t1 Type) (?t2 Type))
  (! 
   (implies (and (ty ?x ?t1) (ty ?x ?t2))
     (= ?t1 ?t2))
   :pattern ((ty ?x ?t1) (ty ?x ?t2)))))

; from int to universe and back
(assert
 (forall ((?x Int)) (! (= ?x (u2i (i2u ?x))) :pattern ((u2i (i2u ?x))) )))
  
(assert
  (forall ((?x Int) (?y Int)) 
   (!
    (implies (= (i2u ?x) (i2u ?y)) (= ?x ?y))
    :pattern ((i2u ?x) (i2u ?y)) )))

; used to have "iff" here which was wrong!
; TODO pattern?
(assert
  (forall 
   ((?x Universe))
    (implies 
     (ty ?x ty.int) 
     (exists ((?y Int)) (= (i2u ?y) ?x)))
    ))

; typing of u2i
(assert
  (forall
   ((?n Int))
   (!
    (ty (i2u ?n) ty.int)
    :pattern ((i2u ?n)) )))

; only true and false are boolean values
(assert
 (forall 
  ((?x Universe)) 
  (implies 
   (ty ?x ty.bool) 
   (or 
    (= ?x termTrue) 
    (= ?x termFalse))
   )))

(assert (= (b2u true) termTrue))
(assert (= (b2u false) termFalse))
(assert (= (u2b termTrue) true))
(assert (= (u2b termFalse) false))

(assert (ty termFalse ty.bool))
(assert (ty termTrue ty.bool))

; --- end of preamble

