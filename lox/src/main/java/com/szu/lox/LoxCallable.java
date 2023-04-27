package com.szu.lox;

import java.util.List;

// lox语言可调用对象需实现的接口
interface LoxCallable {
    // 元数，代表参数数量
    int arity();
    // 调用接口
    Object call(Interpreter interpreter, List<Object> arguments);
}
