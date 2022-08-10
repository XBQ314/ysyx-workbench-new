#include "verilated.h"
#include "verilated_vcd_c.h"
#include "verilated_dpi.h"
#include "VRV64Top.h"
#include <stdio.h>
#include "reg.h"
#include "cpu.h"
#include "mem.h"
#include "sdb.h"
#include "utils.h"
#include "difftest.h"

#define CONFIG_ITRACE 0
#define CONFIG_DIFFTEST 1
#define CONFIG_WATCHPOINT 0

VerilatedContext* contextp = NULL;
VerilatedVcdC* tfp = NULL;

VRV64Top* top;

extern NPCState npc_state;
extern uint64_t NPC_PC;
bool skip_difftest = false;
uint64_t skipdiff_pc = 0;

void step_and_dump_wave()
{
    top->eval();
    contextp->timeInc(1);
    tfp->dump(contextp->time());
}

void single_cycle() 
{
	top->clock = 0; step_and_dump_wave();
	top->clock = 1; step_and_dump_wave();
}

void reset(int n)
{
    top->reset = 1;
    while (n -- > 0) single_cycle();
	top->reset = 0;
    NPC_PC = 0x80000000;
}

void sim_init() 
{
  contextp = new VerilatedContext;
  tfp = new VerilatedVcdC;
  top = new VRV64Top;
  contextp->traceEverOn(true);
  top->trace(tfp, 0);
  tfp->open("./wavedata/VRV64Top.vcd");
}

void sim_exit()
{
  step_and_dump_wave();
  tfp->close();
}

static void trace_and_difftest() 
{
#if CONFIG_DIFFTEST
    // PC等于零说明是flush出的指令，difftest
    // PC等于0x80000000说明是第一条指令，也不difftest，不然NPC会比NEMU慢一拍
    if(mmio_flag == true)
    {
        skipdiff_pc = top->io_mem_pc; // 记录需要让nemu跳过执行的pc
        // printf("skipdiff_pc:0x%lx\n", skipdiff_pc);
        mmio_flag = false;
    }

    if(NPC_PC != 0 && NPC_PC != 0x80000000 && top->io_enMEM2WB)
    {
        // if(top->io_skip_diff)
        if(skip_difftest == true)
        {
            // printf("diffNOTcheck! \t");
            difftest_regcpy(&npc_regs, 1);
            skip_difftest = false;
        }
        else
        {
            // printf("diffcheck!\n");
            difftest_exec(1);
            struct npc_reg_struct nemu_ref_regs;
            difftest_regcpy(&nemu_ref_regs, 0); // 这句话获取了nemu模拟器的状态，并赋值给nemu_ref_regs
            // printf("npc pc:%lx, nemu pc:%lx\n", npc_regs.pc, nemu_ref_regs.pc);
            difftest_check(&nemu_ref_regs);
        }
    }
#endif
#if CONFIG_ITRACE
    if(top->io_inst != 0 && top->io_enMEM2WB)
    {
        char inst_str[128];
        disassemble(inst_str, 128, NPC_PC, (uint8_t *)&(top->io_inst), 4);
        printf("pc:%08lx  ", NPC_PC);
        printf("inst is:%08x  ", top->io_inst);
        printf(" %s\n", inst_str);
    }
#endif
#if CONFIG_WATCHPOINT
    check_wp();
#endif
    return;
}

static void exec_once()
{
    NPC_PC = top->io_pc;
    // top->io_inst = get_inst(NPC_PC);
    single_cycle();
    NPC_PC = top->io_pc;
    update_npc_regs(); // 让结构体中的regs保持更新
    return;
}

static void execute(uint64_t n)
{
    for(;n > 0; n--)
    {
        // struct npc_reg_struct nemu_ref_regs;
        // difftest_regcpy(&nemu_ref_regs, 0);
        // printf("before npc_ex");
        // printf("NPC pc:0x%lx, NEMU PC:0x%lx ", NPC_PC, nemu_ref_regs.pc);
        // printf("io_enMEM2WB:%d\n", top->io_enMEM2WB);
        exec_once();
        // difftest_regcpy(&nemu_ref_regs, 0);
        // printf("before diff");
        // printf("NPC pc:0x%lx, NEMU PC:0x%lx ", NPC_PC, nemu_ref_regs.pc);
        // printf("io_enMEM2WB:%d\n", top->io_enMEM2WB);
        trace_and_difftest();
        // NPC_PC等于skipdiff_pc则说明在这个阶段，NPC和nemu都刚执行过了需要跳过的指令的上一条
        // 所以可以让下一次nemu跳过difftest，直接拷贝NPC执行了之后的结果
        // 注意要避免pc等于零的情况
        if((NPC_PC == skipdiff_pc) && (skipdiff_pc != 0)) skip_difftest = true; 
        if(top->io_inst == 0x100073) // ebreak
        {
            npc_state.state = NPC_END;
            single_cycle();

#if CONFIG_DIFFTEST
            difftest_exec(1); // 加上这个就这样就可以显示nemu的trap了
            difftest_exec(1); // 加上这个就这样就可以显示nemu的trap了
#endif
        }
        if(npc_state.state != NPC_RUNNING)break;
    }
    return;
}

void cpu_exec(uint64_t n)
{
    // single_cycle();
    // single_cycle();single_cycle();single_cycle();single_cycle(); // 让第一条指令跑到WB级
    switch (npc_state.state) 
    {
        case NPC_END: case NPC_ABORT:
        printf("Program execution has ended. To restart the program, exit NPC and run again.\n");
        return;
        default: npc_state.state = NPC_RUNNING;
    }

    execute(n);

    switch (npc_state.state) 
    {
        case NPC_RUNNING: npc_state.state = NPC_STOP; break;

        case NPC_ABORT:break;
        case NPC_END: 
        check_ebreak();

        // fall through
        case NPC_QUIT: return;
    }
}