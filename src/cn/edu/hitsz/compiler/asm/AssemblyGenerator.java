package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.ir.InstructionKind;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {

    // 预处理后的中间代码列表
    private List<Instruction> code = new ArrayList<>();

    // 预处理后中间代码的变量数队列
    private Queue<Integer> varCount = new LinkedList<>();
    // 预处理后中间代码的变量队列
    private Queue<IRVariable> toUseVars = new LinkedList<>();

    /**
     * 初始化待用变量列表
     */
    private void initToUseVar(){
        for(Instruction i:code){
            int count = 1;
            if(i.getKind().isBinary()){
                toUseVars.add(i.getResult());
                if(i.getLHS().isIRVariable()){
                    toUseVars.add((IRVariable) i.getLHS());
                    count++;
                }
                if(i.getRHS().isIRVariable()){
                    toUseVars.add((IRVariable) i.getRHS());
                    count++;
                }
            }
            else if(i.getKind().isUnary()){
                toUseVars.add(i.getResult());
                if(i.getFrom().isIRVariable()){
                    toUseVars.add((IRVariable)i.getFrom());
                    count++;
                }
            }
            else if(i.getKind().isReturn()){
                if(i.getReturnValue().isIRVariable()){
                    toUseVars.add((IRVariable)i.getReturnValue());
                }
            }
            varCount.add(count);
        }
    }

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        for (Instruction i : originInstructions) {
            InstructionKind kind = i.getKind();
            /*
             * 预处理：根据中间代码的指令类型进行处理
             */
            if (kind.isBinary()) {
                if (i.getLHS().isImmediate() && i.getRHS().isImmediate()) {
                    // 如果两个操作数均为立即数
                    IRImmediate lhs = (IRImmediate) i.getLHS();
                    IRImmediate rhs = (IRImmediate) i.getRHS();
                    int result;
                    switch (kind) {
                        case ADD -> result = lhs.getValue() + rhs.getValue();
                        case SUB -> result = lhs.getValue() - rhs.getValue();
                        case MUL -> result = lhs.getValue() * rhs.getValue();
                        default -> throw new RuntimeException("如果两个操作数均为立即数：处理错误");
                    }
                    code.add(Instruction.createMov(i.getResult(), IRImmediate.of(result)));
                } else if (i.getLHS().isIRVariable() && i.getRHS().isImmediate()) {
                    // 左操作数为变量，右操作数为立即数
                    switch (kind) {
                        case ADD, SUB -> code.add(i);
                        case MUL -> {
                            IRVariable temp = IRVariable.temp();
                            code.add(Instruction.createMov(temp, i.getRHS()));
                            code.add(Instruction.createMul(i.getResult(), i.getLHS(), temp));
                        }
                        default -> throw new RuntimeException("左操作数为变量，右操作数为立即数：处理错误");
                    }
                } else if (i.getRHS().isIRVariable() && i.getLHS().isImmediate()) {
                    // 左操作数为立即数，右操作数为变量
                    IRImmediate lhs = (IRImmediate) i.getLHS();
                    IRVariable rhs = (IRVariable) i.getRHS();
                    switch (kind) {
                        case ADD -> code.add(Instruction.createAdd(i.getResult(), rhs, lhs));// 加法：调换立即数和变量的位置，方便构造addi
                        case SUB -> {
                            IRVariable temp = IRVariable.temp();
                            code.add(Instruction.createMov(temp, lhs));
                            code.add(Instruction.createSub(i.getResult(), temp, rhs));
                        }
                        case MUL -> {
                            IRVariable temp = IRVariable.temp();
                            code.add(Instruction.createMov(temp, lhs));
                            code.add(Instruction.createMul(i.getResult(), temp, rhs));
                        }
                        default -> throw new RuntimeException("左操作数为立即数，右操作数为变量：处理错误");
                    }
                } else {
                    // 两个操作数均为变量
                    code.add(i);
                }
            } else if (kind.isUnary()) {
                code.add(i);
            } else if (kind.isReturn()) {
                code.add(i);
                break;//放弃后续指令
            }
        }
        initToUseVar();
    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        // TODO 将所有出现的变量先全部导入一个列表，每当使用一个变量后，删除该变量。遍历该列表判断后续是否还会使用到某个变量。
        RegAllocator ra = new RegAllocator();
        for(Instruction i : code){
            String cpCode;
            InstructionKind kind = i.getKind();
            if(kind.isBinary()){
                int varNum = varCount.remove();
                if(varNum==1){
                    //没有立即数
                    switch (kind) {
                        case ADD -> {

                        }
                    }
                }
            } else if(kind.isUnary()){

            } else if (kind.isReturn()) {

            }
        }
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        for (Instruction i : code) {
            System.out.println(i);
        }
        for(IRVariable iv:toUseVars){
            System.out.println(iv.getName());
        }
    }
}

