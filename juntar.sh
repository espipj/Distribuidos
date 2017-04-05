cat ~/Z/Distribuidos/PractObligatoria/tiempos/0.log ~/Z/Distribuidos/PractObligatoria/tiempos/1.log ~/Z/Distribuidos/PractObligatoria/tiempos/2.log > total.log
sort -k 3 total.log > totalSorted.log
javac Comprobador.java
java Comprobador totalSorted.log $1 $2 > resultado.txt
