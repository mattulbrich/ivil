plugin
  metaFunction : "de.uka.iti.pseudo.rule.meta.RefinementModificationMetaFunction"

properties
  skipmark.refinement "MARK"

function
  int MARK unique

rule refinement
  find |- [%C][<%A>]%phi
  replace $$refinementPrgMod([%C][<%A>]%phi)


