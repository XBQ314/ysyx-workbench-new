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
#include <cpu/difftest.h>
#include "../local-include/reg.h"

bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) 
{
  for(int i = 0; i < 32; i++)
  {
    if(ref_r->gpr[i] != cpu.gpr[i])
    {
      printf("difftest gpr[%d] error.", i);
      printf("ref = 0x%016lX, dut = 0x%016lX.\n", ref_r->gpr[i], cpu.gpr[i]);
      return false;
    }
  }
  if (ref_r->pc != cpu.pc)
  {
    printf("difftest pc error.dut pc = %lx, ref pc = %lx.\n", cpu.pc, ref_r->pc);
    return false;
  }

  if(ref_r->mstatus != cpu.mstatus)
  {
    printf("difftest mstatus error!\n");
    printf("mstatus ref = 0x%016lX, dut = 0x%016lX.\n", ref_r->mstatus, cpu.mstatus);
    printf("mtvec   ref = 0x%016lX, dut = 0x%016lX.\n", ref_r->mtvec, cpu.mtvec);
    printf("mepc    ref = 0x%016lX, dut = 0x%016lX.\n", ref_r->mepc, cpu.mepc);
    printf("mcause  ref = 0x%016lX, dut = 0x%016lX.\n", ref_r->mcause, cpu.mcause);
    return false;
  }
  if(ref_r->mtvec != cpu.mtvec)  
  {
    printf("difftest mtvec error! ");
    printf("ref = 0x%016lX, dut = 0x%016lX.\n", ref_r->mtvec, cpu.mtvec);
    return false;
  }
  if(ref_r->mepc != cpu.mepc)
  {
    printf("difftest mepc error! ");
    printf("ref = 0x%016lX, dut = 0x%016lX.\n", ref_r->mepc, cpu.mepc);
    return false;
  }
  if(ref_r->mcause != cpu.mcause)
  {
    printf("difftest mcause error! ");
    printf("ref = 0x%016lX, dut = 0x%016lX.\n", ref_r->mcause, cpu.mcause);
    return false;
  }
  return true;
}

void isa_difftest_attach() 
{
}
