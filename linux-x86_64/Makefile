CC = cc
SRC = fonts.c
OBJ = $(patsubst %.c, target/%.o, $(SRC))

default: target/classes/linux-x86_64/libasciiart-fonts.so

clean:
	rm -rf target

target/classes/linux-x86_64:
	mkdir -p target/classes/linux-x86_64

target/%.o : ../libasciiart-fonts/src/%.c target/classes/linux-x86_64
	$(CC) $(CFLAGS) -Wall -std=c99 -fPIC -g -c $< -o $@ -I../libasciiart-fonts/include -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

target/classes/linux-x86_64/libasciiart-fonts.so: $(OBJ)
	$(CC) $(CFLAGS) -shared $(OBJ) -o $@ $(LDFLAGS) -Wl,--no-as-needed -ldl -lm
