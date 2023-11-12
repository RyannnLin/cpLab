package cn.edu.hitsz.compiler.asm;

import java.util.LinkedList;
import java.util.List;

public enum Reg {
    t0, t1, t2, t3, t4, t5, t6, a0;


//    //判断该寄存器是否正在使用
//    public boolean isUsed(Reg reg) {
//        for (Reg r : used) {
//            if (reg.equals(r)) return true;
//        }
//        return false;
//    }
//
//    /**
//     * 判断该寄存器是否可用
//     * @param reg 寄存器名
//     * @return 判断结果
//     */
//    public boolean isAvail(Reg reg) {
//        for (Reg r : avail) {
//            if (reg.equals(r)) return true;
//        }
//        return false;
//    }
//
//    /**
//     * 判断是否仍有空寄存器
//     * @return 判断结果
//     */
//    public boolean isRegUnused(){
//        return avail.isEmpty();
//    }
}
