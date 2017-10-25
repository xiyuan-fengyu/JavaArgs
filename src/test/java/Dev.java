import com.xiyuan.args.Args;
import com.xiyuan.args.ArgsExp;

public class Dev extends Args {

    @ArgsExp(exp = "test [-f]")
    public void test(boolean _f) {
        System.out.println("_f = [" + _f + "]");
    }

    public static void main(String[] args) {
        Dev dev = new Dev();
        dev.execute(new String[]{"test"});
        dev.execute(new String[]{"test", "-f"});
        dev.execute("test");
        dev.execute("test -f");
    }

}