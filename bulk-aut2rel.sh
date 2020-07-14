#!/bin/bash
for entry in "$1"/*
do
  if [ -f "$entry" ];then
    java -cp relevance.jar org.jbpt.relevance.Relevance "$entry" "$2" >> "$3"
    echo "$entry"
  fi
done
