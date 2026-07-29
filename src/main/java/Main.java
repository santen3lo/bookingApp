import services.CommandManager;

import java.util.Scanner;
public class Main {
    public static void main(String[] args){
        CommandManager commandManager = new CommandManager(new Scanner(System.in), args);
        commandManager.manage();
    }
}