package me.ferlo;

import com.google.inject.AbstractModule;

import java.io.PrintStream;
import java.util.Scanner;

public class CommandLineModule extends AbstractModule {
    @Override
    protected void configure() {
        super.configure();

        bind(Scanner.class).toInstance(new Scanner(System.in));
        bind(PrintStream.class).toInstance(System.out);
    }
}
