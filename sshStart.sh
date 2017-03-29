#!/bin/bash

# ./sshStart i0901995 172.20.2.14 172.20.2.15
~/Z/Distribuidos/PractObligatoria/tomcat/bin/shutdown.sh
sleep 5
cp -R ~/Z/Distribuidos/PractObligatoria/Distribuidos/Distribuidos/Distribuidos.war ~/Z/Distribuidos/PractObligatoria/tomcat/webapps/Distribuidos.war
~/Z/Distribuidos/PractObligatoria/tomcat/bin/startup.sh

ssh $1@$3 ~/Z/Distribuidos/PractObligatoria/tomcat/bin/shutdown.sh && sleep 5 && ~/Z/Distribuidos/PractObligatoria/tomcat/bin/startup.sh

ssh $1@$4 ~/Z/Distribuidos/PractObligatoria/tomcat/bin/shutdown.sh && sleep 5 && ~/Z/Distribuidos/PractObligatoria/tomcat/bin/startup.sh

curl -v http://localhost:8080/Distribuidos/despachador/inicializar?maquina=0\&ip1=$2\&ip2=$3\&ip3=$4
