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
    if(NPC_PC != 0 && NPC_PC != 0x80000000)
    {
        if(access_device = true)
        {
            difftest_regcpy(&npc_regs, 1);
            access_device = false;
        }
        else
        {
            difftest_exec(1);
            struct npc_reg_struct nemu_ref_regs;
            difftest_regcpy(&nemu_ref_regs, 0); // 这句话获取了nemu模拟器的状态，并赋值给nemu_ref_regs
            // printf("npc pc:%lx, nemu pc:%lx\n", npc_regs.pc, nemu_ref_regs.pc);
            difftest_check(&nemu_ref_regs);
        }
        // printf("npc pc:%lx\n", npc_regs.pc);
        //     difftest_exec(1);
        //     struct npc_reg_struct nemu_ref_regs;
        //     difftest_regcpy(&nemu_ref_regs, 0); // 这句话获取了nemu模拟器的状态，并赋值给nemu_ref_regs
            
        //     difftest_check(&nemu_ref_regs);
    }
#endif
#if CONFIG_ITRACE
    if(top->io_inst != 0)
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
    NPC_PC = top->io_pc; // pc point to next inst
    update_npc_regs(); // 让结构体中的regs保持更新
    return;
}

static void execute(uint64_t n)
{
    for(;n > 0; n--)
    {
        exec_once();
        trace_and_difftest();
        if(top->io_inst == 0x100073) // ebreak
        {
            npc_state.state = NPC_END;
            single_cycle();

#if CONFIG_DIFFTEST
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