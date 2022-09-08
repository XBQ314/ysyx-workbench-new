/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>
#include <cpu/cpu.h>
#include <difftest-def.h>
#include <memory/paddr.h>

struct npc_reg_struct 
{
  word_t gpr[32];
  word_t pc;
  word_t mstatus, mie, mtvec, mepc, mcause, mip;
};

void difftest_memcpy(paddr_t addr, void *buf, size_t n, bool direction) 
{
  if(direction == DIFFTEST_TO_REF)
  {
    int i = 0;
    for(i = 0; i < n; i++)
    {
      vaddr_write(addr+i, 1, *((uint8_t*)buf+i));
    }
    // for(i = 0; i < n; i++)
    // {
    //   printf("%02lx ", vaddr_read(addr+i, 1));
    // }
    // printf("\n");
  }
  else if(direction == DIFFTEST_TO_DUT)
  {
    printf("NO IMPLENMATION.\n");
    assert(0);
  }
  return;
}

// void difftest_csrcpy(void *dut, bool direction)
// {
//   return;
// }

// void difftest_uarchstatus_cpy()
// {
//   return;
// }

void difftest_regcpy(void *dut, bool direction) 
{
  // printf("difftest_regcpy!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
  if(direction == DIFFTEST_TO_REF)
  {
    // printf("DIFFTEST_TO_REF\n");
    struct npc_reg_struct* npc_regs = (struct npc_reg_struct*)dut;
    for(int i=0;i<32;i++)
    {
      cpu.gpr[i] = npc_regs->gpr[i];
      // printf("nemu gpr[%d] = npc gpr[%d] = %lu\n", i, i, npc_regs->gpr[i]);
    }
    cpu.pc = npc_regs->pc;
    cpu.mstatus = npc_regs->mstatus;
    cpu.mie = npc_regs->mie;
    cpu.mepc = npc_regs->mepc;
    cpu.mcause = npc_regs->mcause;
    cpu.mtvec = npc_regs->mtvec;
    cpu.mip = npc_regs->mip;
    // printf("nemu pc = npc pc = %lx\n", npc_regs->pc);
  }
  else if(direction == DIFFTEST_TO_DUT)
  {
    struct npc_reg_struct* ref_regs = (struct npc_reg_struct*)dut;
    for(int i=0;i<32;i++)
    {
      ref_regs->gpr[i] = cpu.gpr[i];
    }
    ref_regs->pc = cpu.pc;
    ref_regs->mstatus = cpu.mstatus;
    ref_regs->mie     = cpu.mie;
    ref_regs->mepc    = cpu.mepc;
    ref_regs->mcause  = cpu.mcause;
    ref_regs->mtvec   = cpu.mtvec;
    ref_regs->mip     = cpu.mip;
  }
  return;
  // assert(0);
}

void difftest_exec(uint64_t n) 
{
  cpu_exec(n);
  // assert(0);
  return;
}

void difftest_raise_intr(word_t NO) 
{
  assert(0);
}

void difftest_init(int port) 
{
  /* Perform ISA dependent initialization. */
  init_isa();
}
