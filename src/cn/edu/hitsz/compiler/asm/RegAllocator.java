package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.IRVariable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static cn.edu.hitsz.compiler.asm.Reg.*;

public class RegAllocator {
    public RegAllocator() {
        avail.add(t0);
        avail.add(t1);
        avail.add(t2);
        avail.add(t3);
        avail.add(t4);
        avail.add(t5);
        avail.add(t6);
    }
    private BMap<IRVariable,Reg> regAllocation = new BMap<>();

    private List<Reg> used = new LinkedList<>();
    private Queue<Reg> avail = new LinkedList<>();


    public boolean hasAvailableReg(){
        return avail.isEmpty();
    }

    /**
     * 从可用列表中返回一个可用的寄存器
     * @return 可用的寄存器
     */
    public Reg getAvailReg(){
        if(hasAvailableReg()){
            used.add(avail.peek());
            return  avail.remove();
        }
        else{
            throw new RuntimeException("没有可用寄存器");
        }
    }


    private void freeReg(Reg r){
        used.remove(r);
        avail.add(r);
    }

    /**
     * 绑定一个变量和一个寄存器，添加到BMap中
     * @param irvar 变量
     */
    public void binding(IRVariable irvar){
        regAllocation.replace(irvar, getAvailReg());
    }

    /**
     * 从寄存器中释放一个变量
     * @param irval 变量
     */
    public void unbinding(IRVariable irval){
        freeReg(regAllocation.getByKey(irval));
        regAllocation.removeByKey(irval);
    }

    /**
     * 清除不再使用的变量并释放寄存器
     */
    public void checkUnusedVar(){

    }

}
