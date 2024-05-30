package types;

import java.util.HashMap;

public class FuncType {
    public String name;
    public HashMap<String,VarType> variables;
    public String returnType;

    public FuncType(String name, HashMap<String, VarType> variables, String returnType) {
        this.name = name;
        this.variables = variables;
        this.returnType = returnType;
    }
}
