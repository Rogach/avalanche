#!/bin/bash

sbt assembly && rsync -avz --progress -e "ssh -o GSSAPIAuthentication=no -p 2222" target/scala-2.11/avalanche.jar platon@meg.mutokukai.ru:/data/programs/platon/bin/avalanche.jar
