javac -cp .:../WebContent/WEB-INF/lib/* GraphGeneration.java GraphVisualization.java LT*.java RIS.java cmd.java
java -Xmx3g -cp .:../WebContent/WEB-INF/lib/* cmd
java -cp .:../WebContent/WEB-INF/lib/* GraphVisualization