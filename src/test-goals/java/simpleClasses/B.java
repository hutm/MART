package simpleClasses;

/**
 * @version 1.0 23.03.2009 17:25:00
 * @author: Maksim Khadkevich
 */
public class B extends A{
    public B(int number){
        super(number);
    }

    protected void printNumber(int number){
        System.out.println("B");
    }


    public static void main(String[] args) {
        B b= new B(2);
    }
}
