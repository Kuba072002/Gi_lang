import types.ArrayType;

import java.util.Stack;

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

    static void declare_int(String id) {
        main_text += "%" + id + " = alloca i32\n";
    }

    static void declare_global_int(String id, String value) {
        header_text += "@" + id + " = global i32 " + value + "\n";
    }

    static void declare_real(String id) {
        main_text += "%" + id + " = alloca double\n";
    }

    static void declare_global_real(String id, String value){
        header_text += "@"+id+" = global double "+value+"\n";
    }

    static int declare_string(int length, String id, String content) {
        header_top += "@__const.main." + (id) + " = private unnamed_addr constant [" + (length + 1) + " x i8] c\"" + (content) + "\\00\"\n";

        main_text += "%" + register + " = alloca [" + (length + 1) + " x i8]\n";
        register++;
        int arrayRegister = register - 1;
        main_text += "%" + register + " = bitcast [" + (length + 1) + " x i8]* %" + arrayRegister + " to i8*\n";
        register++;
        main_text += "call void @llvm.memcpy.p0i8.p0i8.i64(i8* %" + (register - 1) + ", i8* align 1 getelementptr inbounds " +
                "([" + (length + 1) + " x i8], [" + (length + 1) + " x i8]* @__const.main." + id + ", i32 0, i32 0), i64 " + (length + 1) + ", i1 false)\n";

        return arrayRegister;
    }

    static void assign_int(String id, String value) {
        main_text += "store i32 " + value + ", i32* %" + id + "\n";
    }

    static void assign_global_int(String id, String value) {
        main_text += "store i32 " + value + ", i32* 2" + id + "\n";
    }

    static void assign_real(String id, String value) {
        main_text += "store double " + value + ", double* %" + id + "\n";
    }
    static void assign_global_real(String id, String value) {
        main_text += "store double " + value + ", double* @" + id + "\n";
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
        main_text += "%" + (register) + " = alloca [" + (size) + " x i32]\n";
        register++;
        int registerAllocatedArray = register - 1;
        main_text += "%" + (register) + " = bitcast [" + (size) + " x i32]* %" + (register - 1) + " to i8*\n";
        register++;
        main_text += "call void @llvm.memcpy.p0i8.p0i8.i64(i8* %" + (register - 1) + ", i8* bitcast ([" + (size) + " x i32]* " + (globalArrayName) + " to i8*), i64 " + (size * 4) + " , i1 false)\n";
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
        main_text += "%" + (register) + " = alloca [" + (size) + " x double]\n";
        register++;
        int registerAllocatedArray = register - 1;
        main_text += "%" + (register) + " = bitcast [" + (size) + " x double]* %" + (register - 1) + " to i8*\n";
        register++;
        main_text += "call void @llvm.memcpy.p0i8.p0i8.i64(i8* %" + (register - 1) + ", i8* bitcast ([" + (size) + " x double]* " + (globalArrayName) + " to i8*), i64 " + (size * 8) + " , i1 false)\n";
        return registerAllocatedArray;
    }

    public static void getPtrArrayAndStoreValue(int size, String value, int arrayRegisterPtr, int arrayIdx) {
        main_text += "%" + register + " = getelementptr inbounds [" + size + " x double], [" + size + " x double]* %" + arrayRegisterPtr + ", i32 0, i32 " + arrayIdx + "\n";
        register++;
        main_text += "store double " + (value) + ", double* %" + (register - 1) + "\n";
    }

    static void printf_int(String id) {
        main_text += "%" + register + " = load i32, i32* %" + id + "\n";
        register++;
        main_text += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (register - 1) + ")\n";
        register++;
    }

    static void printf_double(String id) {
        main_text += "%" + register + " = load double, double* %" + id + "\n";
        register++;
        main_text += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (register - 1) + ")\n";
        register++;
    }

    static void printf_value_int(String value) {
        main_text += "%" + register + " = alloca i32\n";
        register++;
        main_text += "store i32 " + value + ", i32* %" + (register - 1) + "\n";
        main_text += "%" + register + " = load i32, i32* %" + (register - 1) + "\n";
        register++;
        main_text += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %" + (register - 1) + ")\n";
        register++;
    }

    static void printf_value_double(String value) {
        main_text += "%" + register + " = alloca double\n";
        register++;
        main_text += "store double " + value + ", double* %" + (register - 1) + "\n";
        main_text += "%" + register + " = load double, double* %" + (register - 1) + "\n";
        register++;
        main_text += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %" + (register - 1) + ")\n";
        register++;
    }

    static void printf_string(String id, int length) {
//        main_text += "%"+register+" = load i8*, i8** @"+id+"\n";
        main_text += "%" + register + " = getelementptr inbounds [" + (length + 1) + " x i8], [" + (length + 1) + " x i8]* %" + id + ", i32 0, i32 0\n";
        register++;
        main_text += "%" + register + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strps, i32 0, i32 0), i8* %" + (register - 1) + ")\n";
        register++;
    }

    static void scanf_int(String id) {
        main_text += "%" + register + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* %" + id + ")\n";
        register++;
    }

    static void scanf_double(String id) {
        main_text += "%" + register + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strd, i32 0, i32 0), double* %" + id + ")\n";
        register++;
    }

    static void add_int(String val1, String val2) {
        main_text += "%" + register + " = add i32 " + val1 + ", " + val2 + "\n";
        register++;
    }

    static void add_double(String val1, String val2) {
        main_text += "%" + register + " = fadd double " + val1 + ", " + val2 + "\n";
        register++;
    }

    static void sub_int(String val1, String val2) {
        main_text += "%" + register + " = sub i32 " + val1 + ", " + val2 + "\n";
        register++;
    }

    static void sub_double(String val1, String val2) {
        main_text += "%" + register + " = fsub double " + val1 + ", " + val2 + "\n";
        register++;
    }

    static void mult_int(String val1, String val2) {
        main_text += "%" + register + " = mul i32 " + val1 + ", " + val2 + "\n";
        register++;
    }

    static void mult_double(String val1, String val2) {
        main_text += "%" + register + " = fmul double " + val1 + ", " + val2 + "\n";
        register++;
    }

    static void div_int(String val1, String val2) {
        main_text += "%" + register + " = sdiv i32 " + val1 + ", " + val2 + "\n";
        register++;
    }

    static void div_double(String val1, String val2) {
        main_text += "%" + register + " = fdiv double " + val1 + ", " + val2 + "\n";
        register++;
    }

    static void load_int(String id) {
        main_text += "%" + register + " = load i32, i32* %" + id + "\n";
        register++;
    }

    static void load_global_int(String id) {
        main_text += "%" + register + " = load i32, i32* @" + id + "\n";
        register++;
    }

    static void load_double(String id) {
        main_text += "%" + register + " = load double, double* %" + id + "\n";
        register++;
    }

    static void load_global_double(String id) {
        main_text += "%" + register + " = load double, double* @" + id + "\n";
        register++;
    }

    public static void getArrayPtrInt(int arrayAddress, int numberOfElems, String idx) {
        main_text += "%" + register + " = getelementptr inbounds [" + numberOfElems + " x i32], " +
                "[" + numberOfElems + " x i32]* %" + arrayAddress + ", i32 0, i32 " + idx + "\n";
        register++;
    }

    public static void getArrayPtrReal(int arrayAddress, int numberOfElems, String idx) {
        main_text += "%" + register + " = getelementptr inbounds [" + numberOfElems + " x double]," +
                " [" + numberOfElems + " x double]* %" + arrayAddress + ", i32 0, i32 " + idx + "\n";
        register++;
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
        main_text += "%" + register + " = icmp " + sign + " i32 " + v1 + ", " + v2 + "\n";
        register++;
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
        main_text += "%" + register + " = fcmp " + sign + " double " + v1 + ", " + v2 + "\n";
        register++;
    }

    static void ifstart() {
        br++;
        main_text += "br i1 %" + (register - 1) + ", label %true" + br + ", label %false" + br + "\n";
        main_text += "true" + br + ":\n";
        brstack.push(br);
    }

    static void ifend() {
        int b = brstack.pop();
        main_text += "br label %false" + b + "\n";
        main_text += "false" + b + ":\n";
    }

    static void repeatstart(String repetitions) {
        declare_int(Integer.toString(register));
        int counter = register;
        register++;
        assign_int(Integer.toString(counter), "0");
        br++;
        main_text += "br label %cond" + br + "\n";
        main_text += "cond" + br + ":\n";

        load_int(Integer.toString(counter));
        add_int("%" + (register - 1), "1");
        assign_int(Integer.toString(counter), "%" + (register - 1));

        main_text += "%" + register + " = icmp slt i32 %" + (register - 2) + ", " + repetitions + "\n";
        register++;

        main_text += "br i1 %" + (register - 1) + ", label %true" + br + ", label %false" + br + "\n";
        main_text += "true" + br + ":\n";
        printf_int(String.valueOf(counter));
        brstack.push(br);
    }

    static void repeatend() {
        int b = brstack.pop();
        main_text += "br label %cond" + b + "\n";
        main_text += "false" + b + ":\n";
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
        main_text += "br label %cond" + br + "\n";
        main_text += "cond" + br + ":\n";

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
        main_text += "%" + register + " = icmp slt i32 %" + (register - 2) + ", %" + (register - 1) + "\n";
        register++;

        main_text += "br i1 %" + (register - 1) + ", label %true" + br + ", label %false" + br + "\n";
        main_text += "true" + br + ":\n";

        brstack.push(br);
    }

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
}
