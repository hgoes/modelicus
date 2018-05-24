package modelicus.typing;

import java.util.Set;
import java.util.Map;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

public class Environment {
    private static class TypeEquation {
        public Object leftHand;
        public TypeVariable[] rightHand;
        public TypeEquation(Object l,TypeVariable r) {
            leftHand = l;
            rightHand = new TypeVariable[] {r};
        }
        public TypeEquation(Object l,TypeVariable[] r) {
            leftHand = l;
            rightHand = r;
        }
        public boolean isGrounded() {
            for(TypeVariable var : rightHand) {
                if(var.resolved==null) {
                    return false;
                }
            }
            return true;
        }
        public Type[] grounded() {
            Type[] res = new Type[rightHand.length];
            for(int i = 0; i<rightHand.length; i++) {
                res[i] = rightHand[i].resolved;
                if(res[i]==null) return null;
            }
            return res;
        }
        public boolean tautology() {
            return leftHand==rightHand[0].ref;
        }
        public String toString() {
            return leftHand.toString()+" = "+java.util.Arrays.toString(rightHand);
        }
    }
    private static class TypeVariable {
        public Type resolved;
        public Object ref;
        public TypeVariable(Type res) {
            resolved = res;
            ref = null;
        }
        public TypeVariable(Object obj) {
            ref = obj;
            resolved = null;
        }
        public String toString() {
            if(resolved!=null) {
                return resolved.toString();
            } else {
                return ref.toString();
            }
        }
    }
    private Set<TypeEquation> equations;
    public Environment() {
        equations = new HashSet<TypeEquation>();
    }
    public void addExprType(Object expr,Type tp) {
        equations.add(new TypeEquation(expr,new TypeVariable(tp)));
    }
    public void addEqConstraint(Object e1,Object e2) {
        if(!e1.equals(e2)) {
            equations.add(new TypeEquation(e1,new TypeVariable(e2)));
        }
    }
    private static TypeEquation unifyEquations(TypeEquation eq1,TypeEquation eq2,Set<TypeEquation> resulting) throws TypeError {
        if(eq1.leftHand!=eq2.leftHand) {
            return null;
        }
        TypeVariable[] tp1 = eq1.rightHand;
        TypeVariable[] tp2 = eq2.rightHand;
        return new TypeEquation(eq1.leftHand,unifyVariables(tp1,tp2,resulting));
    }
    private static TypeVariable[] unifyVariables(TypeVariable[] tp1,TypeVariable[] tp2,Set<TypeEquation> resulting) throws TypeError {
        if(tp1.length!=tp2.length) {
            throw new TypeError.DifferentNumberOfArguments(null,null);
        }
        TypeVariable[] res = new TypeVariable[tp1.length];
        for(int i=0;i<tp1.length;i++) {
            if(tp1[i].resolved!=null) {
                if(tp2[i].resolved!=null) {
                    Type cur = tp1[i].resolved.unifyAnd(tp2[i].resolved);
                    if(cur==null || cur.isTypeError()) {
                        throw new TypeError.CantUnify(tp1[i].resolved,tp2[i].resolved);
                    }
                    res[i] = new TypeVariable(cur);
                } else {
                    resulting.add(new TypeEquation(tp2[i].ref,new TypeVariable(tp1[i].resolved)));
                    res[i] = tp1[i];
                }
            } else {
                if(tp2[i].resolved!=null) {
                    resulting.add(new TypeEquation(tp1[i].ref,new TypeVariable(tp2[i].resolved)));
                    res[i] = tp2[i];
                } else {
                    resulting.add(new TypeEquation(tp1[i].ref,new TypeVariable(tp2[i].ref)));
                    res[i] = tp1[i];
                }
            }
        }
        return res;
    }
    private static TypeEquation replaceOccurances(TypeEquation eq,Object what,Type with) {
        return new TypeEquation(eq.leftHand,replaceOccurances(eq.rightHand,what,new TypeVariable(with)));
    }
    private static TypeVariable[] replaceOccurances(TypeVariable[] where,Object what,TypeVariable with) {
        TypeVariable[] res = new TypeVariable[where.length];
        for(int i=0;i<where.length;i++) {
            if(where[i].ref==what) {
                res[i] = with;
            } else {
                res[i] = where[i];
            }
        }
        return res;
    }
    public Map<String,Type[]> solve() throws TypeError {
        Map<String,Type[]> res = new HashMap<String,Type[]>();
        solve(res);
        return res;
    }
    public void solve(Map<String,Type[]> mapping) throws TypeError {
        Map<Object,TypeVariable[]> tmp = new HashMap<Object,TypeVariable[]>();
        while(equations.size()>0) {
            System.out.println("STEP!");
            for(TypeEquation eq : equations) {
                System.out.println(eq.toString());
            }
            Set<TypeEquation> nequations = new HashSet<TypeEquation>();
            Iterator<TypeEquation> it = equations.iterator();
            while(it.hasNext()) {
                TypeEquation eq = it.next();
                System.out.println("Processing "+eq.toString());
                it.remove();
                if(eq.tautology()) {
                    System.out.println("Tautology, dont bother");
                    continue;
                }
                TypeVariable[] res = tmp.get(eq.leftHand);
                if(res!=null) {
                    System.out.println("Already defined as "+java.util.Arrays.toString(res)+", merging...");
                    res = unifyVariables(eq.rightHand,res,nequations);
                    System.out.println("...result: "+java.util.Arrays.toString(res));
                } else {
                    res = eq.rightHand;
                }
                tmp.put(eq.leftHand,res);
                {
                    //Replace all occurances in tmp
                    Iterator<Map.Entry<Object,TypeVariable[]>> it2 = tmp.entrySet().iterator();
                    while(it2.hasNext()) {
                        Map.Entry<Object,TypeVariable[]> entr = it2.next();
                        if(entr.getKey()!=eq.leftHand) {
                            entr.setValue(replaceOccurances(entr.getValue(),eq.leftHand,res[0]));
                        }
                    }
                }
                for(TypeEquation eq2 : equations) {
                    if(eq!=eq2) {
                        eq2.rightHand = replaceOccurances(eq2.rightHand,eq.leftHand,res[0]);
                    }
                }
                for(TypeEquation eq2 : nequations) {
                    if(eq!=eq2) {
                        eq2.rightHand = replaceOccurances(eq2.rightHand,eq.leftHand,res[0]);
                    }
                }
            }
            equations.addAll(nequations);
        }
        for(Map.Entry<Object,TypeVariable[]> entr : tmp.entrySet()) {
            if(entr.getKey() instanceof String) {
                Type[] res = new Type[entr.getValue().length];
                for(int i=0;i<res.length;i++) {
                    res[i] = entr.getValue()[i].resolved;
                    if(res[i]==null) {
                        throw new TypeError.AmbigousType((String)entr.getKey());
                    }
                }
                mapping.put((String)entr.getKey(),res);
            }
        }
    }
    public Environment hide(String var) {
        Environment result = new Environment();
        for(TypeEquation eq : equations) {
            if(eq.leftHand!=var) {
                boolean isIn = false;
                for(TypeVariable v : eq.rightHand) {
                    if(v.ref==var) {
                        isIn=true;
                        break;
                    }
                }
                if(!isIn) {
                    result.equations.add(eq);
                }
            }
        }
        return result;
    }
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(TypeEquation eq : equations) {
            builder.append(eq.toString());
            builder.append("\n");
        }
        return builder.toString();
    }
}
