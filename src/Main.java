import java.util.Scanner;
public class Main {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        Order order = new Order();
        while(true){
            String[] in = sc.next().split(" ");
            if (in[0].equals("book_cancel")){

            } else if (in[0].equals("book_create")) {

            } else if (in[0].equals("book_list")) {

            } else if (in[0].equals("checkout_take")) {

            } else if (in[0].equals("checkout_return")) {

            } else if (in[0].equals("checkout_list")) {

            } else if (in[0].equals("inst_available")) {

            } else if (in[0].equals("book_show")) {

            } else if (in[0].equals("checkout_show")) {

            } else if (in[0].equals("book_reschedule")) {

            } else if (in[0].equals("stop")) {
                break;
            } else {

            }

        }


    }
}