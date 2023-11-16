package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.*;
import cn.edu.hitsz.compiler.symtab.Symbol;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

//TODO: 实验二: 实现 LR 语法分析驱动程序

/**
 * LR 语法分析驱动程序
 * <br>
 * 该程序接受词法单元串与 LR 分析表 (action 和 goto 表), 按表对词法单元流进行分析, 执行对应动作, 并在执行动作时通知各注册的观察者.
 * <br>
 * 你应当按照被挖空的方法的文档实现对应方法, 你可以随意为该类添加你需要的私有成员对象, 但不应该再为此类添加公有接口, 也不应该改动未被挖空的方法,
 * 除非你已经同助教充分沟通, 并能证明你的修改的合理性, 且令助教确定可能被改动的评测方法. 随意修改该类的其它部分有可能导致自动评测出错而被扣分.
 */
public class SyntaxAnalyzer {
    private final SymbolTable symbolTable;
    private final List<ActionObserver> observers = new ArrayList<>();
    private final Stack<Symbol> symbolStack = new Stack<>();//符号栈
    private final Stack<Status> statusStack = new Stack<>();//状态栈
    private final List<Token> tokenlist = new ArrayList<>();//字符缓冲区


    public SyntaxAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * 注册新的观察者
     *
     * @param observer 观察者
     */
    public void registerObserver(ActionObserver observer) {
        observers.add(observer);
        observer.setSymbolTable(symbolTable);
    }

    /**
     * 在执行 shift 动作时通知各个观察者
     *
     * @param currentStatus 当前状态
     * @param currentToken  当前词法单元
     */
    public void callWhenInShift(Status currentStatus, Token currentToken) {
        for (final var listener : observers) {
            listener.whenShift(currentStatus, currentToken);
        }
    }

    /**
     * 在执行 reduce 动作时通知各个观察者
     *
     * @param currentStatus 当前状态
     * @param production    待规约的产生式
     */
    public void callWhenInReduce(Status currentStatus, Production production) {
        for (final var listener : observers) {
            listener.whenReduce(currentStatus, production);
        }
    }

    /**
     * 在执行 accept 动作时通知各个观察者
     *
     * @param currentStatus 当前状态
     */
    public void callWhenInAccept(Status currentStatus) {
        for (final var listener : observers) {
            listener.whenAccept(currentStatus);
        }
    }

    public void loadTokens(Iterable<Token> tokens) {
        // TODO: 加载词法单元
        // 你可以自行选择要如何存储词法单元, 譬如使用迭代器, 或是栈, 或是干脆使用一个 list 全存起来
        // 需要注意的是, 在实现驱动程序的过程中, 你会需要面对只读取一个 token 而不能消耗它的情况,
        // 在自行设计的时候请加以考虑此种情况
        for (Token token : tokens) {
            tokenlist.add(token);
        }
        symbolStack.push(new Symbol(Token.simple(TokenKind.fromString("$"))));//将第一个符号压入符号栈
    }

    /**
     * 将分析表中的初始状态压入状态栈，后续的语法分析中不会再用到该分析表
     *
     * @param table LR分析表
     */
    public void loadLRTable(LRTable table) {
        // TODO: 加载 LR 分析表
        // 你可以自行选择要如何使用该表格:
        // 是直接对 LRTable 调用 getAction/getGoto, 抑或是直接将 initStatus 存起来使用
        statusStack.push(table.getInit());//将初始状态压入状态栈
    }

    public void run() {
        // TODO: 实现驱动程序
        // 你需要根据上面的输入来实现 LR 语法分析的驱动程序
        // 请分别在遇到 Shift, Reduce, Accept 的时候调用上面的 callWhenInShift, callWhenInReduce, callWhenInAccept
        // 否则用于为实验二打分的产生式输出可能不会正常工作
        int index = 0;//指向缓冲区tokenlist的指针
        Status currentStatus;
        while (true) {
            currentStatus = statusStack.peek();//状态栈的栈顶
            Token currentToken = tokenlist.get(index);//当前缓冲区要读取的终结符
            Action.ActionKind currentActionKind = currentStatus.getAction(tokenlist.get(index)).getKind();//获取当前状态下要执行的动作的类型
            if (currentActionKind.equals(Action.ActionKind.Accept)) {
                callWhenInAccept(currentStatus);
                return;
            } else if (currentActionKind.equals(Action.ActionKind.Reduce)) {
                //规约
                List<Term> body = currentStatus.getAction(currentToken).getProduction().body();//获取当前状态对应的产生式的尾部
                NonTerminal head = currentStatus.getAction(currentToken).getProduction().head();//获取当前状态对应的产生式的头部
                callWhenInReduce(currentStatus, currentStatus.getAction(currentToken).getProduction());//通知观察者

                for (Term t : body) {
                    symbolStack.pop();
                    statusStack.pop();
                }//将产生式尾部的符号和状态对应地弹出符号栈和状态栈
                symbolStack.push(new Symbol(head));//产生式头部压入符号栈
                statusStack.push(statusStack.peek().getGoto(head));//将goto压入状态栈
            } else if (currentActionKind.equals(Action.ActionKind.Shift)) {
                //移进
                symbolStack.push(new Symbol(currentToken));//符号入栈
                statusStack.push(currentStatus.getAction(currentToken).getStatus());//状态入栈
                callWhenInShift(currentStatus, tokenlist.get(index));
                index++;//缓冲区指针移向下一位
            } else if (currentActionKind.equals(Action.ActionKind.Error)) {
                throw new RuntimeException("状态错误");
            }
        }
    }
}
