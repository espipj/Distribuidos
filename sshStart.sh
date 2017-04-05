#!/bin/bash

# ./sshStart i0901995 172.20.2.111 172.20.2.14 172.20.2.15

<<<<<<< HEAD
projectDir="/home/$1/Z/Distribuidos/PractObligatoria/"
=======
projectDir="~/Z/Distribuidos/PractObligatoria/"
>>>>>>> 9c2539209e020ebf5bd2662d0e6ecd68950cf2a7
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
<<<<<<< HEAD
ssh $1@$3 mkdir /home/$1/tomcat && cp -R "$tomcat/*" "/home/$1/tomcat/" && "$tomcat/bin/shutdown.sh" && sleep 5 && "$tomcat/bin/startup.sh"
ssh $1@$4 mkdir /home/$1/tomcat && cp -R "$tomcat/*" "/home/$1/tomcat/" && "$tomcat/bin/shutdown.sh" && sleep 5 && "$tomcat/bin/startup.sh"
=======
ssh $1@$3 mkdir ~/tomcat && cp -R "$tomcat/*" "~/tomcat/" && "$tomcat/bin/shutdown.sh" && sleep 5 && "$tomcat/bin/startup.sh"
ssh $1@$4 mkdir ~/tomcat && cp -R "$tomcat/*" "~/tomcat/" && "$tomcat/bin/shutdown.sh" && sleep 5 && "$tomcat/bin/startup.sh"
>>>>>>> 9c2539209e020ebf5bd2662d0e6ecd68950cf2a7

# We launch our program in main server
sleep 1
curl -v http://localhost:8080/Distribuidos/despachador/inicializar?maquina=0\&ip1=$2\&ip2=$3\&ip3=$4
