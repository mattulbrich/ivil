
#
# This belongs to the bytecode translation to ivil
#

plugin
  metaFunction : "de.uka.iti.pseudo.rule.meta.RefinementModificationMetaFunction"
  contextExtension : "de.uka.iti.pseudo.gui.extensions.RefinementExpansionExt"

properties
  skipmark.refinement "MARK"

function
  int MARK unique
  bool INITIAL_VAR('a)

rule refinement
  find |- INITIAL_VAR(%V) -> [%C][<%A>]%phi
  replace $$refinementPrgMod([%C][<%A>]%phi, %V)


