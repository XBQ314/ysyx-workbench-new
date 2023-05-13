#include <am.h>
#include <riscv/riscv.h>
#include <klib.h>

static Context* (*user_handler)(Event, Context*) = NULL;

Context* __am_irq_handle(Context *c) 
{
  // if (user_handler) {
  //   Event ev = {0};
  //   switch (c->mcause) {
  //     default: ev.event = EVENT_ERROR; break;
  //   }

  //   c = user_handler(ev, c);
  //   assert(c != NULL);
  // }

  // return c;

  if (user_handler) 
  {
    Event ev = {0};

    switch (c->mcause)
    {
      case 0x0b: // 在确定了是ecall触发的异常之后，根据通用寄存器a7来进行事件的区分。
        if(c->GPR1 == -1)
        {
          ev.event = EVENT_YIELD;
          c->mepc += 4;
          break;
        }
        else if(c->GPR1 >= 0 && c->GPR1 <= 19)
        {
          ev.event = EVENT_SYSCALL;
          c->mepc += 4;
          break;
        }
      default: ev.event = EVENT_ERROR; break;
    }

    c = user_handler(ev, c); // 调用lite-nano中设定的do_event函数，实现event处理并更改上下文
    assert(c != NULL);
  }

  return c;
}

extern void __am_asm_trap(void);

bool cte_init(Context*(*handler)(Event, Context*)) {
  // initialize exception entry
  asm volatile("csrw mtvec, %0" : : "r"(__am_asm_trap));

  // register event handler
  user_handler = handler;

  return true;
}

Context *kcontext(Area kstack, void (*entry)(void *), void *arg) {
  return NULL;
}

void yield() {
  asm volatile("li a7, -1; ecall");
}

bool ienabled() {
  return false;
}

void iset(bool enable) 
{
  if(enable)
  {
    asm volatile("csrsi mstatus, 8");
    asm volatile("li a0, 128");
    asm volatile("csrs mie, a0");
    //set_csr(mie, MIP_MTIP);
  }
  else
  {
    asm volatile("csrci mstatus, 8");
    // asm volatile("csrci mie, 8");
    //clear_csr(mie, MIP_MTIP);
  }
}
