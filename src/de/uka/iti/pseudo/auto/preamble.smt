;
; Have extra sorts for boolean terms
;   taken from microsoft examples
;

:extrasorts ( Bool )

:extrafuns (
  ( termTrue Bool )
  ( termFalse Bool ))

:assumption
  (distinct termFalse termTrue)