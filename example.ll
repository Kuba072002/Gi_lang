declare i32 @printf(i8*, ...)
declare i32 @__isoc99_scanf(i8*, ...)
declare void @llvm.memset.p0i8.i64(i8* nocapture writeonly, i8, i64, i1 immarg)
declare void @llvm.memcpy.p0i8.p0i8.i64(i8* noalias nocapture writeonly, i8* noalias nocapture readonly, i64, i1 immarg)
@strpi = constant [4 x i8] c"%d\0A\00"
@strpd = constant [4 x i8] c"%f\0A\00"
@strs = constant [3 x i8] c"%d\00"
@strd = constant [4 x i8] c"%lf\00"
@strps = constant [4 x i8] c"%s\0A\00"
@.str = private unnamed_addr constant [3 x i8] c"%s\00"
@__const.main.arr = unnamed_addr constant [3x double] [double 2.1, double 3.3, double 5.5]
%struct.str1 = type { i32, double }
@ppp = global double 3.1111
define i32 @fun1(i32 %0, i32 %1) {
%3 = alloca i32
store i32 %0, i32* %3
%4 = alloca i32
store i32 %1, i32* %4
%5 = load i32, i32* %3
%6 = load i32, i32* %4
%7 = icmp ugt i32 %6, %5
br i1 %7, label %true1, label %false1
true1:
%8 = load i32, i32* %4
%9 = load i32, i32* %3
%10 = add i32 %8, %9
%sum = alloca i32
store i32 %10, i32* %sum
%11 = load i32, i32* %sum
%12 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %11)
br label %false1
false1:
%powerOf2 = alloca i32
store i32 1, i32* %powerOf2
%13 = alloca i32
store i32 0, i32* %13
br label %cond2
cond2:
%14 = load i32, i32* %13
%15 = add i32 %14, 1
store i32 %15, i32* %13
%16 = icmp slt i32 %14, 6
br i1 %16, label %true2, label %false2
true2:
%17 = load i32, i32* %powerOf2
%18 = mul i32 2, %17
store i32 %18, i32* %powerOf2
br label %cond2
false2:
%19 = load i32, i32* %powerOf2
%20 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %19)
%21 = load i32, i32* %4
%22 = load i32, i32* %3
%23 = add i32 %21, %22
store i32 %23, i32* %3
%24 = load i32, i32* %3
%25 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %24)
%26 = load double, double* @ppp
%27 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %26)
%28 = load i32, i32* %3
ret i32 %28
}
define i32 @main() nounwind{
%a2 = alloca i32
store i32 225, i32* %a2
%1 = load i32, i32* %a2
%2 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %1)
%b = alloca i32
store i32 300, i32* %b
%3 = load i32, i32* %a2
%4 = load i32, i32* %b
%5 = call i32 @fun1(i32 %3, i32 %4)
%returnVal = alloca i32
store i32 %5, i32* %returnVal
%6 = load i32, i32* %returnVal
%7 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %6)
%8 = alloca [3 x double]
%9 = bitcast [3 x double]* %8 to i8*
call void @llvm.memcpy.p0i8.p0i8.i64(i8* %9, i8* bitcast ([3 x double]* @__const.main.arr to i8*), i64 24 , i1 false)
%sum = alloca double
store double 0.0, double* %sum
%x = alloca double
%10 = alloca i32
store i32 3, i32* %10
%11 = alloca i32
store i32 -1, i32* %11
br label %cond3
cond3:
%12 = load i32, i32* %11
%13 = add i32 %12, 1
store i32 %13, i32* %11
%14 = getelementptr inbounds [3 x double], [3 x double]* %8, i32 0, i32 %13
%15 = load double, double* %14
store double %15, double* %x
%16 = load i32, i32* %11
%17 = load i32, i32* %10
%18 = icmp slt i32 %16, %17
br i1 %18, label %true3, label %false3
true3:
%19 = load double, double* %x
%20 = load double, double* %sum
%21 = fadd double %19, %20
store double %21, double* %sum
br label %cond3
false3:
%22 = load double, double* %sum
%23 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %22)
%sumInt = alloca i32
store i32 0, i32* %sumInt
%24 = load double, double* %sum
%25 = fptosi double %24 to i32
store i32 %25, i32* %sumInt
%26 = load i32, i32* %sumInt
%27 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %26)
%28 = alloca %struct.str1
%29 = getelementptr inbounds %struct.str1, %struct.str1* %28, i32 0, i32 0
store i32 1, i32* %29
%30 = getelementptr inbounds %struct.str1, %struct.str1* %28, i32 0, i32 1
store double 23.3, double* %30
%31 = getelementptr inbounds %struct.str1, %struct.str1* %28, i32 0, i32 0
%32 = load i32, i32* %31
%tmp = alloca i32
store i32 %32, i32* %tmp
%33 = load i32, i32* %tmp
%34 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpi, i32 0, i32 0), i32 %33)
%35 = getelementptr inbounds %struct.str1, %struct.str1* %28, i32 0, i32 1
%36 = load double, double* %35
%tmp2 = alloca double
store double %36, double* %tmp2
%37 = load double, double* %tmp2
%38 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strpd, i32 0, i32 0), double %37)
ret i32 0 }

