# JavaArgs
command line args parse  

**Quick Start**  
[src/test/java/QuickStart.java](src/test/java/QuickStart.java)  
```java
import com.xiyuan.args.Args;
import com.xiyuan.args.ArgsExp;

public class QuickStart extends Args{

    //ArgExp fromat:
    //commandName [optionName<defaultValue><optionValueRegex>] [optionName] (optionName<optionValueRegex>) <optionName<optionValueRegex>>
    //commandName can be empty str
    //[optionName<defaultValue><optionValueRegex>] is a optional arg
    //[optionName] is a optional arg, if exist, value is true, else false
    //(optionName<optionValueRegex>) is a required arg matched by optionName
    //<optionName<optionValueRegex>> is a required arg matched by the index of args which are not matched by others
    @ArgsExp(exp = "start" +
            " [-t<10><\\d+>]" +
            " [-p]" +
            " (-n<\\d+>)" +
            " <time<\\d+>>",
            usage = "start tasks;" +
            " -t: num of threads, optional arg, default value is 10;" +
            " -p: should print logs during task;" +
            " -n: num of tasks, required arg;" +
            " time: execution time(millisecond), index arg")
    public void startTask(int _t, boolean _p, int _n, long time) {
        System.out.println("_t = [" + _t + "], _p = [" + _p + "], _n = [" + _n + "], time = [" + time + "]");
    }

    public static void main(String[] args) {
        QuickStart quickStart = new QuickStart();
        quickStart.execute("start -t 20 -n 1000 60000");
        quickStart.execute("start 80000 -p -n 1000");
    }

}
```

output
```
_t = [20], _p = [false], _n = [1000], time = [60000]
_t = [10], _p = [true], _n = [1000], time = [80000]
```

**More example**  
[src/test/java/Test.java](src/test/java/Test.java)  
