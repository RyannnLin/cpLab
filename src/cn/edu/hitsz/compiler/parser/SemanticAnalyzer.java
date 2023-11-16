package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.symtab.SymbolTableEntry;

import java.util.Stack;


/**
 * TODO: 实验三: 实现语义分析
 * <br>
 * 作用：解决变量的符号类型问题
 * <br>
 * 生成new_symbol_table
 */

public class SemanticAnalyzer implements ActionObserver {
    //语义分析栈和符号栈
    private final Stack<SourceCodeType> typeStack = new Stack<>();
    private final Stack<Token> symbolStack = new Stack<>();
    private SymbolTable symbolTable; //存储源代码中定义的变量

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
        typeStack.clear();
        symbolStack.clear();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        // 规约
        switch (production.index()) {
            // 只有涉及终结符的需要进行特殊处理，其余的都只需要向语义分析栈typeStack压入null
            case 4 -> {
                // S -> D id;
                // 如果id存在于符号表中，则修改id的type
                Token id = symbolStack.pop();
                if (symbolTable.has(id.getText())) {
                    SymbolTableEntry p = symbolTable.get(id.getText());
                    p.setType(typeStack.pop());
                    typeStack.push(null);
                } else {
                    throw new RuntimeException("没有对应符号");
                }
            }
            case 5 -> {
                // D -> int;
                // 将D的类型设置为int
                typeStack.push(SourceCodeType.Int);
            }
            default -> typeStack.push(null);
        }
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        symbolStack.push(currentToken);
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        symbolTable = table;
    }
}

