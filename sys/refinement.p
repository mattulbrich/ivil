plugin
  metaFunction : "de.uka.iti.pseudo.rule.meta.RefinementModificationMetaFunction"
  contextExtension : "de.uka.iti.pseudo.gui.extensions.RefinementExpansionExt"

properties
  skipmark.refinement "MARK"

function
  int MARK unique

rule refinement
  find |- [%C][<%A>]%phi
  replace $$refinementPrgMod([%C][<%A>]%phi)


