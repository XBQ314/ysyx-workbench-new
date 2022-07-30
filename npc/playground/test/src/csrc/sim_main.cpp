#include <verilated.h>          // 核心头文件
#include <verilated_vcd_c.h>    // 波形生成头文件
#include "VTwoS.h"           // 双控开关头文件
#include <stdio.h>

VTwoS* top;
VerilatedVcdC* tfp;             // 波形生成对象指针
VerilatedContext* contextp;

vluint64_t main_time = 0;           // 仿真时间戳
const vluint64_t sim_time = 1024;   // 最大仿真时间戳

int main(int argc, char **argv)
{
    // 一些初始化工作
    Verilated::commandArgs(argc, argv);
    Verilated::traceEverOn(true);

    // 为对象分配内存空间
    top = new VTwoS;
    tfp = new VerilatedVcdC;
	contextp = new VerilatedContext;

    // tfp初始化工作
    top->trace(tfp, 99);
    tfp->open("two_s.vcd");

    int count = 0;

	for(int i = 0;i<=100;i++)
	{
		contextp->timeInc(1);
		int test1=rand()&1;
		int test2=rand()&1;
		top->io_in1=test1;
		top->io_in2=test2;
		top->eval();
		tfp->dump(contextp->time());	
    	printf("a:%d, b:%d, f:%d\n", top->io_in1, top->io_in2, top->io_out);
	}
   // while(!Verilated::gotFinish() && main_time < sim_time)
   
    //clean 
    tfp->close();
    delete top;
    delete tfp;
    exit(0);
    return 0;
}
