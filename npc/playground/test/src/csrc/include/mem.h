#ifndef MEM_H_
#define MEM_H_
#include <stdint.h>

unsigned int get_inst(unsigned long addr);
unsigned int mem_read(unsigned long addr, int len);
extern "C" void pmem_read(long long raddr,long long *rdata);
extern "C" void pmem_write(long long waddr, long long wdata, uint8_t wmask);
void init_mem();
long load_img(char* img_file_name);

#endif