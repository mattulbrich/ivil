
include "$int.p"
        "$set.p"
        
sort
  seq('a)

function
  'a seqGet(seq('a), int)
  int seqLen(seq('a))
  int seqIndexOf(seq('a), 'a)
  'a seqError
  
binder
  (* primary constructor *)
  seq('a) (\seqDef int; int; int; 'a)

function
  (* secondary constructors *)
  seq('a) seqEmpty
  seq('a) seqSingleton('a)
  seq('a) seqConcat(seq('a), seq('a))
  seq('a) seqSub(seq('a), int, int)
  set('a) seqAsSet(seq('a))
  
#    Seq seqReverse(Seq);

rule seqGetDef
  find seqGet((\seqDef %i; %a; %b; %t), %j)
  replace cond(%a <= %j & %j < %b, 
           $$subst(%i, %j+%a, %t),
           seqError)

rule seqLenDef
  find seqLen((\seqDef %i; %a; %b; %t))
  replace cond(%a <= %b, %b-%a, 0)

(*
 * Now the derived constructors
 *)
rule seqEmptyDef
  find seqEmpty as seq(%'a)
  replace (\seqDef x; 0; 0; arb as %'a)

rule seqSubDef
  find seqSub(%a, %from, %to)
  where freshVar %x, %from, %to, %a
  replace (\seqDef %x; %from; %to; seqGet(%a, %x))

rule seqAsSetDef
  find seqAsSet(%s)
  where freshVar %x, %s
  where freshVar %i, %s
  replace (\set %x; (\exists %i; 0<=%i & %i<seqLen(%s) & 
                          seqGet(%s,%i)=%x))

# TO BE PROVED
rule seqLenOfSub
  find seqLen(seqSub(%a, %from, %to))
  replace cond(%from <= %to, %to-%from, 0)
  tags derived

# proved correct
rule seqGetOfSub
  find seqGet(seqSub(%a, %from, %to), %i)
  replace cond(%from <= %i & %i < %to, seqGet(%a, %i+%from), seqError)
  tags derived 

# proved correct
rule inSeqAsSet
  find %x :: seqAsSet(%s)
  where freshVar %i, %s
  replace
    (\exists %i; 0<=%i & %i < seqLen(%s) & seqGet(%s, %i) = %x)
  tags
    derived

# proved correct
rule seqAsSetEmpty
  find seqAsSet(seqEmpty)
  replace emptyset
  tags derived