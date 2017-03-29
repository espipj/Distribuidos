#!/bin/bash

cp -R ~/Z/Distribuidos/PractObligatoria/Distribuidos/Distribuidos/Distribuidos.war ~/Z/Distribuidos/PractObligatoria/tomcat/webapps/Distribuidos.war
~/Z/Distribuidos/PractObligatoria/tomcat/bin/startup.sh

ssh $1@$2 cp -R ~/Z/Distribuidos/PractObligatoria/Distribuidos/Distribuidos/Distribuidos.war ~/Z/Distribuidos/PractObligatoria/tomcat/webapps/Distribuidos.war && ~/Z/Distribuidos/PractObligatoria/tomcat/bin/startup.sh

ssh $1@$3 cp -R ~/Z/Distribuidos/PractObligatoria/Distribuidos/Distribuidos/Distribuidos.war ~/Z/Distribuidos/PractObligatoria/tomcat/webapps/Distribuidos.war && ~/Z/Distribuidos/PractObligatoria/tomcat/bin/startup.sh

curl -v http://localhost:8080/Distribuidos/despachador/inicializar?maquina=0&ip2=$2&ip3=$3
