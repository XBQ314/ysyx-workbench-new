/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <device/map.h>
#include <device/alarm.h>
#include <utils.h>

static uint32_t *rtc_port_base = NULL;

// 每当amkernels中有调用io_read读写设备寄存器
// 会执行abstract machine中定义的相应设备寄存器的处理函数
// 在相应设备寄存器的处理函数中，调用inb inl之类的函数对设备的MMIO地址进行读写
// 就会触发nemu中由IOMap maps数组记录的一系列设备MMIO信息中对应的一个handler
// 其中，offset是读/写地址相对于map->start的偏移
// len是要读写的字节长度(由abstract machine中设备寄存器处理函数中，使用的in/out指令读写的字节数确定)
// is_write就是是否读写的flag
static void rtc_io_handler(uint32_t offset, int len, bool is_write) 
{
  assert(offset == 0 || offset == 4);
  if (!is_write) 
  {
    uint64_t us = get_time();
    rtc_port_base[0] = (uint32_t)us;
    rtc_port_base[1] = us >> 32;
    // printf("us:%lu  ", us);
    // printf("us_l:%u  ", rtc_port_base[0]);
    // printf("us_h:%u\n", rtc_port_base[1]);
  }
}

#ifndef CONFIG_TARGET_AM
static void timer_intr() 
{
  if (nemu_state.state == NEMU_RUNNING) 
  {
    extern void dev_raise_intr();
    dev_raise_intr();
  }
}
#endif

void init_timer() 
{
  rtc_port_base = (uint32_t *)new_space(8); //申请一个设备寄存器占用的内存
#ifdef CONFIG_HAS_PORT_IO
  add_pio_map ("rtc", CONFIG_RTC_PORT, rtc_port_base, 8, rtc_io_handler);
#else
//add_mmio_map(const char *name, paddr_t addr, void *space, uint32_t len, io_callback_t callback) 
//将上面申请的设备寄存器占用的内存与设备名字、设备MMIO地址、设备寄存器大小、设备模拟函数绑定
  add_mmio_map("rtc", CONFIG_RTC_MMIO, rtc_port_base, 8, rtc_io_handler); 
#endif
  IFNDEF(CONFIG_TARGET_AM, add_alarm_handle(timer_intr));
}
