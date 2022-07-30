#include "verilated.h"
#include "verilated_dpi.h"
#include "VRV64Top.h"
#include <string.h>
#include "reg.h"
#include "utils.h"

const char *regs[] = 
{
  "$0", "ra", "sp", "gp", "tp", "t0", "t1", "t2",
  "s0", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
  "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7",
  "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"
};

struct npc_reg_struct npc_regs;
uint64_t NPC_PC;
uint64_t *cpu_gpr = NULL;
extern "C" void set_gpr_ptr(const svOpenArrayHandle r) 
{
  cpu_gpr = (uint64_t  *)(((VerilatedDpiOpenVar*)r)->datap());
}

extern "C" void read_mstatus(uint64_t a)
{
  npc_regs.mstatus = a;
}

extern "C" void read_mtvec(uint64_t a)
{
  npc_regs.mtvec = a;
}

extern "C" void read_mepc(uint64_t a)
{
  npc_regs.mepc = a;
}

extern "C" void read_mcause(uint64_t a)
{
  npc_regs.mcause = a;
}

void update_npc_regs()
{
  for(int i = 0; i< 32; i++)
  {
    npc_regs.gpr[i] = cpu_gpr[i];
  }
  npc_regs.pc = NPC_PC;
}

void check_ebreak()
{
  if(cpu_gpr[10] == 0)
  {
    PRINT_FONT_GRE
    printf("HIT GOOD TRAP\n");
    PRINT_ATTR_REC
  }
  else
  {
    PRINT_FONT_RED
    printf("HIT BAD TRAP\n");
    PRINT_ATTR_REC
  }
}

// 一个输出RTL中通用寄存器的值的示例
void display_reg()
{
  // int i;
  // printf("%s:\t0x%016lX\n", "pc", NPC_PC);
  // for (i = 0; i < 32; i++) 
  // {
  //   printf("gpr[%d] = 0x%lx\n", i, cpu_gpr[i]);
  // }
  
  printf("%s:0x%016lX\n", "pc", NPC_PC);
  // int i, j;
  for(int i = 0;i < 8;i++)
  {
    for(int j = 0;j < 4;j++)
    {
      int n = 4*i+j;
      printf("%s(%d):0x%016lX \t", regs[n], n, cpu_gpr[n]);
    }
    printf("\n");
  }
}

uint64_t find_reg_str2val(const char *s, bool *success) 
{
  int i;
  for(i = 0; i < 32; i++)
  {
    if(strcmp(regs[i], s) == 0)
    {
      // printf("Find the reg:%s, the value is:%lu\n", s, cpu_gpr[i]);
      *success = true;
      return cpu_gpr[i];
    }
  }
  if(strcmp("pc", s) == 0)
  {
    // printf("Find the reg:%s, the value is:%lu\n", s, NPC_PC);
    *success = true;
    return NPC_PC;
  }
  printf("Can't find the reg:%s\n", s);
  *success = false;
  return 0;
}