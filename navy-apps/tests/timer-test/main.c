#include <stdint.h>
#include <stdio.h>
#include <NDL.h>
#include "syscall.h"

int main()
{
    printf("This is Navy timer-test!\n");
    NDL_Init(0);
    uint32_t ms = NDL_GetTicks();
    while(1)
    {
        while(NDL_GetTicks() < ms);
        ms+=500;
        printf("ms:%d\n", ms);
    }
    return 0;
}