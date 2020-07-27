package com.zuoqin.maven_sample;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;
import jdk.nashorn.internal.ir.FunctionNode;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Some code that uses JavaParser.
 */
public class CallsExtractor {
    public static void main(String[] args) {
        // JavaParser has a minimal logging class that normally logs nothing.
        // Let's ask it to write to standard out:
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());
        
        // SourceRoot is a tool that read and writes Java files from packages on a certain root directory.
        // In this case the root directory is found by taking the root from the current Maven module,
        // with src/main/resources appended.
        SourceRoot sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(CallsExtractor.class).resolve("src/main/resources"));

        // Our sample is in the root of this directory, so no package name.
        CompilationUnit cu = sourceRoot.parse("", "Blabla.java");

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

        // This saves all the files we just read to an output directory.  
        sourceRoot.saveAll(
                // The path of the Maven module/project which contains the LogicPositivizer class.
                CodeGenerationUtils.mavenModuleRoot(CallsExtractor.class)
                        // appended with a path to "output"
                        .resolve(Paths.get("output")));
    }
}
