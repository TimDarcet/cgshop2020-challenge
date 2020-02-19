JC = javac
J = java
CPC = "../lib/*"
CP = "../lib/*:../class"
COUTDIR = ../class

all: GraphViewer

compile: *.java
	$(JC) -d $(COUTDIR) -cp $(CPC) *.java

GraphViewer: compile
	$(J) -cp $(CP) GraphViewer $(TARGET)

clean:
	rm class/*
