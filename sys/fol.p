include
   "$proposition.p"

rule forall_right
  find  |- { (\forall %x; %b) }
  where
    newSkolem { %sk }
    subst {%subst} (*:= subst*) {%x} (* <- *) {%sk} (* in *) {%b}
  samegoal replace { %subst }

rule exists_right
  find  |- { (\exists %x; %b) }
  where
    subst {%subst} (*:= subst*) {%x} (* <- *) {%inst} (* in *) {%b}
  samegoal replace { %subst }

rule forall_left
  find { (\forall %x; %b) } |-
  where
    subst {%subst} {%x} {%inst} {%b}
  samegoal replace { %subst }

rule exists_left
  find  { (\exists %x; %b) } |-
  where
    newSkolem { %sk }
    subst {%subst} (*:= subst*) {%x} (* <- *) {%sk} (* in *) {%b}
  samegoal replace { %subst }


#rule andRight
#        find  |- {%b & %c}
#        replace {%b}
#        replace {%c}
