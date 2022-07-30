// #include <stdint.h>
// #include <stdio.h>
// #include <stdlib.h>
// #include <string.h>
// #include <unistd.h>

// static int evtdev = -1;
// static int fbdev = -1;
// static int screen_w = 0, screen_h = 0;

// uint32_t NDL_GetTicks() {
//   return 0;
// }

// int NDL_PollEvent(char *buf, int len) {
//   return 0;
// }

// void NDL_OpenCanvas(int *w, int *h) {
//   if (getenv("NWM_APP")) {
//     int fbctl = 4;
//     fbdev = 5;
//     screen_w = *w; screen_h = *h;
//     char buf[64];
//     int len = sprintf(buf, "%d %d", screen_w, screen_h);
//     // let NWM resize the window and create the frame buffer
//     write(fbctl, buf, len);
//     while (1) {
//       // 3 = evtdev
//       int nread = read(3, buf, sizeof(buf) - 1);
//       if (nread <= 0) continue;
//       buf[nread] = '\0';
//       if (strcmp(buf, "mmap ok") == 0) break;
//     }
//     close(fbctl);
//   }
// }

// void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) {
// }

// void NDL_OpenAudio(int freq, int channels, int samples) {
// }

// void NDL_CloseAudio() {
// }

// int NDL_PlayAudio(void *buf, int len) {
//   return 0;
// }

// int NDL_QueryAudio() {
//   return 0;
// }

// int NDL_Init(uint32_t flags) {
//   if (getenv("NWM_APP")) {
//     evtdev = 3;
//   }
//   return 0;
// }

// void NDL_Quit() {
// }

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static int evtdev = -1;
static int fbdev = -1;
static int screen_w = 0, screen_h = 0;

extern int _gettimeofday(struct timeval *tv, struct timezone *tz);
static uint32_t time_init_ms = 0;
static struct timeval time_now = {};

uint32_t NDL_GetTicks() 
{
  _gettimeofday(&time_now, NULL);
  return (time_now.tv_sec)*1000+(time_now.tv_usec)/1000-time_init_ms;
}

int NDL_PollEvent(char *buf, int len) 
{
  int fd = open("/dev/events", 0);
  // 如果我没有猜错，这个open最终会调用nanos-lite中的fs_open函数
  // 然后，fsopen函数就会找到这个"/dev/events"虚拟文件，并返回他的索引
  // 这样，在下面这个函数使用fd来open的时候，最终就会调用"/dev/events"虚拟文件设置的读函数
  // 具体来说，"/dev/events"虚拟文件设置的读函数是events_read
  // 这个events_read(自己实现的)会调用abmachine中的ioread来读取按键输入，并按照kd Q等形式把字符串写入buf中

  return read(fd, buf, len);
}

void NDL_OpenCanvas(int *w, int *h) 
{
  if (getenv("NWM_APP")) 
  {
    int fbctl = 4;
    fbdev = 5;
    screen_w = *w; screen_h = *h;
    char buf[64];
    int len = sprintf(buf, "%d %d", screen_w, screen_h);
    // let NWM resize the window and create the frame buffer
    write(fbctl, buf, len);
    while (1) {
      // 3 = evtdev
      int nread = read(3, buf, sizeof(buf) - 1);
      if (nread <= 0) continue;
      buf[nread] = '\0';
      if (strcmp(buf, "mmap ok") == 0) break;
    }
    close(fbctl);
  }

  int fd = open("/proc/dispinfo", 0);
  char wh[128];
  // char buf1[16];char buf2[16];
  read(fd, wh, sizeof(wh));
  sscanf(wh, "WIDTH:%d\nHEIGHT:%d", w, h);
  screen_w = *w;
  screen_h = *h;
  printf("width:%d, height:%d\n", *w, *h);
}

void NDL_DrawRect(uint32_t *pixels, int x, int y, int w, int h) 
{
}

void NDL_OpenAudio(int freq, int channels, int samples) {
}

void NDL_CloseAudio() {
}

int NDL_PlayAudio(void *buf, int len) {
  return 0;
}

int NDL_QueryAudio() {
  return 0;
}

int NDL_Init(uint32_t flags) 
{
  printf("NDL_INIT!!!!!!!!!!!!!!!!!!!!!!!\n");
  if (getenv("NWM_APP")) 
  {
    evtdev = 3;
  }

  struct timeval time_init;
  _gettimeofday(&time_init, NULL);
  time_init_ms = time_init.tv_usec/1000+time_init.tv_sec*1000;
  return 0;
}

void NDL_Quit() 
{
  printf("NDL_QUIT!!!!!!!!!!!!!!!!!!!!!!!\n");
}

