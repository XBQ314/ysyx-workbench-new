#include <sys/time.h>
#include <time.h>
#include "device.h"

static struct timeval boot_time = {};

void init_device()
{
    gettimeofday(&boot_time, NULL);
}

long long gettime()
{
    // struct timeval now;
    // gettimeofday(&now, NULL);
    // long seconds = now.tv_sec - boot_time.tv_sec;
    // long useconds = now.tv_usec - boot_time.tv_usec;
    // return seconds * 1000000 + (useconds + 500);

    struct timeval now;
    gettimeofday(&now, NULL);
    long seconds = now.tv_sec;
    long useconds = now.tv_usec;
    return seconds * 1000000 + (useconds + 500);
}