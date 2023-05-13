#include <am.h>
#include <nemu.h>

#define SYNC_ADDR (VGACTL_ADDR + 4) // 同步寄存器，nemu中未完成实现 0xa000104

void __am_gpu_init() 
{
  // int i;
  // int w = inw(VGACTL_ADDR);  // TODO: get the correct width
  // int h = inw(VGACTL_ADDR+2);  // TODO: get the correct height
  // uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
  // for (i = 0; i < w * h; i ++) fb[i] = i;
  // outl(SYNC_ADDR, 1);
}

// AM显示控制器信息
void __am_gpu_config(AM_GPU_CONFIG_T *cfg) 
{
  uint16_t H = inw(VGACTL_ADDR);
  uint16_t W = inw(VGACTL_ADDR+2);
  *cfg = (AM_GPU_CONFIG_T) 
  {
    .present = true, .has_accel = false,
    .width = W, .height = H,
    .vmemsz = W*H*4
  };
}

// AM帧缓冲控制器
// int x, y; void *pixels; int w, h; bool sync
void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) 
{
  uint32_t *color_buf = (uint32_t *)ctl->pixels;
  for(int j = 0;j < ctl->h;j++)
  {
    for(int i = 0;i < ctl->w;i++)
    {
      int p = 400*(ctl->y+j)+ctl->x+i;
      outl(FB_ADDR + (p<<2), color_buf[j*ctl->w+i]);
    }
  }
  if (ctl->sync)
  {
    outl(SYNC_ADDR, 1);
  }
}

// 
void __am_gpu_status(AM_GPU_STATUS_T *status) 
{
  status->ready = true;
}

//  uint32_t dest; void *src; int size
void __am_gpu_memcpy(AM_GPU_MEMCPY_T *mem)
{
  // uint32_t *src = mem->src;
  // uint32_t *dst = NULL;
  // dst = (FB_ADDR + mem->dest);
  // for (int i = 0; i < mem->size >> 2; i++, src++, dst++)
  // {
  //   *dst = *src;
  // }
  // char *c_src = (char *)src;
  // char *c_dst = (char *)dst;
  // for (int i = 0; i < (mem->size & 3); i++)
  // {
  //   c_dst[i] = c_src[i];
  // }
}