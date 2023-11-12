package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.Instruction;

public class AsmCodeCreator {
    /**
     * 构造不含立即数的三参数量语句：add, sub, mul
     *
     * @param op     操作
     * @param result 结果寄存器
     * @param lhs    寄存器1
     * @param rhs    寄存器2
     * @return 汇编语句
     */
    public static String createBinary(String op, Reg result, Reg lhs, Reg rhs, Instruction i) {
        return String.format("%s %s, %s, %s     # %s", op, result.toString(), lhs.toString(), rhs.toString(),i.toString());
    }

    /**
     * 构造含有1个立即数的三参数语句addi
     *
     * @param result 结果寄存器
     * @param lhs    左变量
     * @param imm    立即数
     * @return 汇编语句
     */
    public static String createBinary(String op, Reg result, Reg lhs, int imm, Instruction i) {
        return String.format("%s %s, %s, %d     # %s", op, result.toString(), lhs.toString(), imm,i.toString());
    }


    /**
     * 构造不含立即数的两参数语句
     *
     * @param op     操作
     * @param result 结果
     * @param from   结果寄存器
     * @return 汇编语句
     */
    public static String createUnary(String op, Reg result, Reg from, Instruction i) {
        return String.format("%s %s, %s     # %s", op, result.toString(), from.toString(),i.toString());
    }

    /**
     * 构造含有1个立即数的两参数语句
     *
     * @param op     操作
     * @param result 结果
     * @param imm    立即数
     * @return 汇编语句
     */
    public static String createUnary(String op, Reg result, int imm, Instruction i) {
        return String.format("%s %s, %d    # %s", op, result.toString(), imm, i.toString());
    }


}
