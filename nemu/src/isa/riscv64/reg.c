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
#include "local-include/reg.h"

const char *regs[] = {
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

void isa_reg_display() 
{
  Log("this is isa reg display");
  printf("%s:0x%016lX\n", "pc", cpu.pc);
  // int i, j;
  for(int i = 0;i < 8;i++)
  {
    for(int j = 0;j < 4;j++)
    {
      int n = 4*i+j;
      printf("%s(%d):0x%016lX \t", regs[n], n, cpu.gpr[n]);
    }
    printf("\n");
  }
  return;
}

void isa_csr_display()
{
  printf("mtvec: 0x%016lX \n", cpu.mtvec);
  printf("mie: 0x%016lX \n", cpu.mie);
  printf("mepc: 0x%016lX \n", cpu.mepc);
  printf("mstatus: 0x%016lX \n", cpu.mstatus);
  printf("mcause: 0x%016lX \n", cpu.mcause);
  printf("mip: 0x%016lX \n", cpu.mip);
}

word_t isa_reg_str2val(const char *s, bool *success) 
{
  int i;
  for(i = 0; i < 32; i++)
  {
    if(strcmp(regs[i], s) == 0)
    {
      // printf("Find the reg:%s, the value is:%lu\n", s, cpu.gpr[i]);
      *success = true;
      return cpu.gpr[i];
    }
  }
  if(strcmp("pc", s) == 0)
  {
    // printf("Find the reg:%s, the value is:%lu\n", s, cpu.pc);
    *success = true;
    return cpu.pc;
  }
  Log("Can't find the reg:%s", s);
  *success = false;
  return 0;
}
