#sort
#        bool   built in  
#        int    built in
        
function
		'a cond(bool, 'a, 'a)

function  # infixes
        int $div(int, int)      infix /  70 
        int $mult(int, int)     infix *  70 
        int $plus(int, int)     infix +  60 
        int $minus(int, int)    infix -  60 
        bool $eq('a, 'a)        infix =  50 
        bool $and(bool, bool)   infix &  40 
        bool $or(bool, bool)    infix |  30 
        bool $impl(bool, bool)  infix ->  20 
        bool $equiv(bool, bool) infix <->  10 
        
function  # prefixes
        int $neg(int)           prefix -
        bool $not(bool)         prefix !         

function  # consts
        bool true
        bool false

binder
        bool (\forall 'a; bool)
        bool (\exists 'a; bool)
        int (\sum int; int; int; int)
        'a (\some 'a; bool)

#rule forAllRight
#        find  |- { (\forall %x; %b) }
#        replace { (\subst %x; c; %b) }

#rule andRight
#        find  |- {%b & %c}
#        replace {%b}
#        replace {%c}
