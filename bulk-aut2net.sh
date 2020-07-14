#!/bin/bash
for entry in "$1"/*
do
  if [ -f "$entry" ];then
    java -cp relevance.jar org.jbpt.relevance.Aut2PN "$entry" "$2"
    echo "$entry"
  fi
done
