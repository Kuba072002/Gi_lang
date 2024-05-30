package types;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Structure {
    public String name;
    public LinkedHashMap<String,VarType> variables;

    public Structure(String name, LinkedHashMap<String, VarType> variables) {
        this.name = name;
        this.variables = variables;
    }
}
