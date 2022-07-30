#ifndef _DIFFTEST_H_
#define _DIFFTEST_H_
#include <stdint.h>
#include <stdio.h>

extern void (*difftest_memcpy)(uint64_t addr, void *buf, size_t n, bool direction);
extern void (*difftest_regcpy)(void *dut, bool direction);
extern void (*difftest_exec)(uint64_t n);
extern void (*difftest_raise_intr)(uint64_t NO);
extern void (*difftest_init)();

void init_npc_difftest(long img_size);
void difftest_check(struct npc_reg_struct* nemu_regs);

extern bool access_device;
#endif