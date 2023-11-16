package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {
    private final Stack<Token> symbolStack = new Stack<>();//符号栈
    private final Stack<IRValue> valueStack = new Stack<>();//属性栈

    private final List<Instruction> code = new ArrayList<>();//中间代码列表
    private SymbolTable symbolTable; //存储源代码中定义的变量

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        symbolStack.push(currentToken);
        TokenKind kind = currentToken.getKind();
        if (kind.equals(TokenKind.fromString("id"))) {
            valueStack.push(IRVariable.named(currentToken.getText()));
        } else if (kind.equals(TokenKind.fromString("IntConst"))) {
            valueStack.push(IRImmediate.of(Integer.parseInt(currentToken.getText())));
        } else {
            valueStack.push(null);
        }
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        switch (production.index()) {
            case 6 -> {
                // S -> id = E;
                //弹出E
                Token e = symbolStack.pop();
                var e_val = valueStack.pop();
                //弹出等号
                Token eq = symbolStack.pop();
                assert (Objects.equals(eq.getKind().getTermName(), "="));
                valueStack.pop();
                //弹出id
                Token id = symbolStack.pop();
                var id_val = (IRVariable) valueStack.pop();
                if (!symbolTable.has(id.getText())) {
                    throw new RuntimeException("不存在该符号");
                }
                // 生成中间代码
                code.add(Instruction.createMov(id_val, e_val));
            }

            case 7 -> {
                // S -> return E;
                //弹出E
                Token e = symbolStack.pop();
                var e_val = valueStack.pop();
                // 生成中间代码
                code.add(Instruction.createRet(e_val));
            }

            case 8 -> {
                // E -> E + A;
                // 弹出A
                Token a = symbolStack.pop();
                var a_val = valueStack.pop();
                // 弹出加号
                Token plus = symbolStack.pop();
                assert (Objects.equals(plus.getKind().getTermName(), "+"));
                valueStack.pop();
                // 弹出右侧的E
                Token e = symbolStack.pop();
                var e_val = valueStack.pop();
                IRVariable result = IRVariable.temp();
                code.add(Instruction.createAdd(result, e_val, a_val));
                // 将左侧的E入栈
                symbolStack.push(e);
                valueStack.push(result);
            }
            case 9 -> {
                // E -> E - A;
                // 弹出A
                Token a = symbolStack.pop();
                var a_val = valueStack.pop();
                // 弹出加号
                Token plus = symbolStack.pop();
                assert (Objects.equals(plus.getKind().getTermName(), "-"));
                valueStack.pop();
                // 弹出右侧的E
                Token e = symbolStack.pop();
                var e_val = valueStack.pop();
                IRVariable result = IRVariable.temp();
                code.add(Instruction.createSub(result, e_val, a_val));
                // 将左侧的E入栈
                symbolStack.push(e);
                valueStack.push(result);
            }
            case 10, 12 -> {
                // E -> A;
                // A -> B;
                // 弹出后再压入，本质上栈的内容不会发生任何改变
                //    // 弹出A/B
                //    var a_val = valueStack.pop();
                //    // 将E/A压入栈
                //    valueStack.push(a_val);
            }
            case 11 -> {
                // A -> A * B;
                // 弹出B
                Token b = symbolStack.pop();
                var b_val = valueStack.pop();
                // 弹出乘号
                Token multi = symbolStack.pop();
                assert (Objects.equals(multi.getKind().getTermName(), "*"));
                valueStack.pop();
                // 弹出右侧的A
                Token a = symbolStack.pop();
                var a_val = valueStack.pop();
                IRVariable result = IRVariable.temp();
                //生成中间代码
                code.add(Instruction.createMul(result, a_val, b_val));
                // 将左侧的A入栈
                symbolStack.push(a);
                valueStack.push(result);
            }

            case 13 -> {
                // B -> ( E );
                // 弹出右括号
                Token rbk = symbolStack.pop();
                assert (Objects.equals(rbk.getKind().getTermName(), ")"));
                valueStack.pop();
                // 弹出E
                Token e = symbolStack.pop();
                var e_val = valueStack.pop();
                // 弹出左括号
                Token lbk = symbolStack.pop();
                assert (Objects.equals(lbk.getKind().getTermName(), "("));
                valueStack.pop();
                // 将B压入栈
                symbolStack.push(e);
                valueStack.push(e_val);
            }

            case 14 -> {
                // B -> id;
                // 弹出id
                Token id = symbolStack.pop();
                valueStack.pop();
                if (!symbolTable.has(id.getText())) {
                    throw new RuntimeException("该符号不存在");
                }
                // 获取id的值
                IRVariable val = IRVariable.named(id.getText());
                // 将id的值赋给B并将B压入栈
                symbolStack.push(id);
                valueStack.push(val);
            }

            case 15 -> {
                // B -> IntConst;
                // 弹出IntConst
                Token intConst = symbolStack.pop();
                valueStack.pop();
                // 获取该常数对应的值
                IRImmediate val = IRImmediate.of(Integer.parseInt(intConst.getText()));
                // 压栈
                symbolStack.push(intConst);
                valueStack.push(val);
            }
            default -> symbolStack.pop();
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
        symbolStack.clear();
        valueStack.clear();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        symbolTable = table;
    }

    public List<Instruction> getIR() {
        // TODO: 返回生成的中间代码列表
        return code;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

