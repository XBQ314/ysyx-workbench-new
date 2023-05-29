#include "verilated.h"
#include "verilated_vcd_c.h"
#include "verilated_dpi.h"
#include "Vysyx_040154_RV64Top.h"
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include "mem.h"
#include "device.h"
#include "difftest.h"

extern long inst_num;
// 测试jal和jalr
    // 0x00468693, // addi a3,a3,4
    // 0x008000ef, // jal; pc=pc+0x08;x[1]=pc+4
    // 0x00360613, // addi a2,a2,3
    // 0x00000013, // nop
    // 0x00000013, // nop
    // 0x00000013, // nop
    // 0x00000013, // nop
    // 0x01c085e7, // jalr; pc=x[1]+1c;x[11]=pc+4
    // 0x00570713, // addi a4,a4,5
    // 0x00100073, // ebreak
static char *img_file = NULL;
extern Vysyx_040154_RV64Top* top;
// 0x00009117, //auipc	sp,0x9
// 0x00000513, // li	a0,0
// 0x00258593, // addi a1,a1,2
// 0x00360613, // addi a2,a2,3
// 0x00468693, // addi a3,a3,4
// 0x00570713, // addi a4,a4,5
// 0x00000013, // nop
// 0x008000ef, // jal; pc=pc+0x08;x[1]=pc+4
// 0x01c085e7, // jalr; pc=x[1]+1c;x[11]=pc+4
// 0x00a58863, // beq x[10], x[11], 0x0c
// 0x00150513, // addi a0,a0,1
// 0x00100073, // ebreak
static const unsigned int img[] = 
{
// 0x00150513, // addi a0,a0,1
// 0x00150513, // addi a0,a0,1
// 0x00500793, // li	a5,5
// 0x01d79793, // slli	a5,a5,0x1d
// 0x3ea78c23, // sb	a0,1016(a5)
// 0x00150513, // addi a0,a0,1
// 0x00150513, // addi a0,a0,1
// 0x00150513, // addi a0,a0,1
0x00258593, // addi a1,a1,2
0x00360613, // addi a2,a2,3
0x00468693, // addi a3,a3,4
0x00570713, // addi a4,a4,5
0x00100073, // ebreak
};

unsigned char mem[0x8000000] = 
{

};

axi4_mem <32,64,4> axi_mem(4096l*1024*1024);

unsigned int get_inst(unsigned long addr)
{

    assert(addr >= 0x80000000);
    unsigned int inst = 0;
    unsigned long real_addr = addr - 0x80000000;
    inst = *(unsigned int*) (mem+real_addr);

    return inst;
}

unsigned int mem_read(unsigned long addr, int len)
{
    assert(addr >= 0x80000000);
    unsigned int result = 0;
    unsigned long real_addr = addr - 0x80000000;
    if(len == 1)
    {
        result = *(unsigned char *)(mem+real_addr);
    }
    else if(len == 2)
    {
        result = *(unsigned short int *)(mem+real_addr);
    }
    else if(len == 4)
    {
        result = *(unsigned int*) (mem+real_addr);
    }
    return result;
}

// extern "C" void pmem_read(long long raddr, long long *rdata) // Load
// {
// // 总是读取地址为`raddr & ~0x7ull`的8字节返回给`rdata`
//     if(raddr == 0xa0000048)
//     {
//         // long long tmp = gettime();
//         mmio_flag = true;
//         *rdata = gettime();
//         // printf("rdata:%lld\n", &rdata);
//     }

//     if(raddr < 0x80000000 || raddr >= 0x88000000)return;
//     else
//     {
//         // 直接改变指针是没有用的，因为改变的是指针的行参
//         // 要直接改变指针指向的内存区域的值，也就是传入的参数的内存区域。
//         // printf("read from raddr:0x%llx\n", raddr);
//         *rdata = *(long long *)((raddr &(~0x7ull)) + mem - 0x80000000);
//         assert(rdata);
//         return;
//     }

// }

extern "C" void pmem_read(long long addr, uint8_t *r_data) // Load
{
// 总是读取地址为`raddr & ~0x7ull`的8字节返回给`rdata`
    assert(addr >= 0x80000000);
    // printf("pmem_read:%llx, %2x\n", addr, *r_data);
    if(addr >= 0x80000000 && addr <= 0x88000000)*r_data = *(uint8_t *)(addr + mem - 0x80000000);
    else if(addr == 0xa0000048)
    {
        // long long tmp = gettime();
        mmio_flag = true;
        *r_data = gettime();
        // printf("rdata:%lld\n", &rdata);
    }
    else
    {
        printf("pmem_read:%llx, %2x\n", addr, *r_data);
        // assert(0);
    }
}

// extern "C" void pmem_write(long long waddr, long long wdata, uint8_t wmask) // Store
// {
// // 总是往地址为`waddr & ~0x7ull`的8字节按写掩码`wmask`写入`wdata`
// // `wmask`中每比特表示`wdata`中1个字节的掩码,
// // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
//     // assert(waddr == (waddr &(~0x7ull)));
//     // if(waddr == 0xa00003f8 && top->io_enMEM2WB) // 串口device
//     if(waddr == 0xa00003f8) // 串口device
//     {
//         // printf("111111111111111111111111111111111111111111111111111\n");
//         mmio_flag = true;
//         char *tmp = NULL;
//         tmp = (char *)(&wdata);
//         assert(tmp);
//         printf("%c", *tmp);
//     }
    
//     if(waddr < 0x80000000 || waddr >= 0x88000000)return;
//     else
//     {
//         // if(*(uint64_t *)(0x8009dfb8 + mem - 0x80000000) != 0)
//         // {
//         //     printf("0x8009dfb8:0x%llx\n", *(uint64_t *)(0x8009dfb8 + mem - 0x80000000));
//         //     // assert(0);
//         // }
//         if(wmask == 0x01 || wmask == 0x03 || wmask == 0x0f || wmask == 0xff)
//         {
//             // if(waddr==0x80013404)
//             // {
//             //     printf("write to waddr:0x%llx, wdata:0x%llx, wmask:0x%x\n", waddr, wdata, wmask);
//             //     printf("inst_num: %ld\n", inst_num);
//             // }            
//         }
//         switch(wmask)
//         { 
//             case 0x01:  
//                 // printf("wmask:0x01, wdata:0x%llx\n", wdata);
//                 *(uint8_t   *)(waddr + mem - 0x80000000) = wdata;break;// sb
//             case 0x03:   
//                 // printf("wmask:0x03, wdata:0x%llx\n", wdata);
//                 *(uint16_t  *)(waddr + mem - 0x80000000) = wdata;break;// sh
//             case 0x0f:
//                 // printf("wmask:0x0f, wdata:0x%llx\n", wdata);
//                 *(uint32_t  *)(waddr + mem - 0x80000000) = wdata;break;// sw
//             case 0xff:
//                 // printf("wmask:0xff, wdata:0x%llx\n", wdata);
//                 *(uint64_t  *)(waddr + mem - 0x80000000) = wdata;break;// sd
//         }
//         return;
//     }
//     // pmem_write(0x80000000, 0xffffffffffffff01, 0x01);
//     // pmem_write(0x80000001, 0xffffffffffff0102, 0x03);
//     // pmem_write(0x80000003, 0xffffffff01020304, 0x0f);
//     // pmem_write(0x80000007, 0x0102030405060708, 0xff);
// }

extern "C" void pmem_write(long long addr, uint8_t w_data)
{
    // assert(addr > 0x80000000);
    // printf("pmem_write:%llx, %2x\n", addr, w_data);
    if(addr == 0xa00003f8) // 串口device
    {
        // printf("111111111111111111111111111111111111111111111111111\n");
        mmio_flag = true;
        char *tmp = NULL;
        tmp = (char *)(&w_data);
        assert(tmp);
        printf("%c", *tmp);
    }
    else if(addr >= 0x80000000 && addr <= 0x88000000)
    {
        *(uint8_t   *)(addr + mem - 0x80000000) = w_data;
        // if(addr == 0x81d7eed8) printf("pmem_write:%llx, %2x\n", addr, w_data);
    }
    else assert(0);
}

void init_mem()
{
    memcpy(mem, img, sizeof(img));
    return;
}

long load_img(char* img_file_name)
{
    img_file = img_file_name;
    if (img_file == NULL) 
    {
        printf("No image is given. Use the default build-in image.\n");
        return 4096; // built-in image size
    }
    

    FILE *fp = fopen(img_file, "rb");
    assert(fp);

    fseek(fp, 0, SEEK_END);
    long size = ftell(fp);

    printf("The image is %s, size = %ld\n", img_file, size);

    fseek(fp, 0, SEEK_SET);
    
    int ret = fread(mem, size, 1, fp); // 将文件流写入指针写定的内存区块中
    
    assert(ret == 1);
    
    fclose(fp);

    axi_mem.load_binary(img_file_name, 0x80000000);
    return size;
}