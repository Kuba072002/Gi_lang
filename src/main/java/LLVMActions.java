import org.antlr.v4.runtime.tree.ParseTree;
import types.ArrayType;
import types.StringType;
import types.VarType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

public class LLVMActions extends Gi_langBaseListener {
    HashMap<String, VarType> variables = new HashMap<>();
    HashMap<String, ArrayType> arrays = new HashMap<>();
    HashMap<String, StringType> strings = new HashMap<>();
    Stack<Value> stack = new Stack<>();

    @Override
    public void exitValue(Gi_langParser.ValueContext ctx) {
        if (ctx.INT() != null)
            stack.push(new Value(ctx.INT().getText(), VarType.INT));
        else if (ctx.REAL() != null)
            stack.push(new Value(ctx.REAL().getText(), VarType.REAL));
        else if (ctx.ID() != null) {
            stack.push(new Value(ctx.ID().getText(), VarType.ID));
            if (strings.containsKey(ctx.ID().getText()))
                stack.push(new Value(ctx.ID().getText(), VarType.STRING));
            else if (!variables.containsKey(ctx.ID().getText()))
                error(ctx.getStart().getLine(), "Undeclared variable");
        }
    }

    @Override
    public void exitAssignString(Gi_langParser.AssignStringContext ctx) {
        String stringName = ctx.ID().getText();
        stringName.concat("asd");
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
        if (array.varType == VarType.INT) {
            LLVMGenerator.getArrayPtrInt(array.arrayAddress, array.size, idx);
            LLVMGenerator.load_int(String.valueOf(LLVMGenerator.register - 1));
            stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.INT));
        }
        if (array.varType == VarType.REAL) {
            LLVMGenerator.getArrayPtrReal(array.arrayAddress, array.size, idx);
            LLVMGenerator.load_double(String.valueOf(LLVMGenerator.register - 1));
            stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.REAL));
        }
    }

    @Override
    public void exitAdd(Gi_langParser.AddContext ctx) {
        Value v1 = getValue();
        Value v2 = getValue();

        if (v1.varType == v2.varType) {
            if (v1.varType == VarType.INT) {
                LLVMGenerator.add_int(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.INT));
            }
            if (v1.varType == VarType.REAL) {
                LLVMGenerator.add_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.REAL));
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
            if (v1.varType == VarType.INT) {
                LLVMGenerator.sub_int(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.INT));
            }
            if (v1.varType == VarType.REAL) {
                LLVMGenerator.sub_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.REAL));
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
            if (v1.varType == VarType.INT) {
                LLVMGenerator.mult_int(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.INT));
            }
            if (v1.varType == VarType.REAL) {
                LLVMGenerator.mult_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.REAL));
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
            if (v1.varType == VarType.INT) {
                LLVMGenerator.div_int(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.INT));
            }
            if (v1.varType == VarType.REAL) {
                LLVMGenerator.div_double(v1.name, v2.name);
                stack.push(new Value("%" + (LLVMGenerator.register - 1), VarType.REAL));
            }
        } else {
            error(ctx.getStart().getLine(), "Div type mismatch");
        }
    }

    @Override
    public void exitAssign(Gi_langParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        Value v = getValue();
        boolean isDeclared = variables.containsKey(id);
        if (isDeclared) {
            VarType varType = variables.get(id);
            if (varType != v.varType) {
                error(ctx.getStart().getLine(), "Wrong value type for variable " + id + "( " + v.varType + " )");
            }
        }
        if (v.varType == VarType.INT) {
            if (!isDeclared) {
                LLVMGenerator.declare_int(id);
                variables.put(id, VarType.INT);
            }
            LLVMGenerator.assign_int(id, v.name);
        }
        if (v.varType == VarType.REAL) {
            if (!isDeclared) {
                LLVMGenerator.declare_real(id);
                variables.put(id, VarType.REAL);
            }
            LLVMGenerator.assign_real(id, v.name);
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
            arrays.put(arrayId, new ArrayType(arrayId, VarType.INT, arrayAddress, intValues.size()));
        }
        if (!realValues.isEmpty()) {
            int arrayAddress = LLVMGenerator.allocateDoubleArrayAndStoreValues(realValues.size(), realValues.toArray(String[]::new));
            arrays.put(arrayId, new ArrayType(arrayId, VarType.REAL, arrayAddress, realValues.size()));
        }
    }

    @Override
    public void exitPrint(Gi_langParser.PrintContext ctx) {
        if (ctx.value().INT() != null) {
            String value = ctx.value().INT().getText();
            LLVMGenerator.printf_value_int(value);
        } else if (ctx.value().REAL() != null) {
            String value = ctx.value().REAL().getText();
            LLVMGenerator.printf_value_double(value);
        } else if (ctx.value().ID() != null) {
            String id = ctx.value().ID().getText();
            if (variables.containsKey(id)) {
                VarType type = variables.get(id);
                if (type == VarType.INT) {
                    LLVMGenerator.printf_int(id);
                } else if (type == VarType.REAL) {
                    LLVMGenerator.printf_double(id);
                }
                return;
            } else if (strings.containsKey(id)) {
                StringType stringType = strings.get(id);
//                System.out.println("///////////////"+stringType);
                LLVMGenerator.printf_string(stringType.name, stringType.length);
            } else error(ctx.getStart().getLine(), "Unrecoginezd variable " + id);
        } else {
            error(ctx.getStart().getLine(), "Invalid print statement");
        }
    }

    @Override
    public void exitRead(Gi_langParser.ReadContext ctx) {
        String ID = ctx.ID().getText();
        if (!variables.containsKey(ID)) {
            error(ctx.getStart().getLine(), "Undeclared variable");
        }
        VarType type = variables.get(ID);
        if (type == VarType.INT) {
            LLVMGenerator.scanf_int(ID);
        } else if (type == VarType.REAL) {
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
            if (v1.varType == VarType.INT) {
                LLVMGenerator.icmp_int(v1.name, v2.name, condition);
            }
            if (v1.varType == VarType.REAL) {
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
        if (v.varType == VarType.REAL)
            error(ctx.getStart().getLine(),"Wrong value type for range statement - real");

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
        if (array.varType == VarType.INT) {
            LLVMGenerator.declare_int(name);
            variables.put(name, VarType.INT);
        }
        if (array.varType == VarType.REAL) {
            LLVMGenerator.declare_real(name);
            variables.put(name, VarType.INT);
        }

        LLVMGenerator.loopstart(name,array);

    }

    @Override
    public void exitProg(Gi_langParser.ProgContext ctx) {
        System.out.println(LLVMGenerator.generate());
    }

    Value getValue() {
        Value v = stack.pop();
        if (v.varType == VarType.ID) convertVar(v);
        return v;
    }

    void convertVar(Value v) {
        if (variables.containsKey(v.name)) {
            v.varType = variables.get(v.name);
            if (v.varType == VarType.INT) {
                LLVMGenerator.load_int(v.name);
                v.name = "%" + (LLVMGenerator.register - 1);
            } else if (v.varType == VarType.REAL) {
                LLVMGenerator.load_double(v.name);
                v.name = "%" + (LLVMGenerator.register - 1);
            }
        }
    }

    void error(int line, String msg) {
        System.err.println("Error at line " + line + ", " + msg);
        System.exit(-1);
    }
}
