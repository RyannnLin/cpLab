package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private List<Token> tokens;
    private List<Character> buf;


    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) throws FileNotFoundException {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        // debug通过
        buf = new ArrayList<>();
        File token = new File(path);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(token)); // 建立一个字符流对象reader(将输入的字节流转换为字符流)
        BufferedReader br = new BufferedReader(reader);// 创建一个缓存字符流对象read
        int next_unicode;
        try {
            while ((next_unicode = br.read()) != -1) {
                buf.add((char) next_unicode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    private int buf_iter;

    public void run() {
        // TODO: 自动机实现的词法分析过程
        state = 0;//初始化
        buf_iter = 0;
        tokens = new ArrayList<>();
        while (buf_iter < buf.size()) {
            setState(buf.get(buf_iter));
        }
        if (buf_iter == (buf.size())) {
            tokens.add(Token.simple(TokenKind.fromString("$")));
        }
    }

    private String word = "";
    private int state;//状态机状态

    /**
     * 状态机
     *
     * @param c 读取的字符
     */
    private void setState(char c) {

        switch (state) {
            case 0 -> {
                if (Character.isWhitespace(c)) {
                    state = 0;//遇到空格、制表符、换行，state置0
                    word = "";
                    buf_iter++;
                } else if (Character.isAlphabetic(c)) {
                    state = 14;
                    word = word + c;
                    buf_iter++;
                } else if (Character.isDigit(c)) {
                    state = 16;
                    word = word + c;
                    buf_iter++;
                } else {
                    switch (c) {
                        case '*' -> {
                            state = 18;
                            buf_iter++;
                        }
                        case '=' -> {
                            state = 21;
                            buf_iter++;
                        }
                        case '\"' -> state = 24;
                        case '(' -> state = 26;
                        case ')' -> state = 27;
                        case ':' -> state = 28;
                        case '+' -> state = 29;
                        case '-' -> state = 30;
                        case '/' -> state = 31;
                        case ',' -> state = 32;
                        case ';' -> state = 33;
                        default -> state = 0;
                    }
                }
            }
            case 14 -> {
                if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                    state = 14;
                    word = word + c;
                    buf_iter++;
                } else {
                    state = 15;
                }
            }
            case 15 -> {
                /*此处要区分保留字和标识符*/
                if (word.equals("int")) {
                    tokens.add(Token.simple(TokenKind.fromString("int")));
                } else if (word.equals("return")) {
                    tokens.add(Token.simple(TokenKind.fromString("return")));
                } else {
                    //标识符部分
                    tokens.add(Token.normal(TokenKind.fromString("id"), word));
                    if(!symbolTable.has(word)){
                        symbolTable.add(word);
                    }
                }
                state = 0;
                word = "";
            }
            case 16 -> {
                if (Character.isDigit(c)) {
                    state = 16;
                    word = word + c;
                    buf_iter++;
                } else {
                    state = 17;
                }
            }
            case 17 -> {
                tokens.add(Token.normal(TokenKind.fromString("IntConst"), word));
                state = 0;
                word = "";
            }
            case 18 -> {
                if (c == '*') {
                    state = 19;
                } else {
                    state = 20;
                }
            }
            case 19 -> {
                tokens.add(Token.simple(TokenKind.fromString("**")));
                state = 0;
                word = "";
                buf_iter++;
            }
            case 20 -> {
                tokens.add(Token.simple(TokenKind.fromString("*")));
                state = 0;
                word = "";
            }
            case 21 -> {
                if (c == '=') {
                    state = 22;
                } else {
                    state = 23;
                }
            }
            case 22 -> {
                tokens.add(Token.simple(TokenKind.fromString("==")));
                state = 0;
                word = "";
                buf_iter++;
            }
            case 23 -> {
                tokens.add(Token.simple(TokenKind.fromString("=")));
                state = 0;
                word = "";
            }
            case 24 -> {
                if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                    state = 24;
                    word = word + c;
                    buf_iter++;
                } else if (c == '\"') {
                    state = 25;
                }
            }
            case 25 -> {
                //双引号的后引号处理
                tokens.add(Token.normal(TokenKind.fromString("str_const"), word));
                word = "";
                buf_iter++;
            }
            case 26 -> {
                tokens.add(Token.simple(TokenKind.fromString("(")));
                state = 0;
                word = "";
                buf_iter++;
            }
            case 27 -> {
                tokens.add(Token.simple(TokenKind.fromString(")")));
                state = 0;
                word = "";
                buf_iter++;
            }
            case 28 -> {
                tokens.add(Token.simple(TokenKind.fromString(":")));
                state = 0;
                word = "";
                buf_iter++;
            }
            case 29 -> {
                tokens.add(Token.simple(TokenKind.fromString("+")));
                state = 0;
                word = "";
                buf_iter++;
            }
            case 30 -> {
                tokens.add(Token.simple(TokenKind.fromString("-")));
                state = 0;
                word = "";
                buf_iter++;
            }
            case 31 -> {
                tokens.add(Token.simple(TokenKind.fromString("/")));
                state = 0;
                word = "";
                buf_iter++;
            }
            case 32 -> {
                tokens.add(Token.simple(TokenKind.fromString(",")));
                state = 0;
                word = "";
                buf_iter++;
            }
            case 33 -> {
                tokens.add(Token.simple(TokenKind.fromString("Semicolon")));
                state = 0;
                word = "";
                buf_iter++;
            }
            default -> {
                state = 0;
                buf_iter++;
                word = "";
            }
        }
    }


    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return tokens;
    }

    /**
     * 打印词法分析结果到Token.txt
     *
     * @param path data/out/old_symbol_table.txt
     */
    public void dumpTokens(String path) {
        FileUtils.writeLines(
                path,
                StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
