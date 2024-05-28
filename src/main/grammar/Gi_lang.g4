grammar Gi_lang;

prog: stat*;

stat: read
    | print
    | stringConcat
    | assign
    | assignArr
    | assignString
    | if
    | loop;

assign: ID '=' expr0 ';';
assignArr: ID '=' '{' ((INT|REAL)',')* (INT|REAL)? '}' ';';
assignString: ID '=' STRING ';';

expr0: expr1            #single0
    | expr1 ADD expr1   #add
    | expr1 SUB expr1   #sub
;

expr1:  expr2			    #single1
      | expr2 MUL expr2	    #mul
      | expr2 DIV expr2	    #div
;

expr2:   value
       | '(' expr0 ')'
;

stringConcat: ID '=' stringValue '.concat(' stringValue ')' ';';
stringValue: STRING|ID;

value: ID | INT | REAL | arrValue;
arrValue: ID '[' INT ']';

print: PRINT '(' value ')'';';
read: READ '(' ID ')'';';

if: IF '(' ifCondition ')' '{' blockIf'}';
ifCondition: value condition value;
blockIf: stat*;
condition: (EQUAL | NOT_EQUAL | GREATER_EQ | LESSER_EQ | GREATER | LESSER);

loop: 'range' '(' rangeValue ')' '{' blockLoop '}';
rangeValue: value;
blockLoop: stat*;

IF: 'if';
EQUAL: '==';
NOT_EQUAL: '!=';
GREATER_EQ: '>=';
LESSER_EQ: '<=';
GREATER: '>';
LESSER: '<';

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';

PRINT: 'print';
READ: 'read';
INT: [0-9]+;
REAL: INT '.' INT;
STRING :  '"' ( ~('\\'|'"') )* '"';
ID: [a-zA-Z0-9]+;
WS : [ \t\r\n]+ -> skip;