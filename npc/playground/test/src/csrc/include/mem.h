#ifndef MEM_H_
#define MEM_H_
#include <stdint.h>
#include "axi4.hpp"
#include "axi4_slave.hpp"
#include "axi4_mem.hpp"

unsigned int get_inst(unsigned long addr);
unsigned int mem_read(unsigned long addr, int len);
// extern "C" void pmem_read(long long raddr,long long *rdata);
// extern "C" void pmem_write(long long waddr, long long wdata, uint8_t wmask);
extern "C" void pmem_read(long long addr, uint8_t *r_data);
extern "C" void pmem_write(long long addr, uint8_t w_data);
void init_mem();
long load_img(char* img_file_name);

#endif