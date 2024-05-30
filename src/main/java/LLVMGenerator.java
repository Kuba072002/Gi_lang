import types.ArrayType;
import types.VarType;

import java.util.*;

import static types.VarType.INT;
import static types.VarType.REAL;

public class LLVMGenerator {
    static String header_text = "";
    static String header_top = "";
    static String main_text = "";
    static int register = 1;
    static int anonymousString = 0;
    static int br = 0;
    static Stack<Integer> brstack = new Stack<>();
    static Map<VarType,String> types = Map.of(INT,"i32",REAL,"double");
    static Map<List<VarType>,String> convertTypes = Map.of(
            List.of(REAL,INT),"sitofp",
            List.of(INT,REAL),"fptosi"
    );
    static boolean global = false;
    static String buffer = "";


    static void declare(String id, VarType type){
        buffer += "%" + id + " = alloca "+types.get(type)+"\n";
        addTextDependOnScope();
    }

    static void assign(String id, String value,VarType type) {
        buffer += "store "+types.get(type)+" " + value + ", "+types.get(type)+"* %" + id + "\n";
        addTextDependOnScope();
    }

    static void changeType(String id,VarType newType,VarType oldType){
        String operator = convertTypes.get(List.of(oldType,newType));
        if (operator == null)
            throw new RuntimeException();
        buffer += "%" + register + " = "+operator+" "+ types.get(newType) + " %" + (register - 1) + " to " + types.get(oldType) + "\n";
        assign(id,"%"+register,oldType);
        register++;
        addTextDependOnScope();
    }
    //deprecated
    static void declare_int(String id) {
        buffer += "%" + id + " = alloca i32\n";
        addTextDependOnScope();
    }

    static void declare_global_int(String id, String value) {
        header_text += "@" + id + " = global i32 " + value + "\n";
        addTextDependOnScope();
    }
    //deprecated
    static void declare_real(String id) {
        buffer += "%" + id + " = alloca double\n";
        addTextDependOnScope();
    }

    static void declare_global_real(String id, String value){
        header_text += "@"+id+" = global double "+value+"\n";
        addTextDependOnScope();
    }

    static int declare_string(int length, String id, String content) {
        header_top += "@__const.main." + (id) + " = private unnamed_addr constant [" + (length + 1) + " x i8] c\"" + (content) + "\\00\"\n";

        buffer += "%" + register + " = alloca [" + (length + 1) + " x i8]\n";
        register++;
        int arrayRegister = register - 1;
        buffer += "%" + register + " = bitcast [" + (length + 1) + " x i8]* %" + arrayRegister + " to i8*\n";
        register++;
        buffer += "call void @llvm.memcpy.p0i8.p0i8.i64(i8* %" + (register - 1) + ", i8* align 1 getelementptr inbounds " +
                "([" + (length + 1) + " x i8], [" + (length + 1) + " x i8]* @__const.main." + id + ", i32 0, i32 0), i64 " + (length + 1) + ", i1 false)\n";
        addTextDependOnScope();
        return arrayRegister;
    }
    //deprecated
    static void assign_int(String id, String value) {
        buffer += "store i32 " + value + ", i32* %" + id + "\n";
        addTextDependOnScope();
    }

    static void assign_global_int(String id, String value) {
        buffer += "store i32 " + value + ", i32* 2" + id + "\n";
        addTextDependOnScope();
    }
    //deprecated
    static void assign_real(String id, String value) {
        buffer += "store double " + value + ", double* %" + id + "\n";
        addTextDependOnScope();
    }
    static void assign_global_real(String id, String value) {
        buffer += "store double " + value + ", double* @" + id + "\n";
        addTextDependOnScope();
    }

    public static int allocateIntArrayAndStoreValues(String arrayName, int size, String[] array) {
        String globalArrayName = "@__const.main." + arrayName;

        header_top += globalArrayName + " = unnamed_addr constant [" + size + "x i32] [";
        for (int i = 0; i < array.length; i++) {
            header_top += "i32 " + array[i];
            if (i != array.length - 1) {
                header_top += ", ";
            }
        }
        header_top += "]\n";
        buffer += "%" + (register) + " = alloca [" + (size) + " x i32]\n";
        register++;
        int registerAllocatedArray = register - 1;
        buffer += "%" + (register) + " = bitcast [" + (size) + " x i32]* %" + (register - 1) + " to i8*\n";
        register++;
        buffer += "call void @llvm.memcpy.p0i8.p0i8.i64(i8* %" + (register - 1) + ", i8* bitcast ([" + (size) + " x i32]* " + (globalArrayName) + " to i8*), i64 " + (size * 4) + " , i1 false)\n";
        addTextDependOnScope();
        return registerAllocatedArray;
    }

    public static int allocateDoubleArrayAndStoreValues(String arrayName, int size, String[] array) {
        String globalArrayName = "@__const.main." + arrayName;

        header_top += globalArrayName + " = unnamed_addr constant [" + size + "x double] [";
        for (int i = 0; i < array.length; i++) {
            header_top += "double " + array[i];
            if (i != array.length - 1) {
                header_top += ", ";
            }
        }
        header_top += "]\n";
        buffer += "%" + (register) + " = alloca [" + (size) + " x double]\n";
        register++;
        int registerAllocatedArray = register - 1;
        buffer += "%" + (register) + " = bitcast [" + (size) + " x double]* %" + (register - 1) + " to i8*\n";
        register++;
        buffer += "call void @llvm.memcpy.p0i8.p0i8.i64(i8* %" + (register - 1) + ", i8* bitcast ([" + (size) + " x double]* " + (globalArrayName) + " to i8*), i64 " + (size * 8) + " , i1 false)\n";
        addTextDependOnScope();
        return registerAllocatedArray;
    }

    public static void getPtrArrayAndStoreValue(int size, String value, int arrayRegisterPtr, int arrayIdx) {
        buffer += "%" + register + " = getelementptr inbounds [" + size + " x double], [" + size + " x double]* %" + arrayRegisterPtr + ", i32 0, i32 " + arrayIdx + "\n";
        register++;
        buffer += "store double " + (value) + ", double* %" + (register - 1) + "\n";
        addTextDependOnScope();
    }

    static void printf_int(String id) {
        buffer += "%" + register + " = load i32, i32* %" + id + "\n";
        register++;
        buffer += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (register - 1) + ")\n";
        register++;
        addTextDependOnScope();
    }

    static void printf_global_int(String id) {
//        buffer += "%" + register + " = load i32, i32* %" + id + "\n";
//        register++;
        buffer += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (register - 1) + ")\n";
        register++;
        addTextDependOnScope();
    }

    static void printf_double(String id) {
        buffer += "%" + register + " = load double, double* %" + id + "\n";
        register++;
        buffer += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (register - 1) + ")\n";
        register++;
        addTextDependOnScope();
    }

    static void printf_global_double(String id) {
//        buffer += "%" + register + " = load double, double* %" + id + "\n";
//        register++;
        buffer += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (register - 1) + ")\n";
        register++;
        addTextDependOnScope();
    }

    static void printf_value_int(String value) {
        buffer += "%" + register + " = alloca i32\n";
        register++;
        buffer += "store i32 " + value + ", i32* %" + (register - 1) + "\n";
        buffer += "%" + register + " = load i32, i32* %" + (register - 1) + "\n";
        register++;
        buffer += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (register - 1) + ")\n";
        register++;
        addTextDependOnScope();
    }

    static void printf_value_double(String value) {
        buffer += "%" + register + " = alloca double\n";
        register++;
        buffer += "store double " + value + ", double* %" + (register - 1) + "\n";
        buffer += "%" + register + " = load double, double* %" + (register - 1) + "\n";
        register++;
        buffer += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (register - 1) + ")\n";
        register++;
        addTextDependOnScope();
    }

    static void printf_string(String id, int length) {
//        buffer += "%"+register+" = load i8*, i8** @"+id+"\n";
        buffer += "%" + register + " = getelementptr inbounds [" + (length + 1) + " x i8], [" + (length + 1) + " x i8]* %" + id + ", i32 0, i32 0\n";
        register++;
        buffer += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strps, i32 0, i32 0), i8* %" + (register - 1) + ")\n";
        register++;
        addTextDependOnScope();
    }

    static void scanf_int(String id) {
        buffer += "%" + register + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* %" + id + ")\n";
        register++;
        addTextDependOnScope();
    }

    static void scanf_double(String id) {
        buffer += "%" + register + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strd, i32 0, i32 0), double* %" + id + ")\n";
        register++;
        addTextDependOnScope();
    }

    static void add_int(String val1, String val2) {
        buffer += "%" + register + " = add i32 " + val1 + ", " + val2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void add_double(String val1, String val2) {
        buffer += "%" + register + " = fadd double " + val1 + ", " + val2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void sub_int(String val1, String val2) {
        buffer += "%" + register + " = sub i32 " + val1 + ", " + val2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void sub_double(String val1, String val2) {
        buffer += "%" + register + " = fsub double " + val1 + ", " + val2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void mult_int(String val1, String val2) {
        buffer += "%" + register + " = mul i32 " + val1 + ", " + val2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void mult_double(String val1, String val2) {
        buffer += "%" + register + " = fmul double " + val1 + ", " + val2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void div_int(String val1, String val2) {
        buffer += "%" + register + " = sdiv i32 " + val1 + ", " + val2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void div_double(String val1, String val2) {
        buffer += "%" + register + " = fdiv double " + val1 + ", " + val2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void load(String id, VarType type){
        buffer += "%" + register + " = load "+types.get(type)+", "+types.get(type)+"* %" + id + "\n";
        register++;
        addTextDependOnScope();
    }

    static void load_global(String id, VarType type){
        buffer += "%" + register + " = load "+types.get(type)+", "+types.get(type)+"* @" + id + "\n";
        register++;
        addTextDependOnScope();
    }

    static void load_int(String id) {
        buffer += "%" + register + " = load i32, i32* %" + id + "\n";
        register++;
        addTextDependOnScope();
    }

    static void load_global_int(String id) {
        buffer += "%" + register + " = load i32, i32* @" + id + "\n";
        register++;
        addTextDependOnScope();
    }

    static void load_double(String id) {
        buffer += "%" + register + " = load double, double* %" + id + "\n";
        register++;
        addTextDependOnScope();
    }

    static void load_global_double(String id) {
        buffer += "%" + register + " = load double, double* @" + id + "\n";
        register++;
        addTextDependOnScope();
    }

    public static void getArrayPtr(int arrayAddress, int numberOfElems, String idx, VarType type) {
        buffer += "%" + register + " = getelementptr inbounds [" + numberOfElems + " x "+types.get(type)+"]," +
                " [" + numberOfElems + " x "+types.get(type)+"]* %" + arrayAddress + ", i32 0, i32 " + idx + "\n";
        register++;
        addTextDependOnScope();
    }

    public static void getArrayPtrInt(int arrayAddress, int numberOfElems, String idx) {
        buffer += "%" + register + " = getelementptr inbounds [" + numberOfElems + " x i32], " +
                "[" + numberOfElems + " x i32]* %" + arrayAddress + ", i32 0, i32 " + idx + "\n";
        register++;
        addTextDependOnScope();
    }

    public static void getArrayPtrReal(int arrayAddress, int numberOfElems, String idx) {
        buffer += "%" + register + " = getelementptr inbounds [" + numberOfElems + " x double]," +
                " [" + numberOfElems + " x double]* %" + arrayAddress + ", i32 0, i32 " + idx + "\n";
        register++;
        addTextDependOnScope();
    }

    static void icmp_int(String v1, String v2, String cond) {
        String sign = switch (cond) {
            case ("==") -> "eq";
            case ("!=") -> "ne";
            case ("<=") -> "ule";
            case (">=") -> "uge";
            case ("<") -> "ult";
            case (">") -> "ugt";
            default -> "";
        };
        buffer += "%" + register + " = icmp " + sign + " i32 " + v1 + ", " + v2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void icmp_double(String v1, String v2, String cond) {
        String sign = switch (cond) {
            case ("==") -> "oeq";
            case ("!=") -> "one";
            case ("<=") -> "ole";
            case (">=") -> "oge";
            case ("<") -> "olt";
            case (">") -> "ogt";
            default -> "";
        };
        buffer += "%" + register + " = fcmp " + sign + " double " + v1 + ", " + v2 + "\n";
        register++;
        addTextDependOnScope();
    }

    static void ifstart() {
        br++;
        buffer += "br i1 %" + (register - 1) + ", label %true" + br + ", label %false" + br + "\n";
        buffer += "true" + br + ":\n";
        brstack.push(br);
        addTextDependOnScope();
    }

    static void ifend() {
        int b = brstack.pop();
        buffer += "br label %false" + b + "\n";
        buffer += "false" + b + ":\n";
        addTextDependOnScope();
    }

    static void repeatstart(String repetitions) {
        declare_int(Integer.toString(register));
        int counter = register;
        register++;
        assign_int(Integer.toString(counter), "0");
        br++;
        buffer += "br label %cond" + br + "\n";
        buffer += "cond" + br + ":\n";

        load_int(Integer.toString(counter));
        add_int("%" + (register - 1), "1");
        assign_int(Integer.toString(counter), "%" + (register - 1));

        buffer += "%" + register + " = icmp slt i32 %" + (register - 2) + ", " + repetitions + "\n";
        register++;

        buffer += "br i1 %" + (register - 1) + ", label %true" + br + ", label %false" + br + "\n";
        buffer += "true" + br + ":\n";
        brstack.push(br);
        addTextDependOnScope();
    }

    static void repeatend() {
        int b = brstack.pop();
        buffer += "br label %cond" + b + "\n";
        buffer += "false" + b + ":\n";
        addTextDependOnScope();
    }

    static void loopstart(String name, ArrayType array) {
        declare_int(Integer.toString(register));
        assign_int(Integer.toString(register), String.valueOf(array.size));
        int repetitions = register;
        register++;

        int counter = register;
        declare_int(Integer.toString(counter));
        register++;
        assign_int(Integer.toString(counter), "-1");
        br++;
        buffer += "br label %cond" + br + "\n";
        buffer += "cond" + br + ":\n";

        load_int(Integer.toString(counter));
        add_int("%" + (register - 1), "1");
        assign_int(Integer.toString(counter), "%" + (register - 1));
//        load_int(String.valueOf(counter));
        if (array.varType == INT) {
            getArrayPtrInt(array.arrayAddress, array.size, "%" + (register - 1));
            load_int(String.valueOf(register - 1));
            assign_int(name, "%" + (register - 1));
        }
        if (array.varType == REAL) {
            getArrayPtrReal(array.arrayAddress, array.size, "%" + (register - 1));
            load_double(String.valueOf(register - 1));
            assign_real(name, "%" + (register - 1));
        }
        load_int(String.valueOf(counter));
        load_int(String.valueOf(repetitions));
        buffer += "%" + register + " = icmp slt i32 %" + (register - 2) + ", %" + (register - 1) + "\n";
        register++;

        buffer += "br i1 %" + (register - 1) + ", label %true" + br + ", label %false" + br + "\n";
        buffer += "true" + br + ":\n";

        brstack.push(br);
        addTextDependOnScope();
    }

//    public static void createStruct(String name, List<VarType> structTypes) {
//        header_text += "%.struct " + name + " = type { ";
//        for(int i = 0; i < structTypes.size(); i++) {
//            header_text += types.get(structTypes.get(i));
//            if(i != structTypes.size() - 1) header_text += ", ";
//        }
//        header_text += " }\n";
//    }

    static String generate() {
        String text = "";
        text += "declare i32 @printf(i8*, ...)\n";
        text += "declare i32 @__isoc99_scanf(i8*, ...)\n";
        text += "declare void @llvm.memset.p0i8.i64(i8* nocapture writeonly, i8, i64, i1 immarg)\n";
        text += "declare void @llvm.memcpy.p0i8.p0i8.i64(i8* noalias nocapture writeonly, i8* noalias nocapture readonly, i64, i1 immarg)\n";
        text += "@strpi = constant [4 x i8] c\"%d\\0A\\00\"\n";
        text += "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n";
        text += "@strs = constant [3 x i8] c\"%d\\00\"\n";
        text += "@strd = constant [4 x i8] c\"%lf\\00\"\n";
        text += "@strps = constant [4 x i8] c\"%s\\0A\\00\"\n";
        text += "@.str = private unnamed_addr constant [3 x i8] c\"%s\\00\"\n";
        text += header_top;
        text += header_text;
        text += "define i32 @main() nounwind{\n";
        text += main_text;
        text += "ret i32 0 }\n";
        return text;
    }

    static int[] enterFunction(String name, String retType, List<String> args, List<VarType> argsTypes ){
        int[] argsNamesInitialMapped = new int[args.size()];
        int[] argsNamesMapped = new int[args.size()];
        register = 0;
        buffer += "define "+retType+" @"+name+"(";
        for(int i = 0; i < args.size(); i++){
            if(argsTypes.get(i) == INT){
                buffer += "i32 %"+register;
            }else if(argsTypes.get(i) == REAL){
                buffer += "double %"+register;
            }
            argsNamesInitialMapped[i] = register;
            register++;
            if(i != args.size()-1){
                buffer += ", ";
            }
        }
        register++;
        buffer += ") {\n";
        for(int i = 0; i < argsNamesInitialMapped.length; i++){
            if(argsTypes.get(i) == INT ){
                buffer += "%"+register+" = alloca i32\n";
                buffer += "store i32 %"+argsNamesInitialMapped[i]+", i32* %" + register+"\n";
                argsNamesMapped[i] = register;
                register++;
            }else if(argsTypes.get(i)== REAL){
                buffer += "%"+register+" = alloca double\n";
                buffer += "store double %"+argsNamesInitialMapped[i]+", double* %" +register+"\n";
                argsNamesMapped[i] = register;
                register++;
            }
        }
        addTextDependOnScope();
        return argsNamesMapped;
    }

    static void exitFunction(String returnVariable, String returnVariableType){
        String mappedRetType = "";
        if(returnVariableType.equals("real")){
            mappedRetType = "double";
        }else if(returnVariableType.equals("int")){
            mappedRetType = "i32";
        }
        buffer += "%"+register + " = load " + mappedRetType + ", "+mappedRetType+"* "+returnVariable+"\n";
        buffer += "ret " + mappedRetType + " %" +register+"\n}\n";
        register = 1;
        addTextDependOnScope();
    }

    static void execFunc(String id, String returnType ,List<String> types, List<String> ids){
        String[] argsTypesMapped = types.stream().map(x -> {
            if(x.equals("REAL")){
                return "double";
            }else{
                return "i32";
            }
        }).toArray(String[]::new);

        String argsMapped = "";
        for(int i = 0; i < ids.size(); i++){
            argsMapped += argsTypesMapped[i] +" %"+register;
            if(i != ids.size() - 1){
                argsMapped += ", ";
            }
            buffer += "%"+register+" = load "+argsTypesMapped[i]+", "+argsTypesMapped[i]+"* %"+ids.get(i)+"\n";
            register++;
        }

        buffer += "%"+register+" = call "+returnType+ " @"+id+"("+argsMapped+")\n";
        register++;
        addTextDependOnScope();
    }

    static void addTextDependOnScope(){
        if(global){
            header_text += buffer;
        }else{
            main_text += buffer;
        }
        buffer = "";
    }

//    public static void declare_stuct(String id, String structId) {
//        buffer += "%"+id+" = alloca %struct." + structId+"\n";
//        register++;
//    }
}
