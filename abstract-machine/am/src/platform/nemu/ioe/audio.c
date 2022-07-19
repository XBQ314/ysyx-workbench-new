#include <am.h>
#include <nemu.h>
#include <klib.h>

#define AUDIO_FREQ_ADDR      (AUDIO_ADDR + 0x00)
#define AUDIO_CHANNELS_ADDR  (AUDIO_ADDR + 0x04)
#define AUDIO_SAMPLES_ADDR   (AUDIO_ADDR + 0x08)
#define AUDIO_SBUF_SIZE_ADDR (AUDIO_ADDR + 0x0c)
#define AUDIO_INIT_ADDR      (AUDIO_ADDR + 0x10)
#define AUDIO_COUNT_ADDR     (AUDIO_ADDR + 0x14)

static volatile int tail = 0;

static void audio_write(uint8_t *buf, int len) 
{
  int i = 0;
  int sbuf_size = inl(AUDIO_SBUF_SIZE_ADDR);
  int nwrite = len;
  while(inl(AUDIO_COUNT_ADDR) + len >= sbuf_size){} // 等待sbuf有空闲，这句话及其重要
  // int count = inl(AUDIO_COUNT_ADDR);

  // 通过维护一个tail变量记录上次写入到了哪里
  // 存在可以直接一次写入和需要把信息截断成两节分别写入的两种情况
  if(tail + nwrite < sbuf_size)
  {
    while(i < nwrite)
    {
      outb(AUDIO_SBUF_ADDR + tail + i, *(buf+i));
      // printf("nwrite:%d, tail:%d\n", nwrite, tail);
      i++;
    }
    tail += nwrite;
  }
  else
  {
    int fisrt_write_bytes = sbuf_size - tail;
    int second_write_bytes = nwrite - fisrt_write_bytes;
    // printf("nwrite:%d, first_bytes:%d, second_bytes:%d, tail:%d\n", nwrite, fisrt_write_bytes, second_write_bytes, tail);
    while(i < fisrt_write_bytes)
    {
      outb(AUDIO_SBUF_ADDR + tail + i, *(buf+i));
      i++;
    }
    while(i < nwrite)
    {
      // printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
      outb(AUDIO_SBUF_ADDR + i, *(buf+i));
      i++;
    }
    tail = second_write_bytes;
  }

  // 更新一下count的值
  outl(AUDIO_COUNT_ADDR, inl(AUDIO_COUNT_ADDR)+len);
}

void __am_audio_init() 
{
}

void __am_audio_config(AM_AUDIO_CONFIG_T *cfg) 
{  
  cfg->present = true;
  cfg->bufsize = inl(AUDIO_SBUF_SIZE_ADDR);
  // printf("cfg->bfusize:%d\n", cfg->bufsize);
}

void __am_audio_ctrl(AM_AUDIO_CTRL_T *ctrl) 
{
  outl(AUDIO_FREQ_ADDR, ctrl->freq);
  outl(AUDIO_CHANNELS_ADDR, ctrl->channels);
  outl(AUDIO_SAMPLES_ADDR, ctrl->samples);
  outl(AUDIO_INIT_ADDR, 1);
  tail = 0;
}

void __am_audio_status(AM_AUDIO_STATUS_T *stat) 
{
  stat->count = inl(AUDIO_COUNT_ADDR);
}

void __am_audio_play(AM_AUDIO_PLAY_T *ctl) 
{
  int len = ctl->buf.end - ctl->buf.start;
  audio_write(ctl->buf.start, len);
}
