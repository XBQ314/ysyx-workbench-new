#include "verilated.h"
#include "verilated_vcd_c.h"
#include "VRegisterFiles.h"
#include <stdio.h>

VerilatedContext* contextp = NULL;
VerilatedVcdC* tfp = NULL;

static VRegisterFiles* top;

void step_and_dump_wave()
{
    top->eval();
    contextp->timeInc(1);
    tfp->dump(contextp->time());
}

static void single_cycle() 
{
	top->clock = 0; step_and_dump_wave();
	top->clock = 1; step_and_dump_wave();
}

static void reset(int n)
{
	top->reset = 1;
    while (n -- > 0) single_cycle();
	top->reset = 0;
}

void sim_init()
{
  contextp = new VerilatedContext;
  tfp = new VerilatedVcdC;
  top = new VRegisterFiles;
  contextp->traceEverOn(true);
  top->trace(tfp, 0);
  tfp->open("./wavedata/VRegisterFiles.vcd");
}

void sim_exit(){
  step_and_dump_wave();
  tfp->close();
}

int main() 
{
    sim_init();
    reset(5);
    long long golden_data[32]={};
    top->io_read2_idx = 0;
    for(int i = 0;i < 10;i++)
    {
        top->io_enw = 1;single_cycle();
        for(int j = 0;j <= 31; j++)
        {
            long long val = rand()%((long long) 1<<60);golden_data[j] = val;
            top->io_write_idx = j;
            top->io_in_data = val;
            single_cycle();
        }
        top->io_enw = 0;single_cycle();
        for(int j = 0;j <= 31; j++)
        {
            top->io_read1_idx = rand()%32;
            top->io_read2_idx = rand()%32;
            single_cycle();
            assert(top->io_out_data1 == golden_data[top->io_read1_idx]);
            assert(top->io_out_data2 == golden_data[top->io_read2_idx]);
        }
    }

    sim_exit();
}