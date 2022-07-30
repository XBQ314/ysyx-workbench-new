#include <common.h>
#include "syscall.h"
#include "fs.h"

static int gettimeofday(void* a)
{
  long *time = (long *)a;
  *time = io_read(AM_TIMER_UPTIME).us / 1000000; //sec
  *(time+1) = io_read(AM_TIMER_UPTIME).us % 1000000; //usec
  return 0;
}

// #define CONFIG_STRACE
static void strace(uintptr_t a0)
{
#ifdef CONFIG_STRACE
  switch(a0)
  {
    case SYS_exit:  printf("SYS_exit!\n");break;
    case SYS_yield: printf("SYS_yield!\n");break;
    case SYS_open:  printf("SYS_open!\n");break;
    case SYS_read:  printf("SYS_read!\n");break;
    case SYS_write: printf("SYS_write!\n");break;
    case SYS_close: printf("SYS_close!\n");break;
    case SYS_lseek: printf("SYS_lseek!\n");break;
    case SYS_brk:   printf("SYS_brk\n");break;
    case SYS_gettimeofday:  printf("SYS_gettimeofday!\n");break;
    default: panic("Unhandled syscall ID = %d", a0);  
  }
#endif
}

void do_syscall(Context *c) 
{
  uintptr_t a[4];
  a[0] = c->GPR1;
  a[1] = c->GPR2;
  a[2] = c->GPR3;
  a[3] = c->GPR4;

  strace(a[0]);
  switch (a[0]) 
  {
    case SYS_exit:  //printf("SYS_exit!\n");
                    halt(a[1]);break;
    case SYS_yield: //printf("SYS_yield!\n");
                    yield();
                    c->GPRx=0;break;
    case SYS_open:  //printf("SYS_open!\n");
                    c->GPRx = fs_open((char *)a[1], a[2], a[3]);break;
    case SYS_read:  //printf("SYS_read!\n");
                    c->GPRx = fs_read(a[1], (void *)a[2], a[3]);break;
    case SYS_write: //printf("SYS_write!\n");
                    c->GPRx = fs_write(a[1], (char*)a[2], a[3]);break;
    case SYS_close: //printf("SYS_close!\n");
                    c->GPRx = fs_close(a[1]);break;
    case SYS_lseek: //printf("SYS_lseek!\n");
                    c->GPRx = fs_lseek(a[1], a[2], a[3]);break;
    case SYS_brk:   //printf("SYS_brk\n");
                    c->GPRx=0; //先默认一定成功
                    break;
    case SYS_gettimeofday:  //printf("SYS_gettimeofday!\n");
                            c->GPRx = gettimeofday((void*)a[1]);
                            break;
    default: panic("Unhandled syscall ID = %d", a[0]);
  }
}
