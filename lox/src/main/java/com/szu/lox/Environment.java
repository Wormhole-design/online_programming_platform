package com.szu.lox;

import java.util.HashMap;
import java.util.Map;

// 环境
class Environment {
    // 外围作用域的引用
    final Environment enclosing;
    // 存放变量 key - value 映射关系
    private final Map<String, Object> values = new HashMap<>();

    // 无参构造函数用于全局作用域环境，他是环境链的结束点
    Environment() {
        enclosing = null;
    }

    // 有参构造函数用于局部作用域环境
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    // 获取变量的值
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // 当前作用域未找到，需要向上查找，到外部环境中查找
        if (enclosing != null) return enclosing.get(name);

        // 全局作用域还没有则报运行时错误
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    // 给变量赋值
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        // 当前作用域未找到，需去外层作用域中查找并赋值
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        // 全局作用域都没有则报运行时错误
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    // 添加或修改变量
    void define(String name, Object value) {
        values.put(name, value);
    }

    Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

}
