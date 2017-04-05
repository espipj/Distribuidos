#!/bin/bash

# ./sshStart i0901995 172.20.2.111 172.20.2.14 172.20.2.15

projectDir="~/Z/Distribuidos/PractObligatoria/"
warDir="Distribuidos/Distribuidos/Distribuidos.war"
tomcatDir="tomcat"
tomcat=$projectDir$tomcatDir

# Shutdown Tomcat server in main machine
"$tomcat/bin/shutdown.sh"
sleep 5
# We clean OLD logs
rm -r "$tomcat/logs/*"
# We copy the new War to the shared folder
cp -R "$projectDirDistribuidos$warDir" "$tomcat/webapps/Distribuidos.war"
# We run tomcat in main machine
"$tomcat/bin/startup.sh"

# We copy tomcat server to local machine, we shutdown the server and we run it again
ssh $1@$3 mkdir ~/tomcat && cp -R "$tomcat/*" "~/tomcat/" && "$tomcat/bin/shutdown.sh" && sleep 5 && "$tomcat/bin/startup.sh"
ssh $1@$4 mkdir ~/tomcat && cp -R "$tomcat/*" "~/tomcat/" && "$tomcat/bin/shutdown.sh" && sleep 5 && "$tomcat/bin/startup.sh"

# We launch our program in main server
sleep 1
curl -v http://localhost:8080/Distribuidos/despachador/inicializar?maquina=0\&ip1=$2\&ip2=$3\&ip3=$4
