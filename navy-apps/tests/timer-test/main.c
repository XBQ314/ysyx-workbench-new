#include <stdint.h>
#include <stdio.h>
#include <NDL.h>
#include "syscall.h"
#include <sys/time.h>
/*
struct timeval 
{
  time_t      tv_sec;     // seconds
  suseconds_t tv_usec;    // microseconds
};
and gives the number of seconds and microseconds since the  Epoch  (see
time(2)).
The tz argument is a struct timezone:
struct timezone 
{
  int tz_minuteswest;     // minutes west of Greenwich 
  int tz_dsttime;         // type of DST correction 
};
*/

int main()
{
    printf("This is Navy timer-test!\n");
    NDL_Init(0);
    time_t current_ms = 0;
    while(1)
    {
        if(NDL_GetTicks() - current_ms >= 500)
        {
            printf("0.5 seconds has pased!\n");
            current_ms = NDL_GetTicks();
        }
    }
    return 0;
}