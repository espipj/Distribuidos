cat ~/Z/Distribuidos/PractObligatoria/tiempos/0.log ~/Z/Distribuidos/PractObligatoria/tiempos/1.log ~/Z/Distribuidos/PractObligatoria/tiempos/2.log > ../tiempos/total.log
sort -k 3 ../tiempos/total.log > ../tiempos/totalSorted.log
javac Comprobador.java
java Comprobador totalSorted.log $1 $2 > ../tiempos/resultado.txt
