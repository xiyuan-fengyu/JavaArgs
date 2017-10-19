import com.xiyuan.args.Args;
import com.xiyuan.args.ArgsExp;

public class QuickStart extends Args{

    //ArgExp fromat:
    //commandName [optionName<defaultValue><optionValueRegex>] (optionName<optionValueRegex>) <optionName<optionValueRegex>>
    //commandName can be empty str
    //[optionName<defaultValue><optionValueRegex>] is a optional arg
    //(optionName<optionValueRegex>) is a required arg matched by optionName
    //<optionName<optionValueRegex>> is a required arg matched by the index of args which are not matched by others
    @ArgsExp(exp = "start" +
            " [-t<10><\\d+>]" +
            " (-n<\\d+>)" +
            " <time<\\d+>>",
            usage = "start tasks;" +
            " -t: num of threads, optional arg, default value is 10;" +
            " -n: num of tasks, required arg;" +
            " time: execution time(millisecond), index arg")
    public void startTask(int _t, int _n, long time) {
        System.out.println("_t = [" + _t + "], _n = [" + _n + "], time = [" + time + "]");
    }

    public static void main(String[] args) {
        QuickStart quickStart = new QuickStart();
        quickStart.execute("start -t 20 -n 1000 60000");
        quickStart.execute("start 80000 -n 1000");
    }

}
