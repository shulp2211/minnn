grammar FilterGrammar;

filter : EXPRESSION ;

STRING : '"' ('""'|~'"')* '"' ;
FILE_NAME : [0-9a-zA-Z_\-.]+ ;
EXPRESSION : [A-Za-z]+ '=' [A-Za-z]+ ;
ID : [0-9a-zA-Z]+ ;
WS : [ \t\n\r]+ -> skip ;
