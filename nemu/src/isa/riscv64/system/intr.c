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

word_t isa_raise_intr(word_t NO, vaddr_t epc) 
{
  /* TODO: Trigger an interrupt/exception with ``NO''.
   * Then return the address of the interrupt/exception vector.
   */
  cpu.mepc = epc;
  cpu.mcause = NO;

  // 将MIE(3)保存到MPIE(7)位，然后关闭MIE
  uint64_t tmp = cpu.mstatus;
  cpu.mstatus = (BITS(tmp, 63, 8) << 8) | (BITS(tmp, 3, 3) << 7) | BITS(tmp, 6, 0);
  cpu.mstatus = cpu.mstatus & (~(1 << 3));
  #ifdef CONFIG_DIFFTEST
    cpu.mstatus |= 0x1800;
  #endif
  IFDEF(CONFIG_ETRACE,printf("ETRACE. Call exception. MEPC=0x%016lX, MCAUSE=%ld, a7=%ld;\n", epc, NO, cpu.gpr[17]));
  return cpu.mtvec;
}

word_t isa_query_intr() {
  return INTR_EMPTY;
}
