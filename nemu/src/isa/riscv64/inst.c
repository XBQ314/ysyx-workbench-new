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

#include "local-include/reg.h"
#include <cpu/cpu.h>
#include <cpu/ifetch.h>
#include <cpu/decode.h>

#define R(i) gpr(i)
#define Mr vaddr_read
#define Mw vaddr_write

static void setCSRs(word_t csr, word_t t)
{
  if(csr == 0x00){cpu.mstatus = t;}
  else if(csr == 0x04){cpu.mie = t;}
  else if(csr == 0x05){cpu.mtvec = t;}
  else if(csr == 0x41){cpu.mepc = t;}
  else if(csr == 0x42){cpu.mcause = t;}
  else if(csr == 0x44){cpu.mip = t;}
  else{printf("csr number:%ld\n", csr);assert(0);}
}

static void getCSRs(word_t *t, word_t csr)
{
  if(csr == 0x00){*t = cpu.mstatus;}
  else if(csr == 0x04){*t = cpu.mie;}
  else if(csr == 0x05){*t = cpu.mtvec;}
  else if(csr == 0x41){*t = cpu.mepc;}
  else if(csr == 0x42){*t = cpu.mcause;}
  else if(csr == 0x44){*t = cpu.mip;}
  else{printf("csr number:%ld\n", csr);assert(0);}
}

static void mret()
{
  // MPIE->MIE
  if(cpu.mstatus & (1<<7))cpu.mstatus |= (1<<3);
  else cpu.mstatus &= (~(1<<3));
  // MIPE=1
  cpu.mstatus |= (1<<7);
  #ifdef CONFIG_DIFFTEST
  // MPP=0
  // cpu.mstatus &= (~(0b11)<<11);
  cpu.mstatus &= (0xffffffffffffe7ff);
  #endif
}

enum 
{
  TYPE_I, TYPE_U, TYPE_S, TYPE_J, TYPE_R, TYPE_B,
  TYPE_N, // none
};

#define src1R(n) do { *src1 = R(n); } while (0)
#define src2R(n) do { *src2 = R(n); } while (0)
#define destR(n) do { *dest = n; } while (0)
#define src1I(i) do { *src1 = i; } while (0)
#define src2I(i) do { *src2 = i; } while (0)
#define destI(i) do { *dest = i; } while (0)

static word_t immI(uint32_t i) { return SEXT(BITS(i, 31, 20), 12); }
static word_t immU(uint32_t i) { return SEXT(BITS(i, 31, 12), 20) << 12; }
static word_t immS(uint32_t i) { return (SEXT(BITS(i, 31, 25), 7) << 5) | BITS(i, 11, 7); }
static word_t immJ(uint32_t i) { return (SEXT(BITS(i, 31, 31), 1) << 20 | (BITS(i, 19, 12) << 12) | (BITS(i, 20, 20) << 11) |(BITS(i, 30, 21) << 1)); }
static word_t immB(uint32_t i) { return (SEXT(BITS(i, 31, 31), 1) << 12 | (BITS(i, 7, 7) << 11| (BITS(i, 30, 25) << 5)| BITS(i, 11, 8) << 1)); }

static void decode_operand(Decode *s, word_t *dest, word_t *src1, word_t *src2, int type) 
{
  uint32_t i = s->isa.inst.val;
  int rd  = BITS(i, 11, 7);
  int rs1 = BITS(i, 19, 15);
  int rs2 = BITS(i, 24, 20);
  destR(rd);
  switch (type) 
  {
    case TYPE_R: src1R(rs1); src2R(rs2);break;
    case TYPE_I: src1R(rs1); src2I(immI(i)); break;
    case TYPE_U: src1I(immU(i)); break;
    case TYPE_S: destI(immS(i)); src1R(rs1); src2R(rs2); break;
    case TYPE_J: src1I(immJ(i)); break;
    case TYPE_B: destI(immB(i));src1R(rs1);src2R(rs2);
  }
}

static int decode_exec(Decode *s) 
{
  uint32_t i = s->isa.inst.val;
  word_t dest = 0, src1 = 0, src2 = 0;
  s->dnpc = s->snpc;
  word_t shamt = BITS(i, 25, 20);
  word_t csr = BITS(i, 27, 20);
  word_t zimm = BITS(i, 19, 15);

#define INSTPAT_INST(s) ((s)->isa.inst.val)
#define INSTPAT_MATCH(s, name, type, ... /* body */ ) { \
  decode_operand(s, &dest, &src1, &src2, concat(TYPE_, type)); \
  __VA_ARGS__ ; \
}

  INSTPAT_START();
  //R-type
  INSTPAT("0000000 ????? ????? 000 ????? 01100 11", add    , R, R(dest) = src1 + src2);
  INSTPAT("0000000 ????? ????? 000 ????? 01110 11", addw   , R, R(dest) = SEXT(BITS((src1 + src2), 31, 0), 32));
  INSTPAT("0100000 ????? ????? 000 ????? 01100 11", sub    , R, R(dest) = src1 - src2);
  INSTPAT("0100000 ????? ????? 000 ????? 01110 11", subw   , R, R(dest) = SEXT(BITS(src1 - src2, 31, 0), 32));
  INSTPAT("0000001 ????? ????? 000 ????? 01100 11", mul    , R, R(dest) = src1 * src2);
  // INSTPAT("0000001 ????? ????? 001 ????? 01100 11", mulh   , R, R(dest) = ((int)src1 * (int)src2) >> 64);
  // INSTPAT("0000001 ????? ????? 010 ????? 01100 11", mulhsu , R, R(dest) = ((int)src1 * src2) >> 64);
  // INSTPAT("0000001 ????? ????? 011 ????? 01100 11", mulhu  , R, R(dest) = (src1 * src2) >> 64);
  INSTPAT("0000001 ????? ????? 000 ????? 01110 11", mulw   , R, R(dest) = SEXT(BITS(src1 * src2, 31, 0), 32));
  INSTPAT("0000001 ????? ????? 100 ????? 01100 11", div    , R, R(dest) = (long) src1 / (long) src2);
  INSTPAT("0000001 ????? ????? 100 ????? 01110 11", divw   , R, R(dest) = SEXT(BITS((int)BITS(src1, 31, 0) / (int)BITS(src2, 31, 0), 31, 0), 32));
  INSTPAT("0000001 ????? ????? 101 ????? 01100 11", divu   , R, R(dest) = src1 / src2);
  INSTPAT("0000001 ????? ????? 101 ????? 01110 11", divuw  , R, R(dest) = SEXT(BITS(BITS(src1, 31, 0) / BITS(src2, 31, 0), 31, 0), 32));
  INSTPAT("0000001 ????? ????? 110 ????? 01100 11", rem    , R, R(dest) = (long)src1 % (long)src2);
  INSTPAT("0000001 ????? ????? 110 ????? 01110 11", remw   , R, R(dest) = SEXT(BITS((int)BITS(src1, 31, 0) % (int)BITS(src2, 31, 0), 31, 0), 32));
  INSTPAT("0000001 ????? ????? 111 ????? 01100 11", remu   , R, R(dest) = src1 % src2);
  INSTPAT("0000001 ????? ????? 111 ????? 01110 11", remuw  , R, R(dest) = SEXT(BITS(BITS(src1, 31, 0) % BITS(src2, 31, 0), 31, 0), 32));
  INSTPAT("0000000 ????? ????? 101 ????? 01100 11", srl    , R, R(dest) = (uint64_t)src1 >> src2);
  INSTPAT("0000000 ????? ????? 001 ????? 01100 11", sll    , R, R(dest) = src1 << src2);
  INSTPAT("0000000 ????? ????? 001 ????? 01110 11", sllw   , R, R(dest) = SEXT(BITS(src1 << BITS(src2, 4, 0), 31, 0), 32));
  INSTPAT("0000000 ????? ????? 101 ????? 01110 11", srlw   , R, R(dest) = SEXT(BITS(((uint32_t)BITS(src1, 31, 0)) >> BITS(src2, 4, 0), 31, 0), 32));
  INSTPAT("0100000 ????? ????? 101 ????? 01110 11", sraw   , R, R(dest) = SEXT(BITS((int)BITS(src1, 31, 0) >> BITS(src2, 4, 0), 31, 0), 32));
  INSTPAT("0000000 ????? ????? 111 ????? 01100 11", and    , R, R(dest) = src1 & src2);
  INSTPAT("0000000 ????? ????? 110 ????? 01100 11", or     , R, R(dest) = src1 | src2);
  INSTPAT("0000000 ????? ????? 100 ????? 01100 11", xor    , R, R(dest) = src1 ^ src2);
  INSTPAT("0000000 ????? ????? 010 ????? 01100 11", slt    , R, if((long long) src1 < (long long) src2)R(dest)=1;else R(dest)=0;);
  INSTPAT("0000000 ????? ????? 011 ????? 01100 11", sltu   , R, if(src1 < src2)R(dest)=1;else R(dest)=0);

  INSTPAT("0011000 00010 00000 000 00000 11100 11", mret   , R, mret();
                                                                s->dnpc = cpu.mepc;);
  
  
  //I-type
  INSTPAT("??????? ????? ????? 000 ????? 00000 11", lb     , I, R(dest) = SEXT(Mr(src1 + src2, 1), 8));
  INSTPAT("??????? ????? ????? 100 ????? 00000 11", lbu    , I, R(dest) = Mr(src1 + src2, 1));
  INSTPAT("??????? ????? ????? 011 ????? 00000 11", ld     , I, R(dest) = Mr(src1 + src2, 8));
  INSTPAT("??????? ????? ????? 010 ????? 00000 11", lw     , I, R(dest) = SEXT(Mr(src1+src2, 4), 32));
  INSTPAT("??????? ????? ????? 110 ????? 00000 11", lwu    , I, R(dest) = Mr(src1+src2, 4));
  INSTPAT("??????? ????? ????? 001 ????? 00000 11", lh     , I, R(dest) = SEXT(Mr(src1+src2, 2), 16));
  INSTPAT("??????? ????? ????? 101 ????? 00000 11", lhu    , I, R(dest) = Mr(src1+src2, 2));

  INSTPAT("??????? ????? ????? 000 ????? 11001 11", jalr   , I, word_t temp;// ret伪指令会被解释成jalr
                                                                temp=s->pc+4;
                                                                s->dnpc=(src1+src2)&(~((word_t) 1));
                                                                R(dest)=temp;);
  INSTPAT("??????? ????? ????? 000 ????? 00100 11", addi   , I, R(dest) = src1 + src2);
  INSTPAT("??????? ????? ????? 000 ????? 00110 11", addiw  , I, R(dest) = SEXT(BITS(src1 + src2, 31, 0), 32));
  INSTPAT("000000? ????? ????? 001 ????? 00100 11", slli   , I, R(dest) = src1 << shamt);
  INSTPAT("000000? ????? ????? 001 ????? 00110 11", slliw  , I, assert(BITS(i, 25, 25)==0);
                                                                R(dest) = SEXT(BITS(src1 << shamt, 31, 0), 32));
  INSTPAT("010000? ????? ????? 101 ????? 00100 11", srai   , I, R(dest) = ((long long)src1) >> shamt);
  INSTPAT("010000? ????? ????? 101 ????? 00110 11", sraiw  , I, assert(BITS(i, 25, 25)==0);
                                                                R(dest) = SEXT(BITS((int) BITS(src1, 31, 0) >> shamt, 31, 0), 32));
  INSTPAT("000000? ????? ????? 101 ????? 00100 11", srli   , I, R(dest) = src1 >> shamt);
  INSTPAT("000000? ????? ????? 101 ????? 00110 11", srliw  , I, assert(BITS(i, 25, 25)==0);
                                                                R(dest) = SEXT(BITS((uint32_t)BITS(src1, 31, 0) >> shamt, 31, 0), 32));
  INSTPAT("??????? ????? ????? 111 ????? 00100 11", andi   , I, R(dest) = src1 & src2);
  INSTPAT("??????? ????? ????? 110 ????? 00100 11", ori    , I, R(dest) = src1 | src2);
  INSTPAT("??????? ????? ????? 100 ????? 00100 11", xori   , I, R(dest) = src1 ^ src2);
  INSTPAT("??????? ????? ????? 010 ????? 00100 11", slti   , I, if((long long) src1 < (long long) src2)R(dest)=1;else R(dest)=0;);
  INSTPAT("??????? ????? ????? 011 ????? 00100 11", sltiu  , I, if(src1 < src2)R(dest)=1;else R(dest)=0;);

  INSTPAT("??????? ????? ????? 010 ????? 11100 11", csrrs  , I, word_t temp;
                                                                getCSRs(&temp, csr);
                                                                setCSRs(csr, (temp | src1));
                                                                R(dest) = temp;);
  INSTPAT("??????? ????? ????? 001 ????? 11100 11", csrrw  , I, word_t temp;
                                                                getCSRs(&temp, csr);
                                                                setCSRs(csr, src1);
                                                                R(dest) = temp;);
  INSTPAT("??????? ????? ????? 110 ????? 11100 11", csrrsi , I, word_t temp;
                                                                getCSRs(&temp, csr);
                                                                setCSRs(csr, (temp | zimm));
                                                                R(dest) = temp;);
  INSTPAT("??????? ????? ????? 111 ????? 11100 11", csrrci , I, word_t temp;
                                                                getCSRs(&temp, csr);
                                                                setCSRs(csr, (temp & (~zimm)));
                                                                R(dest) = temp;);

  //S-type
  INSTPAT("??????? ????? ????? 000 ????? 01000 11", sb     , S, Mw(src1 + dest, 1, src2);
                                                                if(src1 + dest == 0x8001441c)printf("pc:%lx, data:%lx\n", s->pc, src2););
  INSTPAT("??????? ????? ????? 001 ????? 01000 11", sh     , S, Mw(src1 + dest, 2, src2);
                                                                if(src1 + dest == 0x8001441c)printf("pc:%lx, data:%lx\n", s->pc, src2););
  INSTPAT("??????? ????? ????? 010 ????? 01000 11", sw     , S, Mw(src1 + dest, 4, src2);
                                                                if(src1 + dest == 0x8001441c)printf("pc:%lx, data:%lx\n", s->pc, src2););
  INSTPAT("??????? ????? ????? 011 ????? 01000 11", sd     , S, Mw(src1 + dest, 8, src2);
                                                                if(src1 + dest == 0x8001441c)printf("pc:%lx, data:%lx\n", s->pc, src2););
  //word_t i = 0xfffffffffffffff;printf("0x%lx\n", SEXT(i, 32))
  

  //B-type
  INSTPAT("??????? ????? ????? 000 ????? 11000 11", beq    , B, if(src1 == src2)s->dnpc=s->pc+dest);
  INSTPAT("??????? ????? ????? 001 ????? 11000 11", bne    , B, if(src1 != src2)s->dnpc=s->pc+dest);
  INSTPAT("??????? ????? ????? 100 ????? 11000 11", blt    , B, if((long long) src1 <  (long long) src2)s->dnpc = s->pc+dest);
  INSTPAT("??????? ????? ????? 101 ????? 11000 11", bge    , B, if((long long) src1 >= (long long) src2)s->dnpc = s->pc+dest);
  INSTPAT("??????? ????? ????? 110 ????? 11000 11", bltu   , B, if(src1 <  src2)s->dnpc = s->pc+dest);
  INSTPAT("??????? ????? ????? 111 ????? 11000 11", bgeu   , B, if(src1 >= src2)s->dnpc = s->pc+dest);


  //U-tpye
  INSTPAT("??????? ????? ????? ??? ????? 00101 11", auipc  , U, R(dest) = src1 + s->pc);
  INSTPAT("??????? ????? ????? ??? ????? 01101 11", lui    , U, R(dest) = SEXT(src1, 32));

  //J-type
  INSTPAT("??????? ????? ????? ??? ????? 11011 11", jal    , J, R(dest) = s->pc+4;
                                                                s->dnpc=s->pc+src1;);


  INSTPAT("0000000 00001 00000 000 00000 11100 11", ebreak , N, NEMUTRAP(s->pc, R(10))); // R(10) is $a0
  INSTPAT("0000000 00000 00000 000 00000 11100 11", ecall  , N, s->dnpc = isa_raise_intr(0x0b, s->pc)); //NO=11, 机器模式环境调用
  INSTPAT("??????? ????? ????? ??? ????? ????? ??", inv    , N, INV(s->pc));
  INSTPAT_END();

  R(0) = 0; // reset $zero to 0

  return 0;
}

int isa_exec_once(Decode *s) 
{
  s->isa.inst.val = inst_fetch(&s->snpc, 4);
  return decode_exec(s);
}

