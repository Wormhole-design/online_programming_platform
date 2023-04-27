package com.szu.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.szu.lox.TokenType.*;

// 扫描器 ===> 词法标记序列
class Scanner {
    private static final Map<String, TokenType> keywords;

    // 保留字
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    // 扫描器位置跟踪标记
    // start指向被扫描地词素中的第一个字符
    // current指向当前正在处理字符的下一个字符
    // line字段跟踪的是current所在的源文件行数
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // 重置为下一个词素的开头
            // 发现错误，记录错误，忽略错误，继续扫描，尽可能的找出更多错误
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // 一个字符
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            // 一个或两个字符
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // 忽略空白符
                break;

            case '\n':
                line ++ ;
                break;

            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        // 如果不是保留字，则就是用户定义的标识符
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    // 获取字符串字面量
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line ++ ;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // 获取引号之间的内容
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // 获取数字字面量
    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    // 预言判断，有效消费
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current ++ ;
        return true;
    }

    // 前瞻，不消费
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // 再次前瞻，不消费
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // 判断开头是否为[a-zA-Z_]
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    // 判断后面是否为[0-9a-zA-Z_]
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // 判断是否为数字
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // 判断是否消费完所有字符
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // 记录下一次词为位置，并将当前要处理词返回
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
