#include <common.h>

#if defined(MULTIPROGRAM) && !defined(TIME_SHARING)
# define MULTIPROGRAM_YIELD() yield()
#else
# define MULTIPROGRAM_YIELD()
#endif

#define NAME(key) \
  [AM_KEY_##key] = #key,

static const char *keyname[256] __attribute__((used)) = 
{
  [AM_KEY_NONE] = "NONE",
  AM_KEYS(NAME)
};

size_t serial_write(const void *buf, size_t offset, size_t len) 
{
  for(int i=0;i<len;i++)
  {
    putch(*(char*)(buf+i));
  }
  // putstr((char*)buf); // 不可以用putstr，不然有概率会输出一大堆乱码
  return len;
}

size_t events_read(void *buf, size_t offset, size_t len) 
{
  AM_INPUT_KEYBRD_T ev = io_read(AM_INPUT_KEYBRD);
  if (ev.keycode == AM_KEY_NONE) return 0;
  char *key_updown = ev.keydown?"kd ":"ku ";
  strcpy(buf, key_updown);
  strcat(buf, keyname[ev.keycode]);
  for(int len=0;*(char*)(buf+len)!='\0';len++);
  // printf("key dect:%s, buf len=%d\n", (char *)buf, len);
  return len;
}

size_t dispinfo_read(void *buf, size_t offset, size_t len) 
{
  int w = io_read(AM_GPU_CONFIG).width;
  int h = io_read(AM_GPU_CONFIG).height;

  sprintf(buf, "screen_width:%dscreen_height:%d", w, h);
  // printf("dispinfo_read:%s\n", (char *)buf);
  return 0;
}


size_t fb_write(const void *buf, size_t offset, size_t len) 
{
  int screen_width = io_read(AM_GPU_CONFIG).width;
  // printf("offset:%d len:%d\n", offset, len);
  int x = offset % screen_width;
  int y = offset / screen_width;
  io_write(AM_GPU_FBDRAW, x, y, (char*)buf, len>>16, len & 0xffff, true);
  // printf("%d %d %d\n",len >> 16 , len & 0xffff, (len >> 16)* (len & 0xffff));
  return (len>>16) * (len & 0xffff);
}

void init_device() 
{
  Log("Initializing devices...");
  ioe_init();
}
