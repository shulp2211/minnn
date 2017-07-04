grammar CommandLineParser;

commandLine : ('parse' parseOption+) | ('filter' FILE_NAME+ filterOutput+) ;

parseOption : OPTION (ID | STRING | FILE_NAME)* ;
filterOutput : OPTION (EXPRESSION | STRING) FILE_NAME+? ;

OPTION : '--' [a-z]* ;
STRING : '"' ('""'|~'"')* '"' ;
FILE_NAME : [0-9a-zA-Z_\-.]+ ;
EXPRESSION : [A-Za-z]+ '=' [A-Za-z]+ ;
ID : [0-9a-zA-Z]+ ;
WS : [ \t\n\r]+ -> skip ;
