import org.antlr.v4.runtime.tree.ParseTree;
import types.ArrayType;
import types.FuncType;
import types.StringType;
import types.VarType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static types.VarType.*;

public class LLVMActions extends Gi_langBaseListener {
    //    HashMap<String, VarType> variables = new HashMap<>();
    HashMap<String, VarType> localVariables = new HashMap<>();
    Map<String, String> localVariablesMapped = new HashMap<String, String>();
    Map<String, String> functionsWithRetType = new HashMap<>();
    HashMap<String, VarType> globalVariables = new HashMap<>();
    //    HashMap<String, HashMap<String, VarType>> structs = new HashMap<>();
//    HashMap<String, String> declaredStructs = new HashMap<>();
    HashMap<String, FuncType> functions = new HashMap<>();
    HashMap<String, ArrayType> arrays = new HashMap<>();
    HashMap<String, StringType> strings = new HashMap<>();
    Stack<Value> stack = new Stack<>();

    @Override
    public void exitValue(Gi_langParser.ValueContext ctx) {
        if (ctx.INT() != null)
            stack.push(new Value(ctx.INT().getText(), INT));
        else if (ctx.REAL() != null)
            stack.push(new Value(ctx.REAL().getText(), REAL));
        else if (ctx.ID() != null) {
            var id = ctx.ID().getText();
            if (localVariables.containsKey(id) || globalVariables.containsKey(id))
                stack.push(new Value(id, ID));
            else if (strings.containsKey(id))
                stack.push(new Value(id, VarType.STRING));
            else error(ctx.getStart().getLine(), "Undeclared variable");
        } else if (ctx.functionExec() != null) {
            var str = ctx.functionExec().ID().getText();
            var varType = functionsWithRetType.get(str);
            if (varType == "i32"){
                stack.push(new Value("%"+(LLVMGenerator.register-1), INT));
            }if (varType == "double"){
                stack.push(new Value("%"+(LLVMGenerator.register-1), REAL));
            }
        }
    }

    @Override
    public void exitAssignString(Gi_langParser.AssignStringContext ctx) {
        String stringName = ctx.ID().getText();
        String stringContent = ctx.STRING().getText();
        stringContent = stringContent.substring(1, stringContent.length() - 1);
        int stringLengthWithNewLine = stringContent.length();

        int stringPointer = LLVMGenerator.declare_string(stringLengthWithNewLine, stringName, stringContent);
        strings.put(stringName, new StringType(String.valueOf(stringPointer), stringLengthWithNewLine, stringContent));
    }

    @Override
    public void exitStringValue(Gi_langParser.StringValueContext ctx) {
        if (ctx.ID() != null) {
            stack.push(new Value(ctx.ID().getText(), VarType.STRING));
        }
        if (ctx.STRING() != null) {
            String content = ctx.STRING().getText();
            content = content.substring(1, content.length() - 1);
            int lengthWithNewLine = content.length();

            String anonymousName = "anonymous" + LLVMGenerator.anonymousString;
            LLVMGenerator.anonymousString++;

            int stringPointer = LLVMGenerator.declare_string(lengthWithNewLine, anonymousName, content);
            strings.put(anonymousName, new StringType(String.valueOf(stringPointer), lengthWithNewLine, content));
            stack.push(new Value(anonymousName, VarType.STRING));
        }
    }

    @Override
    public void exitStringConcat(Gi_langParser.StringConcatContext ctx) {
        String stringName = ctx.ID().getText();
        if (strings.containsKey(stringName)) {
            error(ctx.getStart().getLine(), "String %s already defined".formatted(stringName));
        }
        Value value2 = stack.pop();
        Value value1 = stack.pop();
        StringType stringObj2 = strings.get(value2.name);
        StringType stringObj1 = strings.get(value1.name);

        int concatedLength = stringObj1.length + stringObj2.length;
        String concatedValue = stringObj1.content + stringObj2.content;

        int stringRegisterPointer = LLVMGenerator.declare_string(concatedLength, stringName, concatedValue);
        strings.put(stringName, new StringType(String.valueOf(stringRegisterPointer), concatedLength, concatedValue));

    }

    @Override
    public void exitArrValue(Gi_langParser.ArrValueContext ctx) {
        String arrayId = ctx.ID().getText();
        if (!arrays.containsKey(arrayId)) {
            error(ctx.getStart().getLine(), "Array " + (arrayId) + " not declared");
        }
        ArrayType array = arrays.get(arrayId);
        String idx = ctx.INT().getText();
        if (array.varType == INT || array.varType == REAL) {
            LLVMGenerator.getArrayPtr(array.arrayAddress, array.size, idx, array.varType);
            LLVMGenerator.load_int(String.valueOf(LLVMGenerator.register - 1));
            stack.push(new Value("%" + (LLVMGenerator.register - 1), array.varType));
        }
    }

    @Override
    public void exitAdd(Gi_langParser.AddContext ctx) {
        Value v1 = getValue();
        Value v2 = getValue();

        if (v1.varType == v2.varType) {
            if (v1.varType == INT) {
                LLVMGenerator.add_int(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), INT));
            }
            if (v1.varType == REAL) {
                LLVMGenerator.add_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), REAL));
            }
        } else {
            error(ctx.getStart().getLine(), "Add type mismatch");
        }
    }

    @Override
    public void exitSub(Gi_langParser.SubContext ctx) {
        Value v1 = getValue();
        Value v2 = getValue();

        if (v1.varType == v2.varType) {
            if (v1.varType == INT) {
                LLVMGenerator.sub_int(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), INT));
            }
            if (v1.varType == REAL) {
                LLVMGenerator.sub_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), REAL));
            }
        } else {
            error(ctx.getStart().getLine(), "Sub type mismatch");
        }
    }

    @Override
    public void exitMul(Gi_langParser.MulContext ctx) {
        Value v1 = getValue();
        Value v2 = getValue();

        if (v1.varType == v2.varType) {
            if (v1.varType == INT) {
                LLVMGenerator.mult_int(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), INT));
            }
            if (v1.varType == REAL) {
                LLVMGenerator.mult_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), REAL));
            }
        } else {
            error(ctx.getStart().getLine(), "Mul type mismatch");
        }
    }

    @Override
    public void exitDiv(Gi_langParser.DivContext ctx) {
        Value v1 = getValue();
        Value v2 = getValue();

        if (v1.varType == v2.varType) {
            if (v1.varType == INT) {
                LLVMGenerator.div_int(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), INT));
            }
            if (v1.varType == REAL) {
                LLVMGenerator.div_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), REAL));
            }
        } else {
            error(ctx.getStart().getLine(), "Div type mismatch");
        }
    }

    @Override
    public void exitAssign(Gi_langParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        Value v = getValue();
        boolean isDeclared;
        isDeclared = localVariables.containsKey(id);
        if (LLVMGenerator.global && localVariablesMapped.containsKey(id)) {
            isDeclared = true;
            id = localVariablesMapped.get(id);
        }
        if (isDeclared) {
            VarType varType = localVariables.get(id);
            if (varType != v.varType) {
                if (varType == INT && v.varType == REAL || varType == REAL && v.varType == INT) {
                    LLVMGenerator.changeType(id, v.varType, varType);
                    return;
                }
            }
        }
        if (v.varType == INT || v.varType == REAL) {
            if (!isDeclared) {
                LLVMGenerator.declare(id, v.varType);
                localVariables.put(id, v.varType);
            }
            LLVMGenerator.assign(id, v.name, v.varType);
        }
    }

    @Override
    public void exitGlobalAssign(Gi_langParser.GlobalAssignContext ctx) {
        String id = ctx.ID().getText();
        Value v = getValue();
        boolean isDeclared = globalVariables.containsKey(id);
        if (isDeclared) {
            VarType varType = globalVariables.get(id);
            if (varType != v.varType) {
                error(ctx.getStart().getLine(), "Wrong value type for variable " + id + "( " + v.varType + " )");
            }
            if (v.varType == INT) LLVMGenerator.assign_global_int(id, v.name);
            if (v.varType == REAL) LLVMGenerator.assign_global_real(id, v.name);
        }
        if (v.varType == INT) {
            LLVMGenerator.declare_global_int(id, v.name);
            globalVariables.put(id, INT);
        }
        if (v.varType == REAL) {
            LLVMGenerator.declare_global_real(id, v.name);
            globalVariables.put(id, REAL);
        }
    }

    @Override
    public void exitAssignArr(Gi_langParser.AssignArrContext ctx) {
        String arrayId = ctx.ID().getText();
        var intValues = ctx.INT().stream().map(ParseTree::getText).toList();
        var realValues = ctx.REAL().stream().map(ParseTree::getText).toList();
        if (!intValues.isEmpty() && !realValues.isEmpty())
            error(ctx.getStart().getLine(), "Try to assign array with different types");
        if (!intValues.isEmpty()) {
            int arrayAddress = LLVMGenerator.allocateIntArrayAndStoreValues(arrayId, intValues.size(), intValues.toArray(String[]::new));
            arrays.put(arrayId, new ArrayType(arrayId, INT, arrayAddress, intValues.size()));
        }
        if (!realValues.isEmpty()) {
            int arrayAddress = LLVMGenerator.allocateDoubleArrayAndStoreValues(arrayId, realValues.size(), realValues.toArray(String[]::new));
            arrays.put(arrayId, new ArrayType(arrayId, REAL, arrayAddress, realValues.size()));
        }
    }

    @Override
    public void exitPrint(Gi_langParser.PrintContext ctx) {
        Value v = stack.pop();
        if (v.varType == ID) {
            VarType idVarType;
            if (localVariables.containsKey(v.name)) {
                idVarType = localVariables.get(v.name);
                if (LLVMGenerator.global && localVariablesMapped.containsKey(v.name))
                    v.name = localVariablesMapped.get(v.name);
                switch (idVarType) {
                    case INT -> LLVMGenerator.printf_int(v.name);
                    case REAL -> LLVMGenerator.printf_double(v.name);
                }
                return;
            } else if (globalVariables.containsKey(v.name)) {
                idVarType = globalVariables.get(v.name);
                switch (idVarType) {
                    case INT -> {
                        LLVMGenerator.load_global_int(v.name);
                        LLVMGenerator.printf_global_int(String.valueOf(LLVMGenerator.register - 1));
                    }
                    case REAL -> {
                        LLVMGenerator.load_global_double(v.name);
                        LLVMGenerator.printf_global_double(String.valueOf(LLVMGenerator.register - 1));
                    }
                }
                return;
            }
        }
        if (v.varType == INT) {
            LLVMGenerator.printf_value_int(v.name);
        } else if (v.varType == REAL) {
            LLVMGenerator.printf_value_double(v.name);
        } else if (v.varType == STRING) {
            StringType stringType = strings.get(v.name);
            LLVMGenerator.printf_string(stringType.name, stringType.length);
        } else {
            error(ctx.getStart().getLine(), "Invalid print statement");
        }
    }

    @Override
    public void exitRead(Gi_langParser.ReadContext ctx) {
        String ID = ctx.ID().getText();
        if (!localVariables.containsKey(ID)) {
            error(ctx.getStart().getLine(), "Undeclared variable");
        }
        VarType type = localVariables.get(ID);
        if (type == INT) {
            LLVMGenerator.scanf_int(ID);
        } else if (type == REAL) {
            LLVMGenerator.scanf_double(ID);
        } else {
            error(ctx.getStart().getLine(), "Can't read value");
        }
    }

    @Override
    public void exitIfCondition(Gi_langParser.IfConditionContext ctx) {
        Value v2 = getValue();
        Value v1 = getValue();
        String condition = ctx.condition().getText();

        if (v1.varType == v2.varType) {
            if (v1.varType == INT) {
                LLVMGenerator.icmp_int(v1.name, v2.name, condition);
            }
            if (v1.varType == REAL) {
                LLVMGenerator.icmp_double(v1.name, v2.name, condition);
            }
        } else {
            error(ctx.getStart().getLine(), "IF statement type mismatch");
        }
    }

    @Override
    public void enterBlockIf(Gi_langParser.BlockIfContext ctx) {
        LLVMGenerator.ifstart();
    }

    @Override
    public void exitBlockIf(Gi_langParser.BlockIfContext ctx) {
        LLVMGenerator.ifend();
    }

    @Override
    public void exitRangeValue(Gi_langParser.RangeValueContext ctx) {
        Value v = getValue();
        if (v.varType == REAL)
            error(ctx.getStart().getLine(), "Wrong value type for range statement - real");

        LLVMGenerator.repeatstart(v.name);
    }

    @Override
    public void exitBlockLoop(Gi_langParser.BlockLoopContext ctx) {
        LLVMGenerator.repeatend();
    }

    @Override
    public void exitForHead(Gi_langParser.ForHeadContext ctx) {
        String name = ctx.ID(0).getText();
        String arrName = ctx.ID(1).getText();
        if (!arrays.containsKey(arrName))
            error(ctx.getStart().getLine(), "Array { %s } not exit".formatted(arrName));
        ArrayType array = arrays.get(arrName);
        if (array.varType == INT || array.varType == REAL) {
            LLVMGenerator.declare(name, array.varType);
            localVariables.put(name, array.varType);
        }
        LLVMGenerator.loopstart(name, array);
    }

    @Override
    public void enterFunc(Gi_langParser.FuncContext ctx) {
        LLVMGenerator.global = true;
        localVariables.clear();
        localVariablesMapped.clear();
        String funcName = ctx.ID().getText();
        if (functions.containsKey(funcName)) error(ctx.getStart().getLine(), "Func already defined");
        localVariables = new HashMap<>();
        var ids = ctx.params().ID().stream().map(ParseTree::getText).toList();
        var types = ctx.params().type().stream().map(x -> VarType.valueOf(x.getText().toUpperCase())).toList();

        String retType = ctx.type().getText();
        String mappedRetType = "";
        if (retType.equals("real")) {
            mappedRetType = "double";
        } else if (retType.equals("int")) {
            mappedRetType = "i32";
        }
        functionsWithRetType.put(funcName, mappedRetType);

        int[] mappedArgsNames = LLVMGenerator.enterFunction(funcName, mappedRetType, ids, types);

        for (int i = 0; i < mappedArgsNames.length; i++) {
            if (types.get(i) == REAL) {
                localVariables.put(ids.get(i), VarType.REAL);
                localVariablesMapped.put(ids.get(i), String.valueOf(mappedArgsNames[i]));
            } else if (types.get(i) == INT) {
                localVariables.put(ids.get(i), VarType.INT);
                localVariablesMapped.put(ids.get(i), String.valueOf(mappedArgsNames[i]));
            }
        }
        functions.put(funcName, new FuncType(funcName, localVariables, null));


    }

    @Override
    public void exitFunc(Gi_langParser.FuncContext ctx) {
        String returnVariable = ctx.ret().ID().getText();
        String mappedReturnVariable = localVariablesMapped.containsKey(returnVariable) ?
                "%" + localVariablesMapped.get(returnVariable) : "%" + returnVariable;
        String returnVariableType = ctx.type().getText();
        LLVMGenerator.exitFunction(mappedReturnVariable, returnVariableType);
        LLVMGenerator.global = false;
        localVariables.clear();
    }

    @Override
    public void exitFunctionExec(Gi_langParser.FunctionExecContext ctx) {
        String ID = ctx.ID().getText();
        var ids = ctx.functionExecParams().ID().stream().map(ParseTree::getText).toList();
        var types = new ArrayList<String>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            String type;
            if (globalVariables.containsKey(ids.get(i))) {
                type = globalVariables.get(ids.get(i)).name();
                types.add(type);
            } else if (localVariables.containsKey(ids.get(i))) {
                type = localVariables.get(ids.get(i)).name();
                types.add(type);
            } else {
                error(ctx.getStart().getLine(), "Not found given variable " + ids.get(i));
            }
        }
        LLVMGenerator.execFunc(ID, functionsWithRetType.get(ID), types, ids);

    }

//    @Override
//    public void enterStruct(Gi_langParser.StructContext ctx) {
////        LLVMGenerator.global = true;
//        String name = ctx.ID().getText();
//        if (structs.containsKey(name))
//            error(ctx.getStart().getLine(), "Struct with %s already defined".formatted(name));
//        variables = new HashMap<>();
//    }
//
//    @Override
//    public void exitStruct(Gi_langParser.StructContext ctx) {
//        var ids = ctx.blockStruct().ID().stream()
//                .map(ParseTree::getText)
//                .toList();
//        var types = ctx.blockStruct().type().stream()
//                .map(t -> VarType.valueOf(t.getText().toUpperCase()))
//                .toList();
//        if (ids.isEmpty() || types.isEmpty())
//            error(ctx.getStart().getLine(), "Wrong struct definition - cannot be empty");
//        IntStream.range(0, ids.size()).forEach(i -> {
//            var id = ids.get(i);
//            if (variables.containsKey(id))
//                error(ctx.getStart().getLine(), "Variable %s already defined in struct".formatted(id));
//            variables.put(id, types.get(i));
//        });
//        String name = ctx.ID().getText();
//        LLVMGenerator.createStruct(name, types);
////        LLVMGenerator.global = false;
//        structs.put(name, variables);
//    }
//
//    @Override
//    public void exitStructAssign(Gi_langParser.StructAssignContext ctx) {
//        String id = ctx.ID(0).getText();
//        String structId = ctx.ID(1).getText();
//        LLVMGenerator.declare_stuct(id, structId);
//        declaredStructs.put(id,structId);
//    }
//
//
//    @Override
//    public void exitStructValueAssign(Gi_langParser.StructValueAssignContext ctx) {
//        String structVariableName = ctx.ID().getText();
//        if(!structuresVariablesMappedNames.containsKey(structVariableName)) error(ctx.getStart().getLine(), "Struct not initialized " + structVariableName);
//        String propName = ctx.structProp().getText();
//        Structure s = structuresVariablesToStructure.get(structVariableName);
//        if(!s.propNames.contains(propName)) error(ctx.getStart().getLine(), "Struct does not have this prop name " + propName);
//        String propType = s.types.get(s.propNames.indexOf(propName));
//        if(propType.equals("i32") && ctx.structPropValue().INT() == null || propType.equals("double") && ctx.structPropValue().REAL() == null ){
//            error(ctx.getStart().getLine(), "Trying to assign inappropriate value " + propName);
//        }
//        String mappedVariable = structuresVariablesMappedNames.get(structVariableName);
//        if(ctx.structPropValue().INT()!=null){
//            String value = ctx.structPropValue().INT().getText();
//            LLVMGenerator.getPtrToStructProp(s.name, mappedVariable, s.propNames.indexOf(propName));
//            LLVMGenerator.assign_i32(String.valueOf(LLVMGenerator.register-1),value);
//        }
//
//        if(ctx.structPropValue().REAL()!=null){
//            String value = ctx.structPropValue().REAL().getText();
//            LLVMGenerator.getPtrToStructProp(s.name, mappedVariable, s.propNames.indexOf(propName));
//            LLVMGenerator.assign_double(String.valueOf(LLVMGenerator.register-1),value);
//        }
//    }

    @Override
    public void exitProg(Gi_langParser.ProgContext ctx) {
        System.out.println(LLVMGenerator.generate());
    }

    Value getValue() {
        Value v = stack.pop();
        if (v.varType == ID) convertVar(v);
        return v;
    }

    void convertVar(Value v) {
//        if ()
        if (localVariables.containsKey(v.name)) {
            v.varType = localVariables.get(v.name);
            if (LLVMGenerator.global) {
                if (localVariablesMapped.containsKey(v.name)) {
                    v.name = localVariablesMapped.get(v.name);
                }
            }
            if (v.varType == INT || v.varType == REAL) {
                LLVMGenerator.load(v.name, v.varType);
                v.name = "%" + (LLVMGenerator.register - 1);
            }
        }
        if (globalVariables.containsKey(v.name)) {
            v.varType = globalVariables.get(v.name);
            if (v.varType == INT || v.varType == REAL) {
                LLVMGenerator.load_global(v.name, v.varType);
                v.name = "%" + (LLVMGenerator.register - 1);
            }
        }
    }

    void error(int line, String msg) {
        System.err.println("Error at line " + line + ", " + msg);
        System.exit(-1);
    }
}
