
(declare-sort Universe )
(declare-sort Type )

(declare-fun ty (Universe) Type)
(declare-fun ty.bool () Type)
(declare-fun ty.int () Type)

(declare-fun termTrue () Universe)
(declare-fun termFalse () Universe)

(declare-fun u2i (Universe) Int)
(declare-fun i2u (Int) Universe)

(declare-fun u2b (Universe) Bool)
(declare-fun b2u (Bool) Universe)
 
(assert
 (forall ((?x Int)) (= ?x (u2i (i2u ?x)))))
  
(assert
  (forall ((?x Int) (?y Int)) (implies (= (i2u ?x) (i2u ?y)) (= ?x ?y))))
  
; used to have "iff" here which was wrong!
(assert
  (forall 
   ((?x Universe))
   (implies 
    (= (ty ?x) ty.int) 
    (exists ((?y Int)) (= (i2u ?y) ?x)))))

; only true and false are boolean values
(assert
 (forall 
  ((?x Universe)) 
  (implies 
   (= (ty ?x) ty.bool) 
   (or 
    (= ?x termTrue) 
    (= ?x termFalse))
   )))

(assert (= (b2u true) termTrue))
(assert (= (b2u false) termFalse))
(assert (= (u2b termTrue) true))
(assert (= (u2b termFalse) false))

(assert
  (forall 
   ((?x Universe))
   (implies 
    (= (ty ?x) ty.bool) 
    (or (= ?x termTrue)
	(= ?x termFalse)))))

; --- end of preamble

