cat ~/tiempos/0.log ~/tiempos/1.log ~/tiempos/2.log > ~/tiempos/total.log
sort -k 3 ~/tiempos/total.log > ~/tiempos/totalSorted.log
javac Comprobador.java -d ~/tiempos/
echo $1
echo $2
java ~/tiempos/Comprobador ~/tiempos/totalSorted.log $1 $2 > ~/tiempos/resultado.txt
