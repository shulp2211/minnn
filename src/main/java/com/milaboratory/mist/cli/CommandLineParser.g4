grammar CommandLineParser;

commandLine : COMMAND option+ ;

option : OPTION STRING;

OPTION: '--' COMMAND ;
COMMAND : [a-z]+ ;
STRING : '"' ('""'|~'"')* '"' ;
WS : [ \t\n\r]+ -> skip ;
