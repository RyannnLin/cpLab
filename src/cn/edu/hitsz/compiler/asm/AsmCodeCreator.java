package cn.edu.hitsz.compiler.asm;

public class AsmCodeCreator {
    /**
     * 构造三变量语句：add, sub, mul, addi
     * @param op 操作
     * @param result 结果寄存器
     * @param lhs 寄存器1
     * @param rhs 寄存器2
     * @return 汇编语句
     */
    public static String createBinary(String op, Reg result, Reg lhs, Reg rhs) {
        return String.format("%s %s, %s, %s", op, result.toString(), lhs.toString(), rhs.toString());
    }

    /**
     * 构造addi
     * @param result 结果寄存器
     * @param lhs 左变量
     * @param imm 立即数
     * @return 汇编语句
     */
    public static String createAddi(Reg result, Reg lhs, int imm){
        return String.format("addi %s, %s, %d", result.toString(),lhs.toString(), imm);
    }


    /**
     * 构造两参数语句：mv
     * @param op 操作
     * @param result j
     * @param from 结果寄存器
     * @return 汇编语句
     */
    public static String createUnary(String op, Reg result, Reg from) {
        return String.format("%s %s, %s", op, result.toString(), from.toString());
    }


}
