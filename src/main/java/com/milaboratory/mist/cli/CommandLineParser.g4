grammar CommandLineParser;

commandLine : COMMAND OPTION+ ;

COMMAND : [a-z]+ ;
OPTION : '-' COMMAND STRING ;
STRING : '"' ('""'|~'"')* '"' ;
WS : [ \t\n\r]+ -> skip ;
