package com.xiyuan.args;

import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Args {

    private HashMap<String, Command> commands = new HashMap<>();

    public boolean autoPrintUsage = true;

    public boolean printError = true;

    {
        Class clazz = this.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            ArgsExp argsExp = method.getAnnotation(ArgsExp.class);
            if (argsExp != null) {
                try {
                    Command command = new Command(this, argsExp, method);
                    Command exist = commands.get(command.name);
                    if (exist != null) {
                        System.err.println("命令申明冲突：\n" + exist.exp + "\n" + command.exp);
                    }
                    else {
                        commands.put(command.name, command);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected String getItemDivider() {
        return " +";
    }

    public Object execute(String args) {
        Matcher matcher = Pattern.compile("(" + getItemDivider() + ")").matcher(args);
        List<Tuple<String, Boolean>> argAndDividers = new ArrayList<>(); // <参数或者分割字符串, 是否为参数>
        ArrayList<Tuple<Integer, Integer>> dividerEdges = new ArrayList<>();

        while (matcher.find()) {
            dividerEdges.add(new Tuple<>(matcher.start(1), matcher.end(1)));
        }

        if (dividerEdges.isEmpty()) {
            argAndDividers.add(new Tuple<>(args, true));
        }
        else {
            for (int i = 0, size = dividerEdges.size(); i < size; i++) {
                Tuple<Integer, Integer> dividerEdge = dividerEdges.get(i);
                if (i == 0 && dividerEdge.t1 != 0) {
                    argAndDividers.add(new Tuple<>(args.substring(0, dividerEdge.t1), true));
                }

                if (i != 0) {
                    argAndDividers.add(new Tuple<>(args.substring(dividerEdges.get(i - 1).t2, dividerEdge.t1), true));
                }

                argAndDividers.add(new Tuple<>(args.substring(dividerEdge.t1, dividerEdge.t2), false));

                if (i + 1 == size && dividerEdge.t2 != args.length()) {
                    argAndDividers.add(new Tuple<>(args.substring(dividerEdge.t2, args.length()), true));
                }
            }
        }

        return execute(argAndDividers);
    }

    private Object execute(List<Tuple<String, Boolean>> argAndDividers) {
        if (argAndDividers == null || argAndDividers.isEmpty()) return null;

        String commandName = "";
        for (Tuple<String, Boolean> argAndDivider : argAndDividers) {
            if (argAndDivider.t2) {
                commandName = argAndDivider.t1;
                break;
            }
        }

        Command command = commands.get(commandName);
        if (command == null) {
            //尝试寻找空命令
            command = commands.get("");
            if (command != null) {
                if (argAndDividers.get(0).t2) {
                    argAndDividers.add(0, new Tuple<>(" ", false));
                }
                argAndDividers.add(0, new Tuple<>("", true));
            }
        }

        if (command == null) {
            if (autoPrintUsage) {
                showAllUsage();
            }
            return null;
        }

        try {
            return command.execute(argAndDividers);
        } catch (Exception e) {
            if (printError) {
                e.printStackTrace();
            }

            if (autoPrintUsage) {
                showUsage(command.name);
            }
        }
        return null;
    }

    public Object execute(String[] args) {
        if (args == null || args.length == 0) return null;

        Command command = commands.get(args[0]);
        if (command == null) {
            //尝试寻找空命令
            command = commands.get("");
            if (command != null) {
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = "";
                System.arraycopy(args, 0, newArgs, 1, args.length);
                args = newArgs;
            }
        }

        if (command == null) {
            if (autoPrintUsage) {
                showAllUsage();
            }
            return null;
        }

        try {
            return command.execute(args);
        } catch (Exception e) {
            if (printError) {
                e.printStackTrace();
            }

            if (autoPrintUsage) {
                showUsage(command.name);
            }
        }
        return null;
    }

    public void showAllUsage() {
        StringBuilder builder = new StringBuilder();
        builder.append("usage:\n");
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            Command command = entry.getValue();
            builder.append(command.exp).append("\t").append(command.usage).append("\n");
        }
        System.out.println(builder.toString());
    }

    public void showUsage(String commandName) {
        Command command = commands.get(commandName);
        if (command != null) {
            System.out.println("usage:\n" + command.exp + "\t" + command.usage + "\n");
        }
    }

    private static class Command {

        private Args ctx;

        private String name;

        private String exp;

        private String usage;

        private HashMap<String, ParamMatcher> paramMatchers;

        private int requiredParamNum;

        private Method executor;

        private HashMap<String, Param> methodParams;

        private static final Matcher paramsMatcher = Pattern.compile("(\\[)([-_]+[a-zA-Z0-9]+|[a-zA-Z][-_a-zA-Z0-9]*)<(.*?)><(.*?)>(\\])|(\\()([-_]+[a-zA-Z0-9]+|[a-zA-Z][-_a-zA-Z0-9]*)(<(.*?)>)(\\))|(<)([-_]+[a-zA-Z0-9]+|[a-zA-Z][-_a-zA-Z0-9]*)(<(.*?)>)(>)|(\\[)([-_]+[a-zA-Z0-9]+|[a-zA-Z][-_a-zA-Z0-9]*)(\\])").matcher("");

        private Command(Args ctx, ArgsExp argsExp, Method executor) {
            this.ctx = ctx;
            exp = argsExp.exp();
            usage = argsExp.usage();

            int spaceIndex = exp.indexOf(' ');
            name = spaceIndex == -1 ? exp : exp.substring(0, spaceIndex);

            paramMatchers = new HashMap<>();
            if (spaceIndex != -1) {
                String paramsStr = exp.substring(spaceIndex + 1);
                paramsMatcher.reset(paramsStr);
                int paramIndex = 0;
                while (paramsMatcher.find()) {
                    ParamMatcher paramMatcher = null;
//                    int groupCount = paramsMatcher.groupCount();
//                    for (int i = 1; i <= groupCount; i++) {
//                        System.out.println(i + "\t" + paramsMatcher.group(i));
//                    }
                    if (paramsMatcher.group(1) != null) {
                        paramMatcher = new OptionalParamMatcher(paramsMatcher.group(2), paramsMatcher.group(4), paramsMatcher.group(3));
                    }
                    else if (paramsMatcher.group(6) != null) {
                        paramMatcher = new RequiredParamMatcher(paramsMatcher.group(7), paramsMatcher.group(9));
                    }
                    else if (paramsMatcher.group(11) != null) {
                        paramMatcher = new IndexedParamMatcher(paramsMatcher.group(12), paramsMatcher.group(14), paramIndex++);
                    }
                    else if (paramsMatcher.group(16) != null) {
                        paramMatcher = new FlagParamMatcher(paramsMatcher.group(17));
                    }

                    if (paramMatcher != null) {
                        paramMatchers.put(paramMatcher.paramName, paramMatcher);
                    }
                }
            }
            for (Map.Entry<String, ParamMatcher> entry : paramMatchers.entrySet()) {
                if (entry.getValue().isRequired) {
                    requiredParamNum++;
                }
            }

            executor.setAccessible(true);
            this.executor = executor;
            this.methodParams = getMethodParams(executor);
        }

        private Object execute(String[] args) throws Exception {
            Stack<Tuple<String, String>> params = new Stack<>();
            if (paramValueMatch(args, 1, 0, params)) {
                return execute(params);
            }
            else throw new Exception("参数解析失败");
        }

        private Object execute(List<Tuple<String, Boolean>> argAndDividers) throws Exception {
            Stack<Tuple<String, String>> params = new Stack<>();
            if (paramValueMatch(argAndDividers, 2, 0, params)) {
                return execute(params);
            }
            else throw new Exception("参数解析失败");
        }

        private Object execute(Stack<Tuple<String, String>> params) throws Exception {
            Object[] paramArr = new Object[methodParams.size()];
            for (Tuple<String, String> keyVal : params) {
                ParamMatcher paramMatcher = paramMatchers.get(keyVal.t1);
                Param paramInfo = paramMatcher == null ? null : methodParams.get(paramMatcher.methodParamName);
                if (paramInfo == null) {
                    throw new Exception("参数不存在：" + keyVal.t1);
                }
                else {
                    paramArr[paramInfo.index] = CastUtil.cast(keyVal.t2, paramInfo.type);
                }
            }

            if (params.size() < methodParams.size()) {
                for (Map.Entry<String, ParamMatcher> matcherEntry : paramMatchers.entrySet()) {
                    ParamMatcher paramMatcher = matcherEntry.getValue();
                    if (paramMatcher instanceof OptionalParamMatcher) {
                        Param paramInfo = methodParams.get(paramMatcher.methodParamName);
                        if (paramArr[paramInfo.index] == null) {
                            paramArr[paramInfo.index] = CastUtil.cast(((OptionalParamMatcher)paramMatcher).defaultValue, paramInfo.type);
                        }
                    }
                    else if (paramMatcher instanceof FlagParamMatcher) {
                        Param paramInfo = methodParams.get(paramMatcher.methodParamName);
                        if (paramArr[paramInfo.index] == null) {
                            paramArr[paramInfo.index] = false;
                        }
                    }
                }

            }

            try {
                return executor.invoke(ctx, paramArr);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private boolean paramValueMatch(String[] args, int curIndex, int indexParamI, Stack<Tuple<String, String>> values) throws Exception {
            if (curIndex >= args.length) {
                //校验参数是否齐全（除了可选参数，其他参数全部有值）
                int curRequiredNum = 0;
                for (Tuple<String, String> value : values) {
                    ParamMatcher paramMatcher = paramMatchers.get(value.t1);
                    if (paramMatcher != null && paramMatcher.isRequired) {
                        curRequiredNum++;
                    }
                }

                if (curRequiredNum == requiredParamNum) {
                    return true;
                }
                else return false;
            }

            boolean result = false;
            String arg = args[curIndex];
            ParamMatcher paramMatcher = paramMatchers.get(arg);
            if (paramMatcher == null || paramMatcher instanceof IndexedParamMatcher) {
                IndexedParamMatcher indexedParamMatcher = findIndexParamMatcher(indexParamI);
                if (indexedParamMatcher != null) {
                    if ("".equals(indexedParamMatcher.paramValueRegex) || arg.matches(indexedParamMatcher.paramValueRegex)) {
                        values.push(new Tuple<>(indexedParamMatcher.paramName, arg));
                        result = paramValueMatch(args, curIndex + 1, indexParamI + 1, values);
                        if (!result) {
                            values.pop();
                        }
                    }
                }
                else {
                    return false;
                }
            }
            else {
                if (paramMatcher instanceof RequiredParamMatcher) {
                    if (curIndex + 1 == args.length) {
                        return false;
                    }
                    else {
                        String nextArg = args[curIndex + 1];
                        if ("".equals(paramMatcher.paramValueRegex) || nextArg.matches(paramMatcher.paramValueRegex)) {
                            values.push(new Tuple<>(paramMatcher.paramName, nextArg));
                            result = paramValueMatch(args, curIndex + 2, indexParamI, values);
                            if (!result) {
                                values.pop();
                            }
                        }
                    }
                }
                else if (paramMatcher instanceof FlagParamMatcher) {
                    values.push(new Tuple<>(paramMatcher.paramName, "true"));
                    result = paramValueMatch(args, curIndex + 1, indexParamI, values);
                    if (!result) {
                        values.pop();
                    }
                }
                else {
                    //如果当前项已经是最后一项，则当前项只能使用默认值
                    if (curIndex + 1 == args.length) {
                        values.push(new Tuple<>(paramMatcher.paramName, ((OptionalParamMatcher) paramMatcher).defaultValue));
                        result = paramValueMatch(args, curIndex + 1, indexParamI, values);
                        if (!result) {
                            values.pop();
                        }
                    }
                    else {
                        //不是最后一项，那么下一项就有三种情况

                        //下一项作为当前项的值
                        String nextArg = args[curIndex + 1];
                        if ("".equals(paramMatcher.paramValueRegex) || nextArg.matches(paramMatcher.paramValueRegex)) {
                            values.push(new Tuple<>(paramMatcher.paramName, nextArg));
                            result = paramValueMatch(args, curIndex + 2, indexParamI, values);
                            if (!result) {
                                values.pop();
                            }
                        }

                        //下一项作为下一个 参数的名称 或者 index项的值
                        if (!result) {
                            values.push(new Tuple<>(paramMatcher.paramName, ((OptionalParamMatcher) paramMatcher).defaultValue));
                            result = paramValueMatch(args, curIndex + 1, indexParamI, values);
                            if (!result) {
                                values.pop();
                            }
                        }
                    }
                }
            }

            return result;
        }

        private boolean paramValueMatch(List<Tuple<String, Boolean>> argAndDividers, int curIndex, int indexParamI, Stack<Tuple<String, String>> values) throws Exception {
            int argAndDividerSize = argAndDividers.size();
            if (curIndex >= argAndDividerSize) {
                //校验参数是否齐全（除了可选参数，其他参数全部有值）
                int curRequiredNum = 0;
                for (Tuple<String, String> value : values) {
                    ParamMatcher paramMatcher = paramMatchers.get(value.t1);
                    if (paramMatcher != null && paramMatcher.isRequired) {
                        curRequiredNum++;
                    }
                }

                if (curRequiredNum == requiredParamNum) {
                    return true;
                }
                else return false;
            }

            boolean result = false;
            Tuple<String, Boolean> argAndDivider = argAndDividers.get(curIndex);
            if (!argAndDivider.t2) {
                return false;
            }

            ParamMatcher paramMatcher = paramMatchers.get(argAndDivider.t1);
            if (paramMatcher == null || paramMatcher instanceof IndexedParamMatcher) {
                IndexedParamMatcher indexedParamMatcher = findIndexParamMatcher(indexParamI);
                if (indexedParamMatcher != null) {
                    //尝试把后面 n 项作为一个值
                    String arg = "";
                    int tempIndex = curIndex;
                    while (tempIndex < argAndDividerSize) {
                        arg += argAndDividers.get(tempIndex).t1;
                        String withoutLastDivider = null;
                        if (tempIndex + 2 == argAndDividerSize) {
                            withoutLastDivider = arg;
                            arg += argAndDividers.get(tempIndex + 1).t1;
                        }

                        if ("".equals(indexedParamMatcher.paramValueRegex) || arg.matches(indexedParamMatcher.paramValueRegex)) {
                            values.push(new Tuple<>(indexedParamMatcher.paramName, arg));
                            result = paramValueMatch(argAndDividers, tempIndex + 2, indexParamI + 1, values);
                            if (!result) {
                                values.pop();
                            }
                            else {
                                break;
                            }
                        }

                        if (withoutLastDivider != null) {
                            if ("".equals(indexedParamMatcher.paramValueRegex) || withoutLastDivider.matches(indexedParamMatcher.paramValueRegex)) {
                                values.push(new Tuple<>(indexedParamMatcher.paramName, withoutLastDivider));
                                result = paramValueMatch(argAndDividers, tempIndex + 2, indexParamI + 1, values);
                                if (!result) {
                                    values.pop();
                                }
                                else {
                                    break;
                                }
                            }
                        }

                        tempIndex++;
                        if (tempIndex < argAndDividerSize) {
                            arg += argAndDividers.get(tempIndex).t1;
                        }
                        else break;
                        tempIndex++;
                    }
                }
                else {
                    return false;
                }
            }
            else if (paramMatcher instanceof FlagParamMatcher) {
                values.push(new Tuple<>(paramMatcher.paramName, "true"));
                result = paramValueMatch(argAndDividers, curIndex + 2, indexParamI, values);
                if (!result) {
                    values.pop();
                }
            }
            else {
                if (paramMatcher instanceof RequiredParamMatcher) {
                    if (curIndex + 2 >= argAndDividerSize) {
                        return false;
                    }
                    else {
                        //尝试把后面的多项作为参数值
                        String arg = "";
                        int tempIndex = curIndex + 2;
                        while (tempIndex < argAndDividerSize) {
                            arg += argAndDividers.get(tempIndex).t1;
                            String withoutLastDivider = null;
                            if (tempIndex + 2 == argAndDividerSize) {
                                withoutLastDivider = arg;
                                arg += argAndDividers.get(tempIndex + 1).t1;
                            }

                            if ("".equals(paramMatcher.paramValueRegex) || arg.matches(paramMatcher.paramValueRegex)) {
                                values.push(new Tuple<>(paramMatcher.paramName, arg));
                                result = paramValueMatch(argAndDividers, tempIndex + 2, indexParamI, values);
                                if (!result) {
                                    values.pop();
                                }
                                else break;
                            }

                            if (withoutLastDivider != null) {
                                if ("".equals(paramMatcher.paramValueRegex) || withoutLastDivider.matches(paramMatcher.paramValueRegex)) {
                                    values.push(new Tuple<>(paramMatcher.paramName, withoutLastDivider));
                                    result = paramValueMatch(argAndDividers, tempIndex + 2, indexParamI, values);
                                    if (!result) {
                                        values.pop();
                                    }
                                    else break;
                                }
                            }

                            tempIndex++;
                            if (tempIndex < argAndDividerSize) {
                                arg += argAndDividers.get(tempIndex).t1;
                            }
                            else break;
                            tempIndex++;
                        }
                    }
                }
                else {
                    //如果当前项已经是最后一项，则当前项只能使用默认值
                    if (curIndex + 2 >= argAndDividerSize) {
                        values.push(new Tuple<>(paramMatcher.paramName, ((OptionalParamMatcher) paramMatcher).defaultValue));
                        result = paramValueMatch(argAndDividers, curIndex + 2, indexParamI, values);
                        if (!result) {
                            values.pop();
                        }
                    }
                    else {
                        //不是最后一项，那么下一项就有三种情况

                        {
                            //尝试把后面的多项作为参数值
                            String arg = "";
                            int tempIndex = curIndex + 2;
                            while (tempIndex < argAndDividerSize) {
                                arg += argAndDividers.get(tempIndex).t1;
                                String withoutLastDivider = null;
                                if (tempIndex + 2 == argAndDividerSize) {
                                    withoutLastDivider = arg;
                                    arg += argAndDividers.get(tempIndex + 1).t1;
                                }

                                if ("".equals(paramMatcher.paramValueRegex) || arg.matches(paramMatcher.paramValueRegex)) {
                                    values.push(new Tuple<>(paramMatcher.paramName, arg));
                                    result = paramValueMatch(argAndDividers, tempIndex + 2, indexParamI, values);
                                    if (!result) {
                                        values.pop();
                                    }
                                    else break;
                                }

                                if (withoutLastDivider != null) {
                                    if ("".equals(paramMatcher.paramValueRegex) || withoutLastDivider.matches(paramMatcher.paramValueRegex)) {
                                        values.push(new Tuple<>(paramMatcher.paramName, withoutLastDivider));
                                        result = paramValueMatch(argAndDividers, tempIndex + 2, indexParamI, values);
                                        if (!result) {
                                            values.pop();
                                        }
                                        else break;
                                    }
                                }

                                tempIndex++;
                                if (tempIndex < argAndDividerSize) {
                                    arg += argAndDividers.get(tempIndex).t1;
                                }
                                else break;
                                tempIndex++;
                            }
                        }

                        //当前项采用默认值，继续解析后面的
                        if (!result) {
                            values.push(new Tuple<>(paramMatcher.paramName, ((OptionalParamMatcher) paramMatcher).defaultValue));
                            result = paramValueMatch(argAndDividers, curIndex + 2, indexParamI, values);
                            if (!result) {
                                values.pop();
                            }
                        }
                    }
                }
            }

            return result;
        }

        private IndexedParamMatcher findIndexParamMatcher(int index) {
            for (Map.Entry<String, ParamMatcher> entry : paramMatchers.entrySet()) {
                ParamMatcher paramMatcher = entry.getValue();
                if (paramMatcher instanceof IndexedParamMatcher) {
                    IndexedParamMatcher temp = (IndexedParamMatcher) paramMatcher;
                    if (temp.index == index)return temp;
                }
            }
            return null;
        }

        private static HashMap<String, Param> getMethodParams(Method method) {
            final String methodName = method.getName();
            final Class<?>[] methodParameterTypes = method.getParameterTypes();
            final int methodParameterCount = methodParameterTypes.length;
            final String className = method.getDeclaringClass().getName();
            final boolean isStatic = Modifier.isStatic(method.getModifiers());

            final HashMap<String, Param> params = new HashMap<>();
            final Param[] paramArr = new Param[methodParameterCount];
            final List<Tuple<String, Integer>> localParams = new ArrayList<>();
            final int[] paramIndex = {0};
            for (int i = 0; i < methodParameterCount; i++) {
                paramArr[i] = new Param(i,null, methodParameterTypes[i]);
            }

            ClassReader classReader = null;
            try {
                classReader = new ClassReader(className);
            } catch (IOException e) {
                e.printStackTrace();
            }

            classReader.accept(new ClassVisitor(Opcodes.ASM6) {

                private boolean matchTypes(Type[] types, Class<?>[] parameterTypes) {
                    if (types.length != parameterTypes.length) {
                        return false;
                    }
                    for (int i = 0; i < types.length; i++) {
                        if (!Type.getType(parameterTypes[i]).equals(types[i])) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

                    final Type[] argTypes = Type.getArgumentTypes(desc);

                    //参数类型不一致
                    if (!methodName.equals(name) || !matchTypes(argTypes, methodParameterTypes)) {
                        return mv;
                    }

                    return new MethodVisitor(Opcodes.ASM6, mv) {

                        @Override
                        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                            //如果是静态方法，第一个参数就是方法参数，非静态方法，则第一个参数是 this ,然后才是方法的参数
                            if (!"this".equals(name)) {
                                localParams.add(new Tuple<>(name, index));
                            }
                            super.visitLocalVariable(name, desc, signature, start, end, index);
                        }
                    };
                }
            }, 0);

            localParams.sort(new Comparator<Tuple<String, Integer>>() {
                @Override
                public int compare(Tuple<String, Integer> o1, Tuple<String, Integer> o2) {
                    return o1.t2 - o2.t2;
                }
            });

            for (int i = 0; i < methodParameterCount; i++) {
                Param param = paramArr[i];
                param.name = localParams.get(i).t1;
                params.put(param.name, param);
            }

            return params;
        }

        /**
         * 是否互为翻转字符串
         */
        private static boolean isReverse(String str1, String str2) {
            if (str1 == null || str2 == null) return true;

            int len1 = str1.length();
            int len2 = str2.length();

            if (len1 != len2) return false;

            for (int i = 0, j = len2 - 1; i < len1; i++, j--) {
                if (str1.charAt(i) != str2.charAt(j)) {
                    return false;
                }
            }
            return true;
        }

    }

    private static class Param {

        private int index;

        private String name;

        private Class type;

        public Param(int index, String name, Class type) {
            this.index = index;
            this.name = name;
            this.type = type;
        }
    }

    private static class ParamMatcher {

        protected String paramName;

        protected String methodParamName;

        protected boolean isRequired;

        protected String paramValueRegex;

        private ParamMatcher(String paramName, boolean isOptional, String paramValueRegex) {
            this.paramName = paramName;
            this.methodParamName = paramName.replace('-', '_');
            this.isRequired = isOptional;
            this.paramValueRegex = paramValueRegex;
        }

    }

    private static class OptionalParamMatcher extends ParamMatcher {

        private String defaultValue;

        private OptionalParamMatcher(String paramName, String paramValueRegex, String defaultValue) {
            super(paramName, false, paramValueRegex);
            this.defaultValue = defaultValue;
        }
    }

    private static class RequiredParamMatcher extends ParamMatcher {
        private RequiredParamMatcher(String paramName, String paramValueRegex) {
            super(paramName, true, paramValueRegex);
        }
    }

    private static class IndexedParamMatcher extends ParamMatcher {

        private int index;

        private IndexedParamMatcher(String paramName, String paramValueRegex, int index) {
            super(paramName, true, paramValueRegex);
            this.index = index;
        }
    }

    private static class FlagParamMatcher extends ParamMatcher {

        private FlagParamMatcher(String paramName) {
            super(paramName, false, "");
        }

    }

}
