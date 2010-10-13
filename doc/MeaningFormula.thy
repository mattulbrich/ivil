theory meaning
imports HOL
begin

theorem "(ALL f rw1 rw2 add1 add2 ctx .
   ((f=rw1 --> add1) & (f=rw2 --> add2) --> ctx))
   =
   (ALL phi f rw1 rw2 add1 add2 ctx.
   (((phi rw1) | add1) & ((phi rw2) | add2) --> ctx))"
apply simp
apply blast
done

lemma "! phi . (!q . phi(q)-->q) = (!q . phi(False) --> q)"
apply simp_all
apply auto
apply (case_tac q)
apply auto
done
