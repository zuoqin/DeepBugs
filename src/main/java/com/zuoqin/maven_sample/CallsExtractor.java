package com.zuoqin.maven_sample;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;


public class CallsExtractor {
    private static FileWriter callfile;
    public static JSONObject getNameOfASTNode(com.github.javaparser.ast.Node node, String pathname) {
        JSONObject res = new JSONObject();
        MethodCallExpr expr1 = (MethodCallExpr) node;

        res.put("src", pathname + " : " + node.getRange().get().begin.line + " - " +
                node.getRange().get().end.line);
        System.out.println(node.getBegin());
//        if (parent.toString().contains("." + node.)){
//
//        }
        System.out.println("2222 " + node.getClass().getName() + "; " + node);
        JSONArray args = new JSONArray();
        JSONArray argTypes = new JSONArray();
        for(int o=0; o<expr1.getArguments().size(); o++){
            argTypes.add(expr1.getArguments().get(o).calculateResolvedType().describe());
            if(expr1.getArguments().get(o).getClass().getName() == "com.github.javaparser.ast.expr.StringLiteralExpr" ||
                    expr1.getArguments().get(o).getClass().getName() == "com.github.javaparser.ast.expr.IntegerLiteralExpr"
            ){
                String arg = expr1.getArguments().get(o).toString();
                args.add("LIT:" + arg);
            }
            else if(expr1.getArguments().get(o).getClass().getName() == "com.github.javaparser.ast.expr.NameExpr"){
                NameExpr expr = (NameExpr) expr1.getArguments().get(o);
                //System.out.println(childNode);
                String arg = expr.calculateResolvedType().describe();
                args.add("ID:" + expr1.getArguments().get(o).toString());
            }

        }
        res.put("argumentTypes", argTypes);
        res.put("arguments", args);
        for(int j=0; j<node.getChildNodes().size(); j++){
            System.out.println(node.getChildNodes().get(j).getClass().getName());
            Node previousNode = null;
            if (j > 0){
                previousNode = node.getChildNodes().get(j-1);
            }
            Node childNode = node.getChildNodes().get(j);
            if(childNode.getClass().getName() == "com.github.javaparser.ast.expr.SimpleName") {
                if (j > 0 && previousNode.getClass().getName() == "com.github.javaparser.ast.expr.NameExpr") {
                    res.put("base", "ID:" + previousNode.toString());
                    res.put("callee", "ID:" + childNode.toString());
                    res.put("calleeLocation", "88888");
                } else{
                    res.put("base", "");
                    res.put("callee", "ID:" + childNode.toString());
                    res.put("calleeLocation", "7777");
                }
            }

//            if(childNode.getClass().getName() == "com.github.javaparser.ast.expr.StringLiteralExpr" ||
//                    childNode.getClass().getName() == "com.github.javaparser.ast.expr.IntegerLiteralExpr"
//            ){
//                String arg = childNode.toString();
//                args.add("LIT:" + arg);
//            }
            //System.out.println(j + "; " + node.getChildNodes().get(j) + "; " + node.getChildNodes().get(j).getClass().getName());
        }
        res.put("filename", pathname);
        System.out.println(res.toString());
        if (node.getClass().getName() == "MethodCallExpr") return getNameOfASTNode(null, pathname);
        //if (node.getClass().getSimpleName() == "MemberExpression") return getNameOfASTNode(node.object);
        //if (node.getClass().getSimpleName() == "MemberExpression") return getNameOfASTNode(node.property);
        //if (node.getClass().getSimpleName() == "Literal") return "LIT:" + String(node.value);
        //if (node.getClass().getSimpleName() == "ThisExpression") return "LIT:this";
        //if (node.getClass().getSimpleName() == "UpdateExpression") return getNameOfASTNode(node.argument);
        return res;
    }
    public static void create_tokens(){
        JSONParser parser = new JSONParser();
        Set<String> hash_Set = new HashSet<String>();
        try {
            Object obj = parser.parse(new FileReader("call01.json"));

            // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
            //JSONObject jsonObject = (JSONObject) obj;

            // A JSON array. JSONObject supports java.util.List interface.
            JSONArray callsList = (JSONArray) obj;//jsonObject.get(null);
            Iterator<JSONObject> iterator = callsList.iterator();
            while (iterator.hasNext()) {
                JSONObject call = iterator.next();
                JSONArray arguments = (JSONArray) call.get("arguments");
                for (int j=0; j < arguments.size(); j++){
                    String arg = arguments.get(j).toString();
                    hash_Set.add(arg);
                }
            }
            Iterator<String> iterator1 = hash_Set.iterator();
            JSONArray tokens = new JSONArray();
            while (iterator1.hasNext()) {
                String token = iterator1.next();
                tokens.add(token);
            }

            try {
                // Constructs a FileWriter given a file name, using the platform's default charset
                callfile = new FileWriter("tokens.json");
                callfile.write(tokens.toJSONString());
                System.out.println("Successfully Copied JSON Object to File...");
                System.out.println("\nJSON Object: " + tokens);

            } catch (IOException e) {
                e.printStackTrace();

            } finally {

                try {
                    callfile.flush();
                    callfile.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void create_calls(JSONArray ja){
        try {
            // Constructs a FileWriter given a file name, using the platform's default charset
            callfile = new FileWriter("call01.json");
            callfile.write(ja.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + ja);

        } catch (IOException e) {
            e.printStackTrace();

        } finally {

            try {
                callfile.flush();
                callfile.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        // JavaParser has a minimal logging class that normally logs nothing.
        // Let's ask it to write to standard out:
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());
        
        // SourceRoot is a tool that read and writes Java files from packages on a certain root directory.
        // In this case the root directory is found by taking the root from the current Maven module,
        // with src/main/resources appended.

        //SourceRoot sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(CallsExtractor.class).resolve("src/main/resources"));

        // Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        String pathname = "src\\main\\resources\\Blabla.java";
        File file = new File(pathname);

        //ParserConfiguration config = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(new CombinedTypeSolver()));
        //JavaParser jp = new JavaParser(config);
        //ParseResult<CompilationUnit> pcu = jp.parse(file);
        CompilationUnit cu = StaticJavaParser.parse(file);
//        if (pcu.isSuccessful() && pcu.getResult().isPresent()) {
//            cu = pcu.getResult().get();
//        } else {
//            Log.error(String.format("解析 newFileSource 失败， " + pcu.getProblem(0).toString()));
//            return;
//        }

//        HashMap<String, String[]> hm = new HashMap<String, String[]>();
//        cu.findAll(MethodDeclaration.class).stream()
//                .filter(f->f.isPrivate())
//                .forEach(f -> {
//                    NodeList <Parameter> theParams = f.getParameters();
//                    String[] names = new String[theParams.size()];
//                    for(int k=0; k<theParams.size(); k++ ){
//                        names[k] = "ID:"+ theParams.get(k).getName().asString();
//                        System.out.println("11111    " + names[k]);
//                    }
//
//                    hm.put(f.getName().asString(), names);
//                });
//        System.out.println(hm);
        JSONArray ja = new JSONArray();
        cu.findAll(MethodCallExpr.class).stream()
                .filter(f->f.getArguments().size() > 1)
                .forEach(f -> {
                    JSONObject jo =  getNameOfASTNode(f, pathname);
                    //jo.put("base", "");
                    //jo.put("callee", getNameOfASTNode(f, pathname));
                    ja.add(jo);
                });

        create_calls(ja);
        create_tokens();
        if(1==1){
            return;
        }
        cu.accept(new ModifierVisitor<Void>() {
            /**
             * For every if-statement, see if it has a comparison using "!=".
             * Change it to "==" and switch the "then" and "else" statements around.
             */
            @Override
            public Visitable visit(MethodCallExpr n, Void arg) {
                System.out.println(n.getScope() + " - " + n.getName());

                return super.visit(n, arg);
            }
        }, null);

    }
}
