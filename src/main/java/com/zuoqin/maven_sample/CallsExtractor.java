package com.zuoqin.maven_sample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;
import jdk.nashorn.internal.ir.FunctionNode;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class CallsExtractor {
    public static JSONObject getNameOfASTNode(com.github.javaparser.ast.Node node) {
        JSONObject res = new JSONObject();
        Optional<Node> parent = node.getParentNode();
        System.out.println(node.getBegin());
//        if (parent.toString().contains("." + node.)){
//
//        }
        System.out.println("2222 " + node.getClass().getName() + "; " + node);
        JSONArray args = new JSONArray();
        for(int j=0; j<node.getChildNodes().size(); j++){
            System.out.println(node.getChildNodes().get(j).getClass().getName());
            if(node.getChildNodes().get(j).getClass().getName() == "com.github.javaparser.ast.expr.SimpleName") {
                if (j > 0 && node.getChildNodes().get(j - 1).getClass().getName() == "com.github.javaparser.ast.expr.NameExpr") {
                    res.put("base", "ID:" + node.getChildNodes().get(j - 1).toString());
                    res.put("callee", "ID:" + node.getChildNodes().get(j).toString());
                    res.put("calleeLocation", "88888");
                } else{
                    res.put("base", "");
                    res.put("callee", "ID:" + node.getChildNodes().get(j).toString());
                    res.put("calleeLocation", "7777");
                }
            }
            if(node.getChildNodes().get(j).getClass().getName() == "com.github.javaparser.ast.expr.StringLiteralExpr" ||
                    node.getChildNodes().get(j).getClass().getName() == "com.github.javaparser.ast.expr.IntegerLiteralExpr"
            ){
                String arg = node.toString();
                args.add("LIT:" + arg);
            }
            if(node.getChildNodes().get(j).getClass().getName() == "com.github.javaparser.ast.expr.NameExpr"){
                NameExpr expr = (NameExpr) node.getChildNodes().get(j);
                System.out.println(node.getChildNodes().get(j));
                String arg = expr.calculateResolvedType().toString();
                args.add("LIT:" + arg);
            }
            res.put("arguments", args);
            //System.out.println(j + "; " + node.getChildNodes().get(j) + "; " + node.getChildNodes().get(j).getClass().getName());
        }
        System.out.println(res.toString());
        if (node.getClass().getName() == "MethodCallExpr") return getNameOfASTNode(null);
        //if (node.getClass().getSimpleName() == "MemberExpression") return getNameOfASTNode(node.object);
        //if (node.getClass().getSimpleName() == "MemberExpression") return getNameOfASTNode(node.property);
        //if (node.getClass().getSimpleName() == "Literal") return "LIT:" + String(node.value);
        //if (node.getClass().getSimpleName() == "ThisExpression") return "LIT:this";
        //if (node.getClass().getSimpleName() == "UpdateExpression") return getNameOfASTNode(node.argument);
        return new JSONObject();
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
        File file = new File("D:\\Data\\javaparser-maven-sample\\src\\main\\resources\\Blabla.java");

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
        Log.info("Positivizing!");

        HashMap<String, String[]> hm = new HashMap<String, String[]>();
        cu.findAll(MethodDeclaration.class).stream()
                .filter(f->f.isPrivate())
                .forEach(f -> {
                    NodeList <Parameter> theParams = f.getParameters();
                    String[] names = new String[theParams.size()];
                    for(int k=0; k<theParams.size(); k++ ){
                        names[k] = "ID:"+ theParams.get(k).getName().asString();
                        System.out.println("11111    " + names[k]);
                    }

                    hm.put(f.getName().asString(), names);
                });
        System.out.println(hm);
        JSONArray ja = new JSONArray();
        cu.findAll(MethodCallExpr.class).stream()
                .filter(f->f.getArguments().size() > 1)
                .forEach(f -> {
                    JSONObject jo = new JSONObject();
                    jo.put("base", "");
                    jo.put("callee", getNameOfASTNode(f));
                    ja.add(jo);
                });


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
