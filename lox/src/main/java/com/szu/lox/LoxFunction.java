package com.szu.lox;

import java.util.List;

// function 函数
class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    // 父环境
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.isInitializer = isInitializer;
        this.closure = closure;
        this.declaration = declaration;
    }

    LoxFunction bind(LoxInstance instance) {
        // 此时的closure指的是class所在的环境
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        // 返回一个闭包处理过的函数
        return new LoxFunction(declaration, environment,
                isInitializer);
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
                    arguments.get(i));
        }

        // 如果捕获到 Return 异常，说明函数返回
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            // 如果是初始化init()函数，则返回this
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }

        if (isInitializer) return closure.getAt(0, "this");

        return null;
    }
}
