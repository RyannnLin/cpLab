package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.IRVariable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static cn.edu.hitsz.compiler.asm.Reg.*;

public class RegAllocator {
    private List<Reg> used = new LinkedList<>();
    private Queue<Reg> avail = new LinkedList<>();

    public RegAllocator() {
        avail.add(t0);
        avail.add(t1);
        avail.add(t2);
        avail.add(t3);
        avail.add(t4);
        avail.add(t5);
        avail.add(t6);
    }

    private BMap<IRVariable, Reg> regAllocation = new BMap<>();

    private boolean hasAvailableReg() {
        return !avail.isEmpty();
    }

    /**
     * 从可用列表中返回一个可用的寄存器
     *
     * @return 可用的寄存器
     */
    private Reg getAvailReg() {
        if (hasAvailableReg()) {
            used.add(avail.peek());
            return avail.remove();
        } else {
            throw new RuntimeException("没有可用寄存器");
        }
    }


    private void freeReg(Reg r) {
        used.remove(r);
        avail.add(r);
    }

    /**
     * 绑定一个变量和一个寄存器，添加到BMap中
     *
     * @param irvar 变量
     * @return 分配的寄存器
     */
    private Reg binding(IRVariable irvar, Queue<IRVariable> toUseVars) {
        if (hasAvailableReg()) {
            regAllocation.replace(irvar, getAvailReg());
            return getRegByVar(irvar);
        } else {
            checkUnusedVar(toUseVars);
            if (hasAvailableReg()) {
                regAllocation.replace(irvar, getAvailReg());
                return getRegByVar(irvar);
            } else {
                throw new RuntimeException("没有可用寄存器");
            }
        }
    }

    /**
     * 从寄存器中释放一个变量
     *
     * @param irval 变量
     */
    private void unbinding(IRVariable irval, Reg r) {
        freeReg(r);
        regAllocation.removeByKey(irval);
    }

    /**
     * 清除不再使用的变量并释放寄存器
     *
     * @param toUseVars 待用变量列表
     */
    private void checkUnusedVar(Queue<IRVariable> toUseVars) {
        for (int i = 0; i < used.size(); i++) {//查询当前各个寄存器中存放的变量
            Reg r = used.get(i);
            boolean toUse = false;
            IRVariable vr = regAllocation.getByValue(r);
            for (IRVariable v : toUseVars) {//在待用符号表中查找该变量
                if (v.equals(vr)) {
                    toUse = true;
                    break;
                }
            }
            if (!toUse) {
                regAllocation.removeByValue(r);
                used.remove(r);
                avail.add(r);
                //unbinding(vr, r);
            }
        }
    }

    /**
     * 查找已分配寄存器的变量
     *
     * @param v 变量名
     * @return 变量对应的寄存器
     */
    public Reg getRegByVar(IRVariable v) {
        return regAllocation.getByKey(v);
    }

    public Reg allocateReg(Queue<IRVariable> toUseVars, IRVariable vr) {
        Reg r = getRegByVar(vr);
        if (r == null) {
            return binding(vr, toUseVars);
        } else return r;
    }
}
