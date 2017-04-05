#!/bin/bash

# ./sshStart i0901995 172.20.2.111 172.20.2.14 172.20.2.15

projectDir="/home/$1/Z/Distribuidos/PractObligatoria/"

warDir="Distribuidos/Distribuidos/Distribuidos.war"
tomcatDir="tomcat"
tomcat=$projectDir$tomcatDir

# We clean OLD logs
rm -r $tomcat/logs/*
# We copy the new War to the shared folder
cp $projectDir$warDir $tomcat/webapps/Distribuidos.war
# We run tomcat in main machine

# We copy tomcat server to local machine, we shutdown the server and we run it again
"mkdir -p /home/$1/tomcat && cp -a $tomcat/. /home/$1/tomcat/ && /home/$1/tomcat/bin/shutdown.sh && sleep 2 && /home/$1/tomcat/bin/startup.sh"
ssh $1@$3 "mkdir -p /home/$1/tomcat && cp -a $tomcat/. /home/$1/tomcat/ && /home/$1/tomcat/bin/shutdown.sh && sleep 2 && /home/$1/tomcat/bin/startup.sh"
ssh $1@$4 "mkdir -p /home/$1/tomcat && cp -a $tomcat/. /home/$1/tomcat/ && /home/$1/tomcat/bin/shutdown.sh && sleep 2 && /home/$1/tomcat/bin/startup.sh"

# We launch our program in main server
sleep 1
curl -v http://localhost:8080/Distribuidos/despachador/inicializar?maquina=0\&ip1=$2\&ip2=$3\&ip3=$4
