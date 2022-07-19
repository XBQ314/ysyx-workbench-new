#include <am.h>
#include <klib-macros.h>

extern char _heap_start;
int main(const char *args);

extern char _pmem_start;
#define PMEM_SIZE (128 * 1024 * 1024)
#define PMEM_END  ((uintptr_t)&_pmem_start + PMEM_SIZE)

Area heap = RANGE(&_heap_start, PMEM_END);
#ifndef MAINARGS
#define MAINARGS ""
#endif
static const char mainargs[] = MAINARGS;

void putch(char ch) {
}

void halt(int code) 
{  
  asm volatile("mv a0, %0; ebreak" : :"r"(code)); // ebreak需要自己实现；
                                                  // nemu中的实现方法是，如果R(10) == 0，就返回goodtrap
  while (1);
}

void _trm_init() {
  int ret = main(mainargs);
  halt(ret);
}
