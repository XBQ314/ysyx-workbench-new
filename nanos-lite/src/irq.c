#include <common.h>

extern void do_syscall(Context *c);

static Context* do_event(Event e, Context* c) 
{
//   switch (e.event) 
//   {
//     default: panic("Unhandled event ID = %d", e.event);
//   }

//   return c;
  switch (e.event) 
  {
    case EVENT_YIELD: printf("EVENT_YIELD!\n"); 
                      // c->mepc += 4; 
                      break;
    case EVENT_SYSCALL: //printf("EVENT_SYSCALL! Type:"); 
                        do_syscall(c); 
                        // c->mepc += 4; 
                        break;
    case EVENT_IRQ_TIMER:
      *((volatile unit64_t *)(0x02004000)) += 7000000;
      break;
    default: panic("Unhandled event ID = %d", e.event);
  }

  return c;
}

void init_irq(void) {
  Log("Initializing interrupt/exception handler...");
  cte_init(do_event);
}
