JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = Stego.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

