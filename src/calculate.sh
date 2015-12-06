javac -cp .:libs/commons-math3-3.5.jar LT*.java GraphGen*.java Main.java
java -cp .:libs/commons-math3-3.5.jar Main yahoo/ydata-ymovies-user-movie-ratings-train-v1_0.txt LTC 50 100
