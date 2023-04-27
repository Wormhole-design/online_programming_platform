package com.szu.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static Interpreter interpreter;
    // 记录是否发生错误
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        interpreter = new Interpreter();
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    // 第三方使用接口
    public static void runApi(String source) throws IOException {
        interpreter = new Interpreter();
        run(source);

        hadError = false;
        hadRuntimeError = false;
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        // 扫描器 ===> Token
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // 解析器 ===> AST
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Expr expression = parser.parse();

        if (hadError) return;

        // System.out.println(new AstPrinter().print(expression));

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) return;

        interpreter.interpret(statements);
    }

    // 错误处理
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
                               String message) {
        System.err.println(
            "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    // 打印错误
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    // 打印运行时错误
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
            "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
