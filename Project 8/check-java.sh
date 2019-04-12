#!/bin/bash

if test $# -lt 1 ; then
    echo "Usage: check-java file.vm"
    exit 1
fi

javac *.java
java VMTranslator $1




