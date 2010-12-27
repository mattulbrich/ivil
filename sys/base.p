#
# This file is part of This file is part of
#    ivil - Interactive Verification on Intermediate Language
#
# Copyright (C) 2009 Universitaet Karlsruhe, Germany
#    written by Mattias Ulbrich
#
# The system is protected by the GNU General Public License.
# See LICENSE.TXT for details.
#

(*
 * This file contains the very basic definitions for the
 * system:
 *  - basic meta functions and where conditions as plugins (plugin.p)
 *  - propositional connectives
 *  - the conditional function (i.e. "if")
 *  - the first order quantifiers
 *  - the hilbert operator
 *)

include "$plugins.p"

function
    'a cond(bool, 'a, 'a)

function  # infixes
    bool $eq('a, 'b)        infix =  50
    bool $and(bool, bool)   infix &  40 
    bool $or(bool, bool)    infix |  30 
    bool $impl(bool, bool)  infix ->  20 
    bool $equiv(bool, bool) infix <->  10 
        
function  # prefixes
    bool $not(bool)         prefix ! 45      

function  # consts
    'a arb

binder
    bool (\forall 'a; bool)
    bool (\exists 'a; bool)
    'a   (\some 'a; bool)
