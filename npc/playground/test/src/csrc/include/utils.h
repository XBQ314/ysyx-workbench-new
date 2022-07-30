#ifndef _UTILS_H__
#define _UTILS_H__

#include <stdint.h>
// ----------- state -----------
// NPC_RUNNING 表示NPC正常进行始终周期更新
// NPC_STOP 表示NPC运行暂停
// NPC_END 表示NPC运行结束。可能是碰到了ebreak
// NPC_ABORT 表示NPC运行中断
// NPC_QUIT
enum { NPC_RUNNING, NPC_STOP, NPC_END, NPC_ABORT, NPC_QUIT };

typedef struct 
{
  int state;
//   uint64_t halt_pc;
//   uint32_t halt_ret;
} NPCState;
extern NPCState npc_state;


#define PRINT_FONT_GRE  printf("\033[32m"); //绿色
#define PRINT_FONT_RED  printf("\033[31m"); //红色
#define PRINT_ATTR_REC  printf("\033[0m");  //重新设置属性到缺省设置 

int is_exit_status_bad();

extern "C" void init_disasm(const char *triple);

// 第一个参数是要写入的字符串
// 第二个参数不知道是啥意思，nemu中用的96,我这里用的128,好像都行
// 第三个参数就是指令的地址(0x80000000)之类的
// 第四个参数是指令值的地址
// 第五个参数是指令的字节长度
extern "C" void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);

#endif