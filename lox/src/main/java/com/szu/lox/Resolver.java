package com.szu.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

// 小型的解释器，用于做语义分析，优化程序
// 每次访问一个变量时，它都会告诉解释器，在当前作用域和变量定义的作用域之间隔着多少层作用域。
// 在运行时，这正好对应于当前环境与解释器可以找到变量值的外围环境之间的environments数量。
class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    // 当前作用域内的栈，每个元素是一个块作用域
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    // 跟踪当前访问的代码是否在一个函数声明内部
    private FunctionType currentFunction = FunctionType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD
    }

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS // 用于检查当前是否在有超类的类中
    }

    // 告诉我们，在遍历语法树时，我们目前是否在一个类声明中
    private ClassType currentClass = ClassType.NONE;

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    // 解析类声明语句
    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);

        // 不能继承自身
        if (stmt.superclass != null &&
            stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
            Lox.error(stmt.superclass.name,
                "A class can't inherit from itself.");
        }

        if (stmt.superclass != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass);
        }

        // 如果有超类，会创建闭包环境
        if (stmt.superclass != null) {
            beginScope();
            scopes.peek().put("super", true);
        }

        // 为了与解释器环境链同步，创建闭包环境
        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;

            if (method.name.lexeme.equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }

            resolveFunction(method, declaration);
        }

        endScope();

        if (stmt.superclass != null) endScope();

        currentClass = enclosingClass;

        return null;
    }

    // 解析表达式语句
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    // 解析函数声明
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // 将函数名绑到当前作用域中
        // 在解析函数体之前急切地定义这个函数名称，使得函数可以在自己的函数体内递归地调用自身
        declare(stmt.name);
        define(stmt.name);

        // 将参数绑定到函数内部作用域中
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    // 解析 If 语句
    // 会解析所有分支
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    // 解析 print 语句
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    // 解析 return 语句
    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        // return 语句不在函数中，没有任何意义
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }

        if (stmt.value != null) {
            // 如果return所在的函数为init()初始化函数，则不允许有返回值
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword,
                        "Can't return a value from an initializer.");
            }

            resolve(stmt.value);
        }

        return null;
    }

    // 解析变量声明
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    // 解析 while 语句
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    // 解析赋值表达式
    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        // 解析待赋值的变量
        resolveLocal(expr, expr.name);
        return null;
    }

    // 解析二元表达式
    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    // 解析函数调用表达式
    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    // 解析类属性访问、函数调用
    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    // 解析括号表达式
    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    // 解析字面量表达式
    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    // 解析逻辑表达式
    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword,
                    "Can't use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            Lox.error(expr.keyword,
                    "Can't use 'super' in a class with no superclass.");
        }

        resolveLocal(expr, expr.keyword);
        return null;
    }

    // 解析 this 表达式
    @Override
    public Void visitThisExpr(Expr.This expr) {
        // 如果 this 不在类中定义，我们认为这是不合法的
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword,
                      "Can't use 'this' outside of a class.");
            return null;
        }

        resolveLocal(expr, expr.keyword);
        return null;
    }

    // 解析一元表达式
    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    // 解析变量表达式
    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        // 表示已经声明了，但是还未被定义，即还未被初始化
        if (!scopes.isEmpty() &&
            scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(expr.name,
                      "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    // 解析语句列表，通常是解析一个块
    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    // 解析语句
    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    // 解析表达式
    private void resolve(Expr expr) {
        expr.accept(this);
    }

    // 解析函数体
    // 这与解释器处理函数声明的方式不同。
    // 在运行时，声明一个函数不会对函数体做任何处理。直到后续函数被调用时，才会触及主体。
    // 在静态分析中，我们会立即遍历函数体
    private void resolveFunction(
        Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    // 标记变量尚未就绪，不可使用
    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();

        // 在同一作用域中重复声明变量，打印错误信息，并不会抛出错误
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name,
            "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme, false);
    }

    // 标记变量已完成初始化，并可以使用
    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    // 在作用域链中查找
    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }
}