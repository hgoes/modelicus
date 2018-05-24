package modelicus.config;

import modelicus.typing.BasicType;
import modelicus.typing.Type;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

import java.lang.reflect.Method;

public class ParsedLanguage extends Language {
    private Map<String,BasicType> types;
    private String name;
    private String implementation;
    private Map<String,PredicateDescription> predicates;
    private static class DynamicBasicType extends BasicType {
        private java.lang.String name;
        public DynamicBasicType(java.lang.String n) {
            name = n;
        }
        public java.lang.String toString() {
            return name;
        }
    }
    private Method parserMethod;
    private Type nameRefType;
    private static class DynamicProperty extends PropertyDescription {
        private Method getter;
        private Type signature;
        public DynamicProperty(Method g,Type t) {
            getter = g;
            signature = t;
        }
        public Type getSignature() {
            return signature;
        }
        public String apply(Object to) {
            try {
                return (String)getter.invoke(to);
            } catch (Exception e) {
                return "";
            }
        }
    }
    private Map<String,DynamicProperty> properties;
    public ParsedLanguage(File f) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
            Element root = doc.getDocumentElement();
            name = root.getAttributes().getNamedItem("name").getNodeValue();
            implementation = root.getAttributes().getNamedItem("implementation").getNodeValue();
            for(Node child = root.getFirstChild();child!=null;child=child.getNextSibling()) {
                if(child.getNodeType()==Node.ELEMENT_NODE) {
                    Element el = (Element)child;
                    if("types".equals(el.getTagName())) {
                        types = new HashMap<String,BasicType>();
                        for(Node cur = el.getFirstChild();cur!=null;cur = cur.getNextSibling()) {
                            if(cur.getNodeType()==Node.ELEMENT_NODE) {
                                String name = cur.getTextContent();
                                types.put(name,new DynamicBasicType(name));
                            }
                        }
                    } else if("predicates".equals(el.getTagName())) {
                        predicates = new HashMap<String,PredicateDescription>();
                        for(Node cur = el.getFirstChild();cur!=null;cur = cur.getNextSibling()) {
                            if(cur.getNodeType()==Node.ELEMENT_NODE) {
                                String name;
                                Type[] sig,sig_not;
                                LinkedList<VariantDescription> variants = new LinkedList<VariantDescription>();
                                LinkedList<VariantDescription> variants_not = new LinkedList<VariantDescription>();
                                name = cur.getAttributes().getNamedItem("name").getNodeValue();
                                sig = parseSignature(cur.getAttributes().getNamedItem("signature").getNodeValue());
                                sig_not = parseSignature(cur.getAttributes().getNamedItem("signature_not").getNodeValue());
                                for(Node sub_cur = cur.getFirstChild();sub_cur!=null;sub_cur=sub_cur.getNextSibling()) {
                                    if(sub_cur.getNodeType()==Node.ELEMENT_NODE) {
                                        String vname;
                                        int instantiation;
                                        int badness;
                                        vname = sub_cur.getAttributes().getNamedItem("name").getNodeValue();
                                        badness = Integer.parseInt(sub_cur.getAttributes().getNamedItem("badness").getNodeValue());
                                        instantiation = parseInstantiation(sub_cur.getAttributes().getNamedItem("instantiation").getNodeValue());
                                        if(sub_cur.getAttributes().getNamedItem("negated").getNodeValue().equals("false")) {
                                            variants.addLast(new VariantDescription(badness,instantiation,vname));
                                        } else {
                                            variants_not.addLast(new VariantDescription(badness,instantiation,vname));
                                        }
                                    }
                                }
                                predicates.put(name,new PredicateDescription(sig,sig_not
                                    ,variants.toArray(new VariantDescription[0]),variants_not.toArray(new VariantDescription[0])));
                            }
                        }
                    } else if("parser".equals(el.getTagName())) {
                        String className =  el.getAttributes().getNamedItem("class").getNodeValue();
                        String methodName = el.getAttributes().getNamedItem("method").getNodeValue();
                        Class parser = Class.forName(className);
                        parserMethod = parser.getMethod(methodName,Class.forName("modelicus.config.Language"),Class.forName("java.lang.String"),Class.forName("modelicus.typing.Type"));
                    } else if("nameType".equals(el.getTagName())) {
                        String sig = el.getAttributes().getNamedItem("signature").getNodeValue();
                        nameRefType = parseSignature(sig)[0];
                    } else if("properties".equals(el.getTagName())) {
                        properties = new HashMap<String,DynamicProperty>();
                        for(Node cur = el.getFirstChild();cur!=null;cur=cur.getNextSibling()) {
                            if(cur.getNodeType()==Node.ELEMENT_NODE) {
                                String name = cur.getAttributes().getNamedItem("name").getNodeValue();
                                String sig = cur.getAttributes().getNamedItem("signature").getNodeValue();
                                String cls = cur.getAttributes().getNamedItem("class").getNodeValue();
                                String mth = cur.getAttributes().getNamedItem("method").getNodeValue();
                                properties.put(name,new DynamicProperty(Class.forName(cls).getMethod(mth),parseSignature(sig)[0]));
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public Type[] parseSignature(String sig) {
        String[] args = sig.split("#");
        Type[] result = new Type[args.length];
        for(int i=0;i<args.length;i++) {
            String[] basics = args[i].split("\\|");
            BasicType[] tp = new BasicType[basics.length];
            for(int j=0;j<basics.length;j++) {
                tp[j] = typeFromString(basics[j]);
            }
            result[i] = Type.construct(tp);
        }
        return result;
    }
    public static int parseInstantiation(String str) {
        int inst = 0;
        for(int i=0;i<str.length();i++) {
            if(str.charAt(i)=='!') {
                inst = inst | (1 << i);
            }
        }
        return inst;
    }
    public BasicType typeFromString(String name) {
        BasicType res = super.typeFromString(name);
        if(res!=null) {
            return res;
        }
        return types.get(name);
    }
	public PredicateDescription getDescription(String name) {
        return predicates.get(name);
    }
    public PropertyDescription getProperty(String name) {
        return properties.get(name);
    }
	public Object parseAST(String source,Type allowed) {
        try {
            return parserMethod.invoke(null,this,source,allowed);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	public Type nameRefType() {
        return nameRefType;
    }
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Language");
        builder.append(name);
        builder.append("(");
        builder.append(implementation);
        builder.append(") [");
        Iterator<String> it = types.keySet().iterator();
        while(it.hasNext()) {
            builder.append(it.next());
            if(it.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("] {");

        Iterator<String> it2 = predicates.keySet().iterator();
        while(it2.hasNext()) {
            builder.append(it2.next());
            if(it2.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("}");
        return builder.toString();
    }
}
