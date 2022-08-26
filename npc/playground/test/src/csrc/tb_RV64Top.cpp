#include <stdio.h>
#include "mem.h"
#include "reg.h"
#include "sdb.h"
#include "cpu.h"
#include "utils.h"
#include "difftest.h"
#include "device.h"
#include "axi4.hpp"
#include "axi4_slave.hpp"
#include "axi4_mem.hpp"


int main(int argc, char *argv[]) 
{
    long img_size = 100;
    init_mem(); // 将默认的img代码写入mem中
    if(argv[1]!=NULL)
    {
        img_size = load_img(argv[1]); //如果从am启动，则将其编译出的bin写到mem中
    }

    sim_init();
    init_device();
    reset(2);

    init_npc_difftest(img_size); // 6月份出现的段错误在这个函数中
    init_regex(); // 编译正则表达式
    init_wp_pool();


    init_disasm("riscv64-pc-linux-gnu"); // llvm初始化
    sdb_mainloop();

    sim_exit();
    return is_exit_status_bad(); // 这个返回值可以决定，如果是从am-kernels启动，退出NPC之后显示PASS还是FAIL.
}