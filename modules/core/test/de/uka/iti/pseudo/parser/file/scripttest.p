include
        "$base.p"
        "$int.p"

proof source
        "scripttest.proof"

plugins
   proofScriptCommand "de.uka.iti.pseudo.parser.file.TestProofScripts$MockProofScriptCommand"

rule r1
 find 1 
 replace 2 
proof (mock)

rule r2
 find 1
 replace 3

rule known_rule_proved_outside
 find 1
 replace 4

lemma p1 1=1

lemma p2 2=2
proof (mock)

program Q1
  assert 3=3

program Q2
  assert 4=4
proof (mock)

proof rule r2 (mock)
proof program Q1_total (mock)

(* a longer proof with arguments *)
proof lemma p1 
  (mock "" a:"a" b:b c "d";
   mock "0";
   mock "00"
     (mock "000" ; mock "0000")
     (mock "001" 
        (mock "0010")
        (mock "0011")
     )
  )

