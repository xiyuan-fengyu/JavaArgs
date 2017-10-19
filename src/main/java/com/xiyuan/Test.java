package com.xiyuan;

import com.xiyuan.args.Args;
import com.xiyuan.args.ArgsExp;

public class Test {

    public static class MyArgs extends Args {

        @ArgsExp(exp = "test" +
                " [-h<localhost><localhost|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}>]" +
                " <port<\\d+>>" +
                " (-s<true|false>)",
                usage = "测试")
        public void test(String _h, int port, boolean _s) {
            System.out.println("_h = [" + _h + "], port = [" + port + "], _s = [" + _s + "]");
        }

        @ArgsExp(exp = "sayHello" +
                " <to<.+>>")
        public void sayHelloTo(String to) {
            System.out.println("Hello, " + to + " !");
        }

        @ArgsExp(exp = "login" +
                " <phone<\\d{11}>>" +
                " <code<\\d{6}>>")
        public void login(String phone, String code) {
            System.out.println("phone = [" + phone + "], code = [" + code + "]");
        }

        @ArgsExp(exp = "testApi" +
                " [-thread<10><\\d+>]" +
                " [-task<1000><\\d+>]" +
                " [-time<60000><\\d+>]" +
                " [-api<*><\\*|(\\d+ *, *)*\\d+>]")
        public void testApi(long _thread, long _task, long _time, String _api) {
            System.out.println("_thread = [" + _thread + "], _task = [" + _task + "], _time = [" + _time + "], _api = [" + _api + "]");
        }

        @ArgsExp(exp = " <str<.*>>")
        public void testEmptyCommand(String str) {
            System.out.println(str);
        }

        @ArgsExp(exp = "user" +
                " <id<\\d+>>" +
                " <name<.+>>" +
                " <age<\\d{1,3}>>" +
                " [-sex<unknow><boy|girl|unknow>]")
        public void user(int id, String name, int age, String _sex) {
            System.out.println("id = [" + id + "], name = [" + name + "], age = [" + age + "], _sex = [" + _sex + "]");
        }

    }

    public static void main(String[] args) {
        MyArgs myArgs = new MyArgs();
        myArgs.showAllUsage();
        myArgs.execute("test 8080 -s   true  ");
        myArgs.execute(new String[]{"sayHello", "Tomcat"});
        myArgs.execute("sayHello Tom cat");
        myArgs.execute("login 18911111111 123456");
        myArgs.execute("testApi -thread 10 -task 1000 -time 60000 -api 1,2,3,4");
        myArgs.execute("testApi");
        myArgs.execute("测试空命令 123");
        myArgs.execute("user 1 xiyuan fengyu 25 -sex boy");
    }

}