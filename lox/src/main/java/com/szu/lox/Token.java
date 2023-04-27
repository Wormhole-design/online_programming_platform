package com.szu.lox;

// 词法单元
class Token {
    // 标记类型
    final TokenType type;
    // 词位
    final String lexeme;
    // 字面量
    final Object literal;
    // 位置
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
