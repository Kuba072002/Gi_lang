grammar Gi_lang;

prog: stat*;

stat: read
    | print
    | stringConcat
    | globalAssign
    | func
    | assign
    | assignArr
    | assignString
    | if
    | repeat
    | for
    | functionExecStmt
    | struct
    | structAssign
    | structValueAssign;

globalAssign: 'global' ID '=' value ';';

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
       | valueFromStructProp
;

stringConcat: ID '=' stringValue '.concat(' stringValue ')' ';';
stringValue: STRING|ID;

value: ID | INT | REAL | arrValue | functionExec
//| structValue
;
arrValue: ID '[' INT ']';

print: PRINT '(' value ')'';';
read: READ '(' ID ')'';';

if: IF '(' ifCondition ')' '{' blockIf'}';
ifCondition: value condition value;
blockIf: stat*;
condition: (EQUAL | NOT_EQUAL | GREATER_EQ | LESSER_EQ | GREATER | LESSER);

repeat: 'range' '(' rangeValue ')' '{' blockLoop '}';
rangeValue: value;
blockLoop: stat*;

for: forHead '{' blockLoop '}';
forHead: 'for' ID 'in' ID;

func: type 'def' ID '(' params ')''{' blockFunc ret?'}';
blockFunc: stat*;
params:  (type ID ',')* (type ID)?;
ret: 'return' ID ';';

functionExecStmt: functionExec ';';
functionExec: ID '('functionExecParams')';
functionExecParams: (ID',')* (ID)?;

struct: 'struct' ID '{'blockStruct'}';
blockStruct: (type ID ',')* (type ID)?;
structAssign: ID '=' 'struct' ID ';';
structValueAssign: ID '.' structProp '=' value ';';
valueFromStructProp: ID '.' structProp;
structProp: ID;

type: 'int' | 'real';

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