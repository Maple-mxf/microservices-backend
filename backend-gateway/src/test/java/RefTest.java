import java.util.HashMap;

public class RefTest {

  static class A {
    String name;
  }

  public static void main(String[] args) {
    A a = new A();
    a.name = "1";
    System.err.println(a.name);
  }

  void modifyA(A a) {
    a = new A();

   // curl https://raw.githubusercontent.com/Maple-mxf/microservices-backend/master/buildimage.sh -sL | bash -
    a.name = "CCCC";
  }
}
