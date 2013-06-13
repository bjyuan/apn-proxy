#!/bin/bash
if [ -f pid ]
then
	pid=`cat pid`
	kill $pid
	rm pid
fi