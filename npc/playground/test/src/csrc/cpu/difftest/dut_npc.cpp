#include <stdio.h>
#include <assert.h>
#include <stdint.h>
#include <dlfcn.h>
#include "difftest.h"
#include "reg.h"
#include "utils.h"

extern unsigned char mem[0x8000000];
extern const char *regs[32];
bool mmio_flag = false;

void (*difftest_memcpy)(uint64_t addr, void *buf, size_t n, bool direction) = NULL;
void (*difftest_regcpy)(void *dut, bool direction) = NULL;
void (*difftest_exec)(uint64_t n) = NULL;
void (*difftest_raise_intr)(uint64_t NO) = NULL;
void (*difftest_init)() = NULL;

void init_npc_difftest(long img_size) 
{
    void* handle = dlopen("/home/xubuqing/ysyx-workbench/nemu/build/riscv64-nemu-interpreter-so", RTLD_LAZY);
    assert(handle);

    /*根据动态链接库操作句柄与符号，返回符号对应的地址*/
    difftest_memcpy = (void (*)(uint64_t, void*, size_t, bool))dlsym(handle, "difftest_memcpy");
    assert(difftest_memcpy);
    difftest_regcpy = (void (*)(void *, bool))dlsym(handle, "difftest_regcpy");
    assert(difftest_regcpy);
    difftest_exec = (void (*)(uint64_t ))dlsym(handle, "difftest_exec");
    assert(difftest_exec);
    difftest_init = (void (*)())dlsym(handle, "difftest_init");
    assert(difftest_init);

    difftest_init();
    difftest_memcpy(0x80000000, mem, img_size, 1);
    update_npc_regs();
    difftest_regcpy((void*) &npc_regs, 1);
    return;
}

// 报错意味着最后一条被执行的PC不等于零的指令出现了问题
void difftest_check(struct npc_reg_struct* nemu_regs)
{
    for(int i=0;i<32;i++)
    {
        if(npc_regs.gpr[i] != nemu_regs->gpr[i])
        {
            npc_state.state = NPC_ABORT;
            PRINT_FONT_RED
            printf("Difftest check error; GPR\n");
            printf("At pc=%lx\n", npc_regs.pc);
            printf("(npc_gpr[%d]%s = %lx) != (nemu_gpr[%d]%s = %lx)\n", i, regs[i], npc_regs.gpr[i], i, regs[i], nemu_regs->gpr[i]);
            PRINT_ATTR_REC
        }
    }
    if(npc_regs.pc != nemu_regs->pc)
    {
        npc_state.state = NPC_ABORT;
        PRINT_FONT_RED
        printf("Difftest check error; PC\n");
        printf("(npc_pc = %lx) != (nemu_pc = %lx)\n", npc_regs.pc, nemu_regs->pc);
        PRINT_ATTR_REC
    }

    bool csr_error_flag = (npc_regs.mstatus != nemu_regs->mstatus) ||
                          (npc_regs.mtvec != nemu_regs->mtvec) ||
                          (npc_regs.mepc != nemu_regs->mepc) ||
                          (npc_regs.mcause != nemu_regs->mcause);
    // printf("(npc_mstatus = %lx) != (nemu_mstatus = %lx)\n", npc_regs.mstatus, nemu_regs->mstatus);
    if(csr_error_flag)
    {
        npc_state.state = NPC_ABORT;
        PRINT_FONT_RED
        printf("Difftest check error; CSR\n");
        printf("At pc=%lx\n", npc_regs.pc);
        if(npc_regs.mstatus != nemu_regs->mstatus)
        printf("(npc_mstatus = %lx) != (nemu_mstatus = %lx)\n", npc_regs.mstatus, nemu_regs->mstatus);
        if(npc_regs.mtvec != nemu_regs->mtvec)
        printf("(npc_mtvec = %lx) != (nemu_mtvec = %lx)\n", npc_regs.mtvec, nemu_regs->mtvec);
        if(npc_regs.mepc != nemu_regs->mepc)
        printf("(npc_mepc = %lx) != (nemu_mepc = %lx)\n", npc_regs.mepc, nemu_regs->mepc);
        if(npc_regs.mcause != nemu_regs->mcause)
        printf("(npc_mcause = %lx) != (nemu_mcause = %lx)\n", npc_regs.mcause, nemu_regs->mcause);
        PRINT_ATTR_REC
    }
    return;
}