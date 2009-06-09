#sort
#        bool   built in  
#        int    built in
        
function
        'a cond(bool, 'a, 'a)

function  # infixes
        bool $eq('a, 'a)        infix =  50
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
        'a (\some 'a; bool)
