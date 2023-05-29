#ifndef _REG_H_
#define _REG_H
#include <stdint.h>

struct npc_reg_struct 
{
  uint64_t gpr[32];
  uint64_t pc;
  uint64_t mstatus, mie, mtvec, mepc, mcause, mip;
};
extern struct npc_reg_struct npc_regs;
extern long inst_num;
void update_npc_regs();
void display_reg();
void display_csr();
void check_ebreak();
uint64_t find_reg_str2val(const char *s, bool *success);

#endif