package com.szu.lox;

// 对 return 返回结果进行封装成运行时异常类
// 目的：当执行return的时候可以抛出该异常，在函数调用处进行捕获，从而跳出中间不再需要执行的部分
class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
