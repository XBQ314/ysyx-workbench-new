#ifndef _CPU_H_
#define _CPU_H_

void step_and_dump_wave();
void single_cycle();
void reset(int n);
void sim_init();
void sim_exit();

void cpu_exec(uint64_t n);

#endif