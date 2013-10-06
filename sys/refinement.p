
#
# This belongs to the bytecode translation to ivil
#

include "$base.p"

plugin
  metaFunction : "de.uka.iti.pseudo.rule.meta.RefinementModificationMetaFunction"
  contextExtension : "de.uka.iti.pseudo.gui.extensions.RefinementExpansionExt"

properties
  skipmark.refinement "MARK"

function
  int MARK unique
  bool INITIAL_VAR('a)

rule refinement_variant
  find |- INITIAL_VAR(%V) -> [[%C]][<%A>]%phi
  replace $$refinementPrgMod([[%C]][<%A>]%phi, %V)

rule refinement_box
  find |- [%C][<%A>]%phi
  replace $$refinementPrgMod([%C][<%A>]%phi, false)

rule refinement_tbox
  find |- [[%A]]%irrel -> [[%C]][<%A>]%phi
  replace $$refinementPrgMod([[%C]][<%A>]%phi, false)

